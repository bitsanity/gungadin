package tbox;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils
{
  public static byte[] getFileBytes( Path fpath ) throws Exception
  {
    return Files.readAllBytes( fpath );
  }

  public static void main( String[] args ) throws Exception
  {
    if (1 != args.length)
    {
      System.err.println( "FileUtils.main(): Usage: <filepath>" );
      return;
    }

    Path pth = Paths.get( args[0] );
    byte[] rawbytes = getFileBytes( pth );

    String b64 = Base64.encode( rawbytes );

    System.out.println( b64 );
  }

}
