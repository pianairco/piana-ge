package ir.piana.dev.gl.render;

import dev.FloatArray;
import glm.Glm;
import glm.mat._4.Mat4;
import glm.vec._2.d.Vec2d;
import glm.vec._2.i.Vec2i;
import glm.vec._3.Vec3;
import jakarta.annotation.PostConstruct;
import org.lwjgl.glfw.GLFW;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.Math.*;

@Component
public class CameraController {
    private Vec3 eye;
    private Vec3 lookAt;
    private Vec3 up;

    // horizontal angle : toward -Z
    private float horizontalAngle = 3.14f;
    // vertical angle : 0, look at the horizon
    private float verticalAngle = 0.0f;
    // Initial Field of View
    private float initialFoV = 45.0f;

    private float speed = 3.0f; // 3 units / second
    private float mouseSpeed = 0.00005f;

    @Autowired
    private WindowContext windowContext;

    private float spinAroundAxisY;//							radian of spin camera around y axis
    private float spinAroundAxisX;//							radian of spin camera around x axis
    private boolean isMove, isVertical, isHorizontal;//			whether mouse move and move is vertical and horizontal
    private float spdHMove, spdVMove;//
    private int mouseX, mouseY;

    @PostConstruct
    public void init() {
//        eye = new Vec3(0, 0, -10);
//        lookAt = new Vec3(0, 0, 1);
//        up = new Vec3(0, 1, 0);

        MCResetMouse();
        MCResetCamera();
        windowContext.addKeyCallback((windowId, key, scancode, action, mods) -> {
            if (key == 'W') {
                MCMoveCameraToForward(1);
            }

            if (key == 'S') {
                MCMoveCameraToBackward(1);
            }
        });
        windowContext.addCursorPosCallback((w, x, y) -> {
//            System.out.println(x + " " + y);
            MCSetMouseMove((int) x, (int) y);
        });
    }

    void MCResetMouse() {
        isMove = false;
        isVertical = false;
        isHorizontal = false;
        spdHMove = 0.1f;
        spdVMove = 0.1f;
        mouseX = 0;
        mouseY = 0;
        //SetCursorPos(mouseX,mouseY);
    }

    void MCResetEyeCamera() {
        eye = new Vec3(0, 0, 0);
    }

    void MCResetLookAtCamera() {
        MCResetSpinCamera();
        lookAt = new Vec3(eye.x + cos(spinAroundAxisX),
                eye.y + sin(spinAroundAxisY),
                eye.z + sin(spinAroundAxisX));
    }

    void MCResetSpinCamera() {
        spinAroundAxisX = 0.0f;
        spinAroundAxisY = 4.71f;
    }

    void MCResetUpCamera() {
        up = new Vec3(0.0f, 1.0f, 0.0f);
    }

    void MCResetCamera() {
        MCResetEyeCamera();
        MCResetLookAtCamera();
        MCResetUpCamera();
    }

    void MCSetEyeCamera(float x, float y, float z) {
        eye = new Vec3(x, y, z);
        MCSetLookAtCamera();
    }

    void MCSetLookAtCamera() {
        lookAt = new Vec3(eye.x + cos(spinAroundAxisY),
                eye.y + sin(spinAroundAxisX),
                eye.z + sin(spinAroundAxisY));
    }

    void MCSetSpinCameraAroundAxisX(float x) {
        spinAroundAxisX = x;
        //MCSetLookAtCamera();
    }

    void MCSetSpinCameraAroundAxisY(float y) {
        spinAroundAxisY = y;
        //MCSetLookAtCamera();
    }

    public Mat4 MCLookAt() {
        MCSetLookAtCamera();
        float FoV = initialFoV;// - 5 * glfwGetMouseWheel(); // Now GLFW 3 requires setting up a callback for this. It's a bit too complicated for this beginner's tutorial, so it's disabled instead.

        // Projection matrix : 45° Field of View, 4:3 ratio, display range : 0.1 unit <-> 100 units
        float[] projectionRes = new float[16];
        Vec2i displaySize = windowContext.getDisplaySize();
        FloatArray.perspective(
                (float) toRadians(FoV),
                (float) displaySize.x / (float) displaySize.y, 0.1f, 100f, projectionRes);

        Mat4 projection = new Mat4(
                projectionRes[0], projectionRes[1], projectionRes[2], projectionRes[3],
                projectionRes[4], projectionRes[5], projectionRes[6], projectionRes[7],
                projectionRes[8], projectionRes[9], projectionRes[10], projectionRes[11],
                projectionRes[12], projectionRes[13], projectionRes[14], projectionRes[15]
        );

        // Camera matrix

        Mat4 mat4d = new Mat4(1);

        Mat4 view = mat4d.lookAt(
                eye,
//                new Vec3(0, 0, 0),
                lookAt,
//                new Vec3(0, 1, 0) // Head is up (set to 0,-1,0 to look upside-down)
                up // Head is up (set to 0,-1,0 to look upside-down)
        );

        Mat4 model = new Mat4(1);

        Mat4 mvp = projection.mul(view).mul(model);

        return mvp;
        /*glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        gluLookAt(eyeX, eyeY, eyeZ,
                lookAtX, lookAtY, lookAtZ,
                upX, upY, upZ);*/
    }

    void MCSpinCameraToLeft() {
        spinAroundAxisY += spdHMove;
    }

    void MCSpinCameraToRight() {
        spinAroundAxisY += spdHMove;
    }

    void MCSpinCameraToUp() {
        spinAroundAxisX += spdVMove;

        /*if (spinAroundAxisX >= 1.57) {
            spinAroundAxisX -= spdVMove;
        }*/
    }

    void MCSpinCameraToDown() {
        spinAroundAxisX += spdVMove;

        if (spinAroundAxisX <= -1.57) {
            spinAroundAxisX -= spdVMove;
        }
    }

    void MCMoveCameraToForward(float speed) {
        eye = new Vec3(eye.x + cos(spinAroundAxisY) * speed,
                eye.y,
                eye.z + sin(spinAroundAxisY) * speed);
    }

    void MCMoveCameraToBackward(float speed) {
        eye = new Vec3(eye.x - cos(spinAroundAxisY) * speed,
                eye.y,
                eye.z - sin(spinAroundAxisY) * speed);
    }

    void MCSetMouseMove(int x, int y) {
        int oldX, oldY;
        oldX = mouseX;
        oldY = mouseY;
        mouseX = x;
        mouseY = y;

        MCSetMouseMoveForSpinCamera(oldX, oldY, mouseX, mouseY);

        if (mouseX > 500) {
            windowContext.setMousePosition(200, mouseY);
            mouseX = 200;
        } else if (mouseX < 100) {
            windowContext.setMousePosition(400, mouseY);
            mouseX = 400;
        }
        if (mouseY > 500) {
            windowContext.setMousePosition(mouseX, 200);
            mouseY = 200;
        } else if (mouseY < 100) {
            windowContext.setMousePosition(mouseX, 400);
            mouseY = 400;
        }
    }

    void MCSetMouseMoveForSpinCamera(int oldX, int oldY, int newX, int newY) {
        spdHMove = newX - oldX;
        if (spdHMove != 0) {
            //isMove=true;
            isHorizontal = true;
            //MCSetSpeedForSpin();
        } else {
            //isMove=false;
            isHorizontal = false;
        }
        spdVMove = oldY - newY;
        if (spdVMove != 0) {
            //isMove=true;
            isVertical = true;
            //MCSetSpeedForSpin();
        } else {
            //isMove=false;
            isVertical = false;
        }

        if (isHorizontal || isVertical) {
            isMove = true;
            MCSetSpeedForSpin();
        } else {
            isMove = false;
        }

    }

    void MCSetSpeedForSpin() {
        if (spdHMove > 0) {
            if (spdHMove > 0 && spdHMove <= 3) {
                spdHMove = 0.01f;
            } else if (spdHMove > 3 && spdHMove <= 10) {
                spdHMove = 0.05f;
            } else {
                spdHMove = 0.1f;
            }
        } else if (spdHMove < 0) {
            if (spdHMove < 0 && spdHMove >= -3) {
                spdHMove = -0.01f;
            } else if (spdHMove < -3 && spdHMove >= -10) {
                spdHMove = -0.05f;
            } else {
                spdHMove = -0.1f;
            }

        }

        if (spdVMove > 0) {
            if (spdVMove > 0 && spdVMove <= 10) {
                spdVMove = 0.01f;
            } else if (spdVMove > 10 && spdVMove <= 20) {
                spdVMove = 0.1f;
            } else {
                spdVMove = 0.1f;
            }
        } else if (spdVMove < 0) {
            if (spdVMove < 0 && spdVMove >= -10) {
                spdVMove = -0.01f;
            } else if (spdVMove < -10 && spdVMove >= -20) {
                spdVMove = -0.1f;
            } else {
                spdVMove = -0.1f;
            }
        }
    }

    public void MCSpinCamera() {
        if (isMove) {
            if (isHorizontal) {
                if (spdHMove > 0) {
                    MCSpinCameraToRight();
                } else if (spdHMove < 0) {
                    MCSpinCameraToLeft();
                }
            }
            if (isVertical) {
                if (spdVMove > 0) {
                    MCSpinCameraToUp();
                } else if (spdVMove < 0) {
                    MCSpinCameraToDown();
                }
            }

            isMove = false;
            isHorizontal = false;
            isVertical = false;
        }
    }

    int MCGetMouseX() {
        return mouseX;
    }

    int MCGetMouseY() {
        return mouseY;
    }

    double lastTime = GLFW.glfwGetTime();

    public Mat4 computeMatricesFromInputs() {

        // glfwGetTime is called only once, the first time this function is called
//        lastTime = GLFW.glfwGetTime();

        // Compute time difference between current and last frame
        double currentTime = GLFW.glfwGetTime();
        float deltaTime = (float) (currentTime - lastTime);

        Vec2d mousePosition = windowContext.getMousePosition();
        System.out.println(mousePosition.x + " " + mousePosition.y);
        Vec2i displaySize = windowContext.getDisplaySize();


        // Reset mouse position for next frame

//        windowContext.resetMousePosition();

        // Compute new orientation
        horizontalAngle = mouseSpeed * (float) (displaySize.x / 2 - mousePosition.x);
//        horizontalAngle += mouseSpeed * (float) (displaySize.x / 2 - mousePosition.x);
        verticalAngle = mouseSpeed * (float) (displaySize.y / 2 - mousePosition.y);
//        verticalAngle += mouseSpeed * (float) (displaySize.y / 2 - mousePosition.y);

        // Direction : Spherical coordinates to Cartesian coordinates conversion
        Vec3 direction = new Vec3(
                cos(verticalAngle) * sin(horizontalAngle),
                sin(verticalAngle),
                cos(verticalAngle) * cos(horizontalAngle)
        );

        // Right vector
        Vec3 right = new Vec3(
                sin(horizontalAngle - 3.14f / 2.0f),
                0,
                cos(horizontalAngle - 3.14f / 2.0f)
        );

        // Up vector
        Vec3 up = Glm.cross(right, direction);

        // Move forward
        if (windowContext.getKey(GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) {
            eye = eye.add(direction.mul(deltaTime).mul(speed));
        }
        // Move backward
        if (windowContext.getKey(GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) {
            eye = eye.sub(direction.mul(deltaTime).mul(speed));
        }
        // Strafe right
        if (windowContext.getKey(GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
            eye = eye.add(right.mul(deltaTime).mul(speed));
        }
        // Strafe left
        if (windowContext.getKey(GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) {
            eye = eye.sub(right.mul(deltaTime).mul(speed));
        }

        float FoV = initialFoV;// - 5 * glfwGetMouseWheel(); // Now GLFW 3 requires setting up a callback for this. It's a bit too complicated for this beginner's tutorial, so it's disabled instead.

        // Projection matrix : 45° Field of View, 4:3 ratio, display range : 0.1 unit <-> 100 units
        float[] projectionRes = new float[16];
        FloatArray.perspective(
                (float) toRadians(FoV),
                (float) displaySize.x / (float) displaySize.y, 0.1f, 100f, projectionRes);

        Mat4 projection = new Mat4(
                projectionRes[0], projectionRes[1], projectionRes[2], projectionRes[3],
                projectionRes[4], projectionRes[5], projectionRes[6], projectionRes[7],
                projectionRes[8], projectionRes[9], projectionRes[10], projectionRes[11],
                projectionRes[12], projectionRes[13], projectionRes[14], projectionRes[15]
        );

        // Camera matrix

        Mat4 mat4d = new Mat4(1);

//        position.add(new Vec3(0, 0, 0));

        Mat4 view = mat4d.lookAt(
                eye,
//                new Vec3(0, 0, 0),
                eye.add(direction),
                new Vec3(0, 1, 0) // Head is up (set to 0,-1,0 to look upside-down)
//                up // Head is up (set to 0,-1,0 to look upside-down)
        );

        Mat4 model = new Mat4(1);

        // For the next frame, the "last time" will be "now"
        lastTime = currentTime;

        Mat4 mvp = projection.mul(view).mul(model);
        return mvp;
    }
}
