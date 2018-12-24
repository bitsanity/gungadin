package daemon;

import java.io.*;
import java.net.*;

import org.json.simple.*;
import org.json.simple.parser.*;

public class WorkerBase extends Thread
{
  public static final int ERR_CHALL = 1;
  public static final int ERR_EXCEP = 2;
  public static final int ERR_BAD_G = 3;
  public static final int ERR_K_UNK = 4;

  private Socket client_ = null;
  private boolean testmode_ = false;

  public WorkerBase( Socket client )
  {
    client_ = client;
  }

  public void run()
  {
    try {
      work();
    }
    catch( Exception e ) {
      e.printStackTrace();
    }

    try {
      client_.close();
    }
    catch( Exception e ) { }
  }

  public void work() throws Exception
  {
    BufferedReader rdr = new BufferedReader(
      new InputStreamReader(client_.getInputStream()) );

    PrintWriter pw = new PrintWriter( client_.getOutputStream(), true );

    JSONParser parser = null; // different parser for each interaction

    while (true)
    {
      String msg = rdr.readLine();
      if (null == msg) break;

      parser = new JSONParser();
      JSONObject jreq = (JSONObject) parser.parse( msg );

      JSONObject repl = replyTo( jreq );

      pw.println( repl.toJSONString() );
    }
  }

  public JSONObject errorMessage( int code,
                                  String message,
                                  String data,
                                  String id ) throws Exception
  {
    JSONObject errbody = new JSONObject();
    errbody.put( "code", new Integer(code) );
    errbody.put( "message", (null != message ? message : "null") );
    errbody.put( "data", (null != data ? data : "null") );

    JSONObject errmsg = new JSONObject();
    errmsg.put( "result", "null" );
    errmsg.put( "error", errbody );
    errmsg.put( "id", (null != id ? id : "null") );
    return errmsg;
  }

  // subclasses to override this with specific implementations
  public JSONObject replyTo( JSONObject request ) throws Exception {
    return null;
  }
}
