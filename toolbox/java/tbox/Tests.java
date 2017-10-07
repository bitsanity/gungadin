package tbox;

public class Tests
{
  public static void main( String[] args ) throws Exception
  {
    String[] noargs = new String[] {};

    // test classes fewest dependencies first
    ByteOps.main( noargs );
    HexString.main( noargs );
    SHA256.main( noargs );
    RIPEMD160.main( noargs );
    Base64.main( noargs );
    AES256.main( noargs );
    Secp256k1.main( noargs );
    ECIES.main( noargs );
    MessagePart.main( noargs );
    Message.main( noargs );

    // if nothing blew up ...
    System.out.println( "Tests.main(): PASS" );
  }
}
