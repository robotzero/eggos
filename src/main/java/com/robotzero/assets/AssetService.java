package com.robotzero.assets;

import com.robotzero.Eggos;
import com.robotzero.entity.Egg;
import com.robotzero.entity.Rail;
import com.robotzero.render.opengl.Renderer2D;
import com.robotzero.render.opengl.text.Font;
import com.robotzero.shader.Color;
import com.robotzero.shader.Texture;
import org.joml.Vector2f;
import org.joml.Vector4f;
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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
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
    private final ConcurrentHashMap<Rail, ConcurrentLinkedQueue<Egg>> eggs = new ConcurrentHashMap<>();

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

    public void loadEggs() {
        eggs.put(Rail.TOP_LEFT, new ConcurrentLinkedQueue<>());
        eggs.put(Rail.TOP_RIGHT, new ConcurrentLinkedQueue<>());
        eggs.put(Rail.BOTTOM_LEFT, new ConcurrentLinkedQueue<>());
        eggs.put(Rail.BOTTOM_RIGHT, new ConcurrentLinkedQueue<>());
        IntStream.range(0, 10).forEach(index -> {
            Asset eggAsset = gameAssets.get("egg_01.png");
            Egg egg = new Egg(eggAsset, null);
            if (index % 2 == 0) {
                eggs.get(Rail.TOP_LEFT).add(egg);
                return;
            }

            if (index % 3 == 0) {
                eggs.get(Rail.TOP_RIGHT).add(egg);
                return;
            }

            if (index % 5 == 0) {
                eggs.get(Rail.BOTTOM_RIGHT).add(egg);
            }

            eggs.get(Rail.BOTTOM_LEFT).add(egg);
        });
    }

    public void setInitialEggPosition() {
        Vector2f Size = new Vector2f(16, 16);
        float texturePerHeight = Eggos.HEIGHT / (float) 16;
        float scaleFactor = texturePerHeight / Eggos.EGGHEIGHTFACTOR;
        Vector2f Scale = new Vector2f(scaleFactor, scaleFactor);
        Vector2f ScaledSize = Scale.mul(Size);
        Vector2f middle = new Vector2f(ScaledSize.x() / 2, ScaledSize.y() / 2);
        Vector2f topLeft = new Vector2f(Eggos.screenMiddle).mul(new Vector2f(0.5f, 1.5f)).sub(middle);
        Vector2f bottomLeft = new Vector2f(Eggos.screenMiddle).mul(new Vector2f(0.5f, 0.5f)).sub(middle);
        Vector2f topRight = new Vector2f(Eggos.screenMiddle).mul(new Vector2f(1.5f, 1.5f)).sub(middle);
        Vector2f bottomRight = new Vector2f(Eggos.screenMiddle).mul(new Vector2f(1.5f, 0.5f)).sub(middle);

        Egg egg1 = this.eggs.get(Rail.TOP_LEFT).iterator().next();
        Egg egg2 = this.eggs.get(Rail.BOTTOM_LEFT).iterator().next();
        Egg egg3 = this.eggs.get(Rail.TOP_RIGHT).iterator().next();
        Egg egg4 = this.eggs.get(Rail.BOTTOM_RIGHT).iterator().next();

        egg1.setScaledSize(ScaledSize);
        egg2.setScaledSize(ScaledSize);
        egg3.setScaledSize(ScaledSize);
        egg4.setScaledSize(ScaledSize);
        egg1.setPosition(new Vector4f(topLeft.x, topLeft.y, topLeft.x + ScaledSize.x, topLeft.y + ScaledSize.y));
        egg2.setPosition(new Vector4f(bottomLeft.x, bottomLeft.y, bottomLeft.x + ScaledSize.x, bottomLeft.y + ScaledSize.y));
        egg3.setPosition(new Vector4f(topRight.x, topRight.y, topRight.x + ScaledSize.x, topRight.y + ScaledSize.y));
        egg4.setPosition(new Vector4f(bottomRight.x, bottomRight.y, bottomRight.x + ScaledSize.x, bottomRight.y + ScaledSize.y));
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
        eggs.forEach((key, value) -> value.clear());
        eggs.clear();;
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

    public ConcurrentHashMap<Rail, ConcurrentLinkedQueue<Egg>> getEggs() {
        return eggs;
    }
}
