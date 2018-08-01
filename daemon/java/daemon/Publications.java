package daemon;

import java.sql.*;
import java.util.*;

import tbox.*;

// Remembers data for all the Publication events pertaining to this node. Up
// to the caller to manage sharding - we store anything

public class Publications
{
  private static Connection db_ = null;

  public Publications() throws Exception
  {
    if (null == db_)
    {
      Class.forName( "org.hsqldb.jdbc.JDBCDriver" );

      db_ = DriverManager.getConnection(
        "jdbc:hsqldb:file:publicationsdb;shutdown=true",
        "SA", "gungadaemon" );
    }

    // subtlety here: if someone publishes the exact same file more than once
    // it will be the same hash and the update will be ignored, which is fine
    // since it has to be exactly the same file (bit for bit) - if someone
    // insists on paying more than once then that is okay by us

    String sql = "CREATE TABLE IF NOT EXISTS Publications " +
                 "( IPFSHashStr VARCHAR(64) PRIMARY KEY, " +
                 "  BlockNumber INTEGER NOT NULL, " +
                 "  LogIndex INTEGER NOT NULL " +
                 " )";

    Statement stmt = db_.createStatement();
    stmt.executeUpdate( sql );
    stmt.close();
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

  // calculate hash of all IPFSHashes in the DB (such as on startup or when
  // out-of-sync somehow)
  public byte[] fullHashSum() throws Exception
  {
    byte[] result = null;

    String sql =
      "SELECT IPFSHashStr FROM Publications ORDER BY BlockNumber,LogIndex";

    Statement stmt = db_.createStatement();
    ResultSet rs = stmt.executeQuery( sql );

    String ipfs;
    while (rs.next())
    {
      ipfs = rs.getString( 1 );
      result = nextHash( result, HexString.decode(ipfs) );
    }

    rs.close();
    stmt.close();

    return result;
  }

  public byte[] nextHash( byte[] lastresult, byte[] nextval )
  throws Exception
  {
    byte[] result = null;

    if (null != lastresult)
      result = Kekkac256.hash( ByteOps.concat(lastresult, nextval) );
    else
      result = Kekkac256.hash( HexString.decode(ipfs) );

    return result;
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

