package javax.microedition.lcdui;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.util.DisplayMetrics;

public class Image {
  private final Bitmap bitmap;

  public Image(final Bitmap bitmap) {
    this.bitmap = bitmap;
  }

  public static Image createImage(final byte[] imageData, final int imageOffset,
      final int imageLength) {
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inDither = true;
    options.inPurgeable = true;
    options.inPreferredConfig = Config.RGB_565; // Probably not used as ARGB_8888 has already been set
    options.inDensity = DisplayMetrics.DENSITY_DEFAULT; // FIXME: must be dynamic
    final Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, imageOffset, imageLength, options);
    return new Image(bitmap);
  }

  public static Image createImage(final Image image, final int x, final int y, final int width,
      final int height, final int transform) {
    //TODO jaanus : transform
    return new Image(Bitmap.createBitmap(image.bitmap, x, y, width, height));
  }

  public static Image createImage(final int width, final int height) {
    return new Image(Bitmap.createBitmap(width, height, Config.ARGB_8888));
  }

  public static Image createImage(final String name) throws java.io.IOException {
    return createImage(Image.class.getResourceAsStream(name));
  }

  public static Image createImage(final InputStream stream) throws IOException {
    final Bitmap bitmap = BitmapFactory.decodeStream(stream);
    return new Image(bitmap);
  }
  
  public static Image createRGBImage(final int[] imageData, final int width, final int height, final boolean processAlpha){
      Config conf = null;
      if(processAlpha){
          conf = Bitmap.Config.ARGB_8888;
      }else{
          conf = Bitmap.Config.RGB_565;
      }
      final Bitmap tmp = Bitmap.createBitmap(imageData,width,height,conf);
      return new Image(tmp);
  }

  public Graphics getGraphics() {
    return new Graphics(new android.graphics.Canvas(bitmap));
  }

  public int getHeight() {
    return bitmap.getHeight();
  }

  public int getWidth() {
    return bitmap.getWidth();
  }

  public void getRGB(final int[] rgbData, final int offset, final int scanlength, final int x,
      final int y, final int width, final int height) {
    bitmap.getPixels(rgbData, offset, scanlength, x, y, width, height);
  }

  public Bitmap getBitmap() {
    return bitmap;
  }
}
