package pubui;

import tbox.*;

public class RedKey
{
  private static RedKey instance_ = null;

  public static RedKey instance()
  {
    if (null == instance_)
      instance_ = new RedKey();

    return instance_;
  }

  private byte[] redkey_;

  private RedKey() {}

  public byte[] get()
  {
    return redkey_;
  }

  public void set( byte[] red ) throws Exception
  {
    Secp256k1 curve = new Secp256k1();
    if (!curve.privateKeyIsValid(red))
      throw new Exception( "bad key" );
    redkey_ = red;
  }

  public String getPublicKey()
  {
    Secp256k1 curve = new Secp256k1();
    byte[] pub = curve.publicKeyCreate( redkey_ );
    return "0x" + HexString.encode( pub );
  }

  public byte[] getPublicKeyBytes()
  {
    Secp256k1 curve = new Secp256k1();
    return curve.publicKeyCreate( redkey_ );
  }

}
