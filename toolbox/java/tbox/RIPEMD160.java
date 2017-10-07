package tbox;

public class RIPEMD160
{
  // ensure java.library.path includes ...
  static
  {
    try
    {
      System.loadLibrary( "tbox" ); // libtbox.so
    }
    catch( UnsatisfiedLinkError ule )
    {
      System.err.println( "RIPEMD160: Cannot find DLL: " + ule );
      System.exit( 1 );
    }
  }

  public static native byte[] digest( byte[] src );

  // Test --------------------------------------------------------------------
  public static void main( String[] args ) throws Exception
  {
    // test vectors from
    //   http://homes.esat.kuleuven.be/~bosselae/ripemd160.html

    String[] msgs = new String[] {
      "",
      "a",
      "abc",
      "message digest",
      "abcdefghijklmnopqrstuvwxyz",
      "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq",
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789",
        "1234567890123456789012345678901234567890"
      + "1234567890123456789012345678901234567890",
      "The quick brown fox jumps over the lazy dog" };

   String[] exps = new String[] {
     "9c1185a5c5e9fc54612808977ee8f548b2258d31",
     "0bdc9d2d256b3ee9daae347be6f4dc835a467ffe",
     "8eb208f7e05d987a9b044a8e98c6b087f15a0bfc",
     "5d0689ef49d2fae572b881b123a85ffa21595f36",
     "f71c27109c692c1b56bbdceb5b9d2865b3708dbc",
     "12a053384a9c0c88e405a06c27dcf49ada62eb2b",
     "b0e20b6e3116640286ed3a87a5713079b21f5189",
     "9b752e45573d4b39f4dbd3323cab82bf63326bfb",
     "37f332f68db77bd9d7edd4969571ad671cf9dd3b" };

    for (int ii = 0; ii < msgs.length; ii++)
    {
      byte[] result = RIPEMD160.digest( msgs[ii].getBytes() );
      String sreslt = HexString.encode( result );

      if (!sreslt.equalsIgnoreCase( exps[ii] ))
      {
        System.out.println( "RMD160.main() FAIL\nHash of " + msgs[ii] +
                            "\n\tgot: " + sreslt +
                            "\n\texp: " + exps[ii] );
        return;
      }
    }

    System.out.println( "RMD160.main(): PASS" );

  } // end main
}
