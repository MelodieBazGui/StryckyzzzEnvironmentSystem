package graphics;

import java.util.List;

public final class FrameData {
    public final List<DrawItem> drawList;
    public final CameraData camera;
    public FrameData(List<DrawItem> drawList, CameraData camera) {
        this.drawList = List.copyOf(drawList);
        this.camera = camera;
    }
}
