package ir.piana.dev.gl.ctrl;

import glm.vec._3.Vec3;
import ir.piana.dev.gl.render.WindowContext;

public class MovementControl {
    // position
    private Vec3 position = new Vec3( 0, 0, 5 );
    // horizontal angle : toward -Z
    private float horizontalAngle = 3.14f;
    // vertical angle : 0, look at the horizon
    private float verticalAngle = 0.0f;
    // Initial Field of View
    private float initialFoV = 45.0f;

    private float speed = 3.0f; // 3 units / second
    private float mouseSpeed = 0.005f;

    private WindowContext windowContext;

    public MovementControl(WindowContext windowContext) {
        this.windowContext = windowContext;
    }
}
