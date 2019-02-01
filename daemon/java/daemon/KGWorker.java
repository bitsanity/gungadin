package daemon;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;

import org.json.simple.*;
import org.json.simple.parser.*;

import tbox.*;

public class KGWorker extends WorkerBase
{
  private Socket client_ = null;
  private NodeIdentity nodeId_ = null;
  private ACL acl_ = null;
  private IPFS ipfs_ = null;
  private EthGateway gateway_ = null;

  private boolean testmode_ = false;

  private Challenge ch_ = null;  // contains pub (G) and priv (g) keys
  private byte[] A_ = null;      // agent acting on client's behalf
  private byte[] K_ = null;      // client's public key
  private String id_ = null;     // client.cookie

  public KGWorker( Socket client,
                   NodeIdentity intid,
                   ACL acl,
                   IPFS ipfs,
                   EthGateway gateway )
  {
    super( client );
    nodeId_ = intid;
    acl_ = acl;
    ipfs_ = ipfs;
    gateway_ = gateway;
  }

  // @override
  public JSONObject replyTo( JSONObject request ) throws Exception
  {
    String method = (String) request.get( "method" );
    JSONArray params = (JSONArray) request.get( "params" );

    if (testmode_ && method.equals( "test.register" ))
    {
      String b64key = (String) params.get( 0 );
      return register( b64key );
    }

    if (method.equals( "adilos.response" ))
    {
      String adilosResp = (String) params.get( 0 );

      Message rspmsg = Message.parse( adilosResp );

      byte[] G = rspmsg.part( 0 ).key();
      byte[] A = rspmsg.part( 1 ).key();
      byte[] K = rspmsg.part( 2 ).key();

      if (!Arrays.equals(ch_.pubKey(), G))
        return errorMessage( ERR_BAD_G, "Invalid G", HexString.encode(G), id_ );

      if ( !acl_.hasKey(K) )
        return errorMessage( ERR_K_UNK, "K Unknown", HexString.encode(K), id_ );

      A_ = A;
      K_ = K;

      return defaultPage();
    }

    id_ = (String) request.get( "id" );

    if (method.equals( "request" ))
      try {
        return handleRequest( request );
      }
      catch( Exception e ) {
        e.printStackTrace();
        return errorMessage( ERR_EXCEP,
                             e.getMessage(),
                             "handleRequest() failed",
                             id_ );
      }

    // if arrived here then request is unrecognized so challenge
    return challenge();
  }

  private JSONObject challenge() throws Exception
  {
    ch_ = new Challenge();
    return errorMessage( ERR_CHALL, "adilos.challenge", ch_.toString(), null );
  }

  private JSONObject defaultPage() throws Exception
  {
    // HELLO is really a login ACK
    byte[] redbytes = ("HELLO 0x" + HexString.encode(K_) + "\n").getBytes();

    Secp256k1 curve = new Secp256k1();
    ECIES ec = new ECIES( ch_.privKey(), A_ );
    String rspb64 = ec.encrypt( redbytes );
    byte[] sig =
      curve.signECDSA( SHA256.hash(rspb64.getBytes()), ch_.privKey() );

    JSONObject rsp = new JSONObject();
    rsp.put( "rsp", rspb64 );
    rsp.put( "sig", Base64.encode(sig) );

    JSONObject reply = new JSONObject();
    reply.put( "result", rsp );
    reply.put( "error", "null" );
    reply.put( "id", ((null != id_) ? id_ : "null" ) );

    return reply;
  }

  private JSONObject handleRequest( JSONObject request ) throws Exception
  {
    JSONArray arr = (JSONArray) request.get( "params" );
    if (null == arr || 1 != arr.size()) return challenge();

    JSONObject blob = (JSONObject) arr.get( 0 );

    String req64 = (String) blob.get("req");
    byte[] sigA = Base64.decode( (String) blob.get("sig") );

    // confirm A signed the request correctly
    Secp256k1 curve = new Secp256k1();

    if (!curve.verifyECDSA(sigA, SHA256.hash(req64.getBytes()), A_))
    {
      System.out.println( "request: sig failed" );
      return challenge();
    }

    System.out.println( "G:" + HexString.encode(ch_.pubKey()) );
    System.out.println( "A:" + HexString.encode(A_) );
    System.out.println( "req: " + req64 );

    // decrypt request
    ECIES ec = new ECIES( ch_.privKey(), A_ );
    byte[] req = ec.decrypt( req64 );

    String reqS = new String( req, "UTF-8" );

    byte[] rawrsp = null;
    try{
      rawrsp = handleCmd( reqS );

      if (null == rawrsp)
        throw new Exception( "handleRequest(): NULL Response" );
    }
    catch( Exception e )
    {
      e.printStackTrace();

      return errorMessage( ERR_EXCEP,
                           e.toString(),
                           e.getMessage(),   // data
                           null ); // id
    }

    // encrypt then sign the encrypted version
    String rsp64 = ec.encrypt( rawrsp );
    byte[] sigG =
      curve.signECDSA( SHA256.hash(rsp64.getBytes()), ch_.privKey() );

    JSONObject rsp = new JSONObject();
    rsp.put( "rsp", rsp64 );
    rsp.put( "sig", Base64.encode(sigG) );

    JSONObject reply = new JSONObject();
    reply.put( "result", rsp );
    reply.put( "error", "null" );
    reply.put( "id", ((null != id_) ? id_ : "null" ) );

    return reply;
  }

  private JSONObject register( String b64Key ) throws Exception
  {
    acl_.addKey( Base64.decode(b64Key) );
    return challenge(); // still have to authenticate
  }

  // -------------------------------------------------------------------------
  private byte[] handleCmd( String reqtxt ) throws Exception
  {
    // expect:
    //     <cmd>   |  <parms>
    //   <command>[&name=value]+

    String[] parts = reqtxt.split("&");

    String cmd = parts[0];

    Hashtable<String,String> parms = new Hashtable<String,String>();

    for (int ii = 1; ii < parts.length; ii++)
    {
      String[] nv = parts[ii].split("=");
      if (nv.length != 2) continue;
      parms.put( nv[0], nv[1] );
    }

    if (cmd.equalsIgnoreCase("upload"))
    {
      // upload&fpath=<filepath>&recipient=<pubkey>

      String fpath = parms.get("fpath");

      File f = new File(fpath);

      if (    null == fpath
           || 0 == fpath.length()
           || null == f
           || !f.exists())
        throw new Exception( "Invalid fpath: " + fpath );

      if (!f.canRead())
        throw new Exception( "Cannot read file: " + fpath );

      String recipkey = parms.get( "recipient" );

      if (null == parms.get("recipient"))
        throw new Exception( "Need recipient" );

      doUpload( fpath, HexString.decode(recipkey) );
    }

    return "OK".getBytes();
  }

  private void doUpload( String fpath, byte[] recippubkey ) throws Exception
  {
    // string length is an int. max size of a string in java is 2^31 - 1 (2GB)
    // but the file will be encrypted and then encoded in Base64 which
    // adds 25% overhead, so 3/4 of 2GB is 1.5GB

    byte[] reddata = FileUtils.getFileBytes( Paths.get(fpath) );
    byte[] redkey = nodeId_.red();
    ECKeyPair pair = new ECKeyPair( redkey );

    JSONObject redwrapped = new JSONObject();
    redwrapped.put( "send", HexString.encode(pair.publickey()) );
    redwrapped.put( "tstamp", new Date().getTime() / 1000 ); // sec, not ms
    redwrapped.put( "fname", new File(fpath).getName() );
    redwrapped.put( "red", Base64.encode(reddata) );

    ECIES encryptor = new ECIES( redkey, recippubkey );
    String blktxt = encryptor.encrypt( redwrapped.toString().getBytes() );

    String[] chunks = Chunkifier.chunkify( blktxt );
    String next = "null";

    for (int ii = chunks.length - 1; ii >= 0; ii--)
    {
      JSONObject data = new JSONObject();
      data.put( "next", next );
      data.put( "black", chunks[ii] );

      byte[] msg = data.toString().getBytes();
      byte[] sig = new Secp256k1().signSchnorr( SHA256.hash(msg), redkey );

      if (null == sig || 0 == sig.length)
        throw new Exception( "schnorr signature failed." );

      JSONObject blkwrapped = new JSONObject();
      blkwrapped.put( "data", data );
      blkwrapped.put( "sig", Base64.encode(sig) );

      String ipfshash = ipfs_.push( blkwrapped.toString() );

      if (null == ipfshash || 0 == ipfshash.length())
        throw new Exception( "IPFS failed" );

      System.out.println( "uploaded: " + ipfshash );

      // only publish the first chunk in a chain
      if (0 == ii)
        gateway_.publish( recippubkey,
                          ipfshash,
                          blkwrapped.toString().getBytes().length );
      else
        next = ipfshash;
    }
  }

  //
  // Test code
  //
  public static void main( String[] args ) throws Exception
  {
    KGWorker wkr = new KGWorker( null, null, null, null, null );

    Secp256k1 curve = new Secp256k1();

    // setup test identities for keymaster and agent

    byte[] a = new byte[32];
    java.security.SecureRandom.getInstance("SHA1PRNG").nextBytes( a );
    byte[] A = curve.publicKeyCreate( a );

    byte[] k = new byte[32];
    java.security.SecureRandom.getInstance("SHA1PRNG").nextBytes( k );
    byte[] K = curve.publicKeyCreate( k );

    wkr.register( Base64.encode(K) );

    //
    // prompt worker to generate a challenge
    //

    JSONObject blankRequest = new JSONObject();
    JSONArray blankArray = new JSONArray();
    blankRequest.put( "method", "request" );
    blankRequest.put( "params", blankArray );
    blankRequest.put( "id", "null" );

    JSONObject challO = wkr.replyTo( blankRequest );

    JSONObject err = (JSONObject) challO.get( "error" );

    String data = (String) err.get( "data" );

    Message chmsg = Message.parse( data );

    //
    // generate the response
    //

    MessagePart gpart = new MessagePart( chmsg.part(0).key(),
                                         chmsg.part(0).sig() );

    byte[] asig = curve.signECDSA( SHA256.hash(gpart.sig()), a );
    MessagePart apart = new MessagePart( A, asig );

    // pretend to exchange QRs with keymaster who provides ksig and K
    byte[] ksig = curve.signECDSA( SHA256.hash(asig), k );
    MessagePart kpart = new MessagePart( K, ksig );

    Message armsg = new Message( new MessagePart[] { gpart, apart, kpart } );

    JSONObject adilosRsp = new JSONObject();
    adilosRsp.put( "method", "adilos.response" );

    JSONArray params = new JSONArray();
    params.add( armsg.toString() );

    adilosRsp.put( "params", params );
    adilosRsp.put( "id", "null" );

    //
    // Processes response and returns the default page
    //
    JSONObject dflt = wkr.replyTo( adilosRsp );

    System.out.println( "PASS" );
  }

}

