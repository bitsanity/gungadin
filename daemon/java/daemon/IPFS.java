package daemon;

import java.io.*;

import io.ipfs.api.*;
import io.ipfs.cid.*;
import io.ipfs.multihash.Multihash;
import io.ipfs.multiaddr.MultiAddress;

import org.json.simple.*;
import org.json.simple.parser.*;

import tbox.*;

public class IPFS
{
  private io.ipfs.api.IPFS ipfs_;

  public IPFS() throws Exception
  {
    ipfs_ = new io.ipfs.api.IPFS( "/ip4/127.0.0.1/tcp/5001" );
  }

  public String push( String data ) throws Exception
  {
    NamedStreamable.ByteArrayWrapper file =
      new NamedStreamable.ByteArrayWrapper( data.getBytes() );

    return ipfs_.add( file ).get( 0 ).hash.toString();
  }

  public boolean saveLocal( String ipfsHash ) throws Exception
  {
    Multihash filePointer = Multihash.fromBase58( ipfsHash );
    ipfs_.pin.add( filePointer );

    return true;
  }

  public static void main( String[] args ) throws Exception
  {
    System.out.println( "IPFS.main: PASS" );
  }
}

