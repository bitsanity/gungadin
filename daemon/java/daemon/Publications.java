package daemon;

import java.sql.*;
import java.util.*;

import tbox.*;

// Remembers data for all the Publication events pertaining to this node. Up
// to the caller to manage sharding - we store anything

public class Publications
{
  private static Connection db_ = null;

  private byte[] lastResult_ = null;

  public Publications( String fpath ) throws Exception
  {
    if (null == db_)
    {
      Class.forName( "org.hsqldb.jdbc.JDBCDriver" );

      db_ = DriverManager.getConnection(
        "jdbc:hsqldb:file:" + fpath + ";shutdown=true", "SA", "gungadaemon" );

      String sql = "CREATE TABLE IF NOT EXISTS Publications " +
                   "( IPFSHashStr VARCHAR(64) PRIMARY KEY, " +
                   "  BlockNumber INTEGER NOT NULL, " +
                   "  LogIndex INTEGER NOT NULL " +
                   " )";

      Statement stmt = db_.createStatement();
      stmt.executeUpdate( sql );
      stmt.close();

      fullHashSum();
    }
  }

  public long insert( String ipfs, long blocknum, int logix) throws Exception
  {
    if (null == ipfs || 0 == ipfs.length() || 0 >= blocknum)
      throw new Exception( "Invalid INSERT param '" + ipfs +
                           "'," + blocknum + "," + logix );

    String sql =
      "INSERT INTO Publications (IPFSHashStr,BlockNumber,LogIndex) " +
      "VALUES ('" + ipfs + "'," + blocknum + "," + logix + ")"

    try {
      Statement stmt = db_.createStatement();
      stmt.executeUpdate( sql );
    }
    finally {
      stmt.close();
    }
  }

  // calculate hash of all IPFSHashes in the DB (such as on startup or if the
  // node gets out-of-sync somehow)
  public byte[] fullHashSum() throws Exception
  {
    lastResult_ = null;

    String sql =
      "SELECT IPFSHashStr FROM Publications ORDER BY BlockNumber,LogIndex";

    Statement stmt = db_.createStatement();
    ResultSet rs = stmt.executeQuery( sql );

    String ipfs;
    while (rs.next())
    {
      ipfs = rs.getString( 1 );
      nextHash( HexString.decode(ipfs) );
    }

    rs.close();
    stmt.close();

    return lastResult_;
  }

  public byte[] nextHash( byte[] nextval ) throws Exception
  {
    if (null != lastResult_)
      lastResult_ = Kekkac256.hash( ByteOps.concat(lastResult_, nextval) );
    else
      lastResult_ = Kekkac256.hash( nextval );

    return lastResult_;
  }

  public static void main( String[] args ) throws Exception
  {
    String ipfs = args[0];
    long blocknum = Long.parseLong(args[1]).longValue();
    int index = Integer.parseInt(args[1]).intValue();

    if (null == args || 0 == args.length) {
      System.out.println( "Usage: <ipfshash> <blocknum> <logindex>" );
      return;
    }

    Publications pb = new Publications();
    pb.insert( ipfs, blocknum, index );

    org.hsqldb.DatabaseManager.closeDatabases(0);

    System.out.println( "Inserted: " + ipfs + ", " + blocknum + ", " + index );
  }
}

