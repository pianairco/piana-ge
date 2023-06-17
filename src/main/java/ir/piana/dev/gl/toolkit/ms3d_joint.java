package ir.piana.dev.gl.toolkit;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ms3d_joint {
    byte flags;
    String name;// [32];
    String parentName;// [32];

    float rot[];// = new float[3];
    float pos[];// = new float[3];

    List<ms3d_keyframe> rotationKeys;
    List<ms3d_keyframe> positionKeys;
    List<ms3d_tangent> tangents;

    List<Character> comment;
    private float color[];// = new float[3];

    // used for rendering

    int parentIndex;
    float matLocalSkeleton[][] = new float[3][4];
    float matGlobalSkeleton[][] = new float[3][4];

    float matLocal[][] = new float[3][4];
    float matGlobal[][] = new float[3][4];
}
