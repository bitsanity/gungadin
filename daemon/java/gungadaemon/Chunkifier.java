package gungadaemon;

public class Chunkifier
{
  // max length of a string in Java
  public static int CHUNKSIZE = Integer.MAX_VALUE; // 2^31 - 1

  public static String[] chunkify( String big ) throws Exception
  {
    return new String[] { big };
  }

  public static String dechunkify( String[] parts ) throws Exception
  {
    assert( 1 == parts.length );
    return parts[0];
  }
}
