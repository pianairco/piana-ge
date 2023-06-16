package ir.piana.dev.gl.toolkit;

import java.util.List;

public class ms3d_material {
    byte name[] = new byte[32];
    float ambient[] = new float[4];
    float diffuse[] = new float[4];
    float specular[] = new float[4];
    float emissive[] = new float[4];
    float shininess;
    float transparency;
    byte mode;
    byte texture[] = new byte[CMS3DModelLoader.MAX_TEXTURE_FILENAME_SIZE];
    byte alphamap[] = new byte[CMS3DModelLoader.MAX_TEXTURE_FILENAME_SIZE];
    int id;
    List<Character> comment;
}
