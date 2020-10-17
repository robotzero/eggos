package com.robotzero.animation;

import com.robotzero.entity.Entity;

import java.util.List;
import java.util.stream.Stream;

public class Keyframe {
  private final List<Entity> entities;
  private static final Keyframe empty = new Keyframe(List.of());

  public Keyframe(final List<Entity> entities) {
    this.entities = entities;
  }

  public Stream<Entity> getEntities() {
    return entities.stream();
  }

  public static Keyframe empty() {
    return empty;
  }
}
