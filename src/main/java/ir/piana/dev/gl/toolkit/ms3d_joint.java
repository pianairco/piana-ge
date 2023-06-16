package ir.piana.dev.gl.toolkit;

import java.util.List;

public class ms3d_joint {
    byte flags;
    byte name[] = new byte[32];
    byte parentName[] = new byte[32];

    float rot[] = new float[3];
    float pos[] = new float[3];

    List<ms3d_keyframe> rotationKeys;
    List<ms3d_keyframe> positionKeys;
    List<ms3d_tangent> tangents;

    List<Character> comment;
    float color[] = new float[3];

    // used for rendering

    int parentIndex;
    float matLocalSkeleton[][] = new float[3][4];
    float matGlobalSkeleton[][] = new float[3][4];

    float matLocal[][] = new float[3][4];
    float matGlobal[][] = new float[3][4];
}
