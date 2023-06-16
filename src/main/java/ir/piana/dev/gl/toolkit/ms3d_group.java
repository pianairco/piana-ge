package ir.piana.dev.gl.toolkit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ms3d_group {
    byte flags;
    String name;// = new byte[32];
    short[] triangleIndices;
    byte materialIndex;
    String comment;
}
