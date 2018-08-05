package daemon;

import java.net.ServerSocket;
import java.nio.Files;
import java.util.*;

public class Daemon
{
  private int uiport_;
  private int egwinport_;
  private int egwoutport_;
  private byte[] ethPeerPubkey_;
  private HWM hwm_;
  private NodeIdentity nodeId_;
  private NodeIdentity daemonId_;
  private Publications pubs_;
  private IPFS ipfs_;
  private EthGateway gateway_;

  public Daemon() {}

  public doDaemonStuff() throws Exception
  {
    ServerSocket ss = new ServerSocket( uiport );
    ServerSocket st = new ServerSocket( egwport );

    try
    {
      while( true )
      {
        // thread to listen to the UI
        new KGWorker( ss.accept() ).start();

        // thread to listen for incoming events from Ethereum
        Runnable ethgw = () -> new EthListener(
          st.accept(),
          ethPeerPubkey_,
          hwm_,
          nodeId_,
          pubs_,
          ipfs_,
          gateway_
        );
        new Thread( ethgw ).start();
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

    String pphr = argsMap.get("pphrase");
    if (null == pphr || 0 == pphr.length())
    {
      Scanner s = new Scanner( System.in );
      pphr = scanner.next();
      scanner.close();

      if (null == pphr || 0 == pphr.length())
        throw new Exception( "No passphrase provided." );
    }

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

    if (null == egwpeerpubkey || 0 == egwpeerpubkey.length())
      throw new Exception( "Need Eth gateway peer's pubkey for comms." );

    if ( !Files.exists(argsMap.get("extkeyfilepath")) )
      throw new Exception(
        "Node key " + argsMap.get("extkeyfilepath") + " does not exist." );

    if ( !Files.exists(argsMap.get("intkeyfilepath")) )
      throw new Exception(
        "IPC key " + argsMap.get("intkeyfilepath") + " does not exist." );

    if (    !Files.exists(argsMap.get("ipfscachedir"))
         || !Files.isDirectory(argsMap.get("ipfscachedir")) )
      throw new Exception(
        "IPFS cache dir must exist: " + argsMap.get("ipfscachedir") );

    System.out.println(
      "Daemon:\n" +
      "\tuiport = " + argsMap.get("uiport") + "\n" +
      "\tegwinport = " + argsMap.get("egwinport") + "\n" +
      "\tegwoutport = " + argsMap.get("egwoutport") + "\n" +
      "\tegwpeerpubkey = " + argsMap.get("egwpeerpubkey") + "\n" +
      "\tpphrase = " + argsMap.get("passphrase") + "\n" +
      "\textkeyfilepath = " + argsMap.get("extkeyfilepath") + "\n" +
      "\tintkeyfilepath = " + argsMap.get("intkeyfilepath") + "\n" +
      "\tpubsdbfilepath = " + argsMap.get("pubsdbfilepath") + "\n" +
      "\thwmdbfilepath = " + argsMap.get("hwmdbfilepath") + "\n" +
      "\tipfscachedir = " + argsMap.get("ipfscachedir") + "\n" +
    );

    Daemon matt = new Daemon();
    matt.uiport_ = uiport;
    matt.egwinport_ = egwinport;
    matt.egwoutport_ = egwoutport;
    matt.ethPeerPubkey_ = HexString.decode( argsMap.get("egwpeerpubkey") );
    matt.nodeId_ = new NodeIdentity( argsMap.get("extkeyfilepath") );
    matt.hwm_ = new HWM( argsMap.get("hwmdbfilepath") );
    matt.pubs_ = new Publications( argsMap.get("pubsdbfilepath") );
    matt.ipfs_ = new IPFS( argsMap.get("ipfscachedir") );
    matt.gateway_ =
       new EthGateway( egwoutport,
                       new NodeIdentity(argsMap.get("intkeyfilepath")).red() );

    matt.doDaemonStuff();
  }
}

