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
      EthListener el =
        new EthListener( st,
                         ethPeerPubkey_,
                         hwm_,
                         nodeAddr_,
                         pubs_,
                         ipfs_,
                         gateway_ );
      el.start();

      KGWorker kgw = new KGWorker( ss, daemonId_, acl_, ipfs_, gateway_ );
      kgw.start();

      // kickstart mining
      // back up a few blocks to make sure we get every event
      long newhwm = hwm_.get() - 100L;
      if (0L > newhwm)
        newhwm = 0L;

      gateway_.setHWM( newhwm );

      // suspend main thread let the daemon threads do the work
      Thread.currentThread().sleep( Long.MAX_VALUE );
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

    String pphr = argsMap.get( "intpassphrase" );
    if (null == pphr || 0 == pphr.length())
    {
      System.out.print( "intpassphrase: " );
      Scanner s = new Scanner( System.in );
      pphr = s.next();
      s.close();

      if (null == pphr || 0 == pphr.length())
        throw new Exception( "No int passphrase provided." );

      System.out.print( "intpassphrase: " + pphr );
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

    if ( !Files.exists(Paths.get(argsMap.get("intkeyfilepath"))) )
      throw new Exception(
        "IPC key " + argsMap.get("intkeyfilepath") + " does not exist." );

    System.out.println(
      "Daemon:\n" +
      "\tuiport = " + argsMap.get("uiport") + "\n" +
      "\tegwinport = " + argsMap.get("egwinport") + "\n" +
      "\tegwoutport = " + argsMap.get("egwoutport") + "\n" +
      "\tegwpeerpubkey = " + argsMap.get("egwpeerpubkey") + "\n" +
      "\textaddress = " + argsMap.get("extaddress") + "\n" +
      "\tintpassphrase = " + argsMap.get("intpassphrase") + "\n" +
      "\tintkeyfilepath = " + argsMap.get("intkeyfilepath") + "\n" +
      "\tpubsdbfilepath = " + argsMap.get("pubsdbfilepath") + "\n" +
      "\thwmdbfilepath = " + argsMap.get("hwmdbfilepath") + "\n" +
      "\taclfilepath = " + argsMap.get("aclfilepath") + "\n"
    );

    Daemon matt = new Daemon();

    matt.uiport_ = Integer.parseInt( argsMap.get("uiport") );
    matt.egwinport_ = Integer.parseInt( argsMap.get("egwinport") );
    matt.egwoutport_ = Integer.parseInt( argsMap.get("egwoutport") );
    matt.ethPeerPubkey_ = HexString.decode( argsMap.get("egwpeerpubkey") );
    matt.hwm_ = new HWM( argsMap.get("hwmdbfilepath") );
    matt.pubs_ = new Publications( argsMap.get("pubsdbfilepath") );
    matt.ipfs_ = new IPFS();
    matt.acl_ = new ACL( argsMap.get("aclfilepath") );
    matt.nodeAddr_ = argsMap.get("extaddress");
    matt.daemonId_ =
      new NodeIdentity( argsMap.get("intpassphrase"),
                        argsMap.get("intkeyfilepath") );

    matt.gateway_ = new EthGateway( egwoutport, matt.daemonId_.red() );

    matt.doDaemonStuff();
  }
}

