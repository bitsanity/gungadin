package tbox;

import java.util.Arrays;

public class ByteOps
{
  public static byte[] xor( byte[] a, byte[] b )
  {
    if ( a.length != b.length ) return null;

    byte[] result = new byte[ a.length ];
    for ( int ii = 0; ii < result.length; ii++ )
      result[ii] = (byte)(a[ii] ^ b[ii]);

    return result;
  }

  public static byte[] concat( byte[] a, byte b )
  {
    byte[] ba = new byte[] { b };
    return concat( a, ba );
  }

  public static byte[] concat( byte[] a, byte[] b )
  {
    if (a == null) return b;
    if (b == null) return a;

    byte[] result = new byte[ a.length + b.length ];
    System.arraycopy( a, 0, result, 0, a.length );
    System.arraycopy( b, 0, result, a.length, b.length );
    return result;
  }

  public static byte[] prepend( byte b, byte[] ba )
  {
    byte[] result = new byte[ ba.length + 1 ];

    result[0] = b;
    System.arraycopy( ba, 0, result, 1, ba.length );

    return result;
  }

  public static byte[] dropFirstByte( byte[] a )
  {
    if (null == a || 0 == a.length) return a;
    return Arrays.copyOfRange( a, 1, a.length );
  }

  public static byte[] padLeft( byte[] a, int padcount, byte pad )
  {
    byte[] padarray = new byte[ padcount ];
    for (int ii = 0; ii < padcount; ii++)
      padarray[ii] = pad;

    return concat( padarray, a );
  }

  public static byte[] padRight( byte[] a, int padcount, byte pad )
  {
    byte[] padarray = new byte[ padcount ];
    for (int ii = 0; ii < padcount; ii++)
      padarray[ii] = pad;

    return concat( a, padarray );
  }

  public static void main( String[] args ) throws Exception
  {
    byte[] a   = new byte[] { (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03 };
    byte[] b   = new byte[] { (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07 };
    byte[] exp = new byte[] { (byte)0x04, (byte)0x04, (byte)0x04, (byte)0x04 };

    byte[] act = xor( a, b );
    for (int ii = 0; ii < a.length; ii++)
      assert act[ii] == exp[ii] : "xor FAIL";

    byte[] cat = concat( a, b );
    assert cat.length == (a.length + b.length) : "concat FAIL";
    assert cat[cat.length - 1] == (byte)0x07 : "cat FAIL";

    byte hdr = (byte)0xFF;
    byte[] pp = prepend( hdr, a );

    assert pp[0] == (byte)0xFF && pp[1] == a[0] : "prepend FAIL";

    byte[] lopped = dropFirstByte( a );
    assert (byte)0x01 == lopped[0] : "dropFirstByte FAIL";

    byte[] padded = padRight( a, 0, (byte)0 );
    assert padded.length == a.length;
    padded = padRight( a, 1, (byte)0 );
    assert padded.length == a.length + 1 : "padRight FAIL";
    assert padded[ padded.length - 1 ] == (byte)0: "padRight FAIL";

    padded = padLeft( b, 2, (byte)0 );
    assert padded[0] == padded[1] && padded[0] == (byte)0 : "padLeft FAIL";
    assert padded[2] != (byte)0 : "padLeft overrun FAIL";
  }
}

