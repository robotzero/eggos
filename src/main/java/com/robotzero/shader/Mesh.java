package com.robotzero.shader;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Mesh {
    private int vaoId;
    private final Map<String, Integer> vboIdList = new HashMap<>();
    private final Texture texture;
    private final int vertexCount;
    private static final int VERTEX_ATTRIB = 0;
    private static final int TCOORD_ATTRIB = 1;
    private final Material material;

    public Mesh(float[] vertices, float[] textCoords, int[] indices, Texture texture, Material material) {
        FloatBuffer verticesBuffer = null;
        FloatBuffer textCoordsBuffer = null;
        IntBuffer indicesBuffer = null;
        this.texture = texture;
        this.material = material;
        vertexCount = indices.length;
        try {
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            // Vertices VBO
            final int vvboId = glGenBuffers();
            vboIdList.put("vbo", vvboId);
            verticesBuffer = MemoryUtil.memAllocFloat(vertices.length);
            verticesBuffer.put(vertices).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vvboId);
            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(VERTEX_ATTRIB, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(VERTEX_ATTRIB);

            // Texture VBO
            final int tvboId = glGenBuffers();
            vboIdList.put("tbo", tvboId);
            textCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.length);
            textCoordsBuffer.put(textCoords).flip();
            glBindBuffer(GL_ARRAY_BUFFER, tvboId);
            glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(TCOORD_ATTRIB, 2, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(TCOORD_ATTRIB);

            // Index VBO
            final int ivboId = glGenBuffers();
            vboIdList.put("ibo", ivboId);
            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ivboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);

        } catch (Exception e) {
            throw new RuntimeException("Error in Mesh " + e.getMessage());
        } finally {
            Optional.ofNullable(textCoordsBuffer).ifPresent(MemoryUtil::memFree);
            Optional.ofNullable(verticesBuffer).ifPresent(MemoryUtil::memFree);
            Optional.ofNullable(indicesBuffer).ifPresent(MemoryUtil::memFree);
        }
    }

    public void render() {
        initRender();
        if (vboIdList.containsKey("ibo")) {
            glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
        } else {
            glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        }
        endRender();
    }

    public void endRender() {
        // Restore state
        glBindVertexArray(0);
        if (vboIdList.containsKey("ibo")) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }

//        Optional.ofNullable(texture).ifPresent(Texture::unbind);
        Optional.ofNullable(texture).ifPresent(texture1 -> texture1.delete());
    }

    private void initRender() {
        Optional.ofNullable(this.texture).ifPresent(t -> {
            // Activate first texture bank
            glActiveTexture(GL_TEXTURE1);
            texture.bind();
        });

        // Draw the mesh
        glBindVertexArray(vaoId);
        if (vboIdList.containsKey("ibo")) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIdList.get("ibo"));
        }
    }

    public void cleanUp() {
        glDisableVertexAttribArray(0);

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        vboIdList.forEach((key, vboId) -> {
            glDeleteBuffers(vboId);
        });

        vboIdList.clear();

        if (texture != null) {
//            texture.cleanup();
            texture.delete();
        }

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    public Material getMaterial() {
        return material;
    }

    public Texture getTexture() {
        return this.texture;
    }
}
