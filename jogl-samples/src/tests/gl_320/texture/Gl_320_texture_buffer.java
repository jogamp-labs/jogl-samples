/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.texture;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2ES3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._4.Mat4;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import java.nio.FloatBuffer;
import glm.vec._2.Vec2;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_texture_buffer extends Test {

    public static void main(String[] args) {
        Gl_320_texture_buffer gl_320_texture_buffer = new Gl_320_texture_buffer();
    }

    public Gl_320_texture_buffer() {
        super("gl-320-texture-buffer", Profile.CORE, 3, 2, new Vec2(Math.PI * 0.2f));
    }

    private final String SHADERS_SOURCE = "texture-buffer";
    private final String SHADERS_ROOT = "src/data/gl_320/texture";

    private int vertexCount = 6;
    private int positionSize = vertexCount * Vec2.SIZE;
    private float[] positionData = {
        -1.0f, -1.0f,
        +1.0f, -1.0f,
        +1.0f, +1.0f,
        +1.0f, +1.0f,
        -1.0f, +1.0f,
        -1.0f, -1.0f};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int DISPLACEMENT = 1;
        public static final int DIFFUSE = 2;
        public static final int MAX = 3;
    }

    private int[] vertexArrayName = {0}, bufferName = new int[Buffer.MAX], displacementTextureName = {0},
            diffuseTextureName = {0};
    private int programName, uniformMvp, uniformDiffuse, uniformDisplacement;

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        boolean validated = true;

        if (validated) {
            validated = initTest(gl3);
        }
        if (validated) {
            validated = initProgram(gl3);
        }
        if (validated) {
            validated = initBuffer(gl3);
        }
        if (validated) {
            validated = initTexture(gl3);
        }
        if (validated) {
            validated = initVertexArray(gl3);
        }

        return validated && checkError(gl3, "begin");
    }

    private boolean initTest(GL3 gl3) {

        boolean validated = true;
        gl3.glEnable(GL_DEPTH_TEST);

        return validated && checkError(gl3, "initTest");
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.init(gl3);

            programName = shaderProgram.program();

            gl3.glBindAttribLocation(programName, Semantic.Attr.POSITION, "position");
            gl3.glBindFragDataLocation(programName, Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl3, System.out);
        }
        if (validated) {

            uniformMvp = gl3.glGetUniformLocation(programName, "mvp");
            uniformDiffuse = gl3.glGetUniformLocation(programName, "diffuse");
            uniformDisplacement = gl3.glGetUniformLocation(programName, "displacement");
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        gl3.glGenBuffers(Buffer.MAX, bufferName, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
        gl3.glBufferData(GL_ARRAY_BUFFER, positionSize, GLBuffers.newDirectFloatBuffer(positionData), GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        float[] position = {
            +0.1f, +0.3f, -1.0f, +1.0f,
            -0.5f, +0.0f, -0.5f, +1.0f,
            -0.2f, -0.2f, +0.0f, +1.0f,
            +0.3f, +0.2f, +0.5f, +1.0f,
            +0.1f, -0.3f, +1.0f, +1.0f};

        gl3.glBindBuffer(GL_TEXTURE_BUFFER, bufferName[Buffer.DISPLACEMENT]);
        FloatBuffer positionBuffer = GLBuffers.newDirectFloatBuffer(position);
        gl3.glBufferData(GL_TEXTURE_BUFFER, position.length * Float.BYTES, positionBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_TEXTURE_BUFFER, 0);

        byte[] diffuse = {
            (byte) 255, (byte) 0, (byte) 0, (byte) 255,
            (byte) 255, (byte) 127, (byte) 0, (byte) 255,
            (byte) 255, (byte) 255, (byte) 0, (byte) 255,
            (byte) 0, (byte) 255, (byte) 0, (byte) 255,
            (byte) 0, (byte) 0, (byte) 255, (byte) 255};

        int[] maxTextureBufferSize = {0};
        gl3.glGetIntegerv(GL_MAX_TEXTURE_BUFFER_SIZE, maxTextureBufferSize, 0);

        gl3.glBindBuffer(GL_TEXTURE_BUFFER, bufferName[Buffer.DIFFUSE]);
        gl3.glBufferData(GL_TEXTURE_BUFFER, Math.min(500_000, maxTextureBufferSize[0]), null, GL_STATIC_DRAW);
        //glBufferData(GL_TEXTURE_BUFFER, sizeof(Diffuse), Diffuse, GL_STATIC_DRAW);
        gl3.glBufferSubData(GL_TEXTURE_BUFFER, 0, diffuse.length * Byte.BYTES, GLBuffers.newDirectByteBuffer(diffuse));
        gl3.glBindBuffer(GL_TEXTURE_BUFFER, 0);

        return checkError(gl3, "initBuffer");
    }

    private boolean initTexture(GL3 gl3) {

        gl3.glGenTextures(1, displacementTextureName, 0);
        gl3.glBindTexture(GL_TEXTURE_BUFFER, displacementTextureName[0]);
        gl3.glTexBuffer(GL_TEXTURE_BUFFER, GL_RGBA32F, bufferName[Buffer.DISPLACEMENT]);
        gl3.glBindTexture(GL_TEXTURE_BUFFER, 0);

        gl3.glGenTextures(1, diffuseTextureName, 0);
        gl3.glBindTexture(GL_TEXTURE_BUFFER, diffuseTextureName[0]);
        gl3.glTexBuffer(GL_TEXTURE_BUFFER, GL_RGBA8, bufferName[Buffer.DIFFUSE]);
        gl3.glBindTexture(GL_TEXTURE_BUFFER, 0);

        return checkError(gl3, "initTexture");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName, 0);
        gl3.glBindVertexArray(vertexArrayName[0]);
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.VERTEX]);
            {
                gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, 0, 0);
            }
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = projection.mul(viewMat4()).mul(model);

        // Set the display viewport
        gl3.glViewport(0, 0, windowSize.x, windowSize.y);

        // Clear color buffer with black
        float[] depth = {1.0f};
        gl3.glClearBufferfv(GL_DEPTH, 0, depth, 0);
        gl3.glClearBufferfv(GL_COLOR, 0, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        // Bind program
        gl3.glUseProgram(programName);
        gl3.glUniformMatrix4fv(uniformMvp, 1, false, mvp.toFa_(), 0);
        gl3.glUniform1i(uniformDisplacement, 0);
        gl3.glUniform1i(uniformDiffuse, 1);

        gl3.glActiveTexture(GL_TEXTURE0);
        gl3.glBindTexture(GL_TEXTURE_BUFFER, displacementTextureName[0]);

        gl3.glActiveTexture(GL_TEXTURE1);
        gl3.glBindTexture(GL_TEXTURE_BUFFER, diffuseTextureName[0]);

        gl3.glBindVertexArray(vertexArrayName[0]);
        gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, 5);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteTextures(1, diffuseTextureName, 0);
        gl3.glDeleteTextures(1, displacementTextureName, 0);
        gl3.glDeleteBuffers(Buffer.MAX, bufferName, 0);
        gl3.glDeleteProgram(programName);
        gl3.glDeleteVertexArrays(1, vertexArrayName, 0);

        return checkError(gl3, "end");
    }
}
