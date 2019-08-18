package daemon;

import java.sql.*;
import java.util.*;

public class Votes
{
  private static Connection db_ = null;

  private String myaddress_;

  public Votes( String fpath,
                String myaddress ) throws Exception
  {
    myaddress_ = myaddress.toLowerCase();

    if (null == db_)
    {
      Class.forName( "org.hsqldb.jdbc.JDBCDriver" );

      db_ = DriverManager.getConnection(
        "jdbc:hsqldb:file:" + fpath + ";shutdown=true", "SA", "gungadaemon" );
    }

    String sql = "CREATE TABLE IF NOT EXISTS Votes " +
                   "(Voter VARCHAR(64) INDEX," +
                   " Hash VARCHAR(64) NOT NULL, " +
                   " BlockNumber INTEGER NOT NULL, " +
                   " LogIndex INTEGER NOT NULL)";

    Statement stmt = db_.createStatement();
    stmt.executeUpdate( sql );
    stmt.close();
  }

  public void insert( String voteraddr,
                      String hash,
                      long blocknum,
                      long logindex )
  throws Exception
  {
    String sql = "INSERT INTO Votes(Voter,Hash,BlockNumber,LogIndex) " +
                 "VALUES ('" + voteraddr.toLowerCase() + "','" +
                    hash + "'," +
                    blocknum + "," +
                    logindex + ")";

    Statement stmt = db_.createStatement();
    stmt.executeUpdate( sql );
    stmt.close();
  }

  public String myVoteOnBlock( long blocknum ) throws Exception
  {
    String result = null;

    String sql = "SELECT Hash FROM Votes WHERE Voter='" + myaddress_ +
                 "' AND BlockNumber=" + blocknum;

    Statement stmt = db_.createStatement();
    ResultSet rs = stmt.executeQuery( sql );
    while (rs.next())
      result = rs.getString(1);

    rs.close();
    stmt.close();

    return result;
  }

  public long latestBlock() throws Exception
  {
    long result = 0;

    String sql = "SELECT MAX(BlockNumber) FROM Votes";
    Statement stmt = db_.createStatement();

    ResultSet rs = stmt.executeQuery( sql );
    while (rs.next())
      result = rs.getLong(1);

    rs.close();
    stmt.close();

    return result;
  }

  public String majorityVote( long blocknum ) throws Exception
  {
    String result = null;

    String sql = "SELECT Hash FROM Votes WHERE BlockNumber=" + blocknum;
    Statement stmt = db_.createStatement();

    Hashtable<String,Integer> ht = new Hashtable<String,Integer>();

    String hash = null;
    int count = 0;
    ResultSet rs = stmt.executeQuery( sql );

    while (rs.next())
    {
      hash = rs.getString( 1 );

      if (null == ht.get(hash))
        ht.put( hash, 0 );

      ht.put( hash, ht.get( hash ) + 1 );
      count++;
    }

    rs.close();
    stmt.close();

    Enumeration<String> htkeys = ht.keys();
    while (htkeys.hasMoreElements())
    {
      String elkey = htkeys.nextElement();
      if (ht.get(elkey) > count / 2) // simple majority
        result = elkey;
    }

    return result;
  }

  public static void main( String[] args ) throws Exception
  {
    if (null == args || 2 < args.length) {
      System.out.println( "Usage: <fpath> <myaddress>" );
      return;
    }

    Votes mv = new Votes( args[0], args[1] );

    org.hsqldb.DatabaseManager.closeDatabases(0);
  }
}

