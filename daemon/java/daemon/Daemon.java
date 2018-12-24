package daemon;

import java.net.ServerSocket;
import java.nio.file.*;
import java.util.*;

import tbox.*;

public class Daemon
{
  private int uiport_;
  private int egwinport_;
  private int egwoutport_;
  private byte[] ethPeerPubkey_;
  private HWM hwm_;
  private String nodeAddr_; // Node's Ethereum address
  private NodeIdentity daemonId_;
  private Publications pubs_;
  private IPFS ipfs_;
  private ACL acl_;
  private EthGateway gateway_;

  public Daemon() {}

  public void doDaemonStuff() throws Exception
  {
    ServerSocket ss = new ServerSocket( uiport_ );
    ServerSocket st = new ServerSocket( egwinport_ );

    try
    {
      while( true )
      {
        // thread to listen to the UI
        new KGWorker( ss.accept(), daemonId_, acl_, ipfs_, gateway_ ).start();

        // thread to listen for incoming events from Ethereum
        new EthListener( st.accept(),
                         ethPeerPubkey_,
                         hwm_,
                         nodeAddr_,
                         pubs_,
                         ipfs_,
                         gateway_ ).start();
      }
    }
    catch( Exception e )
    {
      System.out.println( e.getMessage() );
    }
    finally
    {
      ss.close();
      st.close();
    }
  }

  public static void main( String[] args ) throws Exception
  {
    Hashtable<String,String> argsMap = new Hashtable<String,String>();

    // parse command line args

    for (int ii = 0; ii < args.length; ii++)
    {
      String[] parts = args[ii].split("=");

      if (null != parts && 2 == parts.length)
        argsMap.put( parts[0], parts[1] );
    }

    // specifying pphrase on the command line is a possible security hole

    String pphr = argsMap.get( "extpassphrase" );
    if (null == pphr || 0 == pphr.length())
    {
      System.out.print( "extpassphrase: " );
      Scanner s = new Scanner( System.in );
      pphr = s.next();
      s.close();

      if (null == pphr || 0 == pphr.length())
        throw new Exception( "No ext passphrase provided." );
    }
    argsMap.put( "extpassphrase", pphr );

    pphr = argsMap.get( "intpassphrase" );
    if (null == pphr || 0 == pphr.length())
    {
      System.out.print( "intpassphrase: " );
      Scanner s = new Scanner( System.in );
      pphr = s.next();
      s.close();

      if (null == pphr || 0 == pphr.length())
        throw new Exception( "No int passphrase provided." );
    }
    argsMap.put( "intpassphrase", pphr );

    // optional parameters, default values assigned

    int uiport = Integer.parseInt( argsMap.get("uiport") );
    if (0 >= uiport) uiport = 8804;

    int egwinport = Integer.parseInt( argsMap.get("egwinport") );
    if (0 >= egwinport) egwinport = 8805;

    int egwoutport = Integer.parseInt( argsMap.get("egwoutport") );
    if (0 >= egwoutport) egwoutport = 8806;

    if (null == argsMap.get("pubsdbfilepath"))
      argsMap.put("pubsdbfilepath" , "./publicationsdb" );

    if (null == argsMap.get("hwmdbfilepath"))
      argsMap.put("hwmdbfilepath" , "./hwmdb" );

    // mandatory parameters, crash if not specified

    if (    null == argsMap.get("egwpeerpubkey")
         || 0 == argsMap.get("egwpeerpubkey").length() )
      throw new Exception( "Need Eth gateway peer's pubkey for comms." );

    if ( !Files.exists(Paths.get(argsMap.get("extkeyfilepath"))) )
      throw new Exception(
        "Node key " + argsMap.get("extkeyfilepath") + " does not exist." );

    if ( !Files.exists(Paths.get(argsMap.get("intkeyfilepath"))) )
      throw new Exception(
        "IPC key " + argsMap.get("intkeyfilepath") + " does not exist." );

    if (    !Files.exists(Paths.get(argsMap.get("ipfscachedir")))
         || !Files.isDirectory(Paths.get(argsMap.get("ipfscachedir"))) )
      throw new Exception(
        "IPFS cache dir must exist: " + argsMap.get("ipfscachedir") );

    System.out.println(
      "Daemon:\n" +
      "\tuiport = " + argsMap.get("uiport") + "\n" +
      "\tegwinport = " + argsMap.get("egwinport") + "\n" +
      "\tegwoutport = " + argsMap.get("egwoutport") + "\n" +
      "\tegwpeerpubkey = " + argsMap.get("egwpeerpubkey") + "\n" +
      "\textkeyfilepath = " + argsMap.get("extkeyfilepath") + "\n" +
      "\tintpassphrase = " + argsMap.get("intpassphrase") + "\n" +
      "\tintkeyfilepath = " + argsMap.get("intkeyfilepath") + "\n" +
      "\tpubsdbfilepath = " + argsMap.get("pubsdbfilepath") + "\n" +
      "\thwmdbfilepath = " + argsMap.get("hwmdbfilepath") + "\n" +
      "\tipfscachedir = " + argsMap.get("ipfscachedir") + "\n" +
      "\taclfilepath = " + argsMap.get("aclfilepath") + "\n"
    );

    Daemon matt = new Daemon();

    matt.uiport_ = Integer.parseInt( argsMap.get("uiport") );
    matt.egwinport_ = Integer.parseInt( argsMap.get("egwinport") );
    matt.egwoutport_ = Integer.parseInt( argsMap.get("egwoutport") );
    matt.ethPeerPubkey_ = HexString.decode( argsMap.get("egwpeerpubkey") );
    matt.hwm_ = new HWM( argsMap.get("hwmdbfilepath") );
    matt.pubs_ = new Publications( argsMap.get("pubsdbfilepath") );
    matt.ipfs_ = new IPFS( argsMap.get("ipfscachedir") );
    matt.acl_ = new ACL( argsMap.get("aclfilepath") );

    // nodeId is node's ethereum address. key file is the keystore file
    // format: "UTC--date--address"
    matt.nodeAddr_ = argsMap.get("extkeyfilepath").split("--")[2];

    NodeIdentity internalId =
      new NodeIdentity( argsMap.get("intpassphrase"),
                        argsMap.get("intkeyfilepath") );

    matt.gateway_ = new EthGateway( egwoutport, internalId.red() );

    matt.doDaemonStuff();
  }
}

