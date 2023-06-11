package ir.piana.dev.gl.render;

import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._2.d.Vec2d;
import glm.vec._2.i.Vec2i;
import glm.vec._3.Vec3;
import ir.piana.dev.gl.RenderUnit;
import ir.piana.dev.gl.render.model.DisplaySize;
import ir.piana.dev.gl.util.DisplayUtil;
import jakarta.annotation.PostConstruct;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

@Component
public class WindowContext {
    private long window;
    @Value("${display.window.size.width}")
    private int width;
    @Value("${display.window.size.height}")
    private int height;
    @Value("${display.window.title}")
    private String title;

    private List<GLFWCursorPosCallbackI> cursorPosCallbackList = new ArrayList();
    private List<GLFWKeyCallbackI> keyCallbackList = new ArrayList();

    public WindowContext() {
    }

    @PostConstruct
    public void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_FALSE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(width, height, title, NULL, NULL);

        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED); // the window will be resizable

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (windowId, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(windowId, true); // We will detect this in the rendering loop
            keyCallbackList.stream().forEach(clb -> clb.invoke(windowId, key, scancode, action, mods));
        });

        glfwSetCursorPosCallback(window, (w, x, y) -> {
//            System.out.println(w);
            cursorPosCallbackList.stream().forEach(clb -> clb.invoke(w, x, y));
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        /*org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback(window, (w, x, y) -> {
            System.out.println(x + " " + y);
        });*/
    }

    public void close() {
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void preRenderLoop(RenderUnit renderUnit) {
        renderUnit.preRender();
    }

    public void addKeyCallback(GLFWKeyCallbackI keyCallback) {
        this.keyCallbackList.add(keyCallback);
    }

    public void addCursorPosCallback(GLFWCursorPosCallbackI cursorPosCallback) {
        this.cursorPosCallbackList.add(cursorPosCallback);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(window);
    }

    public void swapBuffers() {
        // swap the color buffers
        glfwSwapBuffers(window);
    }

    public void pollEvents() {
        // Poll for window events. The key callback above will only be
        // invoked during this call.
        glfwPollEvents();
    }

    public Mat4 getMvp(Vec3 from, Vec3 to) {
        return DisplayUtil.getMVP(window, from, to);
    }

    public Vec2i getDisplaySize() {
        return DisplayUtil.getDisplaySize(window);
    }

    public Vec2d getMousePosition() {
        DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
        glfwGetCursorPos(window, xBuffer, yBuffer);
        return new Vec2d(xBuffer.get(0), yBuffer.get(0));
    }

    public void setMousePosition(int x, int y) {
        glfwSetCursorPos(window, x, y);
    }

    public void resetMousePosition() {
        Vec2i displaySize = DisplayUtil.getDisplaySize(window);
        GLFW.glfwSetCursorPos(window, displaySize.x / 2, displaySize.y / 2);
    }

    public int getKey(int key) {
        return GLFW.glfwGetKey(window, key);
    }

    /*public Mat4d getMvp() {
        return DisplayUtil.getMVP(window);
    }*/
}
