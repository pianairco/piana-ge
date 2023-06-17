package ir.piana.dev.gl.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Locale;

import ir.piana.dev.gl.toolkit.CMS3DModelLoader;
import org.apache.commons.io.FileUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL11.*;

public class Texture {
    private static HashMap<String, Integer> idMap = new HashMap<String, Integer>();

    public static int loadTexture(String texture) {
        int width;
        int height;
        ByteBuffer buffer;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (idMap.containsKey(texture)) {
                return idMap.get(texture);
            }
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            URL url = texture.startsWith("classpath-") ?
                    CMS3DModelLoader.class.getResource(
                            texture.substring(10).trim()) :
                    new URL(texture);

            String path = null;
            if (System.getProperty("os.name").contains("Windows")) { // TODO Language/region agnostic value for 'Windows' ?
                // stbi_load requires a file system path, NOT a classpath resource path
                path = url.toString().substring(6);
            } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                path = File.separator + url.toString().substring(6);
            }

            buffer = STBImage.stbi_load(path , w, h, channels, 4);
//            buffer = STBImage.stbi_load_from_memory(byteBuffer, w, h, channels, 4);
            if (buffer == null) {
                throw new Exception("Can't load file " + texture + " " + STBImage.stbi_failure_reason());
            }
            width = w.get();
            height = h.get();

            int id = GL11.glGenTextures();
            idMap.put(texture, id);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);

            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

//            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            STBImage.stbi_image_free(buffer);
            return id;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /*public static int loadPNG(File dir, String texName) {
        File f = new File(dir+"/"+texName+".jpeg");
        try {
            if(!f.exists()) throw new IOException();

            IntBuffer w = BufferUtils.createIntBuffer(1);
            IntBuffer h = BufferUtils.createIntBuffer(1);
            IntBuffer comp = BufferUtils.createIntBuffer(1);
            ByteBuffer buffer = STBImage.stbi_load(f.toString(),w,h,comp,4);

            if(buffer == null) throw new IOException();

            int imgWidth = w.get();
            int imgHeight = h.get();

            int texId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL21.GL_SRGB8_ALPHA8, imgWidth, imgHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
            ARBFramebufferObject.glGenerateMipmap(ARBInternalformatQuery2.GL_TEXTURE_2D);

            GL11.glTexParameteri(ARBInternalformatQuery2.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GL11.glTexParameteri(ARBInternalformatQuery2.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

            buffer.clear();

            return texId;
        } catch (IOException e) {
//            Logging.errMsg("Texture " + texName + " doesn't exit");
            return 0;
        }
    }*/
}
