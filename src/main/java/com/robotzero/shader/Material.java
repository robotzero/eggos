package com.robotzero.shader;

import org.joml.Vector4f;

public class Material {
    private static final Vector4f DEFAULT_COLOUR = new Vector4f(0.5f, 0.5f, 0.5f, 1.0f);
    private final Vector4f color;

    public Material(Vector4f color) {
        this.color = color;
    }

    public Material() {
        this.color = DEFAULT_COLOUR;
    }

    public Vector4f getColor() {
        return this.color;
    }
}
