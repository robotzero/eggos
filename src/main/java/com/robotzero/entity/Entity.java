package com.robotzero.entity;

import com.robotzero.Eggos;
import com.robotzero.assets.Asset;
import com.robotzero.shader.Color;
import org.joml.Vector2f;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
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
  private final ConcurrentLinkedQueue<Asset> assets = new ConcurrentLinkedQueue<>();

  public Entity(final float heightFactor, final Function<Vector2f, Vector2f> positionCalculator, Asset ...assets) {
    this.heightFactor = heightFactor;
    while(assets[0].getTexture() == null) {}
    this.asset = assets[0];
    setAsset(asset, positionCalculator);
    setPosition(positionCalculator.apply(scaledSize));
    for (int i = 0; i < assets.length; i++) {
      while(assets[i] == null || assets[i].getTexture() == null) {};
      i++;
    }
    addAssets(assets);
  }

  public void addAssets(Asset ...asset) {
    for (Asset value : asset) {
      assets.offer(value);
    }
    Optional.ofNullable(assets.peek()).ifPresent(asset1 -> {
      initAsset(asset1);
    });
  }

  private void initAsset(Asset asset) {
    this.Size = new Vector2f(asset.getWidth(), asset.getHeight());
    this.texturePerHeight = Eggos.HEIGHT / this.Size.y;
    this.scaleFactor = texturePerHeight / heightFactor;
    this.Scale = new Vector2f(scaleFactor, scaleFactor);
    this.scaledSize = Scale.mul(Size);
    this.middle = new Vector2f(scaledSize.x() / 2, scaledSize.y() / 2);
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

  public Asset getAsset() {
    return assets.peek();
  }
}
