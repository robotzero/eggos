package com.robotzero.animation;

import com.robotzero.entity.Entity;
import com.robotzero.entity.GameState;
import org.joml.Vector2f;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Animation {
  private final Queue<Keyframe> keyFrames = new LinkedBlockingQueue<>();
  private int tick = 0;
  private final GameState gameState;

  public Animation(GameState gameState) {
    this.gameState = gameState;
  }

  @SafeVarargs
  public final void buildFrames(final List<Entity>... entities) {
    keyFrames.addAll(Arrays.stream(entities).map(Keyframe::new).collect(Collectors.toList()));
  }

  public void nextKeyframe() {
    Keyframe keyframe = keyFrames.poll();
    keyFrames.offer(keyframe);
  }

  public void screenChangedEvent(Vector2f difference, Function<Vector2f, Vector2f> customCalculation) {
    keyFrames.forEach(keyframe -> {
      keyframe.getEntities().forEach(entity -> {
        entity.screenChangedEvent(difference, customCalculation);
      });
    });
  }

  public Keyframe currentKeyframe(GameState gameState) {
    return gameState.equals(this.gameState) ? keyFrames.peek() : Keyframe.empty();
  }

  public int getTick() {
    return tick;
  }

  public void updateTick() {
    this.tick = this.tick + 1;
  }

  public void setTick(int tick) {
    this.tick = tick;
  }
}
