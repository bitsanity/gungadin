package daemon;

import java.io.*;
import java.net.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class IPFS
{
  private static final String UAGENT = "Mozilla/5.0";

  public IPFS() {}

  public static String pushFile( String fpath ) throws Exception
  {
    String result = null;

    System.out.println( "IPFS.pushFile pushing " + fpath );

    URL url = new URL( "http://localhost:5001/api/v0/add" +
                       "?quieter=true&progress=false&pin=false&path=" +
                       fpath.toString() );

    HttpURLConnection cx = (HttpURLConnection) url.openConnection();
    cx.setRequestMethod( "GET" );
    cx.setRequestProperty( "User-Agent", UAGENT );

    int response = cx.getResponseCode();
    if (response != 200) throw new Exception("IPFS add returned: " + response);

    BufferedReader in =
      new BufferedReader(new InputStreamReader(cx.getInputStream()));

    String inputLine;
    StringBuffer buff = new StringBuffer();

    while ((inputLine = in.readLine()) != null)
      buff.append(inputLine);

    in.close();

    // strip off the http header

    result = buff.toString();
    int ix = result.indexOf( "\n\n" );
    result = result.substring( ix + 2 );

    // {
    //   "Name": "<string>"
    //   "Hash": "<string>"
    //   "Bytes": "<int64>"
    // }
    JSONObject reply = (JSONObject)( new JSONParser().parse(result) );

    result = (String)reply.get( "Hash" );
    return result;
  }

  public static boolean saveLocal( String ipfsHash ) throws Exception
  {
    boolean result = true;

    String saveas = Globals.instance().get( "filecachedir" ) + "/" + ipfsHash;

    System.out.println( "IPFS.saveLocal saving " + saveas );

    URL url = new URL( "http://localhost:5001/api/v0/get?" +
                       "arg=" + ipfsHash +
                       "&output=" + saveas );

    HttpURLConnection cx = (HttpURLConnection) url.openConnection();
    cx.setRequestMethod( "GET" );
    cx.setRequestProperty( "User-Agent", UAGENT );

    int response = cx.getResponseCode();

    if (response != 200) result = false;

    pushFile( saveas );

    return result;
  }

  public static void main( String[] args ) throws Exception
  {
    if (null == args || 2 > args.length)
    {
      System.out.println( "Usage:\n" +
        "To push a file to IPFS and print its hash:\n" +
        "\rpush <fpath>\n\n" +
        "To fetch a file from IPFS and dump to file:\n" +
        "\rfetch <hash> <fpath>" );
      return;
    }
  }

}

