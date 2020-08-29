package com.robotzero.assets;

import com.robotzero.Eggos;
import com.robotzero.render.opengl.Renderer2D;
import com.robotzero.render.opengl.text.Font;
import com.robotzero.shader.Texture;
import org.lwjgl.opengl.GL;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.system.MemoryUtil.NULL;

public class AssetService {
    ExecutorService executorService;
    private long sharedWindow;
    private final ConcurrentHashMap<String, Asset> gameAssets = new ConcurrentHashMap<>();

    public AssetService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void LoadAssets(String path) {
        URL resources = this.getClass().getClassLoader().getResource(path);
        try {
            final var resourcesPath = Paths.get(resources.toURI());
            Stream<Path> files = Files.list(resourcesPath);
            sharedWindow = createOpenGLContextForWorkerThread();
            CompletableFuture.runAsync(() -> {
                glfwInit();
                glfwMakeContextCurrent(sharedWindow);
                GL.createCapabilities();
                files.forEach(asset -> {
                    Asset asset1 = new Asset();
                    asset1.loadAsset(resourcesPath + "/" + asset.getFileName().toString());
                    gameAssets.put(asset1.getFileName(), asset1);
                });

                try (InputStream inputStream = AssetService.class.getClassLoader().getResourceAsStream("Inconsolata.ttf")) {
                    Asset font = new Font(inputStream, 16);
                    gameAssets.put("font", font);
                } catch (FontFormatException | IOException ex) {
                    Logger.getLogger(Renderer2D.class.getName()).log(Level.CONFIG, null, ex);
                    Asset font = new Font();
                    gameAssets.put("font", font);
                }
                Asset debugFont = new Font(12, false);
                gameAssets.put("debugfont", debugFont);
            }, executorService).whenComplete((void_, throwable) -> {
                glfwSwapBuffers(sharedWindow);
               Optional.ofNullable(throwable).ifPresent(t -> {
                   t.printStackTrace();
                   throw new RuntimeException("Failed to load assets");
               });
            });
            glfwMakeContextCurrent(Eggos.window);
        } catch (IOException | NullPointerException | URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load assets");
        }
    }

    public void cleanUp() {
        gameAssets.forEach((key, value) -> {
            Optional.ofNullable(value.getTexture()).ifPresent(texture -> {
                texture.unbind();
                texture.delete();
            });
            Optional.ofNullable(value.getData()).ifPresent(ByteBuffer::clear);
        });
        gameAssets.clear();
        executorService.shutdown();
    }

    public long getSharedWindow() {
        return sharedWindow;
    }

    public long createOpenGLContextForWorkerThread() {
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        return glfwCreateWindow(1, 1, "", NULL, Eggos.window);
    }

    public Optional<Texture> bind(String assetName) {
        return Optional.ofNullable(gameAssets.get(assetName)).map(asset -> {
            Texture texture = asset.getTexture();
            texture.bind();
            return texture;
        });
    }

    public Optional<Font> getDebugFont() {
        return Optional.ofNullable(gameAssets.get("debugfont")).map(asset -> {
            return (Font) asset;
        });
    }

    public void unbind(Texture currentTexture) {
        currentTexture.unbind();
    }
}
