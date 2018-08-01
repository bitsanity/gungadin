package daemon;

import java.net.ServerSocket;
import java.util.*;

// runs in the background listening for messages from the UI or events from the
// ethereum blockchain - on different ports

public class Daemon
{
  private boolean keepGoing_ = true;

  public Daemon( int uiport, int egwport ) throws Exception
  {

    if( 0 >= uiport ) throw new Exception("invalid uiport: " + uiport );
    if( 0 >= egwport ) throw new Exception("invalid egwport: " + uiport );

    ServerSocket ss = new ServerSocket( uiport );
    ServerSocket st = new ServerSocket( egwport );

    while( keepGoing_ )
    {
      new KGWorker( ss.accept() ).start();

      Runnable ethgw = () -> new EthGateway( st.accept() );
      new Thread( ethgw ).start();
    }

    ss.close();
    st.close();
  }

  public static void main( String[] args ) throws Exception
  {
    for (int ii = 0; ii < args.length; ii++)
    {
      String[] parts = args[ii].split("=");

      if (null != parts && 2 == parts.length)
        Globals.instance().put( parts[0], parts[1] );
    }

    int uiport = Integer.parseInt( Globals.instance().get("uiport") );
    if (0 >= uiport) uiport = 8804; // default

    int egwport = Integer.parseInt( Globals.instance().get("egwport") );
    if (0 >= egwport) egwport = 8805; // default

    String pphr = Globals.instance().get("pphrase");
    if (null == pphr || 0 == pphr.length())
    {
      Scanner s = new Scanner( System.in );
      pphr = scanner.next();
      scanner.close();

      if (null == pphr || 0 == pphr.length())
        throw new Exception( "No passphrase provided." );
    }

    String keyfilepath = Globals.instance().get( "keyfilepath" );
    if (null == keyfilepath || 0 == keyfilepath.length())
      Globals.instance().put( "keyfilepath", NodeIdentity.DEFAULT_KEYFILEPATH );

    System.out.println(
      "daemon:\n" +
      "\r[uiport = " + Globals.instance().get("uiport") + "]\n" +
      "\r[egwport = " + Globals.instance().get("egwport") + "]\n" +
      "\r[pphrase = ***]\n" +
      "\r[keyfilepath =" + Globals.instance().get("keyfilepath") + "]\n" +
    );

    Daemon matt = new Daemon( uiport, egwport );
  }
}

