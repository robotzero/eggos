package com.robotzero;

import com.robotzero.assets.AssetService;
import com.robotzero.render.opengl.Renderer2D;
import com.robotzero.shader.Color;
import com.robotzero.shader.Texture;
import com.robotzero.utils.Timer;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_DEBUG_CONTEXT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
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
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class HelloWorld {

  // The window handle
  public static long window;
  public static int WIDTH = 720;
  public static int HEIGHT = 360;
  public static final int TARGET_UPS = 60;
  public static final int TARGET_FPS = 60;
  private Renderer2D renderer2D;
  private AssetService assetService;
  private ExecutorService executorService;
  private GLFWFramebufferSizeCallback fbCallback;
  private GLFWWindowSizeCallback wsCallback;
  private Timer timer;
  private double lastFps;
  private int fps;
  private boolean[] keyDown = new boolean[GLFW.GLFW_KEY_LAST];
  private static String TITLE = "Eggos";
  private GLFWErrorCallback glwErrorCallback;

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
    if ( !glfwInit() )
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

    if ( window == NULL ) {
      throw new RuntimeException("Failed to create the GLFW window");
    }
    // Setup a key callback. It will be called every time a key is pressed, repeated or released.
    glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
      if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE ) {
        glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
      }

      if (key == GLFW_KEY_UNKNOWN) {
        return;
      }

      keyDown[key] = action == GLFW_PRESS || action == GLFW_REPEAT;
    });

    glfwSetFramebufferSizeCallback(window, fbCallback = new GLFWFramebufferSizeCallback() {
      public void invoke(long window, int width, int height) {
        if (width > 0 && height > 0 && (WIDTH != width || HEIGHT != height)) {
          WIDTH = width;
          HEIGHT = height;
        }
      }
    });

    glfwSetWindowSizeCallback(window, wsCallback = new GLFWWindowSizeCallback() {
      public void invoke(long window, int width, int height) {
        if (width > 0 && height > 0 && (WIDTH != width || HEIGHT != height)) {
          WIDTH = width;
          HEIGHT = height;
        }
      }
    });

    // Get the thread stack and push a new frame
    try ( MemoryStack stack = stackPush() ) {
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
    lastFps = timer.getTime();
    fps = 0;
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
    timer.init();
    renderer2D.init();

    // Set the clear color
    renderer2D.clearColor();

    // Run the rendering loop until the user has attempted to close
    // the window or has pressed the ESCAPE key.
    while ( !glfwWindowShouldClose(window) ) {
      delta = timer.getDelta();
      accumulator += delta;

      input();
      //update(1f / TARGET_UPS);
      timer.updateUPS();
      accumulator -= interval;

      if ( timer.getLastLoopTime() - lastFps > 1 ) {
        lastFps = timer.getLastLoopTime();
        fps = 0;
      }

      fps++;
      renderer2D.clear();
      Texture currentTexture = assetService.bind("fred_01.png");
      renderer2D.begin();
      renderer2D.drawTextureRegion(0, 0, 30, 30, 0, 0, 1, 1, new Color(1f, 1f, 1f));
      renderer2D.end();
      renderer2D.begin();
      renderer2D.drawTextureRegion((int) (WIDTH / 2), (int) (HEIGHT / 2), (int) (WIDTH / 2) + 10, (int) (HEIGHT / 2) + 10, 0, 0, 1, 1, new Color(1f, 1f, 1f));
      renderer2D.end();
      assetService.unbind(currentTexture);
      renderer2D.drawDebugText("FPS: " + timer.getFPS() + " | UPS: " + timer.getUPS(), 5, HEIGHT - 20);
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
        Logger.getLogger(HelloWorld.class.getName()).log(Level.SEVERE, null, ex);
      }

      now = timer.getTime();
    }
  }

  private void input() {
    if (keyDown[GLFW_KEY_W]) {

    }
  }

  public static void main(String[] args) {
    try {
      new HelloWorld().run();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
