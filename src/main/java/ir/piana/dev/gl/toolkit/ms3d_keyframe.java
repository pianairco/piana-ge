package ir.piana.dev.gl.toolkit;

import lombok.Builder;

@Builder
public class ms3d_keyframe {
    float time;
    float key[] = new float[3];
}
