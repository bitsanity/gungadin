package gungagui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ChallengePanel extends JPanel
{
  public ChallengePanel( ActionListener cb )
  {
    setBorder( BorderFactory.createEmptyBorder(5,5,5,5) );
    setBackground( Color.BLACK );
    setLayout( new BorderLayout() );

    Box b = Box.createHorizontalBox();

    qr_ = new QRView();
    b.add( qr_ );

    add( Box.createRigidArea(new Dimension(5,5)) );

    cam_ = new CameraView();
    b.add( cam_ );

    add( b, BorderLayout.CENTER );

    Box bottom = Box.createHorizontalBox();
    bottom.add( Box.createHorizontalGlue() );
    JButton back = new JButton( "Back" );
    back.addActionListener( cb );
    bottom.add( back );

    add( bottom, BorderLayout.SOUTH );
  }

  public QRView qr() { return qr_; }
  public CameraView camView() { return cam_; }

  private QRView qr_ = null;
  private CameraView cam_ = null;
}

