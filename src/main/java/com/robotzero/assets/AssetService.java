package com.robotzero.assets;

import com.robotzero.Eggos;
import com.robotzero.entity.Egg;
import com.robotzero.entity.Entity;
import com.robotzero.entity.Rail;
import com.robotzero.render.opengl.Renderer2D;
import com.robotzero.render.opengl.text.Font;
import com.robotzero.shader.Texture;
import com.robotzero.entity.WolfState;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
  private final ConcurrentHashMap<Rail, ConcurrentLinkedQueue<Egg>> eggsShowing = new ConcurrentHashMap<>();
  private final List<Entity> crashEggs = new ArrayList<>();
  private final Map<WolfState, String> movementMapping = Map.of(
      WolfState.TOP_LEFT, "wolf_left_top.png",
      WolfState.TOP_RIGHT, "wolf_right_top.png",
      WolfState.BOTTOM_LEFT, "wolf_left_bottom.png",
      WolfState.BOTTOM_RIGHT, "wolf_right_bottom.png"
  );

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

    eggsShowing.put(Rail.TOP_LEFT, new ConcurrentLinkedQueue<>());
    eggsShowing.put(Rail.TOP_RIGHT, new ConcurrentLinkedQueue<>());
    eggsShowing.put(Rail.BOTTOM_LEFT, new ConcurrentLinkedQueue<>());
    eggsShowing.put(Rail.BOTTOM_RIGHT, new ConcurrentLinkedQueue<>());

    CompletableFuture.runAsync(() -> {
      IntStream.range(0, 25).forEach(index -> {
        if (index % 5 == 0) {
          while (gameAssets.get("top_left_egg_1.png") == null) {
          }
          ;
          while (gameAssets.get("top_right_egg_1.png") == null) {
          }
          ;
          while (gameAssets.get("bottom_right_egg_1.png") == null) {
          }
          ;
          while (gameAssets.get("bottom_left_egg_1.png") == null) {
          }
          ;
          Asset eggAsset1a = gameAssets.get("top_left_egg_1.png");
          Asset eggAsset1b = gameAssets.get("top_left_egg_2.png");
          Asset eggAsset1c = gameAssets.get("top_left_egg_3.png");
          Asset eggAsset1d = gameAssets.get("top_left_egg_4.png");
          Asset eggAsset1e = gameAssets.get("top_left_egg_5.png");
          Egg egg1 = new Egg(Rail.TOP_LEFT);
          egg1.addAssets(eggAsset1a, eggAsset1b, eggAsset1c, eggAsset1d, eggAsset1e);
          eggs.get(Rail.TOP_LEFT).add(egg1);
          Asset eggAsset2a = gameAssets.get("top_right_egg_1.png");
          Asset eggAsset2b = gameAssets.get("top_right_egg_2.png");
          Asset eggAsset2c = gameAssets.get("top_right_egg_3.png");
          Asset eggAsset2d = gameAssets.get("top_right_egg_4.png");
          Asset eggAsset2e = gameAssets.get("top_right_egg_5.png");
          Egg egg2 = new Egg(Rail.TOP_RIGHT);
          egg2.addAssets(eggAsset2a, eggAsset2b, eggAsset2c, eggAsset2d, eggAsset2e);
          eggs.get(Rail.TOP_RIGHT).add(egg2);
          Asset eggAsset3a = gameAssets.get("bottom_right_egg_1.png");
          Asset eggAsset3b = gameAssets.get("bottom_right_egg_2.png");
          Asset eggAsset3c = gameAssets.get("bottom_right_egg_3.png");
          Asset eggAsset3d = gameAssets.get("bottom_right_egg_4.png");
          Asset eggAsset3e = gameAssets.get("bottom_right_egg_5.png");
          Egg egg3 = new Egg(Rail.BOTTOM_RIGHT);
          egg3.addAssets(eggAsset3a, eggAsset3b, eggAsset3c, eggAsset3d, eggAsset3e);
          eggs.get(Rail.BOTTOM_RIGHT).add(egg3);
          Asset eggAsset4a = gameAssets.get("bottom_left_egg_1.png");
          Asset eggAsset4b = gameAssets.get("bottom_left_egg_2.png");
          Asset eggAsset4c = gameAssets.get("bottom_left_egg_3.png");
          Asset eggAsset4d = gameAssets.get("bottom_left_egg_4.png");
          Asset eggAsset4e = gameAssets.get("bottom_left_egg_5.png");
          Egg egg4 = new Egg(Rail.BOTTOM_LEFT);
          egg4.addAssets(eggAsset4a, eggAsset4b, eggAsset4c, eggAsset4d, eggAsset4e);
          eggs.get(Rail.BOTTOM_LEFT).add(egg4);
        }
      });
    }, executorService).join();
  }

  public void addCrashEgg(boolean half) {
//    Entity crashEgg = new Entity();
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
    eggs.clear();
    ;
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

  public ConcurrentHashMap<Rail, ConcurrentLinkedQueue<Egg>> getEggsShowing() {
    return eggsShowing;
  }

  public Asset getFred(WolfState wolfState) {
    return gameAssets.get(movementMapping.get(wolfState));
  }

  public Asset getCoop(String coop) {
    return gameAssets.get(coop);
  }

  public Asset getChicken(String chicken) {
    while (gameAssets.get(chicken) == null) {};
    return gameAssets.get(chicken);
  }

  public List<Entity> getCrashEggs() {
    return crashEggs;
  }
}


