package gungadaemon;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.*;
import java.util.*;

import tbox.*;

public class Daemon
{
  public Daemon() throws Exception
  {
    boolean keepGoing = true;

    int port = Integer.parseInt( Globals.instance().get("port") );
    if( 0 >= port ) throw new Exception("invalid port: " + port );

    ServerSocket ss = new ServerSocket( port );

    while( keepGoing )
      new KGWorker( ss.accept() ).start();

    ss.close();
  }

  public static void main( String[] args ) throws Exception
  {
    for (int ii = 0; ii < args.length; ii++)
    {
      String[] parts = args[ii].split("=");

      if (null != parts && 2 == parts.length)
        Globals.instance().put( parts[0], parts[1] );
    }

    String port = Globals.instance().get("port");
    if (null == port || 0 == port.length())
      Globals.instance().put( "port", "8008" );

    persPhrase();
    keyfile();
    ethphrase();
    checkwallet();

    System.out.println(
      "phaethon:\n" +
      "\r[port=" + Globals.instance().get("port") + "]\n" +
      "\r[keyfilepath=" + Globals.instance().get("keyfilepath") + "]\n" +
      "\r<publisherSCA=" + Globals.instance().get("publisherSCA") + ">\n" +
      "\r<ethwalletpath=" + Globals.instance().get("ethwalletpath") + ">\n" );

    Daemon phaethon = new Daemon();
  }

  private static void persPhrase() throws Exception
  {
    String pphrase = Globals.instance().get( "persphrase" );

    boolean redo = (pphrase == null || 0 == pphrase.length());

    while (redo)
    {
      System.out.print( "personal key passphrase: " );
      Scanner sc = new Scanner(System.in);
      String answer = sc.next();
      System.out.println("\n");

      if (null != answer && 0 < answer.length())
      {
        Globals.instance().put( "persphrase", answer );
        redo = false;
      }
    }
  }

  private static void keyfile() throws Exception
  {
    Path raw = null;

    String keypath = Globals.instance().get( "keyfilepath" );

    if (null == keypath || 0 == keypath.length())
    {
      raw = Paths.get( System.getenv("HOME"),
                       ".gungadin",
                       "blackkey" );

      Globals.instance().put( "keyfilepath", raw.toString() );
    }

    KeyIdentity.instance();
  }

  private static void ethphrase() throws Exception
  {
    String pphrase = Globals.instance().get("ethphrase");

    boolean redo = (pphrase == null || 0 == pphrase.length());

    while (redo)
    {
      System.out.print( "Ethereum wallet passphrase: " );
      Scanner sc = new Scanner(System.in);
      String answer = sc.nextLine();

      if (null != answer && 0 < answer.length())
      {
        Globals.instance().put( "ethphrase", answer );
        redo = false;
      }

      System.out.println("\n");
    }
  }

  private static void checkwallet() throws Exception
  {
    String wpath = Globals.instance().get( "ethwalletpath" );

    if (null == wpath || 0 == wpath.length())
      throw new Exception( "need ethwalletpath" );

    EthAccount.instance();
  }
}

