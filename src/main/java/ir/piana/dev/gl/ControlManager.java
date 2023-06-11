package ir.piana.dev.gl;

import ir.piana.dev.gl.render.WindowContext;
import org.lwjgl.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;

import static org.lwjgl.opengl.GL11.*;

@Component
public class ControlManager {
    // The window handle
    @Autowired
    private WindowContext windowContext;

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${manager.renderer}")
    private String renderer;


    public void run() throws ClassNotFoundException {
//        Class<? extends BaseRenderUnit> renderUnitClass = (Class<? extends BaseRenderUnit>) Class.forName(renderer);

        System.out.println("Hello LWJGL: " + Version.getVersion() + "!");
        System.out.println("Renderer: " + renderer + "!");

        BaseRenderUnit renderUnit = applicationContext.getBean(renderer, BaseRenderUnit.class);
/*        try {
            renderUnit = renderUnitClass.getConstructor(WindowContext.class).newInstance(windowContext);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }*/

        loop(renderUnit);

        windowContext.close();
    }

    private void loop(RenderUnit renderUnit) {
        // Set the clear color
        glClearColor(0.0f, 0.6f, 0.8f, 0.0f);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_CULL_FACE);
//        glCullFace(GL_FRONT);
//        glFrontFace(GL_CW);

        renderUnit.preRender();
        // Enable depth test
        glEnable(GL_DEPTH_TEST);
        // Accept fragment if it closer to the camera than the former one
        glDepthFunc(GL_LESS);
        glEnable(GL_CULL_FACE);

        while (!windowContext.shouldClose()) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            renderUnit.render();

            windowContext.swapBuffers();

            windowContext.pollEvents();
        }
    }


}
