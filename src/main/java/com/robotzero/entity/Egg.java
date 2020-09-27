package com.robotzero.entity;

import com.robotzero.Eggos;
import com.robotzero.assets.Asset;
import com.robotzero.shader.Color;
import com.robotzero.shader.Texture;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class Egg {
  private final Asset asset;
  private final Vector4f position = new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);
  private Rail rail;
  private Vector2f dP;
  private static final float EGGHEIGHTFACTOR = 24f;
  private Vector2f Size = new Vector2f(16, 16);
  private float texturePerHeight = Eggos.HEIGHT / (float) 16;
  private float scaleFactor = texturePerHeight / EGGHEIGHTFACTOR;
  private Vector2f Scale = new Vector2f(scaleFactor, scaleFactor);
  private Vector2f scaledSize = Scale.mul(Size);
  private Vector2f middle = new Vector2f(scaledSize.x() / 2, scaledSize.y() / 2);
  private boolean isShowing = false;
  private int tick = 0;
  private int rotation = 0;
  public static final Color defaultColor = new Color(1.0f, 1.0f, 1.0f);

  public Egg(Asset asset, Rail rail) {
    this.asset = asset;
    this.dP = new Vector2f(0f, 0f);
    this.setInitialPosition(rail, new Vector2f(0.0f, 0.0f));
    this.rail = rail;
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

  public void setRotation(int rotation) {
    if (this.rotation == 360 || rotation == 0) {
      this.rotation = rotation;
    } else {
      this.rotation = this.rotation + rotation;
    }
  }

  public int getRotation() {
    return this.rotation;
  }

  public boolean isShowing() {
    return isShowing;
  }

  public void setShowing(boolean showing) {
    isShowing = showing;
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

  public void setInitialPosition(Rail rail, Vector2f offset) {
    this.rail = rail;
    if (rail.equals(Rail.TOP_LEFT)) {
      setPosition(new Vector2f(Eggos.screenMiddle).mul(new Vector2f(0.15f, 1.5f)).sub(getMiddle()).sub(offset));
    }

    if (rail.equals(Rail.BOTTOM_LEFT)) {
      setPosition(new Vector2f(Eggos.screenMiddle).mul(new Vector2f(0.15f, 0.95f)).sub(getMiddle()).sub(offset));
    }

    if (rail.equals(Rail.TOP_RIGHT)) {
      setPosition(new Vector2f(Eggos.screenMiddle).mul(new Vector2f(1.85f, 1.5f)).sub(getMiddle()).sub(offset));
    }

    if (rail.equals(Rail.BOTTOM_RIGHT)) {
      setPosition(new Vector2f(Eggos.screenMiddle).mul(new Vector2f(1.85f, 0.95f)).sub(getMiddle()).sub(offset));
    }
  }

  public void screenChangedEvent(Vector2f difference) {
    this.texturePerHeight = Eggos.HEIGHT / (float) 16;
    this.scaleFactor = texturePerHeight / EGGHEIGHTFACTOR;
    this.Scale = new Vector2f(scaleFactor, scaleFactor);
    this.scaledSize = this.Scale.mul(this.Size);
    this.middle = new Vector2f(this.scaledSize.x() / 2, this.scaledSize.y() / 2);
    setPosition(this.position.mul(new Vector4f(difference.x, difference.y, 1.0f, 1.0f)));
  }

  public Rail getRail() {
    return rail;
  }

  public void setRail(Rail rail) {
    this.rail = rail;
  }
}
