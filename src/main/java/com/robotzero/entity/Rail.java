package com.robotzero.entity;

public enum Rail {
  TOP_LEFT(0), BOTTOM_LEFT(1), TOP_RIGHT(2), BOTTOM_RIGHT(3);

  private final int index;

  Rail(int index) {
    this.index = index;
  }

  public static Rail getRail(int index) {
    return switch (index) {
      case 0 -> TOP_LEFT;
      case 1 -> BOTTOM_LEFT;
      case 2 -> TOP_RIGHT;
      case 3 -> BOTTOM_RIGHT;
      default -> throw new RuntimeException("NOP");
    };
  }
}
