package com.robotzero.assets;

import com.robotzero.HelloWorld;
import com.robotzero.shader.Texture;
import org.lwjgl.opengl.GL;

import java.io.IOException;
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
//                try {
//                    FontTexture fontTexture = new FontTexture(new Font("Arial", Font.PLAIN, 20), Charset.defaultCharset().name());
//                    fontTexture.loadAsset();
//                    gameAssets.put(fontTexture.getFileName(), fontTexture);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }, executorService).whenComplete((void_, throwable) -> {
                glfwSwapBuffers(sharedWindow);
               Optional.ofNullable(throwable).ifPresent(t -> {
                   t.printStackTrace();
                   throw new RuntimeException("Failed to load assets");
               });
            });
            glfwMakeContextCurrent(HelloWorld.window);
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
        return glfwCreateWindow(1, 1, "", NULL, HelloWorld.window);
    }

    public Texture bind(String assetName) {
        return Optional.ofNullable(gameAssets.get(assetName)).map(asset -> {
            Texture texture = asset.getTexture();
            texture.bind();
            return texture;
        }).orElseGet(() -> {
            Texture texture = Texture.createTexture(1, 1, ByteBuffer.allocateDirect(10));
            texture.bind();
            return texture;
        });
    }

    public void unbind(Texture currentTexture) {
        currentTexture.unbind();
    }
}
