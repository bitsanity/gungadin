package tbox;

import java.util.*;
import java.security.*;

public class SHA256
{
  public static byte[] hash( byte[] src ) throws Exception
  {
    MessageDigest md = MessageDigest.getInstance( SHANAME );
    md.update( src );
    return md.digest();
  }

  public static byte[] doubleHash( byte[] src ) throws Exception
  {
    return hash( hash(src) );
  }

  final private static String SHANAME = "SHA-256";

  // Test code ---------------------------------------------------------------
  public static void main( String[] args ) throws Exception
  {
    String[] srcs = new String[]
    {
      "The quick brown fox jumps over the lazy dog",
      "Test vector from febooti.com"
    };

    String[] expected = new String[]
    {
      "d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592",
      "077b18fe29036ada4890bdec192186e10678597a67880290521df70df4bac9ab"
    };

    for ( int ii = 0; ii < srcs.length; ii++ )
    {
      byte[] hashed = SHA256.hash( srcs[ii].getBytes() );
      byte[] exp = HexString.decode( expected[ii] );

      if (!Arrays.equals(hashed, exp))
        throw new Exception( "SHA256.main(): bad hash" );
    }

    // double hash test
    // https://en.bitcoin.it/wiki/Protocol_documentation#Hashes
    String msg = "hello";

    byte[] singlesha = HexString.decode(
      "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824" );

    byte[] doublesha = HexString.decode(
      "9595c9df90075148eb06860365df33584b75bff782a510c6cd4883a419833d50" );

    byte[] singres = hash( msg.getBytes() );

    if ( !Arrays.equals(singres, singlesha) )
      throw new Exception( "SHA256.main(): FAIL single SHA" );

    byte[] doubres = doubleHash( msg.getBytes() );

    if ( !Arrays.equals(doubres, doublesha) )
      throw new Exception( "SHA256.main(): FAIL double SHA" );
  }
}
