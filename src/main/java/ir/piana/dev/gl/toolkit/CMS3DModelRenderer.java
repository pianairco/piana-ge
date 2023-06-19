package ir.piana.dev.gl.toolkit;

import ir.piana.dev.gl.util.Texture;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class CMS3DModelRenderer {
    CMS3DModelLoader model_loader;
    boolean render_joint;

    CMS3DModelRenderer(CMS3DModelLoader model_loader) {
        this.model_loader = model_loader;
    }

    void ToggleRenderJoint()
    {
        if(render_joint		==	true)
        {
            render_joint	=	false;
        }
        else
        {
            render_joint	=	true;
        }
    }

    void BindMaterial(int materialIndex)
    {
        if (materialIndex < 0 || materialIndex >= model_loader.GetNumberOfMaterials())
        {
            GL11.glDepthMask(true);
            glDisable(GL11.GL_ALPHA_TEST);
            glDisable(GL11.GL_TEXTURE_GEN_S);
            glDisable(GL11.GL_TEXTURE_GEN_T);
            GL11.glColor4f(1, 1, 1, 1);
            glDisable(GL_TEXTURE_2D);
            glDisable(GL_BLEND);
            glBindTexture(GL_TEXTURE_2D, 0);
            float ma[]/*[4]*/ = { 0.2f, 0.2f, 0.2f, 1.0f };
            float md[]/*[4]*/ = { 0.8f, 0.8f, 0.8f, 1.0f };
            float ms[]/*[4]*/ = { 0.0f, 0.0f, 0.0f, 1.0f };
            float me[]/*[4]*/ = { 0.0f, 0.0f, 0.0f, 1.0f };
            float mss = 0.0f;
            glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, ma);
            glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, md);
            glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, ms);
            glMaterialfv(GL_FRONT_AND_BACK, GL_EMISSION, me);
            glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, mss);
        }
        else
        {
            ms3d_material material = model_loader.GetMaterial(materialIndex);
            glEnable(GL_TEXTURE_2D);

            if (material.transparency < 1.0f || (material.mode & CMS3DModelLoader.HASALPHA) > 0)
            {
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                glColor4f(1.0f, 1.0f, 1.0f, material.transparency);
                glLightModeli(GL_LIGHT_MODEL_TWO_SIDE, 1);

                if (model_loader.GetTransparencyMode() == CMS3DModelLoader.TRANSPARENCY_MODE_SIMPLE)
                {
                    glDepthMask(false);
                    glEnable(GL_ALPHA_TEST);
                    glAlphaFunc(GL_GREATER, 0.0f);
                }
                else if (model_loader.GetTransparencyMode() == CMS3DModelLoader.TRANSPARENCY_MODE_ALPHAREF)
                {
                    glDepthMask(true);
                    glEnable(GL_ALPHA_TEST);
                    glAlphaFunc(GL_GREATER, model_loader.GetAlphaRef());
                }
            }
            else
            {
                glDisable(GL_BLEND);
                glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                glLightModeli(GL_LIGHT_MODEL_TWO_SIDE, 0);
            }

            if ((material.mode & GL_SPHERE_MAP) > 0)
            {
                glEnable(GL_TEXTURE_GEN_S);
                glEnable(GL_TEXTURE_GEN_T);
                glTexGeni(GL_S, GL_TEXTURE_GEN_MODE, GL_SPHERE_MAP);
                glTexGeni(GL_T, GL_TEXTURE_GEN_MODE, GL_SPHERE_MAP);
            }
            else
            {
                glDisable(GL_TEXTURE_GEN_S);
                glDisable(GL_TEXTURE_GEN_T);
            }
            glBindTexture(GL_TEXTURE_2D, material.id);

            glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, material.ambient);
            glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, material.diffuse);
            glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, material.specular);
            glMaterialfv(GL_FRONT_AND_BACK, GL_EMISSION, material.emissive);
            glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, material.shininess);
        }
    }

    void TearMaterial()
    {
        glDisable(GL_BLEND);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        glLightModeli(GL_LIGHT_MODEL_TWO_SIDE, 0);

        glDepthMask(true);
        glDisable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, model_loader.GetAlphaRef());

        glDisable(GL_TEXTURE_GEN_S);
        glDisable(GL_TEXTURE_GEN_T);
    }

    public List<Mesh> DrawTexturedModel(float x, float y, float z, int shaderProgramId)
    {
        List<Mesh> meshes = new ArrayList();
        for(int i = 0; i < model_loader.GetNumberOFGroups(); i++)
        {
            model_loader.GetMaterialForGroup(i);
            Mesh mesh = Mesh.builder()
                    .addVertices(model_loader.GetGroupVerticesArray(i))
                    .addShaderProgram(shaderProgramId)
                    .addTexture(model_loader.model_materials.get(model_loader.GetMaterialForGroup(i)).id)
                    .addIndices(model_loader.GetVerticesIndices(i))
                    .addTextureCoordinates(model_loader.tex_coord_array);

            meshes.add(mesh);
            /*glBindTexture( GL_TEXTURE_2D, model_loader.GetTexturesArray(i));
            glVertexPointer( 3, GL_FLOAT, 0, model_loader.GetVerticesArray());
            glTexCoordPointer( 2, GL_FLOAT, 0, model_loader.GetTexCoordArray());
            glDrawElements( GL_TRIANGLES, model_loader.GetNumberOfIndexInGroup(i), GL_UNSIGNED_SHORT, model_loader.GetVerticesIndices(i));*/
        }

        return meshes;

        /*if(render_joint)
        {
            DrawJoints(x,y,z);
        }*/
    }

    /*void DrawTexturedModel(const GLfloat &x,const GLfloat &y,const GLfloat &z, const int &frm) const
    {
        glEnable(GL_TEXTURE_2D);
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_COLOR_ARRAY);


        glPushMatrix();

        glTranslated( x, y, z);

        for(int i=0;i<model_loader.GetNumberOFGroups();i++)
        {
            glBindTexture( GL_TEXTURE_2D, model_loader.GetTexturesArray(i));
            glVertexPointer( 3, GL_FLOAT, 0, model_loader.GetGroupVerticesArray(frm));
            glTexCoordPointer( 2, GL_FLOAT, 0, model_loader.GetTexCoordArray());
            glDrawElements( GL_TRIANGLES, model_loader.GetNumberOfIndexInGroup(i), GL_UNSIGNED_SHORT, model_loader.GetVerticesIndices(i));
        }

        glPopMatrix();

        glDisable(GL_TEXTURE_2D);

        if(render_joint)
        {
            DrawJoints(x,y,z,frm);
        }
    }

    void DrawModel()
    {
        glEnable(GL_TEXTURE_2D);
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_COLOR_ARRAY);


        glPushMatrix();

        for(int i=0;i<model_loader.GetNumberOFGroups();i++)
        {
            //glBindTexture( GL_TEXTURE_2D, model_loader.GetTexturesArray(i));
            BindMaterial(model_loader.GetMaterialForGroup(i));
            glVertexPointer( 3, GL_FLOAT, 0, model_loader.GetVerticesArray());
            glTexCoordPointer( 2, GL_FLOAT, 0, model_loader.GetTexCoordArray());
            glDrawElements( GL_TRIANGLES, model_loader.GetNumberOfIndexInGroup(i), GL_UNSIGNED_SHORT, model_loader.GetVerticesIndices(i));
            TearMaterial();
        }

        glPopMatrix();

        glDisable(GL_TEXTURE_2D);
    }

    void DrawModel(const vec3 &pos, const vec3 &rot)
    {
        glEnable(GL_TEXTURE_2D);
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_COLOR_ARRAY);


        glPushMatrix();

        glTranslated( pos.x, pos.y, pos.z);

        glRotatef(RadianToDegree(rot.x),1.0f,0.0f,0.0f);
        glRotatef(RadianToDegree(rot.y),0.0f,1.0f,0.0f);
        glRotatef(RadianToDegree(rot.z),0.0f,0.0f,1.0f);

        for(int i=0;i<model_loader.GetNumberOFGroups();i++)
        {
            //glBindTexture( GL_TEXTURE_2D, model_loader.GetTexturesArray(i));
            BindMaterial(model_loader.GetMaterialForGroup(i));
            glVertexPointer( 3, GL_FLOAT, 0, model_loader.GetVerticesArray());
            glTexCoordPointer( 2, GL_FLOAT, 0, model_loader.GetTexCoordArray());
            glDrawElements( GL_TRIANGLES, model_loader.GetNumberOfIndexInGroup(i), GL_UNSIGNED_SHORT, model_loader.GetVerticesIndices(i));
            TearMaterial();
        }

        glPopMatrix();

        glDisable(GL_TEXTURE_2D);
    }

    void  DrawAnimateModel(const vec3 &pos, const vec3 &rot, const int &f)
    {
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_COLOR_ARRAY);


        glPushMatrix();

        glTranslated( pos.x, pos.y, pos.z);

        glRotatef(RadianToDegree(rot.x),1.0f,0.0f,0.0f);
        glRotatef(RadianToDegree(rot.y),0.0f,1.0f,0.0f);
        glRotatef(RadianToDegree(rot.z),0.0f,0.0f,1.0f);

        for(int i=0;i<model_loader.GetNumberOFGroups();i++)
        {
            BindMaterial(model_loader.GetMaterialForGroup(i));
            glVertexPointer( 3, GL_FLOAT, 0, model_loader.GetGroupVerticesArray(f));
            glTexCoordPointer( 2, GL_FLOAT, 0, model_loader.GetTexCoordArray());
            glDrawElements( GL_TRIANGLES, model_loader.GetNumberOfIndexInGroup(i), GL_UNSIGNED_SHORT, model_loader.GetVerticesIndices(i));
            TearMaterial();
        }

        glPopMatrix();

        glDisable(GL_TEXTURE_2D);

        if(render_joint && model_loader.GetIsJoint())
        {
            DrawJoints(pos, rot, f);
        }
    }

    void DrawJoints(const vec3 &pos, const vec3 &rot, const int f) const
    {
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);

        glDisableClientState(GL_TEXTURE_COORD_ARRAY);

        glLineWidth(4);
        glPushMatrix();

        glTranslated( pos.x, pos.y, pos.z);

        glRotatef(RadianToDegree(rot.x),1.0f,0.0f,0.0f);
        glRotatef(RadianToDegree(rot.y),0.0f,1.0f,0.0f);
        glRotatef(RadianToDegree(rot.z),0.0f,0.0f,1.0f);

        glVertexPointer( 3, GL_FLOAT, 0, model_loader.GetGroupJointsArray(f));
        glColorPointer( 3, GL_FLOAT, 0, model_loader.GetColorJointsArray() );
        glDrawElements( GL_LINES, (model_loader.GetNumberOfJoints() - 1) * 2, GL_UNSIGNED_SHORT, model_loader.GetJointsIndices());

        glPopMatrix();
        glLineWidth(1);

        //----------------------------

        glPointSize(4);
        glPushMatrix();

        glTranslated( pos.x, pos.y, pos.z);

        glRotatef(RadianToDegree(rot.x),1.0f,0.0f,0.0f);
        glRotatef(RadianToDegree(rot.y),0.0f,1.0f,0.0f);
        glRotatef(RadianToDegree(rot.z),0.0f,0.0f,1.0f);

        glVertexPointer( 3, GL_FLOAT, 0, model_loader.GetGroupJointsArray(f) );
        glColorPointer( 3, GL_FLOAT, 0, model_loader.GetPColorJointsArray() );
        glDrawElements( GL_POINTS, model_loader.GetNumberOfJoints(), GL_UNSIGNED_SHORT, model_loader.GetPJointsIndices() );

        glPopMatrix();
        glPointSize(1);

        glEnable(GL_DEPTH_TEST);

    }

    void DrawJoints(const GLfloat &x,const GLfloat &y,const GLfloat &z) const
    {
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);

        glDisableClientState(GL_TEXTURE_COORD_ARRAY);

        glLineWidth(4);
        glPushMatrix();

        glTranslated( x, y, z);

        glVertexPointer( 3, GL_FLOAT, 0, model_loader.GetJointsArray() );
        glColorPointer( 3, GL_FLOAT, 0, model_loader.GetColorJointsArray() );
        glDrawElements( GL_LINES, (model_loader.GetNumberOfJoints() - 1) * 2, GL_UNSIGNED_SHORT, model_loader.GetJointsIndices());

        glPopMatrix();
        glLineWidth(1);

        glPointSize(4);
        glPushMatrix();

        glTranslated( x, y, z);

        glVertexPointer( 3, GL_FLOAT, 0, model_loader.GetJointsArray() );
        glColorPointer( 3, GL_FLOAT, 0, model_loader.GetPColorJointsArray() );
        glDrawElements( GL_POINTS, model_loader.GetNumberOfJoints(), GL_UNSIGNED_SHORT, model_loader.GetPJointsIndices() );

        glPopMatrix();
        glPointSize(1);

        glEnable(GL_DEPTH_TEST);
    }

    void DrawJoints(const GLfloat &x,const GLfloat &y,const GLfloat &z,const int frm) const
    {
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);

        glDisableClientState(GL_TEXTURE_COORD_ARRAY);

        glLineWidth(4);
        glPushMatrix();

        glTranslated(x,y,z);

        glVertexPointer( 3, GL_FLOAT, 0, model_loader.GetGroupJointsArray(frm));
        glColorPointer( 3, GL_FLOAT, 0, model_loader.GetColorJointsArray());
        glDrawElements( GL_LINES, (model_loader.GetNumberOfJoints() - 1) * 2, GL_UNSIGNED_SHORT, model_loader.GetJointsIndices() );

        glPopMatrix();
        glLineWidth(1);

        glPointSize(4);
        glPushMatrix();

        glTranslated(x,y,z);

        glVertexPointer( 3, GL_FLOAT, 0, model_loader.GetGroupJointsArray(frm) );
        glColorPointer( 3, GL_FLOAT, 0, model_loader.GetPColorJointsArray() );
        glDrawElements( GL_POINTS, model_loader.GetNumberOfJoints(), GL_UNSIGNED_SHORT, model_loader.GetPJointsIndices() );

        glPopMatrix();
        glPointSize(1);

        //glDisableClientState(GL_COLOR_ARRAY);

        glEnable(GL_DEPTH_TEST);
    }

    void PrintText(const vec3 &position, const vec3 &rot, char *text)
    {
        glPushMatrix();

        glRotatef(rot.x, 1.0, 0, 0);
        glRotatef(rot.y, 0, 1.0, 0);
        glRotatef(rot.z, 0, 0, 1.0);

        CString :: PrintOnPlanX(position.x, position.y, position.z,GLUT_BITMAP_TIMES_ROMAN_24, text);

        glPopMatrix();
    }*/
}
