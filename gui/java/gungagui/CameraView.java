package gungagui;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

import com.github.sarxos.webcam.*;
import com.github.sarxos.webcam.ds.v4l4j.V4l4jDriver;

public class CameraView extends WebcamPanel
{
  static
  {
    Webcam.setDriver( new V4l4jDriver() );
  }

  public CameraView()
  {
    super( Webcam.getDefault() );

    Webcam.getDefault().close();

    image_ = null;
    setBorder( BorderFactory.createLineBorder(Color.BLACK) );
    setBackground( Color.black );
  }

  public void go()
  {
    Webcam.getDefault().open();
  }

  public void stop()
  {
    Webcam.getDefault().close();
  }

  public BufferedImage getImage()
  {
    return Webcam.getDefault().getImage();
  }

  public Dimension getPreferredSize()
  {
    return dim_;
  }

  public void setImage( BufferedImage i )
  {
    image_ = i;
    repaint();
  }

/*
  public void paintComponent( Graphics g )
  {
    super.paintComponent( g );
    if (null != image_)
      g.drawImage( image_,
                   0, 0,                                  // dstx1, dsty1
                   385, 385,                              // dstx2, dsty2
                   0, 0,                                  // srcx1, srcy1
                   image_.getWidth(), image_.getHeight(), // srcx2, srcy2
                   null );
  }
*/

  private static final Dimension dim_ = new Dimension( 385, 385 );
  private BufferedImage image_;

  public static void main( String[] args ) throws Exception
  {
    Webcam wcam = Webcam.getDefault();
    wcam.setViewSize( WebcamResolution.HD720.getSize() ); // "HD720" => 720p

    WebcamPanel panel = new WebcamPanel( wcam );
    panel.setFPSDisplayed(true);
		panel.setDisplayDebugInfo(true);
		panel.setImageSizeDisplayed(true);
		panel.setMirrored(true);

		JFrame window = new JFrame("Test webcam panel");
		window.add(panel);
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
    window.setVisible(true);
  }
}

