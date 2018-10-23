package pubui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import tbox.*;

public class AccountPanel extends JPanel implements ActionListener
{
  private JTextField pubkey_;
  private JTextField blackKey_;

  private JButton chooseBtn_;
  private JFileChooser fileChooser_;
  private JLabel chosenFile_;
  private JButton loadBtn_;

  public AccountPanel() throws Exception
  {
    super( new GridBagLayout() );

    fileChooser_ = new JFileChooser();
    fileChooser_.setFileHidingEnabled( false ); // reveal hidden files

    setBorder( new EmptyBorder(10,10,10,10) );
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0; gbc.gridy = 0;
    gbc.insets = new Insets( 5, 5, 5, 5 );
    gbc.ipadx = 5;
    gbc.ipady = 5;
    gbc.anchor = GridBagConstraints.LINE_START;

    gbc.gridy += 1;
    gbc.gridwidth = 3;
    add( new JLabel(Resources.instance().get("PubkeyLabel")), gbc );

    gbc.gridy += 1;
    // two rows of 66 colums = 132 chars ("0x" + 65 bytes * 2 chars/byte)
    pubkey_ = new JTextField( 66 );
    pubkey_.setBackground( Color.YELLOW );
    pubkey_.setEditable( false );
    add( pubkey_, gbc );

    gbc.gridy += 1;
    add( new JLabel(" "), gbc ); // spacer

    gbc.gridy += 1;
    add( new JLabel(Resources.instance().get("KeymasterImportLabel")), gbc );

    gbc.gridx = 0;
    gbc.gridwidth = 3;
    gbc.gridy += 1;
    gbc.anchor = GridBagConstraints.CENTER;
    add( new JLabel(Resources.instance().get("EitherLabel")), gbc );

    gbc.gridwidth = 1;
    gbc.gridy += 1;
    gbc.anchor = GridBagConstraints.LINE_START;
    add( new JLabel(Resources.instance().get("LoadFromLabel")), gbc );

    gbc.gridx = 1;
    add( chooseBtn_ =
      new JButton(Resources.instance().get("ChooseBtnLabel")), gbc );
    chooseBtn_.addActionListener( this );

    gbc.gridx = 2;
    add( chosenFile_ =
           new JLabel(Resources.instance().get("DefaultChosenLabel")), gbc );

    gbc.gridx = 0;
    gbc.gridy += 1;
    gbc.gridwidth = 3;
    gbc.anchor = GridBagConstraints.CENTER;
    add( new JLabel(Resources.instance().get("ORLabel")), gbc );

    gbc.gridy += 1;
    gbc.anchor = GridBagConstraints.LINE_START;
    add( new JLabel(Resources.instance().get("PasteKeyLabel")), gbc );

    gbc.gridy += 1;
    add( blackKey_ = new JTextField(66), gbc );

    gbc.gridy += 1;
    gbc.anchor = GridBagConstraints.CENTER;
    add( new JLabel(Resources.instance().get("ThenLabel")), gbc );

    gbc.gridy += 1;
    gbc.gridwidth = 1;
    gbc.anchor = GridBagConstraints.LINE_START;
    add( new JLabel(Resources.instance().get("ClickLabel")), gbc );

    gbc.gridx += 1;
    add( loadBtn_ =
         new JButton(Resources.instance().get("LoadBtnLabel")), gbc );
    loadBtn_.addActionListener( this );

    gbc.gridx = 0;
    gbc.gridy += 1;
    gbc.gridwidth = 3;
    JLabel psl = new JLabel( Resources.instance().get("PlaystoreLink") );
    psl.setForeground( Color.BLUE );
    add( psl, gbc );
  }

  public void actionPerformed( ActionEvent aev )
  {
    if (aev.getSource() == chooseBtn_)
    {
      int retval = fileChooser_.showOpenDialog( chooseBtn_ );

      if (retval == JFileChooser.APPROVE_OPTION)
      {
        File file = fileChooser_.getSelectedFile();
        chosenFile_.setText( file.getName() );

        try {
          byte[] fbytes = Files.readAllBytes( file.toPath() );
          String contents = new String( fbytes );
          makePublicKey( contents );
        }
        catch( Exception e ) {
          System.err.println( e.toString() );
          chosenFile_.setText( Resources.instance().get("DefaultChosenLabel") );
          return;
        }
      }
      else
        chosenFile_.setText( Resources.instance().get("DefaultChosenLabel") );
    }
    else if (aev.getSource() == loadBtn_)
    {
      try {
        makePublicKey( blackKey_.getText() );
      }
      catch( Exception e ) {
        System.err.println( e.toString() );
      }
    }
  }

  public void makePublicKey( String black ) throws Exception
  {
    if ( null == black || 0 == black.length() )
      return;

    String pin = getPIN();
    if (0 < pin.length())
    {
      byte[] red = BIP38.decrypt( black, pin );
      RedKey.instance().set( red );
      pubkey_.setText( RedKey.instance().getPublicKey() );
    }
  }

  public String getPIN()
  {
    JPanel jp = new JPanel();
    JLabel lbl = new JLabel( Resources.instance().get("PINPrompt") );
    JPasswordField pin = new JPasswordField( 6 );
    jp.add( lbl );
    jp.add( pin );

    String[] options = new String[]
    {
       Resources.instance().get("OK"),
       Resources.instance().get("Cancel")
    };

    int retval = JOptionPane.showOptionDialog(
                   loadBtn_,
                   jp,
                   Resources.instance().get("PINPromptTitle"),
                   JOptionPane.NO_OPTION,
                   JOptionPane.PLAIN_MESSAGE,
                   null,
                   options,
                   options[1] );

    if (0 == retval)
    {
      String pins = new String( pin.getPassword() );
      return pins;
    }

    return null;
  }
}
