package gungadaemon;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import tbox.*;

public class KeyIdentity
{
  private static byte[] red_;

  private static KeyIdentity instance_ = null;
  public static KeyIdentity instance() throws Exception
  {
    return (null == instance_) ? instance_ = new KeyIdentity() : instance_;
  }

  private KeyIdentity() throws Exception
  {
    Path keypath = Paths.get( Globals.instance().get("keyfilepath") );

    if (!Files.exists(keypath))
    {
      System.out.println( "file " + keypath.toString() + " doesnt exist" );
      red_ = ECKeyPair.makeNew().privatekey();
      saveKey();
    }

    readKey();
  }

  public byte[] red() { return red_; }

  private static void saveKey() throws Exception
  {
    String pphr = Globals.instance().get( "persphrase" );
    AES256 crypto = new AES256( SHA256.hash(pphr.getBytes()) );
    Path keypath = Paths.get( Globals.instance().get("keyfilepath") );

    byte[] black1 = crypto.encrypt( Arrays.copyOfRange(red_, 0, 16) );
    byte[] black2 = crypto.encrypt( Arrays.copyOfRange(red_, 16, 32) );
    byte[] black = ByteOps.concat( black1, black2 );

    String blackStr = HexString.encode( black );
    Files.write( keypath, blackStr.getBytes(), StandardOpenOption.CREATE );
  }

  private static void readKey() throws Exception
  {
    String pphr = Globals.instance().get( "persphrase" );
    AES256 crypto = new AES256( SHA256.hash(pphr.getBytes()) );
    Path keypath = Paths.get( Globals.instance().get("keyfilepath") );

    byte[] black = HexString.decode( new String(Files.readAllBytes(keypath)) );

    byte[] red1 = crypto.decrypt( Arrays.copyOfRange(black, 0, 16) );
    byte[] red2 = crypto.decrypt( Arrays.copyOfRange(black, 16, 32) );
    red_ = ByteOps.concat( red1, red2 );
  }

}

