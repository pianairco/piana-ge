package ir.piana.dev.gl.toolkit;

import ir.piana.dev.gl.RenderUtil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;

public class Mesh {
    private int vaoId;
    private List<Integer> vboIdList;
    private Integer indexArrayId;
    private int indexCount;
    private int shaderProgramId;
    private Integer textureId;

    private Mesh() {
        vboIdList = new ArrayList();
        vaoId = RenderUtil.createVertexArray();
    }

    public static Mesh builder() {
        return new Mesh();
    }

    public Mesh addVertices(float[] vertices) {
        RenderUtil.bindVertexArray(vaoId);
        vboIdList.add(RenderUtil.fillBuffer(0, 3, vertices));
        RenderUtil.bindVertexArray(0);
        return this;
    }

    public Mesh addColors(float[] colors) {
        RenderUtil.bindVertexArray(vaoId);
        vboIdList.add(RenderUtil.fillBuffer(1, 3, colors));
        RenderUtil.bindVertexArray(0);
        return this;
    }

    public Mesh addTextureCoordinates(float[] texCoords) {
        RenderUtil.bindVertexArray(vaoId);
        vboIdList.add(RenderUtil.fillBuffer(2, 2, texCoords));
        RenderUtil.bindVertexArray(0);
        return this;
    }

    public Mesh addTexture(int textureId) {
        this.textureId = textureId;
        return this;
    }

    public Mesh addIndices(int[] indices) {
        RenderUtil.bindVertexArray(vaoId);
        indexArrayId = RenderUtil.bindIndices(indices);
        this.indexCount = indices.length;
        RenderUtil.bindVertexArray(0);
        return this;
    }


    public Mesh addShaderProgram(int shaderProgramId) {
        this.shaderProgramId = shaderProgramId;
        return this;
    }

    public void render(float[] mvpBuf) {

        if(textureId != null) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        }

        if(indexArrayId != null) {
            GL20.glUseProgram(shaderProgramId); // activate the program

            if (mvpBuf != null) {
                int mvp = GL20.glGetUniformLocation(shaderProgramId, "MVP");
                GL20.glUniformMatrix4fv(mvp, false, mvpBuf);
            }

            GL30.glBindVertexArray(vaoId);
            GL11.glDrawElements(GL11.GL_TRIANGLES, indexCount, GL11.GL_UNSIGNED_INT, 0);
            GL30.glBindVertexArray(0);
        } else {
            GL20.glUseProgram(shaderProgramId); // activate the program
            GL30.glBindVertexArray(vaoId);
//            GL20.glEnableVertexAttribArray(0);
//            GL20.glEnableVertexAttribArray(1);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3); // Starting from vertex 0; 3 vertices total -> 1 triangle
//            GL20.glDisableVertexAttribArray(0);
//            GL20.glDisableVertexAttribArray(1);
        }
        if(textureId != null) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }
        RenderUtil.unbindVertexArray();
    }
}
