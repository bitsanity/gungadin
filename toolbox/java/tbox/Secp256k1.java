package tbox;

// java wrapper around a C wrapper around a well-known C library
// implementing EC operations.
//
// pubkey = 65 bytes if uncompressed, or 33 bytes compressed
// seckey = 32 bytes / 256 bits
// 'Tweak' is a 32-byte array 'N'

public class Secp256k1
{
  // ensure java.library.path includes ...
  static
  {
    try
    {
      System.loadLibrary( "tbox" ); // => libtbox.so
    }
    catch( UnsatisfiedLinkError ule )
    {
      System.err.println( "Secp256k1: Cannot find DLL" + ule );
      System.exit( 1 );
    }
  }

  public Secp256k1()
  {
    int res = this.resetContext();
  }

  // library creates/rerandomizes data structure of precomputed tables, etc
  // to make operations faster. only needs to be done once
  native int resetContext();

  // check private key is valid
  public native boolean privateKeyIsValid( byte[] in_seckey );

  // Q = d . G
  public native byte[] publicKeyCreate( byte[] in_seckey );

  // d + N
  public native byte[] privateKeyAdd( byte[] in_seckey, byte[] in_tweak );

  // d * N
  public native byte[] privateKeyMult( byte[] in_seckey, byte[] in_tweak );

  // Q + N
  public native byte[] publicKeyAdd( byte[] in_pubkey, byte[] in_tweak );

  // Q * N
  public native byte[] publicKeyMult( byte[] in_pubkey, byte[] in_tweak );

  // sign hash of message with seckey
  public native byte[] signECDSA( byte[] hash32, byte[] in_seckey );

  // verify signature is correct for hash of message
  public native boolean
  verifyECDSA( byte[] signature, byte[] hash32, byte[] in_pubkey );

  // make recoverable signature of message hash using seckey
  public native byte[] signECDSARecoverable( byte[] hash32, byte[] in_seckey );

  // verify recoverable signature is correct for hash of message
  public native boolean
  verifyECDSARecoverable( byte[] signature, byte[] hash32, byte[] in_pubkey );

  // recover public key from sig and hash of message
  public native byte[] recoverPublicKey( byte[] msghash32, byte[] sig );

  // test --------------------------------------------------------------------
  public static void main( String[] args ) throws Exception
  {
    Secp256k1 crypto = new Secp256k1();

    // https://en.bitcoin.it/wiki/
    // Technical_background_of_version_1_Bitcoin_addresses

    String seckeyS =
      "18E14A7B6A307F426A94F8114701E7C8E774E7F9A47E2C2035DB29A206321725";
    String exppubS = "04" +
      "50863AD64A87AE8A2FE83C1AF1A8403CB53F53E486D8511DAD8A04887E5B2352" +
      "2CD470243453A299FA9E77237716103ABC11A1DF38855ED6F2EE187E9C582BA6";

    byte[] seckey = HexString.decode( seckeyS );

    if ( !crypto.privateKeyIsValid(seckey) )
      throw new Exception( "Secp256k1.main(): bad key" );

    // test public key creation
    byte[] pubkey = crypto.publicKeyCreate( seckey );
    String pubkeyS = HexString.encode( pubkey );

    if (!exppubS.equalsIgnoreCase(pubkeyS))
      throw new Exception( "Secp256k1.main(): bad pubkey" );

    // test message signing and verifying
    String message = "This is an important sentence.";

    byte[] hash32 = SHA256.hash( message.getBytes("UTF-8") );
    byte[] sig = crypto.signECDSA( hash32, seckey );

    if ( !crypto.verifyECDSA(sig, hash32, pubkey) )
      throw new Exception( "Secp256k1.main(): verify FAIL" );
  }
}
