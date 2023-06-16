package ir.piana.dev.gl.toolkit;

public class ms3d_vertex {
    byte flags;
    float vertex[] = new float[3];
    //-----------------------------------
    //-- index of joints in milkshape
    //-----------------------------------
    byte boneId;
    byte referenceCount;

    byte[] boneIds = new byte[3];
    byte[] weights = new byte[3];
    int extra;
    float renderColor[] = new float[3];

    public ms3d_vertex(byte flags, float[] vertex, byte boneId, byte referenceCount) {
        this.flags = flags;
        this.vertex = vertex;
        this.boneId = boneId;
        this.referenceCount = referenceCount;
    }
}
