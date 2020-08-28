package com.robotzero.assets;

import com.robotzero.shader.Texture;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;

import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;

public class Asset {
    private int width, height;
    private ByteBuffer assetData;
    private String fileName;
    private Texture texture;

    private void createTexture() {
        this.texture = Texture.createTexture(width, height, assetData);
    }

    public void loadAsset(String path) {
        String OS = System.getProperty("os.name").toLowerCase();
        if ((OS.contains("win"))) {
            path = path.replaceFirst("/", "\\\\");
        }
        fileName = Paths.get(path).getFileName().toString();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            /* Prepare image buffers */
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            /* Load image */
            stbi_set_flip_vertically_on_load(true);
            assetData = stbi_load(path, w, h, comp, 4);
            if (assetData == null) {
                throw new RuntimeException("Failed to load a texture file!"
                        + System.lineSeparator() + stbi_failure_reason());
            }

            /* Get width and height of image */
            width = w.get();
            height = h.get();
        }
        createTexture();
    }

    public Texture getTexture() {
        return texture;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getFileName() {
        return this.fileName;
    }

    public ByteBuffer getData() {
        return this.assetData;
    }
}
