package com.robotzero;

import com.robotzero.assets.Asset;
import com.robotzero.assets.AssetService;
import com.robotzero.entity.Egg;
import com.robotzero.entity.Entity;
import com.robotzero.entity.GameState;
import com.robotzero.entity.Rail;
import com.robotzero.render.opengl.Renderer2D;
import com.robotzero.shader.Texture;
import com.robotzero.entity.WolfState;
import com.robotzero.utils.Timer;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.MemoryStack;

import java.math.BigDecimal;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_L;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Q;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_DEBUG_CONTEXT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Eggos {

  // The window handle
  public static long window;
  public static int WIDTH = 720;
  public static int HEIGHT = 420;
  public static final int TARGET_UPS = 60;
  public static final int TARGET_FPS = 60;
  public static Vector2f eggddP = new Vector2f(0.0f, 0.0f);
  public static Vector2i screenMiddle = new Vector2i(WIDTH / 2, HEIGHT / 2);
  public static float eggSpeed = 1f;
  private final float chickenSpeed = 1f;
  private WolfState wolfState = WolfState.TOP_LEFT;
  private Renderer2D renderer2D;
  private AssetService assetService;
  private ExecutorService executorService;
  private Timer timer;
  private double lastFps;
  private boolean[] keyDown = new boolean[GLFW.GLFW_KEY_LAST];
  private static String TITLE = "Eggos";
  private GLFWErrorCallback glwErrorCallback;
  private Vector2f difference = new Vector2f(1.0f, 1.0f);
  private float upsTracking = 0;
  private final float railWidth = 263f;
  private final float railHeight = 195f;
  private Rail railAdded = null;
  private Map<Rail, Integer> eggTicks = new HashMap<>(Map.of(
      Rail.TOP_LEFT, 0,
      Rail.BOTTOM_LEFT, 0,
      Rail.BOTTOM_RIGHT, 0,
      Rail.TOP_RIGHT, 0
  ));
  private int eggsInTheBasket = 0;
  private final Map<Rail, Integer> eggsOnScreen = new HashMap<>(Map.of(
      Rail.TOP_LEFT, 1,
      Rail.BOTTOM_LEFT, 0,
      Rail.BOTTOM_RIGHT, 0,
      Rail.TOP_RIGHT, 0
  ));
  private final Map<Rail, Vector2f> eggsDdp = new HashMap<>(Map.of(
      Rail.TOP_LEFT, new Vector2f(1f, -0.60f),
      Rail.BOTTOM_LEFT, new Vector2f(1f, -0.50f),
      Rail.BOTTOM_RIGHT, new Vector2f(-1f, -0.50f),
      Rail.TOP_RIGHT, new Vector2f(-1f, -0.50f)
  ));

  private int allEggTicks = 0;
  private final Matrix4f identity = new Matrix4f();
  private final Random random = new Random();
  private final Function<Vector2f, Vector2f> wolfPositionCalculator = (scaledSize) -> {
    if (wolfState.equals(WolfState.TOP_RIGHT) || wolfState.equals(WolfState.BOTTOM_RIGHT)) {
      return new Vector2f(screenMiddle.x, screenMiddle.y * 0.15f);
    } else if (wolfState.equals(WolfState.TOP_LEFT) || wolfState.equals(WolfState.BOTTOM_LEFT)) {
      return new Vector2f(screenMiddle.x, screenMiddle.y * 0.15f).sub(scaledSize.x, 0.0f);
    }
    throw new RuntimeException("Invalid state");
  };
  private final Supplier<Function<Entity, Vector2f>> initialPosition = () -> {
    return (coop) -> {
      return new Vector2f(coop.getScaledSize().x / coop.getSize().x, coop.getScaledSize().y / coop.getSize().y);
    };
  };
  private final float propRailWidthNotScaled = (float) Math.sqrt((railWidth * railWidth) + (railHeight * railHeight));
  private float propRailWidth = 1.0f;
  private GameState currentState;
  private Vector2f mainEggLengthDt = new Vector2f(1.0f, 1.0f);
  private Entity wolf;
  private Entity coopLeft;
  private Entity coopRight;
  private Entity chicken;
  private Entity crashChickenRight;

  public void run() throws Exception {
    System.out.println("Hello LWJGL " + Version.getVersion() + "!");

    init();
    loop();

    assetService.cleanUp();
    // Free the window callbacks and destroy the window
    glfwFreeCallbacks(window);
    glwErrorCallback.free();
    glfwDestroyWindow(window);
    glfwFreeCallbacks(assetService.getSharedWindow());
    glfwDestroyWindow(assetService.getSharedWindow());
    // Terminate GLFW and free the error callback
    glfwTerminate();
  }

  private void init() {
    // Setup an error callback. The default implementation
    // will print the error message in System.err.
    GLFWErrorCallback.createPrint(System.err).set();

    // Initialize GLFW. Most GLFW functions will not work before doing this.
    if (!glfwInit())
      throw new IllegalStateException("Unable to initialize GLFW");

    // Configure GLFW
    glfwDefaultWindowHints(); // optional, the current window hints are already the default
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
    glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

    glfwSetErrorCallback(glwErrorCallback = new GLFWErrorCallback() {
      @Override
      public void invoke(int error, long description) {
        System.out.println(GLFWErrorCallback.getDescription(description));
      }
    });

    // Create the window
    window = glfwCreateWindow(WIDTH, HEIGHT, String.format("%s", TITLE), NULL, NULL);

    if (window == NULL) {
      throw new RuntimeException("Failed to create the GLFW window");
    }
    // Setup a key callback. It will be called every time a key is pressed, repeated or released.
    glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
      if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
        glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
      }

      if (key == GLFW_KEY_UNKNOWN) {
        return;
      }

      keyDown[key] = action == GLFW_PRESS || action == GLFW_REPEAT;
    });

    glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
      public void invoke(long window, int width, int height) {
        if (width > 0 && height > 0 && (WIDTH != width || HEIGHT != height)) {
          difference = new Vector2f((float) width / WIDTH, (float) height / HEIGHT);
          WIDTH = width;
          HEIGHT = height;
          screenMiddle = new Vector2i(WIDTH / 2, HEIGHT / 2);
          screenChangedEvent();
          glViewport(0, 0, WIDTH, HEIGHT);
          renderer2D.dispose();
          renderer2D.init();
        }
      }
    });

    glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallback() {
      public void invoke(long window, int width, int height) {
        if (width > 0 && height > 0 && (WIDTH != width || HEIGHT != height)) {
          difference = new Vector2f((float) width / WIDTH, (float) height / HEIGHT);
          WIDTH = width;
          HEIGHT = height;
          screenMiddle = new Vector2i(WIDTH / 2, HEIGHT / 2);
          screenChangedEvent();
          glViewport(0, 0, WIDTH, HEIGHT);
          renderer2D.dispose();
          renderer2D.init();
        }
      }
    });

    // Get the thread stack and push a new frame
    try (MemoryStack stack = stackPush()) {
      IntBuffer pWidth = stack.mallocInt(1); // int*
      IntBuffer pHeight = stack.mallocInt(1); // int*

      // Get the window size passed to glfwCreateWindow
      glfwGetWindowSize(window, pWidth, pHeight);

      // Get the resolution of the primary monitor
      GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

      // Center the window
      glfwSetWindowPos(
          window,
          (vidmode.width() - pWidth.get(0)) / 2,
          (vidmode.height() - pHeight.get(0)) / 2
      );
    } // the stack frame is popped automatically

    // Make the OpenGL context current
    glfwMakeContextCurrent(window);
    // Enable v-sync
    glfwSwapInterval(1);

    // Make the window visible
    glfwShowWindow(window);

    /* Custom starts here */
    executorService = Executors.newCachedThreadPool();
    timer = new Timer();
    renderer2D = new Renderer2D();
    assetService = new AssetService(executorService);
    assetService.LoadAssets("assets");
    assetService.loadEggs();
    wolf = new Entity(2.0f, wolfPositionCalculator, assetService.getFred(wolfState));
    coopRight = new Entity(1.2f, (scaledSize) -> {
      return new Vector2f(Eggos.WIDTH - scaledSize.x, Eggos.HEIGHT - scaledSize.y);
    }, assetService.getCoop("coop_right.png"));
    coopLeft = new Entity(1.2f, (scaledSize) -> {
      return new Vector2f(0.0f, Eggos.HEIGHT - this.coopRight.getScaledSize().y);
    }, assetService.getCoop("coop_left.png"));
    chicken = new Entity(10f, (scaledSize) -> {
      return new Vector2f(0.0f, 0.0f);
    }, assetService.getChicken("crash_chicken.png"));
    crashChickenRight = new Entity(8f, (scaledSize) -> {
      return new Vector2f(Eggos.WIDTH * 0.78f, Eggos.HEIGHT * 0.12f);
    }, assetService.getChicken("chicken_right_crash_1.png"),
        assetService.getChicken("chicken_right_crash_2.png"),
        assetService.getChicken("chicken_right_crash_3.png"),
        assetService.getChicken("chicken_right_crash_4.png"));
    propRailWidth = propRailWidthNotScaled * (coopRight.getScaledSize().x / coopRight.getSize().x);
    mainEggLengthDt = new Vector2f(1.0f * propRailWidth * 0.22f, 1.0f * propRailWidth * 0.22f);
    lastFps = timer.getTime();
    currentState = GameState.RUNNING;
  }

  private void loop() throws Exception {
    float delta;
    float accumulator = 0f;
    float interval = 1f / TARGET_UPS;

    // This line is critical for LWJGL's interoperation with GLFW's
    // OpenGL context, or any context that is managed externally.
    // LWJGL detects the context that is current in the current thread,
    // creates the GLCapabilities instance and makes the OpenGL
    // bindings available for use.
    GL.createCapabilities();
    GLUtil.setupDebugMessageCallback();
    glViewport(0, 0, WIDTH, HEIGHT);
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glfwWindowHint(GLFW_SAMPLES, 4);
    glEnable(GL_MULTISAMPLE);

    timer.init();
    renderer2D.init();

    // Set the clear color
    renderer2D.clearColor();

    // Run the rendering loop until the user has attempted to close
    // the window or has pressed the ESCAPE key.
    while (!glfwWindowShouldClose(window)) {
      delta = timer.getDelta();
      accumulator += delta;

      wolfState = input();
      upsTracking = upsTracking + 1f / TARGET_FPS;

      boolean shouldEggUpdate = upsTracking > eggSpeed;
      beginSim(shouldEggUpdate, currentState);
      update(1f / TARGET_FPS, shouldEggUpdate, currentState);
      endSim(shouldEggUpdate, currentState);


      timer.updateUPS();
      accumulator -= interval;

      if (timer.getLastLoopTime() - lastFps > 1) {
        lastFps = timer.getLastLoopTime();
      }

      renderer2D.clear();
      Optional.ofNullable(assetService.getFred(wolfState)).ifPresent(asset -> {
        Optional<Texture> currentTexture = assetService.bind(asset.getFileName());
        currentTexture.ifPresent(texture -> {
          if (wolf.getScaledSize() == null) {
            wolf.setAsset(asset, wolfPositionCalculator);
          }
          renderer2D.begin();
          renderer2D.setUniform(new Matrix4f().translate(wolf.getPosition().x, wolf.getPosition().y, 0.0f));
          renderer2D.drawTextureRegion(0.0f, 0.0f, wolf.getScaledSize().x, wolf.getScaledSize().y, 0, 0, 1, 1, Entity.defaultColor);
          renderer2D.end();
          assetService.unbind(texture);
        });
      });

      Optional<Texture> coopRight = assetService.bind("coop_right.png");
      coopRight.ifPresent(coopRightTexture -> {
        if (this.coopRight.getScaledSize() == null) {
          this.coopRight.setAsset(assetService.getCoop("coop_right.png"), (scaledSize) -> {
            return new Vector2f(Eggos.WIDTH - scaledSize.x, Eggos.HEIGHT - scaledSize.y);
          });
        }
        renderer2D.begin();
        renderer2D.setUniform(new Matrix4f().translate(this.coopRight.getPosition().x, this.coopRight.getPosition().y, 0.0f));
        renderer2D.drawTextureRegion(0.0f, 0.0f, this.coopRight.getScaledSize().x, this.coopRight.getScaledSize().y, 0, 0, 1, 1, Entity.defaultColor);
        renderer2D.end();
        assetService.unbind(coopRightTexture);
      });

      Optional<Texture> coopLeft = assetService.bind("coop_left.png");
      coopLeft.ifPresent(coopLeftTexture -> {
        Optional.ofNullable(this.coopRight.getScaledSize()).ifPresent(size -> {
          if (this.coopLeft.getScaledSize() == null) {
            this.coopLeft.setAsset(assetService.getCoop("coop_left.png"), (scaledSize) -> {
              return new Vector2f(0.0f, Eggos.HEIGHT - this.coopRight.getScaledSize().y);
            });
          }
          renderer2D.begin();
          renderer2D.setUniform(new Matrix4f().translate(0.0f, this.coopLeft.getPosition().y, 0.0f));
          renderer2D.drawTextureRegion(0.0f, 0.0f, this.coopLeft.getScaledSize().x, this.coopLeft.getScaledSize().y, 0, 0, 1, 1, Entity.defaultColor);
          renderer2D.end();
          assetService.unbind(coopLeftTexture);
        });
      });

      assetService.getEggsShowing().forEach((key, value) -> {
        for (Egg egg : value) {
          Asset asset = egg.getAsset();
          assetService.bind(asset.getFileName());
          renderer2D.begin();
          renderer2D.setUniform(new Matrix4f().translate(egg.getPosition().x, egg.getPosition().y, 0.0f));
          renderer2D.drawTextureRegion(0, 0, egg.getScaledSize().x, egg.getScaledSize().y, 0, 0, 1, 1, Egg.defaultColor);
          renderer2D.flush();
          renderer2D.end();
          assetService.unbind(asset.getTexture());
        }
      });

      Optional<Texture> crashChicken = assetService.bind(crashChickenRight.getAsset().getFileName());
      crashChicken.ifPresent(texture -> {
        renderer2D.begin();
        renderer2D.setUniform(new Matrix4f().translate(crashChickenRight.getPosition().x, crashChickenRight.getPosition().y, 0.0f));
//        renderer2D.setUniform(identity);
        renderer2D.drawTextureRegion(0.0f, 0.0f, this.crashChickenRight.getScaledSize().x, this.crashChickenRight.getScaledSize().y, 0, 0, 1, 1, Entity.defaultColor);
        renderer2D.flush();
        renderer2D.end();
        assetService.unbind(texture);
      });

      renderer2D.setUniform(identity);
      assetService.getDebugFont().ifPresent(font -> {
        font.drawText(renderer2D, "FPS: " + timer.getFPS() + " | UPS: " + timer.getUPS(), 5, HEIGHT - 20);
        font.drawText(renderer2D, "Points: " + BigDecimal.valueOf(eggsInTheBasket).toPlainString(), screenMiddle.x, HEIGHT - 20);
        Optional.ofNullable(font.getTexture()).ifPresent(Texture::unbind);
      });

      glfwSwapBuffers(window); // swap the color buffers
      timer.updateFPS();
      /* Update timer */
      timer.update();
      // Poll for window events. The key callback above will only be
      // invoked during this call.
      glfwPollEvents();

      sync();
    }
  }

  private void sync() {
    double lastLoopTime = timer.getLastLoopTime();
    double now = timer.getTime();
    float targetTime = 1f / TARGET_FPS;

    while (now - lastLoopTime < targetTime) {
      Thread.yield();

      /* This is optional if you want your game to stop consuming too much
       * CPU but you will loose some accuracy because Thread.sleep(1)
       * could sleep longer than 1 millisecond */
      try {
        Thread.sleep(1);
      } catch (InterruptedException ex) {
        Logger.getLogger(Eggos.class.getName()).log(Level.SEVERE, null, ex);
      }

      now = timer.getTime();
    }
  }

  private WolfState input() {
    if (keyDown[GLFW_KEY_Q]) {
      return WolfState.TOP_LEFT;
    }

    if (keyDown[GLFW_KEY_P]) {
      return WolfState.TOP_RIGHT;
    }

    if (keyDown[GLFW_KEY_A]) {
      return WolfState.BOTTOM_LEFT;
    }

    if (keyDown[GLFW_KEY_L]) {
      return WolfState.BOTTOM_RIGHT;
    }

    return wolfState;
  }

  private void endSim(boolean shouldEggUpdate, GameState currentState) {
    if (!shouldEggUpdate) return;
      List<Map.Entry<Rail, Integer>> endTicks = eggTicks.entrySet().stream().filter(entry -> entry.getValue() == 6)
          .collect(Collectors.toList());

      List<Egg> eggsToRemove = assetService.getEggsShowing().entrySet().stream().flatMap(entry -> entry.getValue().stream()).filter(egg -> egg.getTick() == 6)
          .collect(Collectors.toList());

      if (eggsToRemove.size() > 1) {
        throw new RuntimeException("Invalid state, to much eggs to remove");
      }

      this.currentState = Optional.of(eggsToRemove).filter(eggs -> eggs.size() == 1).map(eggs -> eggs.get(0)).map(egg -> {
        return Optional.ofNullable(assetService.getEggsShowing().get(egg.getRail()).poll()).map(eggToRemove -> {
          Rail rail = eggToRemove.getRail();
          eggToRemove.setRail(null);
          assetService.getEggs().get(rail).offer(eggToRemove);
          eggsOnScreen.put(rail, eggsOnScreen.get(rail) - 1);
          Rail randomRail = Rail.getRail(random.nextInt(4));
          while (randomRail.equals(railAdded)) {
            randomRail = Rail.getRail(random.nextInt(4));
          }
          eggsOnScreen.put(randomRail, eggsOnScreen.get(randomRail) + 1);
          eggsInTheBasket = eggsInTheBasket + 1;
          return !rail.name().equals(wolfState.name()) ? GameState.EGG_CRASH : currentState;
        }).orElse(currentState);
      }).orElse(currentState);

      endTicks.forEach(rail -> {
        eggTicks.put(rail.getKey(), 0);
      });

    upsTracking = 0;
    difference.set(1.0f, 1.0f);
    eggddP.set(0.0f, 0.0f);
    railAdded = null;
  }

  private void beginSim(boolean shouldEggUpdate, GameState currentState) {
    if (currentState.equals(GameState.EGG_CRASH) && crashChickenRight.getTick() == 0) {
      System.out.println("CRASHING");
      assetService.getEggsShowing().entrySet().forEach(a -> {
        Optional.ofNullable(a.getValue().poll()).ifPresent(egg -> {
          assetService.getEggs().get(a.getKey()).offer(egg);
        });
      });
      eggsOnScreen.entrySet().forEach(a -> {
        a.setValue(0);
      });
    }
    if (!shouldEggUpdate || currentState.equals(GameState.EGG_CRASH)) return;
      final int allEggsShowing = assetService.getEggsShowing().values().stream().mapToInt(ConcurrentLinkedQueue::size).sum();

      if (allEggTicks > 18 && allEggTicks % 13 == 0 && allEggTicks < 64) {
        Rail railToAdd = Rail.getRail(random.nextInt(4));
        eggsOnScreen.put(railToAdd, eggsOnScreen.get(railToAdd) + 1);
        railAdded = railToAdd;
      }

      List<Map.Entry<Rail, Integer>> endTicks = eggTicks.entrySet().stream().filter(entry -> entry.getValue() == 5)
          .collect(Collectors.toList());

      List<Map.Entry<Rail, Integer>> zeroTicks = eggTicks.entrySet().stream().filter(entry -> entry.getValue() == 0)
          .collect(Collectors.toList());

      List<Map.Entry<Rail, Integer>> standardTicks = eggTicks.entrySet().stream().filter(entry -> entry.getValue() != 0 && entry.getValue() != 5)
          .collect(Collectors.toList());

      final int allEggsOnScreen = eggsOnScreen.values().stream().mapToInt(Integer::intValue).sum();
      if (allEggsShowing == 0 || allEggsShowing < allEggsOnScreen) {
        eggsOnScreen.forEach((railToAdd, value) -> {
          if (value > assetService.getEggsShowing().get(railToAdd).size()) {
            Egg eggToAdd = assetService.getEggs().get(railToAdd).poll();
            Optional.ofNullable(eggToAdd).ifPresent(egg -> {
              egg.setInitialPosition(railToAdd, initialPosition.get().apply(coopLeft), initialPosition.get().apply(coopRight));
              egg.setTick(0);
              egg.setRail(railToAdd);
              assetService.getEggsShowing().get(railToAdd).offer(egg);
            });
          }
        });
      }

      standardTicks.forEach(entry -> {
        if (assetService.getEggsShowing().get(entry.getKey()).size() > 0) {
          eggTicks.put(entry.getKey(), entry.getValue() + 1);
        }
      });

      zeroTicks.forEach(entry -> {
        if (assetService.getEggsShowing().get(entry.getKey()).size() > 0) {
          eggTicks.put(entry.getKey(), entry.getValue() + 1);
        }
      });

      endTicks.forEach(entry -> {
        if (assetService.getEggsShowing().get(entry.getKey()).size() > 0) {
          eggTicks.put(entry.getKey(), entry.getValue() + 1);
        }
    });
  }

  private void update(float fps, boolean shouldEggUpdate, GameState currentState) {
    Optional.ofNullable(wolf.getScaledSize()).ifPresent(scaledSize -> {
      wolf.setPosition(wolfPositionCalculator.apply(scaledSize));
    });
    if (!shouldEggUpdate) return;
    if (!currentState.equals(GameState.EGG_CRASH)) {
      assetService.getEggsShowing().entrySet().forEach(entry -> {
        for (Egg egg : entry.getValue()) {
          if (egg.getTick() != 0) {
            egg.initNextTexture();
            final var ddp = new Vector2f(eggddP).add(new Vector2f(mainEggLengthDt)).mul(eggsDdp.get(entry.getKey()));
            egg.setPosition(new Vector2f(egg.getPosition().x * difference.x, egg.getPosition().y * difference.y).add(ddp.x, ddp.y));
          }
          egg.updateTick();
        }
      });
      allEggTicks = allEggTicks + 1;
    }

      if (currentState.equals(GameState.EGG_CRASH)) {
        if (crashChickenRight.getTick() != 0) {
          crashChickenRight.initNextTexture();
          crashChickenRight.updateTick();
        }
      }
  }

  private void screenChangedEvent() {
    this.assetService.getEggsShowing().entrySet().stream().flatMap(entrySet -> {
      return entrySet.getValue().stream();
    }).forEach(egg -> egg.screenChangedEvent(difference));
    this.assetService.getEggs().entrySet().stream().flatMap(entrySet -> {
      return entrySet.getValue().stream();
    }).forEach(egg -> egg.screenChangedEvent(difference));
    this.wolf.screenChangedEvent(difference, null);
    this.coopRight.screenChangedEvent(difference, (scaledSize) -> {
      return new Vector2f(Eggos.WIDTH - scaledSize.x, Eggos.HEIGHT - scaledSize.y);
    });
    this.coopLeft.screenChangedEvent(difference, null);
    this.crashChickenRight.screenChangedEvent(difference, null);
    propRailWidth = propRailWidthNotScaled * (coopRight.getScaledSize().x / coopRight.getSize().x);
    mainEggLengthDt =  new Vector2f(1.0f * propRailWidth * 0.22f, 1.0f * propRailWidth * 0.22f);
  }

  public static void main(String[] args) {
    try {
      new Eggos().run();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
