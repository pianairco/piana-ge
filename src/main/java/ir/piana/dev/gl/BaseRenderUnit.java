package ir.piana.dev.gl;

import ir.piana.dev.gl.render.CameraController;
import ir.piana.dev.gl.toolkit.Mesh;
import ir.piana.dev.gl.render.WindowContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRenderUnit implements RenderUnit {
    @Autowired
    protected WindowContext windowContext;
    @Autowired
    protected CameraController cameraController;
    protected List<Mesh> meshList = new ArrayList<>();

    public BaseRenderUnit() {
    }
}
