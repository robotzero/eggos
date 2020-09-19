package com.robotzero;

import com.robotzero.assets.AssetService;
import com.robotzero.entity.Egg;
import com.robotzero.entity.Fred;
import com.robotzero.entity.Rail;
import com.robotzero.render.opengl.Renderer2D;
import com.robotzero.shader.Texture;
import com.robotzero.utils.FredState;
import com.robotzero.utils.Timer;
import org.joml.AxisAngle4f;
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

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
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
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Eggos {

  // The window handle
  public static long window;
  public static int WIDTH = 720;
  public static int HEIGHT = 360;
  public static final int TARGET_UPS = 60;
  public static final int TARGET_FPS = 60;
  public static Vector2f eggddP = new Vector2f(0.0f, 0.0f);
  public static Vector2i screenMiddle = new Vector2i(WIDTH / 2, HEIGHT / 2);
  public static int eggSpeed = 1;
  private FredState fredState = FredState.RIGHT;
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
  private float railWidth = WIDTH * 0.15f;
  private float railHeight = HEIGHT * 0.15f;
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
  private final Matrix4f identity = new Matrix4f();
  private float propRailWidth = (float) Math.sqrt((railWidth * railWidth) + (railHeight * railHeight));
  private Fred fred;

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
          railWidth = WIDTH * 0.15f;
          railHeight = HEIGHT * 0.15f;
          propRailWidth = (float) Math.sqrt((railWidth * railWidth) + (railHeight * railHeight));
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
          railWidth = WIDTH * 0.15f;
          railHeight = HEIGHT * 0.15f;
          propRailWidth = (float) Math.sqrt((railWidth * railWidth) + (railHeight * railHeight));
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
    fred = new Fred();
    lastFps = timer.getTime();
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

    timer.init();
    renderer2D.init();

    // Set the clear color
    renderer2D.clearColor();

    // Run the rendering loop until the user has attempted to close
    // the window or has pressed the ESCAPE key.
    while (!glfwWindowShouldClose(window)) {
      delta = timer.getDelta();
      accumulator += delta;

      fredState = input();
      update(timer.getDelta(), 1f / TARGET_FPS);

      timer.updateUPS();
      accumulator -= interval;

      if (timer.getLastLoopTime() - lastFps > 1) {
        lastFps = timer.getLastLoopTime();
      }

      renderer2D.clear();
      Optional<Texture> currentTexture = assetService.bind("fred_01.png");
      currentTexture.ifPresent(texture -> {
        if (fred.getScaledSize() == null) {
          fred.setAsset(assetService.getFred());
        }
        renderer2D.begin();
        renderer2D.setUniform(new Matrix4f().translate(fred.getPosition().x, fred.getPosition().y, 0.0f));
        if (fredState == FredState.RIGHT) {
          renderer2D.drawTextureRegion(0.0f, 0.0f, fred.getScaledSize().x, fred.getScaledSize().y, 0, 0, 1, 1, Fred.defaultColor);
        }

        if (fredState == FredState.LEFT) {
          renderer2D.drawTextureRegion(fred.getScaledSize().x, 0, 0, fred.getScaledSize().y, 0, 0, 1, 1, Fred.defaultColor);

        }
        renderer2D.end();
        assetService.unbind(texture);
      });

      Optional<Texture> testTexture = assetService.bind("test_01.png");
      testTexture.ifPresent(texture -> {
        renderer2D.begin();
        assetService.getEggsShowing().forEach((key, value) -> {
          for (Egg egg : value) {
            renderer2D.setUniform(new Matrix4f().translate(egg.getPosition().x, egg.getPosition().y, 0.0f).translate(egg.getMiddle().x, egg.getMiddle().y, 0f)
                .rotate(new AxisAngle4f((float) Math.toRadians(egg.getRotation()), 0f, 0f, -1).normalize())
                .translate(-egg.getMiddle().x, -egg.getMiddle().y, 0f));
            renderer2D.drawTextureRegion(0, 0, egg.getScaledSize().x, egg.getScaledSize().y, 0, 0, 1, 1, Egg.defaultColor);
          }
        });
        renderer2D.end();
        assetService.unbind(texture);
      });

      renderer2D.setUniform(identity);
      assetService.getDebugFont().ifPresent(font -> {
        font.drawText(renderer2D, "FPS: " + timer.getFPS() + " | UPS: " + timer.getUPS(), 5, HEIGHT - 20);
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

  private FredState input() {
    if (keyDown[GLFW_KEY_A]) {
      return FredState.LEFT;
    }

    if (keyDown[GLFW_KEY_D]) {
      return FredState.RIGHT;
    }

    return fredState;
  }

  private void update(float dt, float fps) {
    upsTracking = upsTracking + fps;
    if (upsTracking >= eggSpeed) {
      eggddP.add(new Vector2f(1.0f * propRailWidth * 0.25f, -(1.0f * propRailWidth * 0.25f)));

      eggTicks.entrySet().stream().filter(entry -> entry.getValue() == 4).forEach(entry -> {
        if (assetService.getEggsShowing().get(entry.getKey()).size() == 0 || assetService.getEggsShowing().get(entry.getKey()).size() < eggsOnScreen.get(entry.getKey())) {
          Optional.ofNullable(assetService.getEggs().get(entry.getKey()).poll()).ifPresent(currentEgg -> {
            currentEgg.setShowing(true);
            assetService.getEggsShowing().get(entry.getKey()).offer(currentEgg);
            eggsOnScreen.put(entry.getKey(), 1);
          });
        } else {
          assetService.getEggsShowing().forEach((key, value) -> Optional.ofNullable(assetService.getEggsShowing().get(key).peek()).ifPresent(egg -> {
            Optional.ofNullable(assetService.getEggsShowing().get(key).poll()).ifPresent(eggToRemove -> {
              eggToRemove.setShowing(false);
              eggToRemove.setInitialPosition(key);
              eggToRemove.setRotation(0);
              assetService.getEggs().get(key).offer(eggToRemove);
              eggsOnScreen.put(key, eggsOnScreen.get(key) - 1);
            });
          }));
        }

        eggTicks.put(entry.getKey(), 0);
      });

      eggTicks.entrySet().stream().filter(entry -> entry.getValue() < 4).forEach(entry -> {
        if (assetService.getEggsShowing().get(entry.getKey()).size() > 0) {
          eggTicks.put(entry.getKey(), eggTicks.get(entry.getKey()) + 1);
        }
      });


      assetService.getEggsShowing().entrySet().parallelStream().forEach(entry -> {
        for (Egg egg : entry.getValue()) {
          egg.setPosition(new Vector2f(egg.getPosition().x * difference.x, egg.getPosition().y * difference.y).add(new Vector2f(eggddP.x, eggddP.y)));
          egg.setRotation(45);
        }
      });
      upsTracking = 0;
    }

    difference.set(1.0f, 1.0f);
    eggddP.set(0.0f, 0.0f);
    // Begin simulation
  }

  private void screenChangedEvent() {
    this.assetService.getEggsShowing().entrySet().stream().flatMap(entrySet -> {
      return entrySet.getValue().stream();
    }).forEach(egg -> egg.screenChangedEvent(difference));
    this.assetService.getEggs().entrySet().stream().flatMap(entrySet -> {
      return entrySet.getValue().stream();
    }).forEach(egg -> egg.screenChangedEvent(difference));
    this.fred.screenChangedEvent(difference);
  }

  public static void main(String[] args) {
    try {
      new Eggos().run();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
