/*
 * Created on Oct 24, 2006
 */
package com.mgmaps.cache;

import java.util.Vector;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.nutiteq.components.MapPos;
import com.nutiteq.components.MapTile;
import com.nutiteq.maps.GeoMap;
import com.nutiteq.ui.ImageProcessor;

/**
 * Caches tiles uncompressed.
 */
public class ScreenCache {
  private MapTile[] tiles;
  private int size;
  private Image[] images;
  private boolean[] valid;
  //BattleTac code starts
  //Added by Krisztian Schaffer, 2010.02.26
  private ImageProcessor imageProcessor;
  private static ScreenCache instance;

  /**
   * Creates a new ScreenCache and returns it. The created instance is installed
   * as the current screen cache, the previous instance will be removed if any.
   * 
   * @param n
   *          maximum number of tiles stored in the created cache
   */
  public static ScreenCache createScreenCache(final int n) {
    ScreenCache newInstance = new ScreenCache(n);
    if (instance != null) {
      newInstance.imageProcessor = instance.imageProcessor;
    }
    return instance = newInstance;
  }

  /**
   * Returns the current ScreenCache. (null if no cache is created yet)
   */
  public static ScreenCache getInstance() {
    return instance;
  }

  //BattleTac code ends

  /**
   * Constructor for ScreenCache.
   * 
   * @param n
   *          maximum number of tiles stored
   */
  private ScreenCache(final int n) {//BattleTac code: Modified to private by Krisztian Schaffer, 2010.03.01
    resize(n);
  }
  
  public void reset() {
    for (int i = 0; i < images.length; i++) {
        if (valid[i]) {
//            android.util.Log.e("TEST", "reset() recycle everything " + tiles[i].getIDString());
            images[i].getBitmap().recycle();
            tiles[i] = null;
            valid[i] = false;
        }
    }
    resize(0);
  }


  /**
   * Resize the screen cache when switching full screen.
   * 
   * @param n
   *          new size (number of tiles)
   */
  public void resize(final int n) {
    final int minSize = Math.min(size, n);
    final boolean cond = minSize > 0;
    size = n;

    valid = new boolean[n];
    if (cond) {
      final boolean[] oldValid = valid;
      System.arraycopy(oldValid, 0, valid, 0, minSize);
    }

    images = new Image[n];
    if (cond) {
      final Image[] oldImages = images;
      System.arraycopy(oldImages, 0, images, 0, minSize);
    }

    tiles = new MapTile[n];
    if (cond) {
      final MapTile[] oldTiles = tiles;
      System.arraycopy(oldTiles, 0, tiles, 0, minSize);
    }
  }

  /**
   * Paint a tile
   * 
   * @param g
   *          graphics object
   * @param i
   *          tile number
   * @param centerCopy
   *          copy of the map center, used for synchronization
   */
  public void paint(final Graphics g, final int i, final MapPos centerCopy, final int screenCenterX, final int screenCenterY) {
    if (images[i] == null || images[i].getBitmap() == null
            || images[i].getBitmap().isRecycled()) {
        images[i] = null;
        valid[i] = false;
        return;
    }
    final int left = tiles[i].getX() - centerCopy.getX() + screenCenterX;
    final int top = tiles[i].getY() - centerCopy.getY() + screenCenterY;
    g.drawImage(images[i], left, top, Graphics.TOP | Graphics.LEFT);
  }

  /**
   * Find the position for a map tile.
   * 
   * @param t
   *          tile to search
   * @return -1 if not found
   */
  public int find(final MapTile t) {
    for (int i = 0; i < size; i++) {
      if (valid[i] && tiles[i].equals(t)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Add a tile to this cache.
   * 
   * @param t
   *          the tile to add
   * @param update
   */
  public int add(final MapTile t, final MapPos mp, final GeoMap displayedMap, final int screenCenterX, final int screenCenterY, final boolean update) {
    // if the tile is not (no longer) visible, return -1
    if (!t.isVisible(mp, displayedMap, screenCenterX, screenCenterY)) {
        t.getImage().getBitmap().recycle();
//        android.util.Log.e("TEST", "add() recycle not visible "+t.getIDString());
      return -1;
    }

    // sweep at every add
    final int pos = sweepFind(t, mp, displayedMap, screenCenterX, screenCenterY);
    if (pos >= 0 && !update) { // found? return
      return pos;
    }
    
    // LOW rewrite if it slows down the app too much (it's O(n))
    // find a place to add
    for (int i = 0; i < size; i++) {
      if (!valid[i] || t.equals(tiles[i])) {
          // Recycle old image at that position
        if (images[i] != null && images[i].getBitmap() != null
                && !images[i].getBitmap().isRecycled()) {
            images[i].getBitmap().recycle();
//            android.util.Log.e("TEST", "add() recycle old "+tiles[i].getIDString());
        }
        //BattleTac code starts
        //Modified by Krisztian Schaffer, 2010.02.26
        Image image = t.getImage();
        if (imageProcessor != null) {
          image = imageProcessor.processImage(image);
        }
        //BattleTac code ends
        images[i] = image;
        tiles[i] = t;
        valid[i] = true;
        return i;
      }
    }

    return -1;
  }

  /**
   * Remove unneeded tiles (invalidate them). Also search for a map tile.
   * 
   * @param t
   *          map tile to search for
   * @return the position of the map tile, or -1 if not found
   */
  private int sweepFind(final MapTile t, final MapPos mp, final GeoMap displayedMap, final int screenCenterX, final int screenCenterY) {
    int found = -1;
    for (int i = 0; i < size; i++) {
      if (!valid[i]) {
        continue;
      }
      // if found, do not remove it
      if (tiles[i].equals(t)) {
        found = i;
      } else if (!tiles[i].isVisible(mp, displayedMap, screenCenterX, screenCenterY)) {
        if (images[i] != null) {
            if(!t.getImage().equals(displayedMap.getMissingTileImage())){
                images[i].getBitmap().recycle();
            }
//            android.util.Log.e("TEST", "sweepfind() recycle found but not visible "+tiles[i].getIDString());
        }
        tiles[i] = null;
        images[i] = null;
        valid[i] = false;
      }
    }
    return found;
  }

  //BattleTac code starts
  //Added by Krisztian Schaffer, 2010.02.26
  /**
   * Sets the tile image processor. The given ImageProcessor will process every
   * tile which is added after this call. This method also clears the cache.
   * 
   * @param processor
   *          the new ImageProcessor to use, can be null to delete the current
   *          processor.
   */
  public void setImageProcessor(final ImageProcessor processor) {
    imageProcessor = processor;
  }
  //BattleTac code ends

  public Vector<MapTile> getTiles() {
    Vector<MapTile> tls = new Vector<MapTile>();
    for(int i=0; i < size; i++) {
      if( valid[i] && tiles[i] != null ) {
        tls.add(tiles[i]);
      }
    }
    return tls;
  }

  public void renewTileImages() {
    for(int i=0; i < size; i++) {
      if( valid[i] && tiles[i] != null ) {
        images[i] = tiles[i].getImage();
      }
    }
  }
}
