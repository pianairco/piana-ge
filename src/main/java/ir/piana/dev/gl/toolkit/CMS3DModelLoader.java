package ir.piana.dev.gl.toolkit;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

    short numVertices;
    short numTriangles;
    short numIndices;
    short numGroups;

    short[] numGroupTriangles;
    int[] numGroupIndices;

    short numMaterials;
    short numJoints;

    short numKeyFramesRot;
    short numKeyFramesPos;

    //------------------------------------------------------
    //---- textures
    //------------------------------------------------------

    int[] materialsIndex;

    //------------------------------------------------------
    //---- variable bones
    //------------------------------------------------------

    char[] parent;

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

    public boolean initModel(String filename) throws IOException {
        int startOfVertices = 0;
        int startOfTriangles = 0;
        int startOfGroups = 0;
        int startOfMaterials = 0;
        int startOfAnimation = 0;
        int startOfJoints = 0;

        InputStream resourceAsStream = CMS3DModelLoader.class.getResourceAsStream(
                filename.substring(10).trim());
        int available = resourceAsStream.available();
        byte[] bytes =  resourceAsStream.readNBytes(10);
        if(!IsCorrectID(new String(bytes))) {
            System.out.println("file is incorrect!");
        }

        try (ByteArrayInputStream bais = (filename.startsWith("classpath-") ?
                new ByteArrayInputStream(CMS3DModelLoader.class.getResourceAsStream(
                        filename.substring(10).trim()).toString().getBytes()) :
                new ByteArrayInputStream(FileUtils.readFileToByteArray(new File(filename))))) {
            bytes = bais.readNBytes(10);
            if(!IsCorrectID(new String(bytes))) {
                System.out.println("file is incorrect!");
                throw new Exception("error occurred!");
            }
        } catch (Exception e) {
            return false;
        }

        //----------------------------------
        //--------------test Id
        //----------------------------------
/*
        //----------------------------------
        //--------------test version
        //----------------------------------

        int version;
        fread( & version, sizeof( int),1, fp);
        if (!IsCorrectVersion(version)) {
            fclose(fp);
            return false;
        }

        ModelClear();

        int sizeOfHeader = (10 * sizeof( char))+sizeof( int);
        startOfVertecis = sizeOfHeader;

        //----------------------------------
        //--------------number of vertices
        //----------------------------------

        pos = startOfVertecis;
        fsetpos(fp, & pos);

        fread( & numVertices, sizeof(unsigned short),1, fp);
        if (numVertices > 0) {
            isVertex = true;
        }
        model_vertices.resize(numVertices);

        for (int i = 0; i < numVertices; i++) {
            fread( & model_vertices[i].flags, sizeof(unsigned char),1, fp);

            float vertex[ 3];
            fread(vertex, sizeof( float),3, fp);
            fread( & model_vertices[i].boneId, sizeof( char),1, fp);
            fread( & model_vertices[i].referenceCount, sizeof(unsigned char),1, fp);
        }

        int sizeOfVertices = sizeof(unsigned short)+(numVertices * ((sizeof( char) *3)+(sizeof( float) *3)));
        startOfTriangles = startOfVertecis + sizeOfVertices;
        //------------------------------------------
        //--------------num of triangle and indices
        //------------------------------------------

        pos = startOfTriangles;
        fsetpos(fp, & pos);

        fread( & numTriangles, sizeof(unsigned short),1, fp);
        if (numTriangles > 0) {
            isTriangle = true;
        }
        model_triangles.resize(numTriangles);

        for (int i = 0; i < numTriangles; i++) {
            fread( & model_triangles[i].flags, sizeof(unsigned short),1, fp);
            fread(model_triangles[i].vertexIndices, sizeof(unsigned short),3, fp);
            fread(model_triangles[i].vertexNormals, sizeof( float),3 * 3, fp);
            fread(model_triangles[i].s, sizeof( float),3, fp);
            fread(model_triangles[i].t, sizeof( float),3, fp);
            fread( & model_triangles[i].smoothingGroup, sizeof(unsigned char),1, fp);
            fread( & model_triangles[i].groupIndex, sizeof(unsigned char),1, fp);

            // TODO: calculate triangle normal
        }

        int sizeOfTriangles = sizeof(unsigned short)+(numTriangles * ((sizeof(unsigned short) *4)+(sizeof( float) *15)
        +(sizeof(unsigned char) *2)));
        startOfGroups = startOfTriangles + sizeOfTriangles;

        //----------------------------------
        //--------------number of groups
        //----------------------------------

        pos = startOfGroups;
        fsetpos(fp, & pos);

        fread( & numGroups, sizeof(unsigned short),1, fp);
        if (numGroups > 0) {
            isGroup = true;
        }
        model_groups.resize(numGroups);

        numGroupTriangles = new GLushort[numGroups];
        numGroupIndices = new GLuint[numGroups];

        int numGT = 0;
        for (int i = 0; i < numGroups; i++) {
            fread( & model_groups[i].flags, sizeof(unsigned char),1, fp);
            fread(model_groups[i].name, sizeof( char),32, fp);

            fread( & numGroupTriangles[i], sizeof(unsigned short),1, fp);
            numGroupIndices[i] = 3 * numGroupTriangles[i];

            model_groups[i].triangleIndices.resize(numGroupTriangles[i]);
            if (numGroupTriangles[i] > 0)
                fread( & model_groups[i].triangleIndices[0], sizeof(unsigned short),numGroupTriangles[i], fp);
            numGT += numGroupTriangles[i];
            fread( & model_groups[i].materialIndex, sizeof( char),1, fp);
        }

        int sizeOfGroups = sizeof(unsigned short)+(numGroups * ((sizeof( char) *34)+(sizeof(unsigned short) *1)))
        +(sizeof(unsigned short) *numGT);
        startOfMaterials = startOfGroups + sizeOfGroups;

        //----------------------------------
        //--------------number of materials
        //----------------------------------

        pos = startOfMaterials;
        fsetpos(fp, & pos);

        fread( & numMaterials, sizeof(unsigned short),1, fp);
        if (numMaterials > 0) {
            isMaterial = true;
        }
        model_materials.resize(numMaterials);

        for (int i = 0; i < numMaterials; i++) {
            fread(model_materials[i].name, sizeof( char),32, fp);
            fread( & model_materials[i].ambient, sizeof( float),4, fp);
            fread( & model_materials[i].diffuse, sizeof( float),4, fp);
            fread( & model_materials[i].specular, sizeof( float),4, fp);
            fread( & model_materials[i].emissive, sizeof( float),4, fp);
            fread( & model_materials[i].shininess, sizeof( float),1, fp);
            fread( & model_materials[i].transparency, sizeof( float),1, fp);
            fread( & model_materials[i].mode, sizeof(unsigned char),1, fp);
            fread(model_materials[i].texture, sizeof( char),MAX_TEXTURE_FILENAME_SIZE, fp);
            fread(model_materials[i].alphamap, sizeof( char),MAX_TEXTURE_FILENAME_SIZE, fp);

            // set alpha
            model_materials[i].ambient[3] = model_materials[i].transparency;
            model_materials[i].diffuse[3] = model_materials[i].transparency;
            model_materials[i].specular[3] = model_materials[i].transparency;
            model_materials[i].emissive[3] = model_materials[i].transparency;
        }

        int sizeOfMaterial = sizeof(unsigned short)+(numMaterials * ((sizeof( char) *
        (33 + (2 * MAX_TEXTURE_FILENAME_SIZE)) )+(sizeof( float) *18)	)	);
        startOfAnimation = startOfMaterials + sizeOfMaterial;

        //-----------------------------------------
        //-------------animation
        //-----------------------------------------

        pos = startOfAnimation;
        fsetpos(fp, & pos);

        fread( & model_animationFps, sizeof( float),1, fp);

        if (model_animationFps < 1.0f)
            model_animationFps = 1.0f;
        fread( & model_currentTime, sizeof( float),1, fp);
        fread( & model_totalFrames, sizeof( int),1, fp);

        //-------------------------
        //------------ joints
        //-------------------------

        fread( & numJoints, sizeof(unsigned short),1, fp);
        if (numJoints > 0) {
            isJoint = true;
        }
        model_joints.resize(numJoints);

        parent = new MSchar[numJoints];

        for (int i = 0; i < numJoints; i++) {
            parent[i] = new char[32];
        }

        for (int i = 0; i < numJoints; i++) {
            fread( & model_joints[i].flags, sizeof(unsigned char),1, fp);
            fread(model_joints[i].name, sizeof( char),32, fp);
            strcpy(parent[i], model_joints[i].name);
            fread(model_joints[i].parentName, sizeof( char),32, fp);
            fread(model_joints[i].rot, sizeof( float),3, fp);
            fread(model_joints[i].pos, sizeof( float),3, fp);

            fread( & numKeyFramesRot, sizeof(unsigned short),1, fp);
            model_joints[i].rotationKeys.resize(numKeyFramesRot);

            fread( & numKeyFramesPos, sizeof(unsigned short),1, fp);
            model_joints[i].positionKeys.resize(numKeyFramesPos);

            // the frame time is in seconds, so multiply it by the animation fps, to get the frames
            // rotation channel
            for (int j = 0; j < numKeyFramesRot; j++) {
                fread( & model_joints[i].rotationKeys[j].time, sizeof( float),1, fp);
                fread( & model_joints[i].rotationKeys[j].key, sizeof( float),3, fp);
                model_joints[i].rotationKeys[j].time *= model_animationFps;
            }
            // translation channel
            for (int j = 0; j < numKeyFramesPos; j++) {
                fread( & model_joints[i].positionKeys[j].time, sizeof( float),1, fp);
                fread( & model_joints[i].positionKeys[j].key, sizeof( float),3, fp);
                model_joints[i].positionKeys[j].time *= model_animationFps;
            }
            if (i == 0) {
                model_joints[i].parentIndex = -1;
            } else {
                for (int j = 0; j < numJoints; j++) {
                    if (strcmp(model_joints[j].name, model_joints[i].parentName) == 0) {
                        model_joints[i].parentIndex = j;
                        break;
                    }
                }
            }

        }

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

class ms3d_vertex {
    char flags;
    float vertex[] = new float[3];
    //-----------------------------------
    //-- index of joints in milkshape
    //-----------------------------------
    char boneId;
    char referenceCount;

    char boneIds[] = new char[3];
    char weights[] = new char[3];
    int extra;
    float renderColor[] = new float[3];
};

class ms3d_triangle {
    short flags;
    short vertexIndices[] = new short[3];
    float vertexNormals[][] = new float[3][3];
    float s[] = new float[3];
    float t[] = new float[3];
    float normal[] = new float[3];
    char smoothingGroup;
    char groupIndex;
};

class ms3d_group {
    char flags;
    char name[] = new char[32];
    List<Short> triangleIndices;
    char materialIndex;
    List<Character> comment;
};

class ms3d_material {
    char name[] = new char[32];
    float ambient[] = new float[4];
    float diffuse[] = new float[4];
    float specular[] = new float[4];
    float emissive[] = new float[4];
    float shininess;
    float transparency;
    char mode;
    char texture[] = new char[CMS3DModelLoader.MAX_TEXTURE_FILENAME_SIZE];
    char alphamap[] = new char[CMS3DModelLoader.MAX_TEXTURE_FILENAME_SIZE];
    int id;
    List<Character> comment;
};

class ms3d_keyframe {
    float time;
    float key[] = new float[3];
};

class ms3d_tangent {
    float tangentIn[] = new float[3];
    float tangentOut[] = new float[3];
};

class ms3d_joint {
    char flags;
    char name[] = new char[32];
    char parentName[] = new char[32];

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
};