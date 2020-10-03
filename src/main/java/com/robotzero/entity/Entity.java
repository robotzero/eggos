package com.robotzero.entity;

import com.robotzero.Eggos;
import com.robotzero.assets.Asset;
import com.robotzero.shader.Color;
import org.joml.Vector2f;

import java.util.function.Function;

public class Entity {
  private Asset asset;
  private Vector2f position;
  private final float heightFactor;
  private Vector2f Size;
  private float texturePerHeight;
  private float scaleFactor;
  private Vector2f Scale;
  private Vector2f scaledSize;
  private Vector2f middle;
  public static final Color defaultColor = new Color(1.0f, 1.0f, 1.0f);

  public Entity(final float heightFactor, final Asset asset, final Function<Vector2f, Vector2f> positionCalculator) {
    this.heightFactor = heightFactor;
    this.asset = asset;
    while(asset.getTexture() == null) {}
    setAsset(asset, positionCalculator);
  }

  public void setAsset(final Asset asset, final Function<Vector2f, Vector2f> positionCalculator) {
    this.asset = asset;
    this.Size = new Vector2f(asset.getTexture().getWidth(), asset.getTexture().getHeight());
    texturePerHeight = Eggos.HEIGHT / (float) asset.getTexture().getHeight();
    scaleFactor = texturePerHeight / heightFactor;
    Scale = new Vector2f(scaleFactor, scaleFactor);
    scaledSize = Scale.mul(Size);
    middle = new Vector2f(scaledSize.x() / 2, scaledSize.y() / 2);
    position = positionCalculator.apply(scaledSize);
  }

  public void screenChangedEvent(Vector2f difference, Function<Vector2f, Vector2f> customCalculation) {
    this.Size = new Vector2f(asset.getTexture().getWidth(), asset.getTexture().getHeight());
    this.texturePerHeight = Eggos.HEIGHT / (float) asset.getTexture().getHeight();
    this.scaleFactor = texturePerHeight / heightFactor;
    this.Scale = new Vector2f(scaleFactor, scaleFactor);
    this.scaledSize = this.Scale.mul(this.Size);
    this.middle = new Vector2f(this.scaledSize.x() / 2, this.scaledSize.y() / 2);
    if (customCalculation != null) {
      setPosition(customCalculation.apply(scaledSize));
    } else {
      setPosition(this.position.mul(difference.x, difference.y));
    }
  }

  public void setPosition(Vector2f position) {
    this.position.x = position.x;
    this.position.y = position.y;
  }

  public Vector2f getPosition() {
    return this.position;
  }

  public Vector2f getScaledSize() {
    return this.scaledSize;
  }

  public Vector2f getSize() {
    return this.Size;
  }
}
