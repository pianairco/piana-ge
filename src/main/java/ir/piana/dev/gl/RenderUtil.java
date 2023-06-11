package ir.piana.dev.gl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class RenderUtil {
    public static FloatBuffer createFloatBuffer(float[] data) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    public static IntBuffer createIntBuffer(int[] data) {
        IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    public static void unbindVertexArray() {
        GL30.glBindVertexArray(0);
    }

    public static int createVertexArray() {
        int vao = GL30.glGenVertexArrays();
        return vao;
    }

    public static void bindVertexArray(int vaoId) {
        GL30.glBindVertexArray(vaoId);
    }

    public static int fillBuffer(int locationInShader, int dimensions, float[] data) {
        GL20.glEnableVertexAttribArray(locationInShader);
        int vbo = GL15.glGenBuffers(); //Creates a VBO ID
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo); //Loads the current VBO to store the data
        FloatBuffer buffer = createFloatBuffer(data);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(locationInShader, dimensions, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0); //Unloads the current VBO when done.
        return vbo;
    }

    public static int bindIndices(int[] data) {
        int vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbo);
        IntBuffer buffer = createIntBuffer(data);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        return vbo;
    }

    public static int createVertexShader(String vertexShaderSource) {
        int vertexShaderId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);

        GL20.glShaderSource(vertexShaderId, vertexShaderSource); // attach the shader obj
        // 1st argument: the object
        // 2nd argument: how many strings

        GL20.glCompileShader(vertexShaderId);
        return vertexShaderId;
    }

    public static int createFragmentShader(String fragmentShaderSource) {
        int fragmentShaderId = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);

        GL20.glShaderSource(fragmentShaderId, fragmentShaderSource); // attach the shader obj
        // 1st argument: the object
        // 2nd argument: how many strings

        GL20.glCompileShader(fragmentShaderId);
        return fragmentShaderId;
    }

    public static int createShaderProgram(int vertexShaderId, int fragmentShaderId) {
        int shaderProgramId = GL20.glCreateProgram();
        GL20.glAttachShader(shaderProgramId, vertexShaderId); // attach the shaders to the program obj
        GL20.glAttachShader(shaderProgramId, fragmentShaderId);

        GL20.glLinkProgram(shaderProgramId);
        GL20.glValidateProgram(shaderProgramId);
// check for linking errors...
        GL20.glDeleteShader(vertexShaderId); // once we link the shader objects, they are no longer needed
        GL20.glDeleteShader(fragmentShaderId);
        return shaderProgramId;
    }

    public static int createShaderProgram(String vertexShaderSource, String fragmentShaderSource) {
        return createShaderProgram(
                createVertexShader(vertexShaderSource),
                createFragmentShader(fragmentShaderSource));
    }

    public static void useShaderProgram(int shaderProgramId) {
        GL20.glUseProgram(shaderProgramId); // activate the program
    }
}
