package tbox;

import javax.xml.bind.DatatypeConverter;

public class Base64
{
  final public static String DEFAULT_ENCODING = "UTF-8";

  public static String encode( String src ) throws Exception
  {
    return DatatypeConverter.printBase64Binary(
             src.getBytes(DEFAULT_ENCODING) );
  }

  public static String encode( byte[] bytes ) throws Exception
  {
    return DatatypeConverter.printBase64Binary(bytes);
  }

  public static byte[] decode( String src ) throws Exception
  {
    return DatatypeConverter.parseBase64Binary( src );
  }

  // -----------------------------------------------
  // tests from https://tools.ietf.org/html/rfc4648
  // -----------------------------------------------
  public static void main( String[] args ) throws Exception
  {
    String[] srcs = new String[]
    {
      "",
      "f",
      "fo",
      "foo",
      "foob",
      "fooba",
      "foobar"
    };

    String[] exps = new String[]
    {
      "",
      "Zg==",
      "Zm8=",
      "Zm9v",
      "Zm9vYg==",
      "Zm9vYmE=",
      "Zm9vYmFy"
    };

    for (int ii = 0; ii < srcs.length; ii++)
      if ( !encode(srcs[ii]).equals(exps[ii]) )
        throw new Exception( "Base64.main(): enc[" + ii + "] FAIL" );

    for (int jj = 0; jj < exps.length; jj++)
    {
      String dec = new String( decode(exps[jj]), "UTF-8" );
      if (!dec.equals(srcs[jj]))
        throw new Exception( "Base64.main(): dec[" + jj + "] FAIL" );
    }
  }
}
