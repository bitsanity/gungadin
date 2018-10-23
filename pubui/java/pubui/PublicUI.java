package pubui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class PublicUI
{
  public static void main( String[] args ) throws Exception
  {
    JTabbedPane tabs = new JTabbedPane();
    tabs.setBorder( new EmptyBorder(20,20,20,20) );

    tabs.addTab( Resources.instance().get( "AccountTabText" ),
                 Resources.instance().icon( "res/AccountTabIcon.png" ),
                 new AccountPanel(),
                 Resources.instance().get( "AccountTabTooltip" )
               );
    tabs.setMnemonicAt( 0, KeyEvent.VK_A );

    tabs.addTab( Resources.instance().get( "FilesTabText" ),
                 Resources.instance().icon( "res/FilesTabIcon.png" ),
                 new FilesPanel(),
                 Resources.instance().get( "FilesTabTooltip" )
               );
    tabs.setMnemonicAt( 1, KeyEvent.VK_F );

    tabs.addTab( Resources.instance().get( "SystemTabText" ),
                 Resources.instance().icon( "res/SystemTabIcon.png" ),
                 new SystemPanel(),
                 Resources.instance().get( "SystemTabTooltip" )
               );
    tabs.setMnemonicAt( 2, KeyEvent.VK_S );

    JFrame frame = new JFrame("GUNGADIN PUBLIC UI");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add( tabs, BorderLayout.CENTER );
    frame.pack();
    frame.setVisible(true);
  }
}
