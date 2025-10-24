package graphis.guiutils;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;

public final class GLUtils {
    public static int compile(GL4 gl, int type, String src) {
        int id = gl.glCreateShader(type);
        gl.glShaderSource(id, 1, new String[]{src}, null, 0);
        gl.glCompileShader(id);
        int[] ok = new int[1]; gl.glGetShaderiv(id, GL4.GL_COMPILE_STATUS, ok, 0);
        if (ok[0] == GL.GL_FALSE) throw new RuntimeException(getLog(gl, id));
        return id;
    }
    public static int linkProgram(GL4 gl, int... shaders) {
        int p = gl.glCreateProgram();
        for (int s: shaders) gl.glAttachShader(p, s);
        gl.glLinkProgram(p);
        int[] ok = new int[1]; gl.glGetProgramiv(p, GL4.GL_LINK_STATUS, ok, 0);
        if (ok[0] == GL.GL_FALSE) throw new RuntimeException(getProgLog(gl, p));
        for (int s: shaders) gl.glDetachShader(p, s);
        return p;
    }
    private static String getLog(GL4 gl, int shader) {
        int[] len = new int[1]; gl.glGetShaderiv(shader, GL4.GL_INFO_LOG_LENGTH, len, 0);
        byte[] b = new byte[len[0]]; gl.glGetShaderInfoLog(shader, b.length, null, 0, b, 0);
        return new String(b);
    }
    private static String getProgLog(GL4 gl, int prog) {
        int[] len = new int[1]; gl.glGetProgramiv(prog, GL4.GL_INFO_LOG_LENGTH, len, 0);
        byte[] b = new byte[len[0]]; gl.glGetProgramInfoLog(prog, b.length, null, 0, b, 0);
        return new String(b);
    }
}
