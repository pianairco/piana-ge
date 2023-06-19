package ir.piana.dev.gl.t6;

import glm.vec._3.Vec3;
import ir.piana.dev.gl.BaseRenderUnit;
import ir.piana.dev.gl.RenderUtil;
import ir.piana.dev.gl.render.CameraController;
import ir.piana.dev.gl.toolkit.CMS3DModelLoader;
import ir.piana.dev.gl.toolkit.CMS3DModelRenderer;
import ir.piana.dev.gl.util.Texture;
import ir.piana.dev.gl.toolkit.Mesh;
import ir.piana.dev.gl.render.WindowContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("T6RenderUnit")
public class T6RenderUnit extends BaseRenderUnit {

    public T6RenderUnit() {
        super();
    }

    @Override
    public void preRender() {
        CMS3DModelRenderer renderer = null;
        try {
//            modelLoader.initModel("C:\\projects\\piana-ge\\src\\main\\resources\\models\\dwarf1.ms3d");
//            modelLoader.initModel("/home/rahmati/projects/piana-ge/src/main/resources/models/dwarf1.ms3d");
            renderer = CMS3DModelLoader.createModelRenderer("/home/rahmati/projects/piana-ge/src/main/resources/models/dwarf1.ms3d");
//            modelLoader.initModel("classpath-/models/dwarf1.ms3d");
        } catch (IOException e) {
            e.printStackTrace();
        }

        int shaderProgramId = RenderUtil.createShaderProgram(
                "#version 330 core\n" +
                        "layout (location = 0) in vec3 aPos;\n" +
                        "layout (location = 2) in vec2 vertexUV;\n" +
                        "out vec2 UV;\n" +
                        "// Values that stay constant for the whole mesh.\n" +
                        "uniform mat4 MVP;\n" +
                        "void main()\n" +
                        "{\n" +
                        " gl_Position = MVP * vec4(aPos, 1.0);\n" +
                        " UV = vertexUV;\n" +
                        "}\0",
                "#version 330 core\n" +
                        "// Interpolated values from the vertex shaders\n" +
                        "in vec2 UV;\n" +
                        "// Output data\n" +
                        "out vec3 color;\n" +
                        "// Values that stay constant for the whole mesh.\n" +
                        "uniform sampler2D myTextureSampler;\n" +
                        "void main(){\n" +
                        "    // Output color = color of the texture at the specified UV\n" +
                        "    color = texture( myTextureSampler, UV ).rgb;\n" +
                        "}");

        Mesh mesh = null;

        mesh = Mesh.builder().addVertices(new float[]{
                -1.0f, -1.0f, -9.0f,
                1.0f, -1.0f, -9.0f,
                0.0f, 1.0f, -9.0f,
        }).addColors(new float[]{
                0.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
        }).addShaderProgram(
                shaderProgramId
        ).addTexture(
                Texture.loadTexture("classpath-/models/sky.jpg")
//                Texture.loadTexture("/home/rahmati/projects/piana-gl/src/main/resources/res/texture-2.jpeg")
        ).addTextureCoordinates(new float[] {
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.5f, 1.0f
        });
        meshList.add(mesh);

        mesh = Mesh.builder().addVertices(new float[]{
                -1.0f, 0.0f, -10.0f,
                0.0f, 0.0f, -10.0f,
                -0.5f, 0.5f, -10.0f,
        }).addColors(new float[]{
                0.6f, 1.0f, 0.0f,
                0.5f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.7f,
        }).addIndices(new int[] {
                0, 1, 2
        }).addShaderProgram(
                shaderProgramId
        ).addTexture(
                Texture.loadTexture("classpath-/models/sky.jpg")
//                Texture.loadTexture("/home/rahmati/projects/piana-gl/src/main/resources/res/texture-2.jpeg")
        ).addTextureCoordinates(new float[] {
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.5f, 1.0f
        });

        meshList.add(mesh);

        meshList.addAll(renderer.DrawTexturedModel(1, 2, 3, shaderProgramId));
    }

    @Override
    public void render() {

        cameraController.MCSpinCamera();

        meshList.stream().forEach(mesh -> {
//            float[] floats = cameraController.computeMatricesFromInputs().toFa_();
            float[] floats = cameraController.MCLookAt().toFa_();

            mesh.render(cameraController.MCLookAt().toFa_());
//            mesh.render(cameraController.computeMatricesFromInputs().toFa_());
//            mesh.render(windowContext.getMvp(new Vec3(0, 0, 10), new Vec3(0, 0, 0)).toFa_());
        });
    }
}
