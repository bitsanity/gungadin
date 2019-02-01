package pubui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.file.*;
import java.text.*;
import java.time.*;
import java.util.*;
import javax.net.*;
import javax.swing.*;
import javax.swing.event.*;

import com.github.lgooddatepicker.components.*;
import com.github.lgooddatepicker.optionalusertools.*;
import com.github.lgooddatepicker.zinternaltools.*;

import com.subgraph.orchid.*;

import org.json.simple.*;
import org.json.simple.parser.*;

import tbox.*;

public class FilesPanel extends JPanel
                        implements ActionListener,
                                   DateChangeListener,
                                   ListSelectionListener
{
  private DatePicker from_;
  private DatePicker to_;
  private JRadioButton desc_;
  private JRadioButton asc_;
  private JList<String> list_;
  private JButton decrypt_;

  private TorClient tor_ = null;

  public FilesPanel() throws Exception
  {
    super( new GridBagLayout() );

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0; gbc.gridy = 0;
    gbc.insets = new Insets( 5, 5, 5, 5 );
    gbc.ipadx = 5;
    gbc.ipady = 5;
    gbc.anchor = GridBagConstraints.LINE_START;
    gbc.gridwidth = 1;

    gbc.gridx = 0;
    gbc.gridy += 1;
    add( new JLabel(Resources.instance().get("IncludeFromLabel")), gbc );

    gbc.gridx += 1;
    add( from_ = new DatePicker(), gbc );
    from_.addDateChangeListener( this );

    gbc.gridx += 1;
    gbc.anchor = GridBagConstraints.LINE_END;
    add( new JLabel(Resources.instance().get("IncludeToLabel")), gbc );

    gbc.gridx += 1;
    gbc.anchor = GridBagConstraints.LINE_START;
    add( to_ = new DatePicker(), gbc );
    to_.addDateChangeListener( this );

    gbc.gridx = 0;
    gbc.gridy += 1;
    add( new JLabel(Resources.instance().get("SortByDateLabel")), gbc );

    gbc.gridx += 1;
    add( desc_ = new JRadioButton(Resources.instance().get("DescLabel")),
         gbc );
    desc_.setMnemonic( KeyEvent.VK_D );
    desc_.setSelected( true );
    desc_.addActionListener( this );

    gbc.gridx += 1;
    add( asc_ = new JRadioButton(Resources.instance().get("AscLabel")), gbc );
    asc_.setMnemonic( KeyEvent.VK_A );
    asc_.addActionListener( this );

    ButtonGroup bg = new ButtonGroup();
    bg.add( desc_ );
    bg.add( asc_ );

    gbc.gridx = 0;
    gbc.gridy += 1;
    gbc.gridwidth = 4;
    gbc.fill = GridBagConstraints.BOTH;
    list_ = new JList<>();
    list_.setListData( new String[0] );
    list_.setVisibleRowCount( 18 );
    list_.setSelectionMode(ListSelectionModel.SINGLE_SELECTION );
    list_.setFont( new Font("Monospaced", Font.PLAIN, 12) );
    list_.addListSelectionListener( this );
    JScrollPane jsp = new JScrollPane( list_ );
    jsp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
    add( jsp, gbc );

    gbc.gridy += 1;
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.NONE;
    decrypt_ = new JButton(Resources.instance().get("DecryptBtnLabel"));
    decrypt_.addActionListener( this );
    decrypt_.setForeground( Color.RED );
    add( decrypt_, gbc );

    gbc.gridx += 3;
    gbc.anchor = GridBagConstraints.LINE_END;
    JButton refresh = new JButton(Resources.instance().get("RefreshBtnLabel"));
    refresh.addActionListener( this );
    add( refresh, gbc );
  }

  public void dateChanged( DateChangeEvent dce )
  {
    try {
      refreshList();
    }
    catch( Exception e ) { e.printStackTrace(); }
  }

  public void valueChanged( ListSelectionEvent lse )
  {
    if (lse.getValueIsAdjusting()) return;

    decrypt_.setEnabled( -1 != list_.getSelectedIndex() );
  }

  public void actionPerformed( ActionEvent aev )
  {
    try {
    if (aev.getSource() == decrypt_)
      decryptFile();
    else
      refreshList();
    }
    catch( Exception e ) { e.printStackTrace(); } // TODO : report errors
  }

  private void refreshList() throws Exception
  {
    JSONObject parm0 = new JSONObject();
    parm0.put( "pubkey", RedKey.instance().getPublicKey() );
    String msgStr = HexString.encode( parm0.toJSONString().getBytes() );

    Secp256k1 curve = new Secp256k1();
    // NOTE: clientservices is js/Ethereum, expects Keccak256 instead of SHA2
    byte[] sig = curve.signECDSA( Keccak256.hash(msgStr.getBytes()),
                                  RedKey.instance().get() );

    JSONArray parms = new JSONArray();
    parms.add( parm0 );
    parms.add( HexString.encode(sig) );

    JSONObject request = new JSONObject();
    request.put( "method", "getIPFSHashes" );
    request.put( "params", parms );
    request.put( "id", RedKey.instance().getPublicKey() );

    String response = null;

    if (null == tor_)
      tor_ = new TorClient();

    try {
      SocketFactory sfac = tor_.getSocketFactory();
      Socket cl = sfac.createSocket( Onions.instance().pickRandom(), 80 );
      PrintWriter pw = new PrintWriter( cl.getOutputStream(), true );
      pw.println( request.toJSONString() );

      BufferedReader rdr = new BufferedReader(
        new InputStreamReader(cl.getInputStream()) );

      response = rdr.readLine();
      if (null == response)
        throw new Exception( "null response from tor service" );
    }
    catch( Exception e ) {
      System.err.println( "refreshList: " + e.getMessage() );
      return;
    }

    JSONObject rsp = (JSONObject)(new JSONParser()).parse(response);
    JSONArray hashes = (JSONArray)rsp.get( "hashes" );
    JSONArray tstamps = (JSONArray)rsp.get( "tstamps" );

    String[] listData = new String[ hashes.size() ];

    for (int ii = 0; ii < hashes.size(); ii++)
    {
      Date dt = new Date( Long.parseLong((String)tstamps.get(ii)) * 1000L );

      Date frm = null;
      Date tod = null;

      if (null != from_.getDate())
        frm = Date.from(
          from_.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant() );
      else
        frm = new Date(0L);

      if (null != to_.getDate())
        tod = Date.from(
          to_.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant() );
      else
        tod = new Date();

      if (dt.after(frm) && !dt.after(tod))
        listData[ii] = new SimpleDateFormat("yyyy MM dd ").format(dt) +
                       (String)hashes.get(ii);
    }

    if (asc_.isSelected())
      Arrays.sort( listData );
    else if (desc_.isSelected())
      Arrays.sort( listData, Collections.reverseOrder() );

    list_.clearSelection();
    list_.setListData( listData );
  }

  private void decryptFile() throws Exception
  {
    String ipfshash = null;

    String fhash = list_.getSelectedValue();
    if (null == fhash || 0 == fhash.length())
      ipfshash = getHash();
    else
      ipfshash = list_.getSelectedValue().split(" ")[1];

    if (null == ipfshash || 0 == ipfshash.length())
      throw new Exception( "no file selected" );

    Secp256k1 curve = new Secp256k1();

    StringBuffer blackBuffer = new StringBuffer();

    byte[] senderpubkey = null;

    JSONObject current = getChunk( ipfshash );
    while (null != current)
    {
      byte[] sig = tbox.Base64.decode( (String)current.get( "sig" ) );
      JSONObject data = (JSONObject)current.get( "data" );
      byte[] msg = data.toJSONString().getBytes();

      senderpubkey = curve.recoverSchnorr( SHA256.hash(msg), sig );
      if (null == senderpubkey)
        throw new Exception( "failed to recover sender key" );

      blackBuffer.append( (String)data.get("black") );

      String next = (String) data.get( "next" );
      if (next == null || next.equalsIgnoreCase("null") || next.length() == 0)
        break;

      current = getChunk( (String)data.get("next") );
    }

    if (0 == blackBuffer.length()) throw new Exception( "no data" );

    JFileChooser saveas = new JFileChooser();
    saveas.setDialogTitle( "Please choose a directory to store the file:" );

    saveas.setFileHidingEnabled( false ); // allow hidden files
    saveas.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
    if (saveas.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
      return;

    Path target = saveas.getSelectedFile().toPath();

    ECIES decryptor = new ECIES( RedKey.instance().get(), senderpubkey );

    JSONObject redobj = (JSONObject)(new JSONParser()).parse(
      new String( decryptor.decrypt(blackBuffer.toString()), "UTF-8") );

    String fname =
      saveas.getSelectedFile() + "/" + (String)redobj.get("fname");

    byte[] red = tbox.Base64.decode( (String)redobj.get("red") );

    try (FileOutputStream fos = new FileOutputStream(fname))
    {
      fos.write( red );
    }
  }

  private JSONObject getChunk( String ipfshash ) throws Exception
  {
    URL url = new URL( Endpoints.instance().pickRandom() + ipfshash );

    InputStream is = null;
    ByteArrayOutputStream baos = null;

    try
    {
      is = new BufferedInputStream( url.openStream() );
      baos = new ByteArrayOutputStream();
      byte[] buff = new byte[16 * 1024];
      int b;

      while ( (b = is.read(buff)) > 0 )
        baos.write( buff, 0, b );
    }
    catch( Exception e )
    {
      System.err.println( e.getMessage() );
    }
    finally
    {
      if (null != is) is.close();
    }

    String chunkStr = baos.toString();
    baos.reset();

    if (null == chunkStr || 0 == chunkStr.length())
      return null;

    return (JSONObject)(new JSONParser()).parse( chunkStr );
  }

  private String getHash()
  {
    JPanel jp = new JPanel();
    JLabel lbl = new JLabel( Resources.instance().get("HashPrompt") );
    JTextField hash = new JTextField( 32 );
    jp.add( lbl );
    jp.add( hash );

    String[] options = new String[]
    {
       Resources.instance().get("OK"),
       Resources.instance().get("Cancel")
    };

    int retval = JOptionPane.showOptionDialog(
                   decrypt_,
                   jp,
                   Resources.instance().get("HashPromptTitle"),
                   JOptionPane.NO_OPTION,
                   JOptionPane.PLAIN_MESSAGE,
                   null,
                   options,
                   options[1] );

    if (0 == retval)
      return hash.getText().trim();

    return null;
  }
}

