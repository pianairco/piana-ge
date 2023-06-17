package ir.piana.dev.gl.toolkit;

import org.apache.commons.io.FileUtils;
import org.lwjgl.system.MemoryStack;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class CMS3DModelLoader {
    public static final int MAX_VERTICES = 65534;
    public static final int MAX_TRIANGLES = 65534;
    public static final int MAX_GROUPS = 255;
    public static final int MAX_MATERIALS = 128;
    public static final int MAX_JOINTS = 128;
    public static final int MAX_TEXTURE_FILENAME_SIZE = 128;

    public static final int SELECTED = 1;
    public static final int HIDDEN = 2;
    public static final int SELECTED2 = 4;
    public static final int DIRTY = 8;
    public static final int ISKEY = 16;
    public static final int NEWLYCREATED = 32;
    public static final int MARKED = 64;

    public static final int SPHEREMAP = 0x80;
    public static final int HASALPHA = 0x40;
    public static final int COMBINEALPHA = 0x20;

    public static final int TRANSPARENCY_MODE_SIMPLE = 0;
    public static final int TRANSPARENCY_MODE_DEPTHSORTEDTRIANGLES = 1;
    public static final int TRANSPARENCY_MODE_ALPHAREF = 2;


    protected float model_animationFps;
    protected float model_currentTime;
    protected int model_totalFrames;

    protected float model_jointSize;
    protected int model_transparencyMode;
    protected float model_alphaRef;

    protected List<ms3d_vertex> model_vertices = new ArrayList<>();
    protected List<ms3d_triangle> model_triangles = new ArrayList<>();
    protected List<ms3d_group> model_groups = new ArrayList<>();
    protected List<ms3d_material> model_materials = new ArrayList<>();
    protected List<ms3d_joint> model_joints = new ArrayList<>();
    protected List<Character> model_comment = new ArrayList<>();

    //------------------------------------------------------
    //---- model inclusive
    //------------------------------------------------------

    protected boolean isVertex;
    protected boolean isTriangle;
    protected boolean isGroup;
    protected boolean isMaterial;
    protected boolean isJoint;

    //------------------------------------------------------
    //---- for store vertices and joints original info
    //------------------------------------------------------

    protected float[] vertices_array;

    protected float[] joints_array;

    //------------------------------------------------------
    //----
    //------------------------------------------------------

    float[] group_joints_array;
    float[] color_joints_array;
    float[] p_color_joints_array;

    float[] group_vertices_array;
    float[] normals_array;
    float[] tex_coord_array;

    //------------------------------------------------------
    //----
    //------------------------------------------------------

    short[] joints_indices;
    short[] p_joints_indices;

    short[] vertices_indices;

    //------------------------------------------------------
    //----
    //------------------------------------------------------

    char numVertices;
    char numTriangles;
    char numIndices;
    char numGroups;

    char[] numGroupTriangles;
    char[] numGroupIndices;

    char numMaterials;
    char numJoints;

    char numKeyFramesRot;
    char numKeyFramesPos;

    //------------------------------------------------------
    //---- textures
    //------------------------------------------------------

    int[] materialsIndex;

    //------------------------------------------------------
    //---- variable bones
    //------------------------------------------------------

    String[] parent;

    public CMS3DModelLoader() {
        ModelClear();
    }

    void ModelClear() {
        isVertex = false;
        isJoint = false;
        isTriangle = false;
        isGroup = false;
        isMaterial = false;

        model_animationFps = 24.0f;
        model_currentTime = 1.0f;
        model_totalFrames = 30;

        model_jointSize = 1.0f;
        model_transparencyMode = TRANSPARENCY_MODE_SIMPLE;
        model_alphaRef = 0.5f;

        model_vertices.clear();
        model_triangles.clear();
        model_groups.clear();
        model_materials.clear();
        model_joints.clear();
        model_comment.clear();
    }

    boolean IsCorrectID(String id) {
        if (!id.equalsIgnoreCase("MS3D000000")) {
            // "This is not a valid MS3D file format!"
            return false;
        }
        return true;
    }

    boolean IsCorrectVersion(final int version) {
        if (version != 4) {
            // "This is not a valid MS3D file version!"
            return false;
        }
        return true;
    }

    public String readString(ByteArrayInputStream bais, int length) throws IOException {
        return new String(bais.readNBytes(length), Charset.forName("ASCII"));
    }

    public byte[] readBytes(ByteArrayInputStream bais, int length) throws IOException {
        return bais.readNBytes(length);
    }

    public byte readByte(ByteArrayInputStream bais) throws IOException {
        return bais.readNBytes(1)[0];
    }

    public float readFloat(ByteArrayInputStream bais) throws IOException {
        float[] floats = new float[1];
        ByteBuffer.wrap(bais.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(floats);
        return floats[0];
    }

    public float[] readFloats(ByteArrayInputStream bais, int length) throws IOException {
        float[] floats = new float[length];
        ByteBuffer.wrap(bais.readNBytes(length * 4)).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(floats);
        return floats;
    }

    public int readInt(ByteArrayInputStream bais) throws IOException {
        int[] versionBuffer = new int[1];
        ByteBuffer.wrap(bais.readNBytes(4)).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(versionBuffer);
        return versionBuffer[0];
    }

    public int[] readInts(ByteArrayInputStream bais, int length) throws IOException {
        int[] versionBuffer = new int[length];
        ByteBuffer.wrap(bais.readNBytes(length * 4)).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(versionBuffer);
        return versionBuffer;
    }

    public short readShort(ByteArrayInputStream bais) throws IOException {
        short[] sizeBuffer = new short[1];
        ByteBuffer.wrap(bais.readNBytes(2)).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(sizeBuffer);
        return sizeBuffer[0];
    }

    public short[] readShorts(ByteArrayInputStream bais, int length) throws IOException {
        short[] sizeBuffer = new short[length];
        ByteBuffer.wrap(bais.readNBytes(2 * length)).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(sizeBuffer);
        return sizeBuffer;
    }

    public char readChar(ByteArrayInputStream bais) throws IOException {
        char[] sizeBuffer = new char[1];
        ByteBuffer.wrap(bais.readNBytes(2)).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer().get(sizeBuffer);
        return sizeBuffer[0];
    }

    public char[] readChars(ByteArrayInputStream bais, int length) throws IOException {
        char[] sizeBuffer = new char[length];
        ByteBuffer.wrap(bais.readNBytes(2 * length)).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer().get(sizeBuffer);
        return sizeBuffer;
    }

    public char readSize(ByteArrayInputStream bais) throws IOException {
        char[] sizeBuffer = new char[1];
        ByteBuffer.wrap(bais.readNBytes(2)).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer().get(sizeBuffer);
        return sizeBuffer[0];
    }

    public boolean initModel(String filename) throws IOException {
        int startOfVertices = 0;
        int startOfTriangles = 0;
        int startOfGroups = 0;
        int startOfMaterials = 0;
        int startOfAnimation = 0;
        int startOfJoints = 0;

        /*InputStream resourceAsStream = CMS3DModelLoader.class.getResourceAsStream(
                filename.substring(10).trim());
        int available = resourceAsStream.available();
        byte[] bytes =  resourceAsStream.readNBytes(10);
        if(!IsCorrectID(new String(bytes))) {
            System.out.println("file is incorrect!");
        }*/

        try (MemoryStack stack = MemoryStack.stackPush();
             ByteArrayInputStream bais = (filename.startsWith("classpath-") ?
                new ByteArrayInputStream(CMS3DModelLoader.class.getResourceAsStream(
                        filename.substring(10).trim()).readAllBytes()) :
                new ByteArrayInputStream(FileUtils.readFileToByteArray(new File(filename))))) {
            byte[] bytes = readBytes(bais, 10);
            if(!IsCorrectID(new String(bytes))) {
                System.out.println("file is incorrect!");
                throw new Exception("error occurred!");
            }

            int version = readInt(bais);
            if (!IsCorrectVersion(version)) {
                System.out.println("file is incorrect!");
                throw new Exception("error occurred!");
            }

            ModelClear();

            //----------------------------------
            //--------------number of vertices
            //----------------------------------

            numVertices = readChar(bais);
            if (numVertices > 0) {
                isVertex = true;
            }

            model_vertices = new ArrayList<>(numVertices);

            for (int i = 0; i < numVertices; i++) {
                byte flags = readByte(bais);
                float[] vertex = readFloats(bais, 3);
                byte boneId = readByte(bais);
                byte referenceCount = readByte(bais);

                model_vertices.add(new ms3d_vertex(flags, vertex, boneId, referenceCount));
            }

            numTriangles = readSize(bais);
            if (numTriangles > 0) {
                isTriangle = true;
            }
            model_triangles = new ArrayList<>(numTriangles);

            for (int i = 0; i < numTriangles; i++) {

                short flags = readShort(bais);
                char[] vertexIndices = readChars(bais, 3);
                float[] vertexNormals = readFloats(bais, 9);
                float[] s = readFloats(bais, 3);
                float[] t = readFloats(bais, 3);
                byte smoothingGroup = readByte(bais);
                byte groupIndex = readByte(bais);


                model_triangles.add(new ms3d_triangle(
                        flags, vertices_indices, vertexNormals, s, t, new float[3], smoothingGroup, groupIndex
                ));
                // TODO: calculate triangle normal
            }

            numGroups = readChar(bais);
            if (numGroups > 0) {
                isGroup = true;
            }
            model_groups = new ArrayList(numGroups);

            numGroupTriangles = new char[numGroups];
            numGroupIndices = new char[numGroups];

            int numGT = 0;
            for (int i = 0; i < numGroups; i++) {
                byte flags = readByte(bais);
                String name = readString(bais, 32);

                numGroupTriangles[i] = readChar(bais);
                numGroupIndices[i] = (char) (3 * numGroupTriangles[i]);

                short[] triangleIndices = new short[numGroupTriangles[i]];
                if (numGroupTriangles[i] > 0)
                    triangleIndices = readShorts(bais, numGroupTriangles[i]);
                numGT += numGroupTriangles[i];
                byte materialIndex = readByte(bais);

                model_groups.add(new ms3d_group(flags, name, triangleIndices, materialIndex, null));
            }

            numMaterials = readChar(bais);
            if (numMaterials > 0) {
                isMaterial = true;
            }
            model_materials = new ArrayList(numMaterials);

            for (int i = 0; i < numMaterials; i++) {
                String name = readString(bais, 32);
                float[] ambient = readFloats(bais, 4);
                float[] diffuse = readFloats(bais, 4);
                float[] specular = readFloats(bais, 4);
                float[] emissive = readFloats(bais, 4);
                float shininess = readFloat(bais);
                float transparency = readFloat(bais);
                byte mode = readByte(bais);
                String texture = readString(bais, MAX_TEXTURE_FILENAME_SIZE);
                String alphamap = readString(bais, MAX_TEXTURE_FILENAME_SIZE);

                // set alpha
                ambient[3] = transparency;
                diffuse[3] = transparency;
                specular[3] = transparency;
                emissive[3] = transparency;

                model_materials.add(new ms3d_material(name,
                        ambient, diffuse, specular, emissive,
                        shininess, transparency, mode,
                        texture, alphamap, 0, null));

            }

            //-----------------------------------------
            //-------------animation
            //-----------------------------------------

            model_animationFps = readFloat(bais);

            if (model_animationFps < 1.0f)
                model_animationFps = 1.0f;
            model_currentTime = readFloat(bais);
            model_totalFrames = readInt(bais);

            //-------------------------
            //------------ joints
            //-------------------------

            numJoints = readChar(bais);
            if (numJoints > 0) {
                isJoint = true;
            }
            model_joints = new ArrayList(numJoints);

            parent = new String[numJoints];

            /*for (int i = 0; i < numJoints; i++) {
                parent[i] = new char[32];
            }*/

            for (int i = 0; i < numJoints; i++) {
                byte flags = readByte(bais);
                String name = readString(bais, 32);
                parent[i] = name;
                String parentName =readString(bais, 32);
                float[] rot = readFloats(bais, 3);
                float[] pos = readFloats(bais, 3);

                numKeyFramesRot = readChar(bais);
                List<ms3d_keyframe> rotationKeys = new ArrayList(numKeyFramesRot);

                numKeyFramesPos = readChar(bais);
                List<ms3d_keyframe> positionKeys = new ArrayList(numKeyFramesPos);

                // the frame time is in seconds, so multiply it by the animation fps, to get the frames
                // rotation channel
                for (int j = 0; j < numKeyFramesRot; j++) {
                    rotationKeys.add(ms3d_keyframe.builder()
                            .time(model_animationFps * readFloat(bais))
                            .key(readFloats(bais, 3))
                            .build());
                }
                // translation channel
                for (int j = 0; j < numKeyFramesPos; j++) {
                    positionKeys.add(ms3d_keyframe.builder()
                            .time(model_animationFps * readFloat(bais))
                            .key(readFloats(bais, 3))
                            .build());
                }

                int parentIndex = -1;

                if (i == 0) {
                    parentIndex = -1;
                } else {
                    for (int j = 0; j < numJoints; j++) {
                        if (model_joints.get(j).name.equals(model_joints.get(i).parentName)) {
                            parentIndex = j;
                            break;
                        }
                    }
                }
                model_joints.add(new ms3d_joint(flags, name, parentName, rot, pos,
                        rotationKeys, positionKeys, null,
                        null, null, parentIndex,
                        null, null, null, null));
            }
            System.out.println();
        } catch (Exception e) {
            return false;
        }

/*

        long filePos = ftell(fp);
        if (filePos < fSize) {
            int subVersion = 0;
            fread( & subVersion, sizeof( int),1, fp);
            if (subVersion == 1) {
                int numComments = 0;
                size_t commentSize = 0;

                // group comments
                fread( & numComments, sizeof( int),1, fp);
                for (int i = 0; i < numComments; i++) {
                    int index;
                    fread( & index, sizeof( int),1, fp);
                    List<char> comment;
                    fread( & commentSize, sizeof(size_t), 1, fp);
                    comment.resize(commentSize);
                    if (commentSize > 0)
                        fread( & comment[0], sizeof( char),commentSize, fp);
                    if (index >= 0 && index < (int) model_groups.size())
                        model_groups[index].comment = comment;
                }

                // material comments
                fread( & numComments, sizeof( int),1, fp);
                for (int i = 0; i < numComments; i++) {
                    int index;
                    fread( & index, sizeof( int),1, fp);
                    List<char> comment;
                    fread( & commentSize, sizeof(size_t), 1, fp);
                    comment.resize(commentSize);
                    if (commentSize > 0)
                        fread( & comment[0], sizeof( char),commentSize, fp);
                    if (index >= 0 && index < (int) model_materials.size())
                        model_materials[index].comment = comment;
                }

                // joint comments
                fread( & numComments, sizeof( int),1, fp);
                for (int i = 0; i < numComments; i++) {
                    int index;
                    fread( & index, sizeof( int),1, fp);
                    List<char> comment;
                    fread( & commentSize, sizeof(size_t), 1, fp);
                    comment.resize(commentSize);
                    if (commentSize > 0)
                        fread( & comment[0], sizeof( char),commentSize, fp);
                    if (index >= 0 && index < (int) model_joints.size())
                        model_joints[index].comment = comment;
                }

                // model comments
                fread( & numComments, sizeof( int),1, fp);
                if (numComments == 1) {
                    List<char> comment;
                    fread( & commentSize, sizeof(size_t), 1, fp);
                    comment.resize(commentSize);
                    if (commentSize > 0)
                        fread( & comment[0], sizeof( char),commentSize, fp);
                    model_comment = comment;
                }
            } else {
                // "Unknown subversion for comments %d\n", subVersion);
            }
        }

        filePos = ftell(fp);
        if (filePos < fSize) {
            int subVersion = 0;
            fread( & subVersion, sizeof( int),1, fp);
            if (subVersion == 2) {
                for (int i = 0; i < numVertices; i++) {
                    fread( & model_vertices[i].boneIds[0], sizeof( char),3, fp);
                    fread( & model_vertices[i].weights[0], sizeof(unsigned char),3, fp);
                    fread( & model_vertices[i].extra, sizeof(unsigned int),1, fp);
                }
            } else if (subVersion == 1) {
                for (int i = 0; i < numVertices; i++) {
                    fread( & model_vertices[i].boneIds[0], sizeof( char),3, fp);
                    fread( & model_vertices[i].weights[0], sizeof(unsigned char),3, fp);
                }
            } else {
                // "Unknown subversion for vertex extra %d\n", subVersion);
            }
        }

        // joint extra
        filePos = ftell(fp);
        if (filePos < fSize) {
            int subVersion = 0;
            fread( & subVersion, sizeof( int),1, fp);
            if (subVersion == 1) {
                for (int i = 0; i < numJoints; i++) {
                    fread( & model_joints[i].color, sizeof( float),3, fp);
                }
            } else {
                // "Unknown subversion for joint extra %d\n", subVersion);
            }
        }

        // model extra
        filePos = ftell(fp);
        if (filePos < fSize) {
            int subVersion = 0;
            fread( & subVersion, sizeof( int),1, fp);
            if (subVersion == 1) {
                fread( & model_jointSize, sizeof( float),1, fp);
                fread( & model_transparencyMode, sizeof( int),1, fp);
                fread( & model_alphaRef, sizeof( float),1, fp);
            } else {
                //"Unknown subversion for model extra %d\n", subVersion);
            }
        }

        //------------------------------------------------------
        //---- get memory
        //------------------------------------------------------

        if (isJoint) {
            joints_array = new GLfloat[model_joints.size() * 3];

            group_joints_array = new MSfloat[model_totalFrames];
            for (int i = 0; i < model_totalFrames; i++) {
                group_joints_array[i] = new GLfloat[model_joints.size() * 3];
            }

            color_joints_array = new GLfloat[model_joints.size() * 3];
            p_color_joints_array = new GLfloat[model_joints.size() * 3];

            joints_indices = new GLushort[(model_joints.size() - 1) * 2];
            p_joints_indices = new GLushort[model_joints.size()];
        }

        //--------------------------------------------------------

        if (isVertex) {
            vertices_array = new GLfloat[numVertices * 3];

            group_vertices_array = new MSfloat[model_totalFrames];
            for (int i = 0; i < model_totalFrames; i++) {
                group_vertices_array[i] = new GLfloat[numVertices * 3];
            }

            vertices_indices = new MSushort[numGroups];
            for (int i = 0; i < numGroups; i++) {
                vertices_indices[i] = new GLushort[numGroupTriangles[i] * 3];
            }

            normals_array = new GLfloat[numVertices * 3];
            tex_coord_array = new GLfloat[numVertices * 2];
        }


        //--------------------------------------------------------

        if (isMaterial) {
            materialsIndex = new int[numGroups];
        }

        //--------------------------------------------------------

        if (isJoint) {
            SetupJoints();
            SetFrame(-1);
        }
    */
        return true;
    }

    /*void SetupJoints() {
        for (int i = 0; i < model_joints.size(); i++) {
            ms3d_joint * joint = &model_joints[i];
            AngleMatrix(joint -> rot, joint -> matLocalSkeleton);
            joint -> matLocalSkeleton[0][3] = joint -> pos[0];
            joint -> matLocalSkeleton[1][3] = joint -> pos[1];
            joint -> matLocalSkeleton[2][3] = joint -> pos[2];

            if (joint -> parentIndex == -1 || joint -> parentIndex < 0) {
                memcpy(joint -> matGlobalSkeleton, joint -> matLocalSkeleton, sizeof(joint -> matGlobalSkeleton));
            } else {
                ms3d_joint * parentJoint = &model_joints[joint -> parentIndex];
                R_ConcatTransforms(parentJoint -> matGlobalSkeleton, joint -> matLocalSkeleton, joint -> matGlobalSkeleton);
            }

            SetupTangents();
        }
    }*/

    /*void SetupTangents() {
        for (int j = 0; j < model_joints.size(); j++) {
            ms3d_joint * joint = &model_joints[j];
            int numPositionKeys = (int) joint -> positionKeys.size();
            joint -> tangents.resize(numPositionKeys);

            // clear all tangents (zero derivatives)
            for (int k = 0; k < numPositionKeys; k++) {
                joint -> tangents[k].tangentIn[0] = 0.0f;
                joint -> tangents[k].tangentIn[1] = 0.0f;
                joint -> tangents[k].tangentIn[2] = 0.0f;
                joint -> tangents[k].tangentOut[0] = 0.0f;
                joint -> tangents[k].tangentOut[1] = 0.0f;
                joint -> tangents[k].tangentOut[2] = 0.0f;
            }

            // if there are more than 2 keys, we can calculate tangents, otherwise we use zero derivatives
            if (numPositionKeys > 2) {
                for (int k = 0; k < numPositionKeys; k++) {
                    // make the curve tangents looped
                    int k0 = k - 1;
                    if (k0 < 0)
                        k0 = numPositionKeys - 1;
                    int k1 = k;
                    int k2 = k + 1;
                    if (k2 >= numPositionKeys)

                        k2 = 0;
                    // calculate the tangent, which is the vector from key[k - 1] to key[k + 1]
                    float tangent[ 3];
                    tangent[0] = (joint -> positionKeys[k2].key[0] - joint -> positionKeys[k0].key[0]);
                    tangent[1] = (joint -> positionKeys[k2].key[1] - joint -> positionKeys[k0].key[1]);
                    tangent[2] = (joint -> positionKeys[k2].key[2] - joint -> positionKeys[k0].key[2]);

                    // weight the incoming and outgoing tangent by their time to avoid changes in speed, if the keys are not within the same interval
                    float dt1 = joint -> positionKeys[k1].time - joint -> positionKeys[k0].time;
                    float dt2 = joint -> positionKeys[k2].time - joint -> positionKeys[k1].time;
                    float dt = dt1 + dt2;
                    joint -> tangents[k1].tangentIn[0] = tangent[0] * dt1 / dt;
                    joint -> tangents[k1].tangentIn[1] = tangent[1] * dt1 / dt;
                    joint -> tangents[k1].tangentIn[2] = tangent[2] * dt1 / dt;

                    joint -> tangents[k1].tangentOut[0] = tangent[0] * dt2 / dt;
                    joint -> tangents[k1].tangentOut[1] = tangent[1] * dt2 / dt;
                    joint -> tangents[k1].tangentOut[2] = tangent[2] * dt2 / dt;
                }
            }
        }
    }*/

    /*void SetFrame(float frame) {
        if (frame < 0.0f) {
            for (int i = 0; i < model_joints.size(); i++) {
                ms3d_joint * joint = &model_joints[i];
                memcpy(joint -> matLocal, joint -> matLocalSkeleton, sizeof(joint -> matLocal));
                memcpy(joint -> matGlobal, joint -> matGlobalSkeleton, sizeof(joint -> matGlobal));
            }
        } else {
            for (int i = 0; i < model_joints.size(); i++) {
                EvaluateJoint(i, frame);
            }
        }

        model_currentTime = frame;
    }*/

    /*void SetAnimationFPS(int fps) {
        model_animationFps = fps;
    }*/

    /*void EvaluateJoint(int index, float frame) {
        ms3d_joint * joint = &model_joints[index];

        //
        // calculate joint animation matrix, this matrix will animate matLocalSkeleton
        //
        vec3_t pos = {0.0f, 0.0f, 0.0f};
        int numPositionKeys = (int) joint -> positionKeys.size();
        if (numPositionKeys > 0) {
            int i1 = -1;
            int i2 = -1;

            // find the two keys, where "frame" is in between for the position channel
            for (int i = 0; i < (numPositionKeys - 1); i++) {
                if (frame >= joint -> positionKeys[i].time && frame < joint -> positionKeys[i + 1].time) {
                    i1 = i;
                    i2 = i + 1;
                    break;
                }
            }

            // if there are no such keys
            if (i1 == -1 || i2 == -1) {
                // either take the first
                if (frame < joint -> positionKeys[0].time) {
                    pos[0] = joint -> positionKeys[0].key[0];
                    pos[1] = joint -> positionKeys[0].key[1];
                    pos[2] = joint -> positionKeys[0].key[2];
                }

                // or the last key
                else if (frame >= joint -> positionKeys[numPositionKeys - 1].time) {
                    pos[0] = joint -> positionKeys[numPositionKeys - 1].key[0];
                    pos[1] = joint -> positionKeys[numPositionKeys - 1].key[1];
                    pos[2] = joint -> positionKeys[numPositionKeys - 1].key[2];
                }
            }

            // there are such keys, so interpolate using hermite interpolation
            else {
                ms3d_keyframe * p0 = &joint -> positionKeys[i1];
                ms3d_keyframe * p1 = &joint -> positionKeys[i2];
                ms3d_tangent * m0 = &joint -> tangents[i1];
                ms3d_tangent * m1 = &joint -> tangents[i2];

                // normalize the time between the keys into [0..1]
                float t = (frame - joint -> positionKeys[i1].time) / (joint -> positionKeys[i2].time - joint -> positionKeys[i1].time);
                float t2 = t * t;
                float t3 = t2 * t;

                // calculate hermite basis
                float h1 = 2.0f * t3 - 3.0f * t2 + 1.0f;
                float h2 = -2.0f * t3 + 3.0f * t2;
                float h3 = t3 - 2.0f * t2 + t;
                float h4 = t3 - t2;

                // do hermite interpolation
                pos[0] = h1 * p0 -> key[0] + h3 * m0 -> tangentOut[0] + h2 * p1 -> key[0] + h4 * m1 -> tangentIn[0];
                pos[1] = h1 * p0 -> key[1] + h3 * m0 -> tangentOut[1] + h2 * p1 -> key[1] + h4 * m1 -> tangentIn[1];
                pos[2] = h1 * p0 -> key[2] + h3 * m0 -> tangentOut[2] + h2 * p1 -> key[2] + h4 * m1 -> tangentIn[2];
            }
        }

        vec4_t quat = {0.0f, 0.0f, 0.0f, 1.0f};
        int numRotationKeys = (int) joint -> rotationKeys.size();
        if (numRotationKeys > 0) {
            int i1 = -1;
            int i2 = -1;

            // find the two keys, where "frame" is in between for the rotation channel
            for (int i = 0; i < (numRotationKeys - 1); i++) {
                if (frame >= joint -> rotationKeys[i].time && frame < joint -> rotationKeys[i + 1].time) {
                    i1 = i;
                    i2 = i + 1;
                    break;
                }
            }

            // if there are no such keys
            if (i1 == -1 || i2 == -1) {
                // either take the first key
                if (frame < joint -> rotationKeys[0].time) {
                    AngleQuaternion(joint -> rotationKeys[0].key, quat);
                }

                // or the last key
                else if (frame >= joint -> rotationKeys[numRotationKeys - 1].time) {
                    AngleQuaternion(joint -> rotationKeys[numRotationKeys - 1].key, quat);
                }
            }

            // there are such keys, so do the quaternion slerp interpolation
            else {
                float t = (frame - joint -> rotationKeys[i1].time) / (joint -> rotationKeys[i2].time - joint -> rotationKeys[i1].time);
                vec4_t q1;
                AngleQuaternion(joint -> rotationKeys[i1].key, q1);
                vec4_t q2;
                AngleQuaternion(joint -> rotationKeys[i2].key, q2);
                QuaternionSlerp(q1, q2, t, quat);
            }
        }

        // make a matrix from pos/quat
        float matAnimate[ 3][4];
        QuaternionMatrix(quat, matAnimate);
        matAnimate[0][3] = pos[0];
        matAnimate[1][3] = pos[1];
        matAnimate[2][3] = pos[2];

        // animate the local joint matrix using: matLocal = matLocalSkeleton * matAnimate
        R_ConcatTransforms(joint -> matLocalSkeleton, matAnimate, joint -> matLocal);

        // build up the hierarchy if joints
        // matGlobal = matGlobal(parent) * matLocal
        if (joint -> parentIndex == -1 || joint -> parentIndex < 0) {
            memcpy(joint -> matGlobal, joint -> matLocal, sizeof(joint -> matGlobal));
        } else {
            ms3d_joint * parentJoint = &model_joints[joint -> parentIndex];
            R_ConcatTransforms(parentJoint -> matGlobal, joint -> matLocal, joint -> matGlobal);
        }
    }*/

    /*vec3 TransformJoint( const GLfloat v[3], const GLfloat m[3][4]) {
        vec3 out;

        // M00 M01 M02 M03				V0
        //
        // M10 M11 M12 M13				V1
        //						*
        // M20 M21 M22 M23				V2

        out.x = m[0][0] * v[0] + m[0][1] * v[1] + m[0][2] * v[2] + m[0][3];
        out.y = m[1][0] * v[0] + m[1][1] * v[1] + m[1][2] * v[2] + m[1][3];
        out.z = m[2][0] * v[0] + m[2][1] * v[1] + m[2][2] * v[2] + m[2][3];

        return out;
    }*/

    /*vec3 TransformVertex(const ms3d_vertex *vertex) const

    {
        vec3_t out;
        int jointIndices[ 4],jointWeights[4];
        FillJointIndicesAndWeights(vertex, jointIndices, jointWeights);

        if (jointIndices[0] < 0 || jointIndices[0] >= (int) model_joints.size() || model_currentTime < 0.0f) {
            out[0] = vertex -> vertex[0];
            out[1] = vertex -> vertex[1];
            out[2] = vertex -> vertex[2];
        } else {
            // count valid weights
            int numWeights = 0;
            for (int i = 0; i < 4; i++) {
                if (jointWeights[i] > 0 && jointIndices[i] >= 0 && jointIndices[i] < (int) model_joints.size())
                    ++numWeights;
                else
                    break;
            }

            // init
            out[0] = 0.0f;
            out[1] = 0.0f;
            out[2] = 0.0f;

            float weights[ 4] ={
                (float) jointWeights[0] / 100.0f, (float) jointWeights[1] / 100.0f, (float) jointWeights[2] / 100.0f, (float) jointWeights[3] / 100.0f
            } ;
            if (numWeights == 0) {
                numWeights = 1;
                weights[0] = 1.0f;
            }
            // add weighted vertices
            for (int i = 0; i < numWeights; i++) {
			const ms3d_joint * joint = &model_joints[jointIndices[i]];
                vec3_t tmp, vert;
                VectorITransform(vertex -> vertex, joint -> matGlobalSkeleton, tmp);
                VectorTransform(tmp, joint -> matGlobal, vert);

                out[0] += vert[0] * weights[i];
                out[1] += vert[1] * weights[i];
                out[2] += vert[2] * weights[i];
            }
        }
        vec3 v = {out[0], out[1], out[2]};
        return v;
    }*/

    /*void FillJointIndicesAndWeights(const ms3d_vertex *vertex, int jointIndices[4], int jointWeights[4]) const

    {
        jointIndices[0] = vertex -> boneId;
        jointIndices[1] = vertex -> boneIds[0];
        jointIndices[2] = vertex -> boneIds[1];
        jointIndices[3] = vertex -> boneIds[2];
        jointWeights[0] = 100;
        jointWeights[1] = 0;
        jointWeights[2] = 0;
        jointWeights[3] = 0;
        if (vertex -> weights[0] != 0 || vertex -> weights[1] != 0 || vertex -> weights[2] != 0) {
            jointWeights[0] = vertex -> weights[0];
            jointWeights[1] = vertex -> weights[1];
            jointWeights[2] = vertex -> weights[2];
            jointWeights[3] = 100 - (vertex -> weights[0] + vertex -> weights[1] + vertex -> weights[2]);
        }
    }*/

    /*void LoadModel() {
        //--------------------------------------------------------------
        //----------------- Load Vertices
        //--------------------------------------------------------------

        SetupVertexArray();

        //--------------------------------------------------------------
        //----------------- Load Index Groups
        //--------------------------------------------------------------

        int *gIdx = new int[numGroups];
        for (int j = 0; j < numGroups; j++) {
            gIdx[j] = 0;
        }
        int gIndex = 0;

        for (int i = 0; i < numTriangles; i++) {
            gIndex = model_triangles[i].groupIndex;
            vertices_indices[gIndex][gIdx[gIndex]] = model_triangles[i].vertexIndices[0];
            gIdx[gIndex] = gIdx[gIndex] + 1;
            vertices_indices[gIndex][gIdx[gIndex]] = model_triangles[i].vertexIndices[1];
            gIdx[gIndex] = gIdx[gIndex] + 1;
            vertices_indices[gIndex][gIdx[gIndex]] = model_triangles[i].vertexIndices[2];
            gIdx[gIndex] = gIdx[gIndex] + 1;

            //--------------------------------------------------------------
            //----------------- Load Vertices Normals
            //--------------------------------------------------------------

            normals_array[model_triangles[i].vertexIndices[0]] = model_triangles[i].vertexNormals[0][0];
            normals_array[model_triangles[i].vertexIndices[0]] = model_triangles[i].vertexNormals[0][1];
            normals_array[model_triangles[i].vertexIndices[0]] = model_triangles[i].vertexNormals[0][2];

            normals_array[model_triangles[i].vertexIndices[1]] = model_triangles[i].vertexNormals[1][0];
            normals_array[model_triangles[i].vertexIndices[1]] = model_triangles[i].vertexNormals[1][1];
            normals_array[model_triangles[i].vertexIndices[1]] = model_triangles[i].vertexNormals[1][2];

            normals_array[model_triangles[i].vertexIndices[2]] = model_triangles[i].vertexNormals[2][0];
            normals_array[model_triangles[i].vertexIndices[2]] = model_triangles[i].vertexNormals[2][1];
            normals_array[model_triangles[i].vertexIndices[2]] = model_triangles[i].vertexNormals[2][2];

            //--------------------------------------------------------------
            //----------------- Load Vertices Coordinates ( s for first_axis)
            //--------------------------------------------------------------

            tex_coord_array[model_triangles[i].vertexIndices[0] * 2] = model_triangles[i].s[0];
            tex_coord_array[model_triangles[i].vertexIndices[1] * 2] = model_triangles[i].s[1];
            tex_coord_array[model_triangles[i].vertexIndices[2] * 2] = model_triangles[i].s[2];

            //--------------------------------------------------------------
            //----------------- Load Vertices Coordinates ( t for second_axis)
            //--------------------------------------------------------------

            tex_coord_array[model_triangles[i].vertexIndices[0] * 2 + 1] = model_triangles[i].t[0];
            tex_coord_array[model_triangles[i].vertexIndices[1] * 2 + 1] = model_triangles[i].t[1];
            tex_coord_array[model_triangles[i].vertexIndices[2] * 2 + 1] = model_triangles[i].t[2];
        }

        //--------------------------------------------------------------
        //----------------- Load Textures For Materials And Groups
        //--------------------------------------------------------------

        SetupTextureArray();

        if (isJoint) {
            SetupJointsArray();
        }

        model_vertices.clear();
        model_triangles.clear();
        model_groups.clear();
        model_joints.clear();

    }*/

    /*void SetupTextureArray() {
        for (int i = 0; i < numMaterials; i++) {
            LoadTextureFromBitmapFileForMS3D(model_materials[i].texture, model_materials[i].id);//materialsArray[i]);
            for (int j = 0; j < numGroups; j++) {
                if (model_groups[j].materialIndex == i) {
                    materialsIndex[j] = i;
                }

            }
        }
    }*/

    /*void SetupVertexArray() {
        for (int i = 0; i < numVertices; i++) {
            //3 element of vertex for(x,y,z)
            vertices_array[i * 3] = model_vertices[i].vertex[0];//x
            vertices_array[i * 3 + 1] = model_vertices[i].vertex[1];//y
            vertices_array[i * 3 + 2] = model_vertices[i].vertex[2];//z
        }

        for (int i = 0; i < model_totalFrames; i++) {
            SetFrame(i);

            for (int j = 0; j < numVertices; j++) {
                ms3d_vertex * vertex =	&model_vertices[j];
                vec3 v = TransformVertex(vertex);
                //3 element of vertex for(x,y,z)
                group_vertices_array[i][j * 3] = v.x;//model_vertices[i].vertex[0];//x
                group_vertices_array[i][j * 3 + 1] = v.y;//model_vertices[i].vertex[1];//y
                group_vertices_array[i][j * 3 + 2] = v.z;//model_vertices[i].vertex[2];//z
            }
        }
    }*/

    /*void SetupJointsArray() {
        SetFrame(-1);
        for (int i = 0; i < model_joints.size(); i++) {
            vec3 p;

            int parentIdx = model_joints[i].parentIndex;
            if (parentIdx > -1) {
                p = TransformJoint(model_joints[i].pos, model_joints[parentIdx].matGlobal);
            } else {
                p.x = model_joints[i].pos[0];
                p.y = model_joints[i].pos[1];
                p.z = model_joints[i].pos[2];
            }

            joints_array[i * 3] = p.x;
            joints_array[i * 3 + 1] = p.y;
            joints_array[i * 3 + 2] = p.z;

            color_joints_array[i * 3] = 0.9;
            color_joints_array[i * 3 + 1] = 0.8;
            color_joints_array[i * 3 + 2] = 0;

            p_color_joints_array[i * 3] = 1;
            p_color_joints_array[i * 3 + 1] = 0;
            p_color_joints_array[i * 3 + 2] = 0;

        }

        for (int i = 0; i < model_totalFrames; i++) {
            SetFrame(i);

            for (int j = 0; j < numJoints; j++) {
                vec3 p;

                int parentIdx = model_joints[j].parentIndex;
                if (parentIdx > -1) {
                    p = TransformJoint(model_joints[j].pos, model_joints[parentIdx].matGlobal);
                } else {
                    p.x = model_joints[j].pos[0];
                    p.y = model_joints[j].pos[1];
                    p.z = model_joints[j].pos[2];
                }

                group_joints_array[i][j * 3] = p.x;
                group_joints_array[i][j * 3 + 1] = p.y;
                group_joints_array[i][j * 3 + 2] = p.z;
            }
        }

        for (int i = 0; i < (model_joints.size() - 1); i++) {
            joints_indices[i * 2] = i + 1;
            joints_indices[i * 2 + 1] = model_joints[i + 1].parentIndex;

            p_joints_indices[i] = i;
        }
        p_joints_indices[model_joints.size() - 1] = model_joints.size() - 1;
    }*/

    int GetNumberOfVertex() {
        return numVertices;
    }

    int GetNumberOFGroups() {
        return numGroups;
    }

    int GetNumberOfMaterials() {
        return numMaterials;
    }

    int GetNumberOfIndexInGroup(int index) {
        return numGroupIndices[index];
    }

    int GetNumberOfJoints() {
        return numJoints;
    }

    int GetNumberOfTotalFrame() {
        return model_totalFrames;
    }

    float GetAnimationFPS() {
        return model_animationFps;
    }

    int GetTexturesArray(int index) {
        return 0;
    }

    float[] GetVerticesArray() {
        return vertices_array;
    }

    float GetGroupVerticesArray(int index) {
        return group_vertices_array[index];
    }

    float[] GetTexCoordArray() {
        return tex_coord_array;
    }

    short GetVerticesIndices(int index) {
        return vertices_indices[index];
    }

    ms3d_material GetMaterial(int index) {
        return model_materials.get(index);
    }

    int GetTransparencyMode() {
        return model_transparencyMode;
    }

    float GetAlphaRef() {
        return model_alphaRef;
    }

    /*char GetMaterialForGroup(int index) {
        return materialsIndex[index];
    }*/

    float[] GetJointsArray() {
        return joints_array;
    }

    float GetGroupJointsArray(int index) {
        return group_joints_array[index];
    }

    float[] GetColorJointsArray() {
        return color_joints_array;
    }

    float[] GetPColorJointsArray() {
        return p_color_joints_array;
    }

    short[] GetJointsIndices() {
        return joints_indices;
    }

    short[] GetPJointsIndices() {
        return p_joints_indices;
    }

    boolean GetIsVertex() {
        return isVertex;
    }

    boolean GetIsTriangle() {
        return isTriangle;
    }

    boolean GetIsGroup() {
        return isGroup;
    }

    boolean GetIsMaterial() {
        return isMaterial;
    }

    boolean GetIsJoint() {
        return isJoint;
    }
}
