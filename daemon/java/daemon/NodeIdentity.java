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
    byte[] black = crypto.encrypt( red_ );
    String blackStr = HexString.encode( black );
    Files.write( keypath_, blackStr.getBytes(), StandardOpenOption.CREATE );
  }

  private void readKey() throws Exception
  {
    AES256 crypto = new AES256( SHA256.hash(pphr_.getBytes()) );
    byte[] black = HexString.decode( new String(Files.readAllBytes(keypath_)) );
    red_ = crypto.decrypt( black );
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

