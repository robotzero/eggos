package com.robotzero.entity;

import com.robotzero.Eggos;
import com.robotzero.assets.Asset;
import com.robotzero.shader.Texture;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Optional;

public class Egg {
  private final Asset asset;
  private final Vector4f position;
  private Vector2f dP;
  private static final float EGGHEIGHTFACTOR = 24f;
  private Vector2f Size = new Vector2f(16, 16);
  private float texturePerHeight = Eggos.HEIGHT / (float) 16;
  private float scaleFactor = texturePerHeight / EGGHEIGHTFACTOR;
  private Vector2f Scale = new Vector2f(scaleFactor, scaleFactor);
  private Vector2f scaledSize = Scale.mul(Size);
  private Vector2f middle = new Vector2f(scaledSize.x() / 2, scaledSize.y() / 2);

  public Egg(Asset asset, Vector4f initialPosition) {
    this.asset = asset;
    this.position =  Optional.ofNullable(initialPosition).orElse(new Vector4f(0f, 0f, 0f, 0f));
    this.dP = new Vector2f(0f, 0f);
  }

  public void setPosition(Vector2f newPosition) {
    this.position.x = newPosition.x;
    this.position.y = newPosition.y;
    this.position.z = this.position.x + this.scaledSize.x;
    this.position.w = this.position.y + this.scaledSize.y;
  }

  public void setPosition(Vector4f newPosition) {
    this.position.x = newPosition.x;
    this.position.y = newPosition.y;
    this.position.z = newPosition.z;
    this.position.w = newPosition.w;
  }

  public Vector4f getPosition() {
    return this.position;
  }

  public Texture getTexture() {
    return this.asset.getTexture();
  }

  public void setScaledSize(Vector2f scaledSize) {
    this.scaledSize = scaledSize;
  }

  public Vector2f getdP() {
    return this.dP;
  }

  public void setdP(Vector2f dP) {
    this.dP = dP;
  }

  public Vector2f getMiddle() {
    return this.middle;
  }

  public Vector2f getScaledSize() {
    return this.scaledSize;
  }

  public void screenChangedEvent() {

  }
}
