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

  private ServerSocket sock_ = null;
  private boolean testmode_ = false;

  public WorkerBase( ServerSocket sock )
  {
    sock_ = sock;
  }

  public void run()
  {
    while( true )
    {
      Socket client = null;
      PrintWriter pw = null;

      try {
        client = sock_.accept();

        BufferedReader rdr = new BufferedReader(
          new InputStreamReader(client.getInputStream()) );

        pw = new PrintWriter( client.getOutputStream(), true );

        JSONParser parser = null;
        while (true)
        {
          String msg = rdr.readLine();
          if (null == msg) break;

          parser = new JSONParser();
          JSONObject jreq = (JSONObject) parser.parse( msg );
          JSONObject repl = replyTo( jreq );
          pw.println( repl.toJSONString() );

          pw.flush();
          pw.close();
        }
      }
      catch( Exception e ) {
        e.printStackTrace();
      }
      finally {
        try{ client.close(); } catch( Exception e ) {}
      }

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

  public JSONObject okMessage( String id ) throws Exception
  {
    JSONObject okBody = new JSONObject();
    okBody.put( "code", new Integer(200) );
    okBody.put( "message", "null" );
    okBody.put( "data", "null" );

    JSONObject okMsg = new JSONObject();
    okMsg.put( "result", "null" );
    okMsg.put( "error", okBody );
    okMsg.put( "id", (null != id ? id : "null") );
    return okMsg;
  }

  // subclasses will override this with specific implementations
  public JSONObject replyTo( JSONObject request ) throws Exception {
    return null;
  }
}

