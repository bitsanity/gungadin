package tbox;

public class HexString
{
  public static String encode( byte[] bytes )
  {
    char[] hexChars = new char[ bytes.length * 2 ];

    int val = 0;
    for (int jj = 0; jj < bytes.length; jj++ )
    {
      val = bytes[jj] & 0xFF;
      hexChars[jj * 2] = hexArray[val >>> 4];
      hexChars[jj * 2 + 1] = hexArray[val & 0x0F];
    }

    return new String( hexChars );
  }

  public static byte[] decode( String hexString )
  {
    int len = hexString.length();
    byte[] data = new byte[len/2];

    for(int ii = 0; ii < len; ii += 2)
    {
      data[ii/2] = (byte) ((Character.digit(hexString.charAt(ii), 16) << 4) +
                            Character.digit(hexString.charAt(ii+1), 16));
    }

    return data;
  }

  private static char[] hexArray = "0123456789abcdef".toCharArray();

  // Test ---------------------------------------------------------------------
  public static void main( String[] args ) throws Exception
  {
    String test = "Hi There";
    String asHx = "4869205468657265";

    byte[] raw = test.getBytes();
    String hexEnc = encode( raw );

    if ( !hexEnc.equals(asHx) )
      throw new Exception( "HexString.main(): " );

    byte[] rev = decode( hexEnc );
    String revTst = new String( rev );

    if (!revTst.equalsIgnoreCase(test))
      throw new Exception( "HexString.main(): " );
  }
}
