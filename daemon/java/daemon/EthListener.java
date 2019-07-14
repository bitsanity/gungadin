package daemon;

import java.net.*;
import java.util.Arrays;

import org.json.simple.*;
import org.json.simple.parser.*;

import tbox.*;

// responds to INCOMING messages from the Ethereum gateway (events)

// these messages dont have to be encrypted because its public anyway, but to
// deter rogue local processes we insist on signatures made by preconfigured
// keys made at deployment time

public class EthListener extends WorkerBase
{
  // require peer to sign messages sent into here with this pubkey
  private byte[] ethgwpubkey_;

  private String nodeAddr_; // for sharding need last char of address
  private Publications pubs_;
  private HWM hwm_;
  private Votes votes_;
  private IPFS ipfs_;
  private EthGateway gateway_;

  public EthListener( ServerSocket sock,
                      byte[] ethgwpubkey,
                      HWM hwm,
                      Votes votes,
                      String nodeAddr,
                      Publications pubs,
                      IPFS ipfs,
                      EthGateway gateway ) throws Exception
  {
    super( sock );
    ethgwpubkey_ = ethgwpubkey;
    hwm_ = hwm;
    votes_ = votes;
    nodeAddr_ = nodeAddr;
    pubs_ = pubs;
    ipfs_ = ipfs;
    gateway_ = gateway;
  }

  // @override
  // handle message from separate process that talks to Ethereum
  public JSONObject replyTo( JSONObject request ) throws Exception
  {
    // public key of the process, not a person
    byte[] pubkey = HexString.decode( (String)request.get( "id" ) );

    if (null == pubkey || (pubkey.length != 33 && pubkey.length != 65))
      return errorMessage( ERR_BAD_G,
          "Improper pubkey",
          "null or invalid length",
          ((pubkey == null) ? "<null>" : HexString.encode(pubkey)) );

    if (!Arrays.equals(pubkey, ethgwpubkey_))
      return errorMessage( ERR_BAD_G,
        "Key mismatch",
        "Peer pubkey does not match expected key",
        "expected: " + HexString.encode(ethgwpubkey_) +
        "received: " + ((pubkey == null) ? "<null>"
                                         : HexString.encode(pubkey)) );

    String method = (String) request.get( "method" );
    JSONArray params = (JSONArray) request.get( "params" );

    String msg = (String) params.get( 0 ); // hexstringified JSON object
    String sig = (String) params.get( 1 ); // hexstring sig(msg.getBytes())

    Secp256k1 curve = new Secp256k1();

    // remember: Ethereum uses Keccak instead of SHA256
    if (!curve.verifyECDSA( HexString.decode(sig),
                            Keccak256.hash(msg.getBytes()),
                            pubkey ))
      return errorMessage( ERR_BAD_G,
        "Bad signature", "ECDSA verify fail",
        (pubkey == null) ? "<null>" : HexString.encode(pubkey) );

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(
                        new String(HexString.decode(msg), "UTF-8") );

    System.out.println( "EthListener.replyTo: " + json.toString() );

    if (method.equals( "Published" ))
    {
      JSONArray evts = (JSONArray) json.get( "events" );

      for (int ii = 0; ii < evts.size(); ii++) {
        JSONObject obj = (JSONObject) evts.get( ii );
        String pk = (String) obj.get( "receiverpublickey" );
        String ip = (String) obj.get( "ipfshash" );
        Long bn = (Long) obj.get( "blocknum" );
        Long li = (Long) obj.get( "logindex" );

        handlePublished( pk, ip, bn, li );
      }
    }

    if (method.equals( "Fee" )) handleFee( (String)json.get("fee") );

    if (method.equals( "Voted" ))
      handleVoted( (String)json.get("voteraddr"),
                   Long.parseLong((String)json.get("blockNum")),
                   Integer.parseInt((String)json.get("logindex")),
                   (String)json.get("ipfsHash") );

    // should reply with daemon public key but ethgw ignores this response for
    // now. TODO make it check
    return okMessage( null );
  }

  private void handlePublished( String pubkey,
                                String ipfshash,
                                long blockNum,
                                long logindex ) throws Exception
  {
    System.out.println( "ipfshash: " + ipfshash +
                        ", block: " + blockNum +
                        ", index: " + logindex );

    // ipfshash is base58 but our address is hexstring, so convert ipfshash
    // to hexstring by hashing then determine if last character matches

    char myLast = nodeAddr_.charAt( nodeAddr_.length() - 1 );

    String ipfsrehashed = new String( Keccak256.hash(ipfshash.getBytes()) );
    char ipfslast = ipfsrehashed.charAt( ipfsrehashed.length() - 1 );

    if (Character.toUpperCase(myLast) == Character.toUpperCase(ipfslast))
    {
      ipfs_.saveLocal( ipfshash );
      hwm_.set( blockNum );
      pubs_.insert( ipfshash, blockNum, logindex );
      byte[] hash = pubs_.nextHash( HexString.decode(ipfshash) );

      System.out.println( "voting on block: " + blockNum );
      gateway_.vote( blockNum, HexString.encode(hash) );
    }
    else
      System.out.println( "no match: " + myLast + " != " + ipfslast );

    if (blockNum > hwm_.get())
      gateway_.setHWM( blockNum );
  }

  private void handleFee( String newfee ) throws Exception
  {
    System.out.println( "new fee: " + newfee );
  }

  private void handleVoted( String voteraddr,
                            long blockNum,
                            long logindex,
                            String ipfshash ) throws Exception
  {
    System.out.println( "handleVoted voter: " + voteraddr +
                        ", block: " + blockNum +
                        ", hash: " + ipfshash );

    String ipfsrehashed = new String( Keccak256.hash(ipfshash.getBytes()) );
    char ipfslast = ipfsrehashed.charAt( ipfshash.length() - 1 );
    char myLast = nodeAddr_.charAt( nodeAddr_.length() - 1 );
    if (Character.toUpperCase(myLast) != Character.toUpperCase(ipfslast))
    {
      System.out.println( "not my shard: " + ipfslast );
      return;
    }

    String myhash = votes_.myVoteOnBlock( blockNum );
    if (null != myhash)
    {
      if ( !myhash.equalsIgnoreCase(votes_.majorityVote(blockNum)) )
      {
        pubs_.clearAll();
        hwm_.set( 0L );
        gateway_.setHWM( 0L );
      }
    }
  }

}

