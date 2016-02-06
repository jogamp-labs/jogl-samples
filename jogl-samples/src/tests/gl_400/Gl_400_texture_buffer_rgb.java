/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_400;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._4.Mat4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import glm.vec._2.Vec2;

/**
 *
 * @author GBarbieri
 */
public class Gl_400_texture_buffer_rgb extends Test {

    public static void main(String[] args) {
        Gl_400_texture_buffer_rgb gl_400_texture_buffer_rgb = new Gl_400_texture_buffer_rgb();
    }

    public Gl_400_texture_buffer_rgb() {
        super("gl-400-texture-buffer-rgb", Profile.CORE, 4, 0, new Vec2(Math.PI * 0.2f));
    }

    private final String SHADERS_SOURCE = "texture-buffer";
    private final String SHADERS_ROOT = "src/data/gl_400";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * 2 * Float.BYTES;
    private float[] vertexData = {
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, 1.0f,
        -1.0f, 1.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int DISPLACEMENT = 2;
        public static final int DIFFUSE = 3;
        public static final int MAX = 4;
    };

    private class Texture {

        public static final int DISPLACEMENT = 0;
        public static final int DIFFUSE = 1;
        public static final int MAX = 2;
    };

    private int[] bufferName = new int[Buffer.MAX], textureName = new int[Texture.MAX], vertexArrayName = {0};
    private int programName, uniformMvp, uniformDiffuse, uniformDisplacement;

    private boolean initTest(GL4 gl4) {

        boolean validated = true;
        gl4.glEnable(GL_DEPTH_TEST);

        return validated && checkError(gl4, "initTest");
    }

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = true;

        if (validated) {
            validated = initTest(gl4);
        }
        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initBuffer(gl4);
        }
        if (validated) {
            validated = initTexture(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }

        return validated && checkError(gl4, "begin");
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create program
        if (validated) {

            ShaderProgram shaderProgram = new ShaderProgram();

            ShaderCode vertexShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragmentShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            shaderProgram.init(gl4);

            shaderProgram.add(vertexShaderCode);
            shaderProgram.add(fragmentShaderCode);

            programName = shaderProgram.program();

            shaderProgram.link(gl4, System.out);

        }

        // Get variables locations
        if (validated) {

            uniformMvp = gl4.glGetUniformLocation(programName, "mvp");
            uniformDiffuse = gl4.glGetUniformLocation(programName, "diffuse");
            uniformDisplacement = gl4.glGetUniformLocation(programName, "displacement");
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        gl4.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        gl4.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);
        gl4.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);

        float[] displacement = {
            +0.1f, +0.3f, -1.0f,
            -0.5f, +0.0f, -0.5f,
            -0.2f, -0.2f, +0.0f,
            +0.3f, +0.2f, +0.5f,
            +0.1f, -0.3f, +1.0f};

        gl4.glBindBuffer(GL_TEXTURE_BUFFER, bufferName[Buffer.DISPLACEMENT]);
        gl4.glBufferData(GL_TEXTURE_BUFFER, displacement.length * Float.BYTES,
                GLBuffers.newDirectFloatBuffer(displacement), GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_TEXTURE_BUFFER, 0);

        float[] diffuse = {
            1.0f, 0.0f, 0.0f,
            1.0f, 0.5f, 0.0f,
            1.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 1.0f};

        gl4.glBindBuffer(GL_TEXTURE_BUFFER, bufferName[Buffer.DIFFUSE]);
        gl4.glBufferData(GL_TEXTURE_BUFFER, diffuse.length * Float.BYTES,
                GLBuffers.newDirectFloatBuffer(diffuse), GL_STATIC_DRAW);
        gl4.glBindBuffer(GL_TEXTURE_BUFFER, 0);

        return checkError(gl4, "initBuffer");
    }

    private boolean initTexture(GL4 gl4) {

        gl4.glGenTextures(Texture.MAX, textureName, 0);

        gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.DISPLACEMENT]);
        gl4.glTexBuffer(GL_TEXTURE_BUFFER, GL_RGB32F, bufferName[Buffer.DISPLACEMENT]);
        gl4.glBindTexture(GL_TEXTURE_BUFFER, 0);

        gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.DIFFUSE]);
        gl4.glTexBuffer(GL_TEXTURE_BUFFER, GL_RGB32F, bufferName[Buffer.DIFFUSE]);
        gl4.glBindTexture(GL_TEXTURE_BUFFER, 0);

        return checkError(gl4, "initTextureBuffer");
    }

    private boolean initVertexArray(GL4 gl4) {

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        {
            gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            gl4.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0, 0);

            gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);

            gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.ELEMENT]);
        }
        gl4.glBindVertexArray(0);

        return checkError(gl4, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = projection.mul(viewMat4()).mul(model);

        gl4.glViewport(0, 0, windowSize.x, windowSize.y);

        float[] depth = {1.0f};
        gl4.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl4.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        gl4.glUseProgram(programName);
        gl4.glUniformMatrix4fv(uniformMvp, 1, false, mvp.toFa_(), 0);
        gl4.glUniform1i(uniformDisplacement, 0);
        gl4.glUniform1i(uniformDiffuse, 1);

        gl4.glActiveTexture(GL_TEXTURE0);
        gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.DISPLACEMENT]);

        gl4.glActiveTexture(GL_TEXTURE1);
        gl4.glBindTexture(GL_TEXTURE_BUFFER, textureName[Texture.DIFFUSE]);

        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 5, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteTextures(Texture.MAX, textureName, 0);
        gl4.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);

        return checkError(gl4, "end");
    }
}
