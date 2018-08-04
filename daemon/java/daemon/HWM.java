package daemon;

import java.sql.*;
import java.util.*;

// High Water Mark : latest Ethereum block number that contained an event
// we processed. Its ok to reprocess old events, not ok to miss events

public class HWM
{
  private static Connection db_ = null;

  public HWM( String fpath ) throws Exception
  {
    if (null == db_)
    {
      Class.forName( "org.hsqldb.jdbc.JDBCDriver" );

      db_ = DriverManager.getConnection(
        "jdbc:hsqldb:file:" + fpath + ";shutdown=true", "SA", "gungadaemon" );
    }

    String sql = "CREATE TABLE IF NOT EXISTS HWM " +
                 "( BlockNum BIGINT PRIMARY KEY )";
    Statement stmt = db_.createStatement();
    stmt.executeUpdate( sql );
    stmt.close();

    int rows = 0;
    stmt = db_.createStatement();
    sql = "SELECT COUNT(*) FROM HWM";
    ResultSet rs = stmt.executeQuery( sql );
    while (rs.next())
      rows = rs.getInt(1);
    rs.close();
    stmt.close();

    if (0 == rows)
    {
      sql = "INSERT INTO HWM (BlockNum) VALUES (0)";
      stmt = db_.createStatement();
      stmt.executeUpdate( sql );
      stmt.close();
    }
  }

  // yes, hsqldb caches file
  public long get() throws Exception
  {
    long result = 0;

    Statement stmt = db_.createStatement();
    String sql = "SELECT BlockNum FROM HWM";

    ResultSet rs = stmt.executeQuery( sql );
    while (rs.next())
      result = rs.getLong(1);

    rs.close();
    stmt.close();

    return result;
  }

  // assume humanity will be long since dead by the time Ethereum block number
  // reaches a count of 2**63 - 1
  public void set( long newHigh ) throws Exception
  {
    String sql = "UPDATE HWM SET BlockNum=" + newHigh;

    Statement stmt = db_.createStatement();
    stmt.executeUpdate( sql );
    stmt.close();
  }

  public static void main( String[] args ) throws Exception
  {
    if (null == args || 0 == args.length) {
      System.out.println( "Usage: <get|set> [hwm]" );
      return;
    }

    HWM hwm = new HWM();

    if (args[0].equalsIgnoreCase("set"))
      hwm.set( Long.parseLong(args[1]) );

    System.out.println( "HWM: " + hwm.get() );

    org.hsqldb.DatabaseManager.closeDatabases(0);
  }
}

