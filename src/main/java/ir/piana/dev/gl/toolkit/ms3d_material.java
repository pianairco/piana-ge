package ir.piana.dev.gl.toolkit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ms3d_material {
    String name;//[32];
    float ambient[];// = new float[4];
    float diffuse[];// = new float[4];
    float specular[];// = new float[4];
    float emissive[];// = new float[4];
    float shininess;
    float transparency;
    byte mode;
    String texture;
//    byte texture[] = new byte[CMS3DModelLoader.MAX_TEXTURE_FILENAME_SIZE];
    String alphamap;
//    byte alphamap[] = new byte[CMS3DModelLoader.MAX_TEXTURE_FILENAME_SIZE];
    int id;
    List<Character> comment;
}
