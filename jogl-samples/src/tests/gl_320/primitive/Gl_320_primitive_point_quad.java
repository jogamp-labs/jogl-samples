/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.primitive;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2GL3.*;
import com.jogamp.opengl.GL3;
import static com.jogamp.opengl.GL3.GL_PROGRAM_POINT_SIZE;
import static com.jogamp.opengl.GL3ES3.*;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import glm.glm;
import glm.mat._4.Mat4;
import glm.vec._3.Vec3;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glm.vec._4.Vec4;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_primitive_point_quad extends Test {

    public static void main(String[] args) {
        Gl_320_primitive_point_quad gl_320_primitive_point_quad = new Gl_320_primitive_point_quad();
    }

    public Gl_320_primitive_point_quad() {
        super("gl-320-primitive-point-quad", Profile.CORE, 3, 2);
    }

    private final String SHADERS_SOURCE = "primitive-point-quad";
    private final String SHADERS_ROOT = "src/data/gl_320/primitive";

    private int vertexCount, programName, uniformMvp, uniformMv, uniformCameraPosition;
    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1), bufferName = GLBuffers.newDirectIntBuffer(1);
    private FloatBuffer clearColor = GLBuffers.newDirectFloatBuffer(new float[]{0.0f, 0.0f, 0.0f, 0.0f}),
            clearDepth = GLBuffers.newDirectFloatBuffer(new float[]{1.0f});

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;
        //caps Caps(caps::CORE);

        boolean validated = true;

        if (validated) {
            validated = initProgram(gl3);
        }
        if (validated) {
            validated = initBuffer(gl3);
        }
        if (validated) {
            validated = initVertexArray(gl3);
        }

        gl3.glEnable(GL_DEPTH_TEST);
        gl3.glDepthFunc(GL_LESS);
        gl3.glEnable(GL_PROGRAM_POINT_SIZE);
        //glPointParameteri(GL_POINT_SPRITE_COORD_ORIGIN, GL_LOWER_LEFT);
        gl3.glPointParameteri(GL_POINT_SPRITE_COORD_ORIGIN, GL_UPPER_LEFT);

        return validated && checkError(gl3, "begin");
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl3, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE, "vert", null, true);
            ShaderCode geomShaderCode = ShaderCode.create(gl3, GL_GEOMETRY_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE, "geom", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl3, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT, null,
                    SHADERS_SOURCE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.add(vertShaderCode);
            shaderProgram.add(geomShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.init(gl3);

            programName = shaderProgram.program();

            gl3.glBindAttribLocation(programName, Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName, Semantic.Attr.COLOR, "color");
            gl3.glBindFragDataLocation(programName, Semantic.Frag.COLOR, "color");

            shaderProgram.link(gl3, System.out);
        }
        if (validated) {

            uniformMvp = gl3.glGetUniformLocation(programName, "mvp");
            uniformMv = gl3.glGetUniformLocation(programName, "mv");
            uniformCameraPosition = gl3.glGetUniformLocation(programName, "cameraPosition");
        }

        return validated & checkError(gl3, "initProgram");
    }

    // Buffer update using glBufferSubData
    private boolean initBuffer(GL3 gl3) {

        // Generate a buffer object
        gl3.glGenBuffers(1, bufferName);

        // Bind the buffer for use
        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(0));

        vertexCount = 5;

        // Reserve buffer memory but don't copy the values
        gl3.glBufferData(GL_ARRAY_BUFFER, glf.Vertex_v4fc4f.SIZE * vertexCount, null, GL_STATIC_DRAW);

        ByteBuffer data = gl3.glMapBufferRange(GL_ARRAY_BUFFER,
                0, // Offset
                glf.Vertex_v4fc4f.SIZE * vertexCount, // Size,
                GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

        float[] floatArray = {
            0.0f, 0.0f, -0.5f, 1.0f,
            1.0f, 0.0f, +0.0f, 1.0f,
            0.2f, 0.0f, +0.5f, 1.0f,
            1.0f, 0.5f, +0.0f, 1.0f,
            0.4f, 0.0f, +1.5f, 1.0f,
            1.0f, 1.0f, +0.0f, 1.0f,
            0.6f, 0.0f, +2.5f, 1.0f,
            0.0f, 1.0f, +0.0f, 1.0f,
            0.8f, 0.0f, +3.5f, 1.0f,
            0.0f, 0.0f, +1.0f, 1.0f};

        data.asFloatBuffer().put(floatArray).rewind();

        gl3.glUnmapBuffer(GL_ARRAY_BUFFER);

        // Unbind the buffer
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        return checkError(gl3, "initArrayBuffer");
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(1, vertexArrayName);
        gl3.glBindVertexArray(vertexArrayName.get(0));
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(0));
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 4, GL_FLOAT, false, glf.Vertex_v4fc4f.SIZE, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.COLOR, 4, GL_FLOAT, false, glf.Vertex_v4fc4f.SIZE, Vec4.SIZE);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.COLOR);
        }
        gl3.glBindVertexArray(0);

        return checkError(gl3, "initVertexArray");
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, 4.0f / 3.0f, 0.1f, 100.0f);
        Mat4 view = viewMat4();
        Mat4 model = new Mat4(1.0f);
        Mat4 mvp = projection.mul(view).mul(model);
        Mat4 mv = view.mul(model);

        gl3.glViewport(0, 0, windowSize.x, windowSize.y);
        gl3.glClearBufferfv(GL_COLOR, 0, clearColor);
        gl3.glClearBufferfv(GL_DEPTH, 0, clearDepth);

        gl3.glDisable(GL_SCISSOR_TEST);
        Vec3 cameraPosition = cameraPosition().negate();
        //glm::vec3 CameraPosition(glm::vec4(glm::normalize(glm::vec3(1.0)), 1.0) * this->view());

        gl3.glUseProgram(programName);
        gl3.glUniform3fv(uniformCameraPosition, 1, cameraPosition.toFa_(), 0);
        gl3.glUniformMatrix4fv(uniformMv, 1, false, mv.toFa_(), 0);
        gl3.glUniformMatrix4fv(uniformMvp, 1, false, mvp.toFa_(), 0);

        gl3.glBindVertexArray(vertexArrayName.get(0));

        gl3.glDrawArraysInstanced(GL_POINTS, 0, vertexCount, 1);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteBuffers(1, bufferName);
        gl3.glDeleteProgram(programName);
        gl3.glDeleteVertexArrays(1, vertexArrayName);

        BufferUtils.destroyDirectBuffer(bufferName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);

        BufferUtils.destroyDirectBuffer(clearColor);
        BufferUtils.destroyDirectBuffer(clearDepth);

        return checkError(gl3, "end");
    }
}
