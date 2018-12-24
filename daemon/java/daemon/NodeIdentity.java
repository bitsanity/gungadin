package daemon;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.*;
import java.util.*;

import tbox.*;

public class NodeIdentity
{
  private String pphr_;
  private Path keypath_;
  private byte[] red_;

  public NodeIdentity( String pphr, String fpath ) throws Exception
  {
    if (null == pphr || 0 == pphr.length())
      throw new Exception( "Invalid passphrase; " + pphr );

    if (null == fpath || 0 == fpath.length())
      throw new Exception( "Invalid fpath; " + fpath );

    pphr_ = pphr;
    keypath_ = Paths.get( fpath );

    if (!Files.exists(keypath_))
    {
      red_ = ECKeyPair.makeNew().privatekey();
      saveKey();
    }

    readKey();
  }

  private static void overwrite( byte[] newred, String pphr, String fpath )
  throws Exception
  {
    NodeIdentity ni = new NodeIdentity( pphr, fpath );
    ni.red_ = newred;
    ni.saveKey();
  }

  public byte[] red() { return red_; }

  private void saveKey() throws Exception
  {
    AES256 crypto = new AES256( SHA256.hash(pphr_.getBytes()) );

    byte[] black1 = crypto.encrypt( Arrays.copyOfRange(red_, 0, 16) );
    byte[] black2 = crypto.encrypt( Arrays.copyOfRange(red_, 16, 32) );
    byte[] black = ByteOps.concat( black1, black2 );

    String blackStr = HexString.encode( black );
    Files.write( keypath_, blackStr.getBytes(), StandardOpenOption.CREATE );
  }

  private void readKey() throws Exception
  {
    AES256 crypto = new AES256( SHA256.hash(pphr_.getBytes()) );

    byte[] black = HexString.decode( new String(Files.readAllBytes(keypath_)) );

    byte[] red1 = crypto.decrypt( Arrays.copyOfRange(black, 0, 16) );
    byte[] red2 = crypto.decrypt( Arrays.copyOfRange(black, 16, 32) );

    red_ = ByteOps.concat( red1, red2 );
  }

  public static void main( String[] args ) throws Exception
  {
    // <passphrase> <filepath>
    if (args.length != 2)
      throw new Exception( "Usage: <pphrase> <filepath>" );

    NodeIdentity nid = new NodeIdentity( args[0], args[1] );

    ECKeyPair eckp = new ECKeyPair( nid.red() );
    System.out.println( HexString.encode(eckp.publickey()) );
  }
}

