package com.robotzero.entity;

import com.robotzero.Eggos;
import com.robotzero.assets.Asset;
import com.robotzero.shader.Color;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Egg {
  private final Vector4f position = new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);
  private Rail rail;
  private int tick = 0;
  public static final Color defaultColor = new Color(1.0f, 1.0f, 1.0f);
  private Vector2f Size;
  private float texturePerHeight;
  private float scaleFactor;
  private final float EGGHEIGHTFACTOR = 24f;
  private final float initialPositionLeftNotScaled = 220f;
  private final ConcurrentLinkedQueue<Asset> assets = new ConcurrentLinkedQueue<>();
  private Vector2f Scale;
  private Vector2f scaledSize;
  private Vector2f middle;


  public Egg(Rail rail) {
    this.rail = rail;
  }

  public void addAssets(Asset ...asset) {
    for (Asset value : asset) {
      assets.offer(value);
    }
    Optional.ofNullable(assets.peek()).ifPresent(asset1 -> {
      initAsset(asset1);
      this.setInitialPosition(rail, new Vector2f(1.0f, 1.0f), new Vector2f(1.0f, 1.0f));
    });
  }

  public void initNextTexture() {
    Asset asset = Optional.ofNullable(assets.poll()).orElseThrow(() -> new RuntimeException("No asset"));
      initAsset(asset);
      assets.offer(asset);
  }

  private void initAsset(Asset asset) {
    this.Size = new Vector2f(asset.getWidth(), asset.getHeight());
    this.texturePerHeight = Eggos.HEIGHT / this.Size.y;
    this.scaleFactor = texturePerHeight / EGGHEIGHTFACTOR;
    this.Scale = new Vector2f(scaleFactor, scaleFactor);
    this.scaledSize = Scale.mul(Size);
    this.middle = new Vector2f(scaledSize.x() / 2, scaledSize.y() / 2);
  }

  public Asset getAsset() {
    return assets.peek();
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

  public Vector2f getMiddle() {
    return this.middle;
  }

  public Vector2f getScaledSize() {
    return this.scaledSize;
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

  public void setInitialPosition(Rail rail, Vector2f coopLeftRatio, Vector2f coopRightRatio) {
    this.rail = rail;
    if (rail.equals(Rail.TOP_LEFT)) {
      setPosition(new Vector2f(initialPositionLeftNotScaled * coopLeftRatio.x, Eggos.HEIGHT * 0.68f).sub(getMiddle()));
    }

    if (rail.equals(Rail.BOTTOM_LEFT)) {
      setPosition(new Vector2f(initialPositionLeftNotScaled * coopLeftRatio.x, Eggos.HEIGHT * 0.42f).sub(getMiddle()));
    }

    if (rail.equals(Rail.TOP_RIGHT)) {
      setPosition(new Vector2f(Eggos.WIDTH - initialPositionLeftNotScaled * coopRightRatio.x, Eggos.HEIGHT * 0.65f).sub(getMiddle()));
    }

    if (rail.equals(Rail.BOTTOM_RIGHT)) {
      setPosition(new Vector2f(Eggos.WIDTH - initialPositionLeftNotScaled * coopRightRatio.x, Eggos.HEIGHT * 0.40f).sub(getMiddle()));
    }
  }

  public void screenChangedEvent(Vector2f difference) {
    this.texturePerHeight = Eggos.HEIGHT / this.Size.y;
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
