package gungadaemon;

import java.io.*;
import java.net.*;
import org.json.simple.*;
import org.json.simple.parser.*;

// thing that adds files to and fetches files from IPFS using the HTTP API
// that should already be running on localhost port 5001

public class IPFS
{
  private static final String UAGENT = "Mozilla/5.0";

  public IPFS() {}

  public static String pushFile( String fpath ) throws Exception
  {
    String result = null;

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

  public static boolean getFile( String ipfsHash,
                                 String fpath ) throws Exception
  {
    boolean result = true;

    URL url = new URL( "http://localhost:5001/api/v0/get?" +
                       "arg=" + ipfsHash +
                       "&output=" + fpath.toString() );

    HttpURLConnection cx = (HttpURLConnection) url.openConnection();
    cx.setRequestMethod( "GET" );
    cx.setRequestProperty( "User-Agent", UAGENT );

    int response = cx.getResponseCode();

    if (response != 200) result = false;

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

