package ir.piana.dev.gl.toolkit;

import glm.mat._4.d.Mat4d;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;
import glm.vec._4.d.Vec4d;

public class MathLib {
    public static final double Q_PI = 3.14159265358979323846;

    double DotProduct(Vec3 x, Vec3 y) {
        return (x.x * y.x + x.y * y.y + x.z * y.z);
    }

    public static float DotProduct(float[] x, float[] y) {
        return (x[0] * y[0] + x[1] * y[1] + x[2] * y[2]);
    }

    public static void AngleMatrix(float angles[], float matrix[][]) {
        float angle;
        float sr, sp, sy, cr, cp, cy;

        angle = angles[2];
        sy = (float) Math.sin(angle);
        cy = (float) Math.cos(angle);
        angle = angles[1];
        sp = (float) Math.sin(angle);
        cp = (float) Math.cos(angle);
        angle = angles[0];
        sr = (float) Math.sin(angle);
        cr = (float) Math.cos(angle);

        // matrix = (Z * Y) * X
        matrix[0][0] = cp * cy;
        matrix[1][0] = cp * sy;
        matrix[2][0] = -sp;
        matrix[0][1] = sr * sp * cy + cr * -sy;
        matrix[1][1] = sr * sp * sy + cr * cy;
        matrix[2][1] = sr * cp;
        matrix[0][2] = (cr * sp * cy + -sr * -sy);
        matrix[1][2] = (cr * sp * sy + -sr * cy);
        matrix[2][2] = cr * cp;
        matrix[0][3] = 0.0f;
        matrix[1][3] = 0.0f;
        matrix[2][3] = 0.0f;
    }

    public static void AngleMatrix(Vec3 angles, Mat4d matrix) {
        float angle;
        double sr, sp, sy, cr, cp, cy;

        angle = angles.z;
        sy = Math.sin(angle);
        cy = Math.cos(angle);
        angle = angles.y;
        sp = Math.sin(angle);
        cp = Math.cos(angle);
        angle = angles.x;
        sr = Math.sin(angle);
        cr = Math.cos(angle);

        // matrix = (Z * Y) * X
        matrix.m00 = cp * cy;
        matrix.m10 = cp * sy;
        matrix.m20 = -sp;
        matrix.m01 = sr * sp * cy + cr * -sy;
        matrix.m11 = sr * sp * sy + cr * cy;
        matrix.m21 = sr * cp;
        matrix.m02 = (cr * sp * cy + -sr * -sy);
        matrix.m12 = (cr * sp * sy + -sr * cy);
        matrix.m22 = cr * cp;
        matrix.m03 = 0.0;
        matrix.m13 = 0.0;
        matrix.m23 = 0.0;
    }

//-----------------------------------------------------------------------

    public static void R_ConcatTransforms(float in1[][], float in2[][], float out[][])
//    void R_ConcatTransforms (float in1[3][4], const float in2[3][4], float out[3][4])
    {
        out[0][0] = in1[0][0] * in2[0][0] + in1[0][1] * in2[1][0] +
                in1[0][2] * in2[2][0];
        out[0][1] = in1[0][0] * in2[0][1] + in1[0][1] * in2[1][1] +
                in1[0][2] * in2[2][1];
        out[0][2] = in1[0][0] * in2[0][2] + in1[0][1] * in2[1][2] +
                in1[0][2] * in2[2][2];
        out[0][3] = in1[0][0] * in2[0][3] + in1[0][1] * in2[1][3] +
                in1[0][2] * in2[2][3] + in1[0][3];
        out[1][0] = in1[1][0] * in2[0][0] + in1[1][1] * in2[1][0] +
                in1[1][2] * in2[2][0];
        out[1][1] = in1[1][0] * in2[0][1] + in1[1][1] * in2[1][1] +
                in1[1][2] * in2[2][1];
        out[1][2] = in1[1][0] * in2[0][2] + in1[1][1] * in2[1][2] +
                in1[1][2] * in2[2][2];
        out[1][3] = in1[1][0] * in2[0][3] + in1[1][1] * in2[1][3] +
                in1[1][2] * in2[2][3] + in1[1][3];
        out[2][0] = in1[2][0] * in2[0][0] + in1[2][1] * in2[1][0] +
                in1[2][2] * in2[2][0];
        out[2][1] = in1[2][0] * in2[0][1] + in1[2][1] * in2[1][1] +
                in1[2][2] * in2[2][1];
        out[2][2] = in1[2][0] * in2[0][2] + in1[2][1] * in2[1][2] +
                in1[2][2] * in2[2][2];
        out[2][3] = in1[2][0] * in2[0][3] + in1[2][1] * in2[1][3] +
                in1[2][2] * in2[2][3] + in1[2][3];
    }

//-----------------------------------------------------------------------

    public static void AngleQuaternion(float[] angles, float[] quaternion) {
        float angle;
        float sr, sp, sy, cr, cp, cy;

        // FIXME: rescale the inputs to 1/2 angle
        angle = angles[2] * 0.5f;
        sy = (float) Math.sin(angle);
        cy = (float) Math.cos(angle);
        angle = angles[1] * 0.5f;
        sp = (float) Math.sin(angle);
        cp = (float) Math.cos(angle);
        angle = angles[0] * 0.5f;
        sr = (float) Math.sin(angle);
        cr = (float) Math.cos(angle);

        quaternion[0] = sr * cp * cy - cr * sp * sy; // X
        quaternion[1] = cr * sp * cy + sr * cp * sy; // Y
        quaternion[2] = cr * cp * sy - sr * sp * cy; // Z
        quaternion[3] = cr * cp * cy + sr * sp * sy; // W
    }

    void AngleQuaternion(Vec3 angles, Vec4d quaternion)
//    void AngleQuaternion( const vec3_t angles, vec4_t quaternion )
    {
        double angle;
        double sr, sp, sy, cr, cp, cy;

        // FIXME: rescale the inputs to 1/2 angle
        angle = angles.z * 0.5;
        sy = Math.sin(angle);
        cy = Math.cos(angle);
        angle = angles.y * 0.5;
        sp = Math.sin(angle);
        cp = Math.cos(angle);
        angle = angles.x * 0.5;
        sr = Math.sin(angle);
        cr = Math.cos(angle);

        quaternion.x = sr * cp * cy - cr * sp * sy; // X
        quaternion.y = cr * sp * cy + sr * cp * sy; // Y
        quaternion.z = cr * cp * sy - sr * sp * cy; // Z
        quaternion.w = cr * cp * cy + sr * sp * sy; // W
    }

//-----------------------------------------------------------------------

    float getFromVec(Vec4 v, int index) {
        return index == 0 ? v.x : (index == 1 ? v.y : (index == 2 ? v.z : v.w));
    }

    public static void QuaternionSlerp(float[] p/*[4]*/, float[] q/*[4]*/, float t, float[] qt/*[4]*/) {
        int i;
        float omega, cosom, sinom, sclp, sclq;

        // decide if one of the quaternions is backwards
        float a = 0;
        float b = 0;
        for (i = 0; i < 4; i++) {
            a += (p[i] - q[i]) * (p[i] - q[i]);
            b += (p[i] + q[i]) * (p[i] + q[i]);
        }
        if (a > b) {
            for (i = 0; i < 4; i++) {
                q[i] = -q[i];
            }
        }

        cosom = p[0] * q[0] + p[1] * q[1] + p[2] * q[2] + p[3] * q[3];

        if ((1.0 + cosom) > 0.00000001) {
            if ((1.0 - cosom) > 0.00000001) {
                omega = (float) Math.acos(cosom);
                sinom = (float) Math.sin(omega);
                sclp = (float) Math.sin((1.0 - t) * omega) / sinom;
                sclq = (float) Math.sin(t * omega) / sinom;
            } else {
                sclp = 1.0f - t;
                sclq = t;
            }
            for (i = 0; i < 4; i++) {
                qt[i] = sclp * p[i] + sclq * q[i];
            }
        } else {
            qt[0] = -p[1];
            qt[1] = p[0];
            qt[2] = -p[3];
            qt[3] = p[2];
            sclp = (float) Math.sin((1.0 - t) * 0.5 * Q_PI);
            sclq = (float) Math.sin(t * 0.5 * Q_PI);
            for (i = 0; i < 3; i++) {
                qt[i] = sclp * p[i] + sclq * qt[i];
            }
        }
    }

//-----------------------------------------------------------------------

    public static void QuaternionMatrix(float[] quaternion/*[4]*/, float[][] matrix/*[4][4]*/) {
        matrix[0][0] = 1.0f - 2.0f * quaternion[1] * quaternion[1] - 2.0f * quaternion[2] * quaternion[2];
        matrix[1][0] = 2.0f * quaternion[0] * quaternion[1] + 2.0f * quaternion[3] * quaternion[2];
        matrix[2][0] = 2.0f * quaternion[0] * quaternion[2] - 2.0f * quaternion[3] * quaternion[1];

        matrix[0][1] = 2.0f * quaternion[0] * quaternion[1] - 2.0f * quaternion[3] * quaternion[2];
        matrix[1][1] = 1.0f - 2.0f * quaternion[0] * quaternion[0] - 2.0f * quaternion[2] * quaternion[2];
        matrix[2][1] = 2.0f * quaternion[1] * quaternion[2] + 2.0f * quaternion[3] * quaternion[0];

        matrix[0][2] = 2.0f * quaternion[0] * quaternion[2] + 2.0f * quaternion[3] * quaternion[1];
        matrix[1][2] = 2.0f * quaternion[1] * quaternion[2] - 2.0f * quaternion[3] * quaternion[0];
        matrix[2][2] = 1.0f - 2.0f * quaternion[0] * quaternion[0] - 2.0f * quaternion[1] * quaternion[1];
    }

//-----------------------------------------------------------------------

    public static void VectorRotate(float[] in1/*[3]*/, float[][] in2/*[3][4]*/, float[] out/*[3]*/) {
        out[0] = DotProduct(in1, in2[0]);
        out[1] = DotProduct(in1, in2[1]);
        out[2] = DotProduct(in1, in2[2]);
    }
//-----------------------------------------------------------------------

    // rotate by the inverse of the matrix
    public static void VectorIRotate(float[] in1, float[][] in2/*[3][4]*/, float[] out/*[3]*/) {
        out[0] = in1[0] * in2[0][0] + in1[1] * in2[1][0] + in1[2] * in2[2][0];
        out[1] = in1[0] * in2[0][1] + in1[1] * in2[1][1] + in1[2] * in2[2][1];
        out[2] = in1[0] * in2[0][2] + in1[1] * in2[1][2] + in1[2] * in2[2][2];
    }

//-----------------------------------------------------------------------

    void VectorTransform(float[] in1/*[3]*/, float[][] in2/*[3][4]*/, float[] out/*[3]*/) {
        out[0] = DotProduct(in1, in2[0]) + in2[0][3];
        out[1] = DotProduct(in1, in2[1]) + in2[1][3];
        out[2] = DotProduct(in1, in2[2]) + in2[2][3];
    }

//-----------------------------------------------------------------------

    void VectorITransform(float[] in1/*[3]*/, float[][] in2/*[3][4]*/, float[] out/*[3]*/) {
        float[] tmp = new float[3];
        tmp[0] = in1[0] - in2[0][3];
        tmp[1] = in1[1] - in2[1][3];
        tmp[2] = in1[2] - in2[2][3];
        VectorIRotate(tmp, in2, out);
    }

//-----------------------------------------------------------------------

    float RadianToDegree(float angle) {
        return (float) ((angle * 180.0f) / Q_PI);
    }
}
