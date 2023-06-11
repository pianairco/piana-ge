package ir.piana.dev.gl.util;

import dev.FloatArray;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._2.i.Vec2i;
import glm.vec._3.Vec3;
import ir.piana.dev.gl.render.model.DisplaySize;
import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;

public class DisplayUtil {
    public static Vec2i getDisplaySize(long window) {
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        glfwGetWindowSize(window, w, h);
        int width = w.get();
        int height = h.get();
        return new Vec2i(width, height);
    }

    /*public static Mat4d getMVP(long window) {
        DisplaySize displaySize = DisplayUtil.getDisplayWidth(window);

        float[] projectionRes = new float[16];

        FloatArray.perspective(
                (float) Math.toRadians(45.0f),
                (float) displaySize.getWidth() / (float) displaySize.getHeight(), 0.1f, 100f, projectionRes);

        Mat4d projection = new Mat4d(
                projectionRes[0], projectionRes[1], projectionRes[2], projectionRes[3],
                projectionRes[4], projectionRes[5], projectionRes[6], projectionRes[7],
                projectionRes[8], projectionRes[9], projectionRes[10], projectionRes[11],
                projectionRes[12], projectionRes[13], projectionRes[14], projectionRes[15]
        );

        Mat4d mat4d = new Mat4d();
        Mat4d view = mat4d.lookAt(
                new Vec3d(4, 3, 3), // Camera is at (4,3,3), in World Space
                new Vec3d(0, 0, 0), // and looks at the origin
                new Vec3d(0, 1, 0)  // Head is up (set to 0,-1,0 to look upside-down)
        );
        Mat4d model = new Mat4d();

        Mat4d mvp = projection.mul(view).mul(model);
        return mvp;
    }*/

    public static Mat4 getMVP(long window, Vec3 from, Vec3 to) {
        Vec2i displaySize = DisplayUtil.getDisplaySize(window);
        float[] projectionRes = new float[16];
        FloatArray.perspective(
                (float) Math.toRadians(45.0f),
                (float) displaySize.x / (float) displaySize.y, 0.1f, 100f, projectionRes);

        Mat4 projection = new Mat4(
                projectionRes[0], projectionRes[1], projectionRes[2], projectionRes[3],
                projectionRes[4], projectionRes[5], projectionRes[6], projectionRes[7],
                projectionRes[8], projectionRes[9], projectionRes[10], projectionRes[11],
                projectionRes[12], projectionRes[13], projectionRes[14], projectionRes[15]
        );

        Mat4 mat4d = new Mat4();

        Mat4 view = mat4d.lookAt(
                from, to,
//                new Vec3(0, 0, -10), // Camera is at (4,3,3), in World Space
//                new Vec3(0, 0, 10), // and looks at the origin
                new Vec3(0, 1, 0)  // Head is up (set to 0,-1,0 to look upside-down)
        );
        Mat4 model = new Mat4(1);

//        Mat4 mvp = model.mul(view).mul(projection);
        Mat4 mvp = projection.mul(view).mul(model);
        return mvp;
    }
}
