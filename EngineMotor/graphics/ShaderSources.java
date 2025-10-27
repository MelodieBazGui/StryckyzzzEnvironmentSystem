package graphics;

import com.jogamp.opengl.*;
import utils.Logger;

import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.*;

/**
 * Robust OpenGL shader manager with:
 *  - class-based logging (no exceptions on failure, always logs and cleans up)
 *  - static built-in DEFAULT_VS / DEFAULT_FS (auto-used if sources are missing)
 *  - precomputed shadow/lighting snippets with conditional injection
 *  - uniform helpers (Mat4 / float[] / FloatBuffer, row-major or column-major)
 *  - allocation-aware (thread-local direct buffers, clearing big arrays)
 */
public final class ShaderSources implements AutoCloseable {

    private static final Logger LOG = new Logger(ShaderSource.class);

    // =====================================================
    // Default GLSL snippets (static, precomputed)
    // =====================================================
    public static final class Snippets {
        public static final String SHADOW_UNIFORMS = """
            // ---- Shadow uniforms ----
            uniform sampler2D uShadowMap;
            uniform mat4 uLightSpaceMatrix;
            uniform float uShadowBias;
            uniform float uShadowStrength;
            """;

        public static final String SHADOW_FUNCTION = """
            // ---- Shadow function ----
            float computeShadow(vec4 fragPosLightSpace, vec3 normal, vec3 lightDir) {
                vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
                projCoords = projCoords * 0.5 + 0.5;
                if (projCoords.z > 1.0) return 0.0;
                float bias = max(uShadowBias * (1.0 - dot(normal, lightDir)), 0.0005);
                float shadow = 0.0;
                vec2 texelSize = 1.0 / textureSize(uShadowMap, 0);
                for (int x = -1; x <= 1; ++x)
                    for (int y = -1; y <= 1; ++y) {
                        float pcfDepth = texture(uShadowMap, projCoords.xy + vec2(x,y)*texelSize).r;
                        shadow += projCoords.z - bias > pcfDepth ? 1.0 : 0.0;
                    }
                return (shadow / 9.0) * uShadowStrength;
            }
            """;

        public static final String DEFAULT_LIGHT = """
            // ---- Default directional light ----
            struct DirectionalLight {
                vec3 direction;
                vec3 color;
            };
            uniform DirectionalLight uDirLight;
            vec3 applyLighting(vec3 normal, vec3 albedo, vec4 fragPosLightSpace) {
                vec3 lightDir = normalize(-uDirLight.direction);
                float diff = max(dot(normal, lightDir), 0.0);
                float shadow = computeShadow(fragPosLightSpace, normal, lightDir);
                return (1.0 - shadow) * uDirLight.color * diff * albedo;
            }
            """;

        package graphics;

        /**
         * Built-in shader sources used by the minimal Renderer.
         * Matches VAO layout (location 0: pos, 1: normal, 2: uv)
         * and uniforms: uProj, uView, uModel, uColor.
         */
        public final class ShaderSources {
            private ShaderSources() {}

            /** Vertex shader */
            public static final String VS = """
                #version 430 core
                layout(location = 0) in vec3 in_Position;
                layout(location = 1) in vec3 in_Normal;
                layout(location = 2) in vec2 in_TexCoord;

                uniform mat4 uProj;
                uniform mat4 uView;
                uniform mat4 uModel;

                out vec3 vNormal;
                out vec3 vWorldPos;
                out vec2 vUV;

                void main() {
                    vec4 worldPos = uModel * vec4(in_Position, 1.0);
                    vWorldPos = worldPos.xyz;

                    // normal matrix (no non-uniform scale assumed for this minimal shader)
                    vNormal = mat3(uModel) * in_Normal;

                    vUV = in_TexCoord;
                    gl_Position = uProj * uView * worldPos;
                }
                """;

            /** Fragment shader */
            public static final String FS = """
                #version 430 core
                in vec3 vNormal;
                in vec3 vWorldPos;
                in vec2 vUV;

                uniform vec4 uColor;

                out vec4 FragColor;

                void main() {
                    // simple lambert from a hard-coded light dir for now
                    vec3 N = normalize(vNormal);
                    vec3 L = normalize(vec3(0.4, 0.7, 0.2));
                    float diff = max(dot(N, L), 0.0);
                    vec3 base = uColor.rgb * (0.2 + 0.8 * diff);
                    FragColor = vec4(base, uColor.a);
                }
                """;
        }

        
        public static final String STATIC_SHADOW_PREAMBLE =
                SHADOW_UNIFORMS + "\n" + SHADOW_FUNCTION + "\n" + DEFAULT_LIGHT;

        private Snippets() {}
    }

    // =====================================================
    // Built-in fallback shaders (match your layout & uniforms)
    // =====================================================
    public static final String DEFAULT_VS = """
        #version 430 core
        layout(location = 0) in vec3 in_Position;
        layout(location = 1) in vec3 in_Normal;
        layout(location = 2) in vec2 in_TexCoord;

        uniform mat4 uProj;
        uniform mat4 uView;
        uniform mat4 uModel;

        out vec3 vNormal;
        out vec3 vWorldPos;
        out vec2 vUV;

        void main() {
            vec4 worldPos = uModel * vec4(in_Position, 1.0);
            vWorldPos = worldPos.xyz;
            vNormal = mat3(uModel) * in_Normal; // assumes uniform scale
            vUV = in_TexCoord;
            gl_Position = uProj * uView * worldPos;
        }
        """;

    public static final String DEFAULT_FS = """
        #version 430 core
        in vec3 vNormal;
        in vec3 vWorldPos;
        in vec2 vUV;

        uniform vec4 uColor;

        out vec4 FragColor;

        void main() {
            vec3 N = normalize(vNormal);
            vec3 L = normalize(vec3(0.4, 0.7, 0.2));
            float diff = max(dot(N, L), 0.0);
            vec3 base = uColor.rgb * (0.2 + 0.8 * diff);
            FragColor = vec4(base, uColor.a);
        }
        """;

    // =====================================================
    // State
    // =====================================================
    private int programId = 0;
    private String vsSource, fsSource;
    private final Map<String, Integer> uniformCache = new HashMap<>(64);
    private final Map<Integer, String> attribBindings = new HashMap<>(8);
    private String label = "<unnamed>";

    // Thread-local buffers (reuse to avoid GC)
    private static final ThreadLocal<FloatBuffer> TL_FLOAT16 =
            ThreadLocal.withInitial(() -> ByteBuffer.allocateDirect(16 * Float.BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer());
    private static final ThreadLocal<FloatBuffer> TL_FLOAT16_BIS =
            ThreadLocal.withInitial(() -> ByteBuffer.allocateDirect(16 * Float.BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer());
    private static final ThreadLocal<IntBuffer> TL_INT4 =
            ThreadLocal.withInitial(() -> ByteBuffer.allocateDirect(4 * Integer.BYTES)
                    .order(ByteOrder.nativeOrder()).asIntBuffer());

    // =====================================================
    // Builder
    // =====================================================
    public static Builder fromFiles() { return new Builder(new ShaderSource()).readFromFiles(true); }
    public static Builder fromStrings() { return new Builder(new ShaderSource()); }
    public static Builder defaults() {
        return new Builder(new ShaderSource()).vertex(DEFAULT_VS).fragment(DEFAULT_FS);
    }

    public static final class Builder {
        private final ShaderSource s;
        private boolean readFiles;

        private Builder(ShaderSource s) { this.s = s; }

        private Builder readFromFiles(boolean v) { this.readFiles = v; return this; }

        public Builder label(String label) { s.label = label != null ? label : s.label; return this; }

        public Builder vertex(String pathOrSrc) { s.vsSource = loadMaybe(pathOrSrc, readFiles); return this; }
        public Builder fragment(String pathOrSrc) { s.fsSource = loadMaybe(pathOrSrc, readFiles); return this; }

        public Builder bindAttrib(int index, String name) {
            if (name != null) s.attribBindings.put(index, name);
            return this;
        }

        public ShaderSource build(GL4 gl) {
            s.ensureDefaultsIfMissing();     // <- use built-ins when missing
            s.injectDefaultSnippets();       // <- add shadows/light if needed
            s.compileAndLink(gl);
            return s;
        }

        private static String loadMaybe(String arg, boolean readFile) {
            if (arg == null) return null;
            if (!readFile) return arg;
            try {
                byte[] bytes = Files.readAllBytes(Path.of(arg));
                String text = new String(bytes, StandardCharsets.UTF_8);
                Arrays.fill(bytes, (byte) 0);
                return text;
            } catch (IOException e) {
                LOG.error("Failed to read shader file: " + arg, e);
                return null; // return null so defaults can kick in
            }
        }
    }

    /** If user did not provide sources, auto-use our built-in defaults. */
    private void ensureDefaultsIfMissing() {
        if (vsSource == null || vsSource.isBlank()) {
            vsSource = DEFAULT_VS;
            LOG.info("Using built-in DEFAULT_VS for shader: " + label);
        }
        if (fsSource == null || fsSource.isBlank()) {
            fsSource = DEFAULT_FS;
            LOG.info("Using built-in DEFAULT_FS for shader: " + label);
        }
    }

    // =====================================================
    // Default snippet injection
    // =====================================================
    private void injectDefaultSnippets() {
        if (fsSource == null) return;
        boolean hasShadow = fsSource.contains("computeShadow");
        boolean hasLight = fsSource.contains("DirectionalLight");
        if (!hasShadow || !hasLight) {
            fsSource = "// --- Engine default snippet injection ---\n"
                    + Snippets.STATIC_SHADOW_PREAMBLE + "\n"
                    + "// --- User fragment ---\n" + fsSource;
            LOG.info("Injected default shadow/lighting code into shader: " + label);
        }
    }

    // =====================================================
    // Compile + Link
    // =====================================================
    public void compileAndLink(GL4 gl) {
        if (programId != 0) return;

        int prog = gl.glCreateProgram();
        if (prog == 0) {
            LOG.error("glCreateProgram failed for " + label, null);
            return;
        }

        try {
            if (vsSource != null) attach(gl, prog, GL2ES2.GL_VERTEX_SHADER, vsSource);
            if (fsSource != null) attach(gl, prog, GL2ES2.GL_FRAGMENT_SHADER, fsSource);

            for (Map.Entry<Integer, String> e : attribBindings.entrySet()) {
                gl.glBindAttribLocation(prog, e.getKey(), e.getValue());
            }

            gl.glLinkProgram(prog);
            IntBuffer status = TL_INT4.get().clear();
            gl.glGetProgramiv(prog, GL2ES2.GL_LINK_STATUS, status);
            if (status.get(0) == GL.GL_FALSE) {
                String info = getProgramInfoLog(gl, prog);
                LOG.error("Shader link failed for " + label + ":\n" + info, null);
                gl.glDeleteProgram(prog);
                return;
            }

            // release big strings
            vsSource = fsSource = null;
            attribBindings.clear();
            programId = prog;
            LOG.info("Shader linked successfully: " + label);

        } catch (Throwable t) {
            LOG.error("Shader linking failed for " + label, t);
            try { gl.glDeleteProgram(prog); } catch (Throwable ignored) {}
        }
    }

    private void attach(GL4 gl, int program, int type, String src) {
        int shader = gl.glCreateShader(type);
        if (shader == 0) {
            LOG.error("glCreateShader failed: " + shaderTypeName(type), null);
            return;
        }

        String[] arr = {src};
        int[] len = {src.length()};
        gl.glShaderSource(shader, 1, arr, len, 0);
        gl.glCompileShader(shader);

        IntBuffer status = TL_INT4.get().clear();
        gl.glGetShaderiv(shader, GL2ES2.GL_COMPILE_STATUS, status);
        if (status.get(0) == GL.GL_FALSE) {
            String info = getShaderInfoLog(gl, shader);
            LOG.error("Shader compile failed (" + shaderTypeName(type) + ") in " + label + ":\n" + info, null);
            gl.glDeleteShader(shader);
            return;
        }

        gl.glAttachShader(program, shader);
        gl.glDeleteShader(shader);
        // scrub locals
        arr[0] = null; Arrays.fill(len, 0);
    }

    // =====================================================
    // Info Log helpers
    // =====================================================
    private static String getShaderInfoLog(GL4 gl, int shader) {
        IntBuffer lenBuf = TL_INT4.get().clear();
        gl.glGetShaderiv(shader, GL2ES2.GL_INFO_LOG_LENGTH, lenBuf);
        int len = lenBuf.get(0);
        if (len <= 1) return "";
        ByteBuffer bb = ByteBuffer.allocateDirect(len);
        gl.glGetShaderInfoLog(shader, len, null, bb);
        byte[] arr = new byte[len];
        bb.get(arr);
        String s = new String(arr, StandardCharsets.UTF_8);
        Arrays.fill(arr, (byte) 0);
        return s;
    }

    private static String getProgramInfoLog(GL4 gl, int program) {
        IntBuffer lenBuf = TL_INT4.get().clear();
        gl.glGetProgramiv(program, GL2ES2.GL_INFO_LOG_LENGTH, lenBuf);
        int len = lenBuf.get(0);
        if (len <= 1) return "";
        ByteBuffer bb = ByteBuffer.allocateDirect(len);
        gl.glGetProgramInfoLog(program, len, null, bb);
        byte[] arr = new byte[len];
        bb.get(arr);
        String s = new String(arr, StandardCharsets.UTF_8);
        Arrays.fill(arr, (byte) 0);
        return s;
    }

    private static String shaderTypeName(int type) {
        return switch (type) {
            case GL2ES2.GL_VERTEX_SHADER -> "VERTEX";
            case GL2ES2.GL_FRAGMENT_SHADER -> "FRAGMENT";
            default -> "UNKNOWN(" + type + ")";
        };
    }

    // =====================================================
    // Public API
    // =====================================================
    public void use(GL4 gl) {
        if (programId == 0) {
            LOG.warn("Attempt to use shader before linking: " + label);
            return;
        }
        gl.glUseProgram(programId);
    }

    public void stop(GL4 gl) { gl.glUseProgram(0); }

    public void dispose(GL4 gl) {
        if (programId != 0) {
            gl.glUseProgram(0);
            gl.glDeleteProgram(programId);
            programId = 0;
            uniformCache.clear();
            LOG.info("Shader disposed: " + label);
        }
    }

    @Override public void close() { /* explicit dispose required */ }

    public int id() { return programId; }
    public String label() { return label; }

    // =====================================================
    // Uniforms
    // =====================================================
    private int uniformLocation(GL4 gl, String name) {
        Integer cached = uniformCache.get(name);
        if (cached != null) return cached;
        int loc = gl.glGetUniformLocation(programId, name);
        uniformCache.put(name, loc);
        if (loc < 0) LOG.warn("Uniform not found: " + name + " [" + label + "]");
        return loc;
    }

    public void setUniform(GL4 gl, String name, float v) {
        int loc = uniformLocation(gl, name);
        if (loc >= 0) gl.glUniform1f(loc, v);
    }

    public void setUniform(GL4 gl, String name, int v) {
        int loc = uniformLocation(gl, name);
        if (loc >= 0) gl.glUniform1i(loc, v);
    }

    /** Mat4 (row-major internal) -> upload column-major (transpose=false). */
    public void setUniformMat4(GL4 gl, String name, math.Mat4 mat) {
        int loc = uniformLocation(gl, name);
        if (loc < 0) return;
        FloatBuffer fb = TL_FLOAT16.get();
        fb.clear();
        float[] col = mat.toColumnMajorArray();
        fb.put(col).flip();
        gl.glUniformMatrix4fv(loc, 1, false, fb);
        Arrays.fill(col, 0f);
    }

    /** Upload a 4x4 matrix from a column-major float[16]. */
    public void setUniformMat4Col(GL4 gl, String name, float[] colMajor16) {
        int loc = uniformLocation(gl, name);
        if (loc < 0) return;
        if (colMajor16 == null || colMajor16.length < 16) {
            LOG.warn("setUniformMat4Col: invalid array for " + name);
            return;
        }
        FloatBuffer fb = TL_FLOAT16.get();
        fb.clear();
        fb.put(colMajor16, 0, 16).flip();
        gl.glUniformMatrix4fv(loc, 1, false, fb);
    }

    /** Upload a 4x4 matrix from a row-major float[16] by transposing on the fly. */
    public void setUniformMat4Row(GL4 gl, String name, float[] rowMajor16) {
        int loc = uniformLocation(gl, name);
        if (loc < 0) return;
        if (rowMajor16 == null || rowMajor16.length < 16) {
            LOG.warn("setUniformMat4Row: invalid array for " + name);
            return;
        }
        FloatBuffer fb = TL_FLOAT16_BIS.get();
        fb.clear();
        fb.put(rowMajor16[0]).put(rowMajor16[4]).put(rowMajor16[8]).put(rowMajor16[12]);
        fb.put(rowMajor16[1]).put(rowMajor16[5]).put(rowMajor16[9]).put(rowMajor16[13]);
        fb.put(rowMajor16[2]).put(rowMajor16[6]).put(rowMajor16[10]).put(rowMajor16[14]);
        fb.put(rowMajor16[3]).put(rowMajor16[7]).put(rowMajor16[11]).put(rowMajor16[15]);
        fb.flip();
        gl.glUniformMatrix4fv(loc, 1, false, fb);
    }

    /** Program-uniform variant (no glUseProgram needed). */
    public void programUniformMat4Row(GL4 gl, int program, int location, float[] rowMajor16) {
        if (program == 0 || location < 0) return;
        FloatBuffer fb = TL_FLOAT16.get();
        fb.clear();
        // row-major -> col-major packing
        fb.put(rowMajor16[0]).put(rowMajor16[4]).put(rowMajor16[8]).put(rowMajor16[12]);
        fb.put(rowMajor16[1]).put(rowMajor16[5]).put(rowMajor16[9]).put(rowMajor16[13]);
        fb.put(rowMajor16[2]).put(rowMajor16[6]).put(rowMajor16[10]).put(rowMajor16[14]);
        fb.put(rowMajor16[3]).put(rowMajor16[7]).put(rowMajor16[11]).put(rowMajor16[15]);
        fb.flip();
        gl.glProgramUniformMatrix4fv(program, location, 1, false, fb);
    }
}
