package ir.piana.dev.gl.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class ByteManipulator {
    public static String readString(ByteArrayInputStream bais, int length) throws IOException {
        return new String(bais.readNBytes(length), Charset.forName("ASCII"));
    }

    public static byte[] readBytes(ByteArrayInputStream bais, int length) throws IOException {
        return bais.readNBytes(length);
    }

    public static byte readByte(ByteArrayInputStream bais) throws IOException {
        return bais.readNBytes(1)[0];
    }

    public static float readFloat(ByteArrayInputStream bais) throws IOException {
        float[] floats = new float[1];
        ByteBuffer.wrap(bais.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(floats);
        return floats[0];
    }

    public static float[] readFloats(ByteArrayInputStream bais, int length) throws IOException {
        float[] floats = new float[length];
        ByteBuffer.wrap(bais.readNBytes(length * 4)).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(floats);
        return floats;
    }

    public static int readInt(ByteArrayInputStream bais) throws IOException {
        int[] versionBuffer = new int[1];
        ByteBuffer.wrap(bais.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(versionBuffer);
        return versionBuffer[0];
    }

    public static int[] readInts(ByteArrayInputStream bais, int length) throws IOException {
        int[] versionBuffer = new int[length];
        ByteBuffer.wrap(bais.readNBytes(length * 4)).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(versionBuffer);
        return versionBuffer;
    }

    public static short readShort(ByteArrayInputStream bais) throws IOException {
        short[] sizeBuffer = new short[1];
        ByteBuffer.wrap(bais.readNBytes(2)).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(sizeBuffer);
        return sizeBuffer[0];
    }

    public static short[] readShorts(ByteArrayInputStream bais, int length) throws IOException {
        short[] sizeBuffer = new short[length];
        ByteBuffer.wrap(bais.readNBytes(2 * length)).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(sizeBuffer);
        return sizeBuffer;
    }

    public static char readChar(ByteArrayInputStream bais) throws IOException {
        char[] sizeBuffer = new char[1];
        ByteBuffer.wrap(bais.readNBytes(2)).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer().get(sizeBuffer);
        return sizeBuffer[0];
    }

    public static char[] readChars(ByteArrayInputStream bais, int length) throws IOException {
        char[] sizeBuffer = new char[length];
        ByteBuffer.wrap(bais.readNBytes(2 * length)).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer().get(sizeBuffer);
        return sizeBuffer;
    }

    public static char readSize(ByteArrayInputStream bais) throws IOException {
        char[] sizeBuffer = new char[1];
        ByteBuffer.wrap(bais.readNBytes(2)).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer().get(sizeBuffer);
        return sizeBuffer[0];
    }
}
