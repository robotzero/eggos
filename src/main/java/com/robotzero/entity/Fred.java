package com.robotzero.entity;

import com.robotzero.Eggos;
import com.robotzero.assets.Asset;
import com.robotzero.shader.Color;
import org.joml.Vector2f;

public class Fred {
  private Asset asset;
  private Vector2f position;
  private static final float FREDHEIGHTFACTOR = 2.8f;
  private Vector2f Size;
  private float texturePerHeight;
  private float scaleFactor;
  private Vector2f Scale;
  private Vector2f scaledSize;
  private Vector2f middle;
  public static final Color defaultColor = new Color(1.0f, 1.0f, 1.0f);


  public void setAsset(final Asset asset) {
    this.asset = asset;
    this.Size = new Vector2f(asset.getTexture().getSize());
    texturePerHeight = Eggos.HEIGHT / (float) asset.getTexture().getHeight();
    scaleFactor = texturePerHeight / FREDHEIGHTFACTOR;
    Scale = new Vector2f(scaleFactor, scaleFactor);
    scaledSize = Scale.mul(Size);
    middle = new Vector2f(scaledSize.x() / 2, scaledSize.y() / 2);
    position = new Vector2f(Eggos.WIDTH / 2f, Eggos.HEIGHT / 2f).sub(middle);
  }

  public void screenChangedEvent(Vector2f difference) {
    this.texturePerHeight = Eggos.HEIGHT / (float) asset.getTexture().getHeight();
    this.scaleFactor = texturePerHeight / FREDHEIGHTFACTOR;
    this.Scale = new Vector2f(scaleFactor, scaleFactor);
    this.scaledSize = this.Scale.mul(this.Size);
    this.middle = new Vector2f(this.scaledSize.x() / 2, this.scaledSize.y() / 2);
    setPosition(this.position.mul(difference.x, difference.y));
  }

  private void setPosition(Vector2f position) {
    this.position.x = position.x;
    this.position.y = position.y;
  }

  public Vector2f getPosition() {
    return this.position;
  }

  public Vector2f getScaledSize() {
    return this.scaledSize;
  }
}
