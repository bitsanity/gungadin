package daemon;

import java.util.Arrays;

import org.json.simple.*;
import org.json.simple.parser.*;

// responds to INCOMING messages from the Ethereum gateway (events)

// these messages dont have to be encrypted because its public anyway, but to
// deter rogue local processes we insist on signatures according to keys
// created at deployment time

public class EthGateway extends WorkerBase
{
  // require peer to sign messages sent into here with this pubkey
  private byte[] peerpubkey_;

  private NodeIdentity extId_; // for sharding need last char of address

  public EthGateway( Socket client, byte[] peerpubkey, NodeIdentity extid )
  {
    super( client );
    peerpubkey_ = peerpubkey;
    extId_ = extid;
  }

  // @override
  // handle message from separate process that talks to Ethereum
  public JSONObject replyTo( JSONObject request ) throws Exception
  {
    // public key of the communicating peer, not a person/subject
    byte[] pubkey = HexString.decode( (String)request.get( "id" ) );

    if (null == pubkey || (pubkey.length() != 33 && pubkey.length() != 65))
      return errorMessage( ERR_BAD_G,
          "Improper pubkey",
          "null or invalid length",
          (pubkey == null) ? "<null>" : pubkey );

    if (!Arrays.equals(pubkey, peerpubkey_))
      return errorMessage( ERR_BAD_G,
        "Peer pubkey does not match expected key",
        "expected: " + HexString.encode(peerpubkey_) +
        "received: " + ((pubkey == null) ? "<null>"
                                         : HexString.encode(pubkey)) );

    String method = (String) request.get( "method" );
    JSONArray params = (JSONArray) request.get( "params" );

    String msg = (String) params.get( 0 ); // hexstringified JSON object
    String sig = (String) params.get( 1 ); // hexstring sig(msg.getBytes())

    Secp256k1 curve = new Secp256k1();

    if (!curve.verifyECDSA( HexString.decode(sig),
                            SHA256.hash(msg.getBytes()),
                            HexString.decode(pubkey) ))
      return errorMessage( ERR_BAD_G,
        "Bad signature", "ECDSA verify fail",
        (pubkey == null) ? "<null>" : HexString.encode(pubkey) );

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(
                        new String(HexString.decode(msg), "UTF-8") );

    if (method.equals( "Published" ))
    {
      JSONArray evts = (JSONArray) request.get( "events" );

      for (int ii = 0; ii < evts.length(); ii++) {
        JSONObj obj = (JSONObject) evts.get( ii );
        String pk = (String) obj.get( "pubkey" );
        String ip = (String) obj.get( "ipfshash" );
        String bn = (String) obj.get( "block" );

        handlePublished( pk, ip, Long.parse(bn).longValue() );
      }
    }

    if (method.equals( "Fee" )) handleFee( json.get("fee") );
  }

  private handlePublished( String pubkey, String ipfshash, long blockNum )
  throws Exception
  {
    System.out.println( "receiverpubkey: " + pubkey );
    System.out.println( "ipfshash: " + ipfshash );

    // ipfshash is in base58 but our address is hexstring, so convert ipfshash
    // to hexstring by hashing it, then determine if last character matches

    String myaddr = new EthereumAddress( extId_.red() ).toString();
    char myLast = myaddr.charAt( myaddr.length() - 1 );

    String ipfsrehashed =
      new String( SHA256.hash(ipfshash.getBytes()) ).toUpperCase();

    char ipfslast = ipfsrehashed.charAt( ipfsrehashed.length() - 1 );

    if (myLast == ipfslast)
    {
      System.out.println( "match: " + myLast );

      IPFS.saveLocal( ipfshash );
      new HWM().set( blockNum );
      recalculate the running hash
      vote the result
    }
    else
      System.out.println( "no match: " + myLast + " != " + ipfslast );
  }

  private void handleFee( String newfee ) throws Exception
  {
    System.out.println( "new fee: " + newfee );
  }
}
