package ir.piana.dev.gl.toolkit;

public class ms3d_triangle {
    short flags;
    char[] vertexIndices;// = new short[3];
    float[] vertexNormals;// = new float[9];
    float[] s = new float[3];
    float[] t = new float[3];
    float[] normal = new float[3];
    byte smoothingGroup;
    byte groupIndex;

    public ms3d_triangle(short flags,
                         char vertexIndices[],
                         float vertexNormals[],
                         float s[],
                         float t[],
                         float normal[],
                         byte smoothingGroup,
                         byte groupIndex) {
        this.flags = flags;
        this.vertexIndices = vertexIndices;
        this.vertexNormals = vertexNormals;
        this.s = s;
        this.t = t;
        this.normal = normal;
        this.smoothingGroup = smoothingGroup;
        this.groupIndex = groupIndex;
    }
}
