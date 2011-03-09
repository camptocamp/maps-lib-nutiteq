package com.nutiteq.controls;

import javax.microedition.lcdui.Graphics;


public interface OnScreenZoomControls {

  /**
   * Not part of public API
   * 
   * @param g
   * @param displayWidth
   * @param displayHeight
   */
  public void paint(final Graphics g, final int displayWidth, final int displayHeight);

  /**
   * Not part of public API
   * 
   * @param x
   * @param y
   * @return action code
   */
  public int getControlAction(final int x, final int y);
}
