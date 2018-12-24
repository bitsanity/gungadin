package daemon;

import java.io.*;
import java.net.*;

import org.json.simple.*;
import org.json.simple.parser.*;

import tbox.*;

public class EthGateway
{
  private int port_;
  private byte[] red_;

  public EthGateway( int port, byte[] daemonprivkey ) throws Exception
  {
    port_ = port;
    red_ = daemonprivkey;
  }

  public void setHWM( long newhwm ) throws Exception
  {
    JSONObject msgbody = new JSONObject();
    msgbody.put( "newhwm", Long.toString(newhwm) );
    String msg = HexString.encode( msgbody.toJSONString().getBytes() );

    send( makeRpc("setHWM", msg) );
  }

  public void vote( long blocknum, String hashResult ) throws Exception
  {
    JSONObject msgbody = new JSONObject();
    msgbody.put( "blocknum", Long.toString(blocknum) );
    msgbody.put( "hash", hashResult );
    String msg = HexString.encode( msgbody.toJSONString().getBytes() );

    send( makeRpc("vote", msg) );
  }

  public void publish( byte[] recipkey, String hash, long fsize )
  throws Exception
  {
    JSONObject msgbody = new JSONObject();
    msgbody.put( "recipkey", HexString.encode(recipkey) );
    msgbody.put( "hash", hash );
    msgbody.put( "fsize", fsize );
    String msg = HexString.encode( msgbody.toJSONString().getBytes() );

    send( makeRpc("publish", msg) );
  }

  private String makeRpc( String method, String msg )
  throws Exception
  {
    Secp256k1 curve = new Secp256k1();
    byte[] sig = curve.signECDSA( Keccak256.hash(msg.getBytes()), red_ );
    String sigs = HexString.encode( sig );

    JSONArray parts = new JSONArray();
    parts.add( msg );
    parts.add( sig );

    JSONObject result = new JSONObject();
    result.put( "method", method );
    result.put( "params", parts );
    result.put( "id", HexString.encode(curve.publicKeyCreate(red_)) );
    return result.toJSONString();
  }

  private void send( String rpcmsg ) throws Exception
  {
    Socket sock = new Socket( "localhost", port_ );

    try {
      PrintWriter pw = new PrintWriter( sock.getOutputStream(), true );
      pw.println( rpcmsg );
    }
    catch( Exception e ) {
      System.out.println( e.getMessage() );
    }
    finally {
      try { sock.close(); } catch( Exception x ) {}
    }
  }
}
