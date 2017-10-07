package gungagui;

import java.awt.*;
import java.awt.image.*;
import java.util.Hashtable;
import javax.swing.*;

import com.google.zxing.*;
import com.google.zxing.common.*;
import com.google.zxing.qrcode.*;
import com.google.zxing.qrcode.decoder.*;

public class QRView extends JPanel
{
  public QRView()
  {
    challenge_ = "Hello World"; // default until message is set
    setBorder( BorderFactory.createLineBorder(Color.BLACK) );
    setBackground( Color.black );
  }

  public java.awt.Dimension getPreferredSize()
  {
    return dim_;
  }

  public void paintComponent( Graphics g )
  {
    super.paintComponent( g );

    Hashtable<EncodeHintType, ErrorCorrectionLevel> hints =
      new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
    hints.put( EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L );

    try
    {
      QRCodeWriter qrw = new QRCodeWriter();
      BitMatrix mtx =
        qrw.encode( challenge_, BarcodeFormat.QR_CODE, size_, size_, hints );

      int wid = mtx.getWidth();
      BufferedImage bi =
        new BufferedImage( wid, wid, BufferedImage.TYPE_INT_RGB );

      Graphics2D gpx = bi.createGraphics();
      gpx.setColor( Color.WHITE );
      gpx.fillRect( 0, 0, wid, wid );
      gpx.setColor( Color.BLACK );

      for (int ii = 0; ii < wid; ii++)
        for (int jj = 0; jj < wid; jj++)
          if (mtx.get(ii, jj))
            gpx.fillRect( ii, jj, 1, 1 );

      g.drawImage( bi, 0, 0, null );
    }
    catch( Exception e )
    {
      e.printStackTrace( System.err );
    }
  }

  public String challenge() { return challenge_; }
  public void setChallenge( String newChallenge )
  {
    challenge_ = newChallenge;
    repaint();
  }

  private String challenge_ = null;
  private static final int size_ = 385;

  private static java.awt.Dimension dim_ =
    new java.awt.Dimension( size_, size_ );
}
