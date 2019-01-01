package gungagui;
 
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Hashtable;
import javax.swing.*;

import org.json.simple.*;
import org.json.simple.parser.*;

import tbox.*;

public class AgentUI implements ActionListener, Runnable
{
  private JFrame jf_ = null;
  private ChallengePanel cp_ = null;
  private JFileChooser jfc_;
  private JLabel idLabel_;
  private JLabel receiverLabel_;
  private JLabel uploadedLabel_;

  private boolean isScanning_ = false;
  private boolean isSignIn_ = false;

  private Message chmsg_;

  private ECKeyPair aA_;
  private byte[] K_; // user
  private byte[] R_; // recipient
  private byte[] G_; // gatekeeper

  private Socket sock_;
  private static int port_;

  public AgentUI() throws Exception
  {
    aA_ = ECKeyPair.makeNew();
  }

  public void run()
  {
    while (isScanning_)
    {
      BufferedImage img = cp_.camView().getImage();
      if (null == img) continue;

      String rsp = null;
      try
      {
        rsp = QR.decode( img );
        if (null == rsp || 0 == rsp.length()) continue;

        rsp = rsp.replaceAll( "\\s", "" );
        System.out.println( "rsp:\n" + rsp + "\n" );

        // parse keymaster response
        MessagePart kp = null;
        try
        {
          kp = MessagePart.fromBytes( Base64.decode(rsp) );

          int ix = (isSignIn_) ? 1 : 0;

          Secp256k1 curve = new Secp256k1();
          if (!curve.verifyECDSA( kp.sig(),
                                  SHA256.hash(chmsg_.part(ix).sig()),
                                  kp.key()))
          {
            System.err.println(
              "invalid response:\n" +
              "ix: " + ix +
              "\nkey: " + HexString.encode(kp.key()) +
              "\nmsg: " + HexString.encode(chmsg_.part(ix).sig()) +
              "\nsig: " + HexString.encode(kp.sig()) + "\n" );
            continue;
          }
        }
        catch( Exception ex ) {
          ex.printStackTrace();
          continue;
        }

        if (isSignIn_)
        {
          // append message to our challenge and fwd parts to gk
          Message toGk = new Message(
            new MessagePart[] { chmsg_.part(0),
                                chmsg_.part(1),
                                kp } );

          JSONArray params = new JSONArray();
          params.add( toGk.toString() );
          JSONObject toSvr = new JSONObject();
          toSvr.put( "method", "adilos.response" );
          toSvr.put( "params", params );
          toSvr.put( "id", "null" );

          JSONObject response = doRPC( toSvr );

          // just confirm no error
          JSONObject err = (JSONObject) response.get( "error" );
          if (err != null)
            System.out.println( err.toString() );
          else
          {
            K_ = kp.key();
            G_ = HexString.decode( (String)response.get("id") );
            String pubk = HexString.encode( K_ );
            idLabel_.setText( pubk );
          }
        }
        else
        {
          R_ = kp.key();
          String pubk = HexString.encode( R_ );
          receiverLabel_.setText( pubk );
          stopScanning();
        }
      }
      catch( Exception e )
      {
        System.err.println( e.getMessage() );
      }
    }
  }

  public void actionPerformed( ActionEvent aev )
  {
    try
    {
      String cmd = aev.getActionCommand();

      if (cmd.startsWith("Sign"))
      {
        initSession();

        isSignIn_ = true;
        startScanning();
      }
      else if (cmd.startsWith("Set"))
      {
        Challenge ch = new Challenge();
        chmsg_ = Message.parse( ch.toString() );
        cp_.qr().setChallenge( ch.toString() );

        isSignIn_ = false;
        startScanning();
      }
      else if (cmd.startsWith("Send"))
      {
        CardLayout cl = (CardLayout) jf_.getContentPane().getLayout();
        cl.show( jf_.getContentPane(), "FUP" );
      }
      else if (cmd.startsWith("Back") || cmd.startsWith("Cancel"))
      {
        stopScanning();
      }
      else if (cmd.startsWith("Approve"))
      {
        String red = "upload&fpath=" + jfc_.getSelectedFile().toString() +
                     "&recipient=" + HexString.encode( R_ );
        System.out.println( red );

        ECIES ec = new ECIES( aA_.privatekey(), G_ );
        String black = ec.encrypt( red.getBytes() );
        Secp256k1 curve = new Secp256k1();
        byte[] sig = curve.signECDSA( SHA256.hash(black.getBytes()),
                                      aA_.privatekey() );

        JSONObject request = new JSONObject();
        request.put( "method", "request" );
        JSONArray params = new JSONArray();
        JSONObject blob = new JSONObject();
        blob.put( "req", black );
        blob.put( "sig", Base64.encode(sig) );
        params.add( blob );
        request.put( "params", params );
        request.put( "id", HexString.encode(K_) );

        JSONObject response = doRPC( request );
        JSONObject err = (JSONObject) response.get( "error" );
        if (err != null)
          uploadedLabel_.setText( err.toString() );
        else
          uploadedLabel_.setText( "uploaded: " +
            jfc_.getSelectedFile().toString() );
      }
    }
    catch( Exception x )
    {
      System.out.println( x );
    }
  }

  private void initSession() throws Exception
  {
    // close any previously open socket
    try { sock_.close(); } catch( Exception e ) {}

    sock_ = new Socket( "localhost", port_ );

    // gatekeeper will respond to an empty request with a challenge
    JSONArray blankArray = new JSONArray();
    JSONObject blankRequest = new JSONObject();
    blankRequest.put( "method", "request" );
    blankRequest.put( "params", blankArray );
    blankRequest.put( "id", "null" );

    JSONObject response = doRPC( blankRequest );

    // challenge is data inside an 'error' message
    JSONObject err = (JSONObject) response.get( "error" );

    Message msg = Message.parse( (String)err.get("data") );

    // agent signs and appends part
    Secp256k1 curve = new Secp256k1();
    byte[] asig = curve.signECDSA( SHA256.hash(msg.part(0).sig()),
                                   aA_.privatekey() );

    chmsg_ = new Message(
        new MessagePart[] { msg.part(0),
                            new MessagePart(aA_.publickey(), asig) } );

    cp_.qr().setChallenge( chmsg_.toString() );
  }

  private JSONObject doRPC( JSONObject json ) throws Exception
  {
    JSONObject fromServer = null;

    try
    {
      PrintWriter pw = new PrintWriter( sock_.getOutputStream(), true );
      BufferedReader br = new BufferedReader(
        new InputStreamReader(sock_.getInputStream()) );

      pw.println( json.toJSONString() );
      String rsp = br.readLine();

      JSONParser parser = new JSONParser();
      fromServer = (JSONObject) parser.parse( rsp );
    }
    catch( Exception e )
    {
      System.out.println( "toDaemon(): " + e.getMessage() );
      try { sock_.close(); } catch( Exception x ) {}
    }

    return fromServer;
  }

  private void startScanning()
  {
    cp_.camView().go();
    CardLayout cl = (CardLayout)jf_.getContentPane().getLayout();
    cl.show( jf_.getContentPane(), "CHP" );
    isScanning_ = true;
    new Thread(this).start();
  }

  private void stopScanning()
  {
    isScanning_ = false;
    CardLayout cl = (CardLayout)jf_.getContentPane().getLayout();
    cl.show( jf_.getContentPane(), "CP" );
    cp_.camView().stop();
  }

  private void makeUI()
  {
    jf_ = new JFrame( "Agent" );
    jf_.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

    jf_.getContentPane().setLayout( new CardLayout() );

    jf_.getContentPane().add( commandPanel(), "CP" );
    jf_.getContentPane().add( cp_ = new ChallengePanel(this), "CHP" );
    jf_.getContentPane().add( fileUploadPanel(), "FUP" );

    jf_.pack();
    jf_.setVisible( true );
  }

  private JPanel commandPanel()
  {
    String blank = "                                   ";

    JPanel result = new JPanel( new GridBagLayout() );
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets( 10, 10, 10, 10 );

    gbc.gridx = 0; gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.LINE_END;
    JButton sender = new JButton( "Sign In ..." );
    sender.addActionListener( this );
    result.add( sender, gbc );

    Font mono = new Font( "Monospaced", Font.PLAIN, 12 );

    gbc.gridx++;
    idLabel_ = new JLabel( blank );
    idLabel_.setFont( mono );
    gbc.anchor = GridBagConstraints.LINE_START;
    result.add( idLabel_, gbc );

    gbc.gridx--; gbc.gridy++;
    gbc.anchor = GridBagConstraints.LINE_END;
    JButton recver = new JButton( "Set Receiver ..." );
    recver.addActionListener( this );
    result.add( recver, gbc );

    gbc.gridx++;
    receiverLabel_ = new JLabel( blank );
    receiverLabel_.setFont( mono );
    gbc.anchor = GridBagConstraints.LINE_START;
    result.add( receiverLabel_, gbc );

    gbc.gridx--; gbc.gridy++;
    JButton fileup = new JButton( "Send File ..." );
    gbc.anchor = GridBagConstraints.LINE_END;
    fileup.addActionListener( this );
    result.add( fileup, gbc );

    gbc.gridx++;
    uploadedLabel_ = new JLabel( blank );
    uploadedLabel_.setFont( mono );
    gbc.anchor = GridBagConstraints.LINE_START;
    result.add( uploadedLabel_, gbc );

    return result;
  }

  private JPanel fileUploadPanel()
  {
    jfc_ = new JFileChooser();
    jfc_.addActionListener( this );

    JPanel result = new JPanel();
    result.setLayout( new BorderLayout() );
    result.add( jfc_, BorderLayout.CENTER );

    return result;
  }

  private String testResponse() throws Exception
  {
    ECKeyPair keymaster = ECKeyPair.makeNew();
    Secp256k1 curve = new Secp256k1();

    byte[] sigBytes = curve.signECDSA( SHA256.hash(chmsg_.part(1).sig()),
                                       keymaster.privatekey() );

    MessagePart respPart = new MessagePart( keymaster.publickey(), sigBytes );
    Message respMsg = new Message( new MessagePart[] { respPart } );
    return respMsg.toString();
  }

  public static void main( String[] args ) throws Exception
  {
    Hashtable<String,String> parms = new Hashtable<String,String>();

    for (int ii = 0; ii < args.length; ii++)
    {
      String[] parts = args[ii].split("=");

      if (null != parts && 2 == parts.length)
        parms.put( parts[0], parts[1] );
    }

    String port = parms.get("port");
    if (null == port || 0 == port.length())
      throw new Exception( "daemon's ui port is required param" );

    port_ = Integer.parseInt( parms.get("port") );

    AgentUI aui = new AgentUI();

    SwingUtilities.invokeLater( new Runnable() {
      public void run() { aui.makeUI(); }
    } );
  }
}

