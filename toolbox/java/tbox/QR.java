package tbox;

import java.awt.*;
import java.awt.image.*;
import java.util.*;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.*;
import com.google.zxing.qrcode.*;
import com.google.zxing.qrcode.decoder.*;

public class QR
{
  public static BufferedImage encode( String challenge, int size )
  throws Exception
  {
    QRCodeWriter writer = new QRCodeWriter();

    Hashtable<EncodeHintType, ErrorCorrectionLevel> hints =
      new Hashtable<EncodeHintType, ErrorCorrectionLevel>();

    hints.put( EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M );

    BitMatrix matrix =
      writer.encode( challenge , BarcodeFormat.QR_CODE, size, size, hints );

    BufferedImage image =
      new BufferedImage( size, size, BufferedImage.TYPE_INT_RGB );
    image.createGraphics();

    Graphics2D graphics = (Graphics2D) image.getGraphics();
    graphics.setColor( Color.WHITE );
    graphics.fillRect( 0, 0, size, size );
    graphics.setColor(Color.BLACK);

    for (int ii = 0; ii < size; ii++)
    {
      for (int jj = 0; jj < size; jj++)
        if (matrix.get(ii, jj))
          graphics.fillRect( ii, jj, 1, 1 );
    }
    return image;
  }

  public static String decode( BufferedImage img ) throws Exception
  {
    if (null == img) return null;

/*
    Map<DecodeHintType,Object> decHints =
      new EnumMap<DecodeHintType, Object>(DecodeHintType.class);

    decHints.put( DecodeHintType.TRY_HARDER, Boolean.TRUE );
    decHints.put( DecodeHintType.POSSIBLE_FORMATS,
                  EnumSet.allOf(BarcodeFormat.class) );
    decHints.put( DecodeHintType.PURE_BARCODE, Boolean.TRUE );
*/
		Result qrCodeResult = null;

    try
    {
      QRCodeReader rdr = new QRCodeReader();

		  BinaryBitmap bmap = new BinaryBitmap(
        new HybridBinarizer( new BufferedImageLuminanceSource(img) ) );

      qrCodeResult = rdr.decode( bmap );
                   //new MultiFormatReader().decode( bmap, decHints );
    }
    catch( com.google.zxing.NotFoundException nfe )
    {
      return null; // normal, no barcode found in image
    }
    catch( Exception e )
    {
      //System.out.println( "QR.decode oops: " + e.getMessage() );
      return null;
    }

		return qrCodeResult.getText();
	}

  public static void main( String[] args ) throws Exception
  {
    String chall = "BJ1c4OJqTQqPTSIp+F6XNBtc9tE7o1cMyztdQI2FPS84fdMEXEfRQ7T" +
                   "cvUXZJN9gWYQTm38WjpXO9JsNrcemlgk=";

    BufferedImage bi = encode( chall, 390 );

    String decoded = decode( bi );

    if (null != decoded && chall.equals(decoded))
      System.out.println( "QR.main: PASS" );
    else
      System.out.println( "QR.main: FAIL" );

    try
    {
      bi = javax.imageio.ImageIO.read( new java.io.File("kmresp.png") );
      decoded = decode( bi );
      System.out.println( "read: " + decoded );
    }
    catch (java.io.IOException e)
    {
      System.out.println( "FAIL" );
    }
  }
}
