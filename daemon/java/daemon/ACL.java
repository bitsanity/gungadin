package daemon;

import java.sql.*;
import java.util.*;

import tbox.*;

// Access Control List, a persistent store of keys we recognize.

public class ACL
{
  private Connection db_;

  public ACL( String dbfpath ) throws Exception
  {
    Class.forName( "org.hsqldb.jdbc.JDBCDriver" );

    db_ = DriverManager.getConnection(
      "jdbc:hsqldb:file:" + dbfpath + ";shutdown=true", "SA", "kgserver" );

    String sql = "CREATE TABLE IF NOT EXISTS KGACL " +
                 "( K VARCHAR(256) PRIMARY KEY )";

    Statement stmt = db_.createStatement();
    stmt.executeUpdate( sql );
    stmt.close();
  }

  public boolean hasKey( byte[] key ) throws Exception
  {
    int count = 0;

    Statement stmt = db_.createStatement();
    String sql = "SELECT COUNT(*) FROM KGACL WHERE K='" +
                 HexString.encode(key) + "'";

    ResultSet rs = stmt.executeQuery( sql );

    while (rs.next())
      count = rs.getInt(1);

    stmt.close();

    return 0 != count;
  }

  public void addKey( byte[] key ) throws Exception
  {
    // not an error to re-add
    if (hasKey(key)) return;

    Statement stmt = db_.createStatement();
    String sql = "INSERT INTO KGACL (K) VALUES ('" +
                 HexString.encode(key) + "')";

    stmt.executeUpdate( sql );
    stmt.close();
  }

  public void removeKey( byte[] key ) throws Exception
  {
    if (!hasKey(key)) throw new Exception( "rm WTF" );

    Statement stmt = db_.createStatement();
    String sql = "DELETE FROM KGACL WHERE K='" + HexString.encode(key) + "'";
    stmt.executeUpdate( sql );
    stmt.close();
  }

  public Vector<String> list() throws Exception
  {
    Vector<String> result = new Vector<String>();

    Statement stmt = db_.createStatement();
    String sql = "SELECT K FROM KGACL";
    ResultSet rs = stmt.executeQuery( sql );

    while (rs.next())
      result.add( rs.getString(1) );

    stmt.close();

    return result;
  }

  // CLI usage:
  //
  // add a key: add <dbfile> <pubkey,hex>
  // show keys: ls
  // remove key: rm <dbfile> <pubkey,hex>
  //
  public static void main( String[] args ) throws Exception
  {
    if (null == args || 2 >= args.length)
    {
      System.out.println( "Usage: <fpath> <add|ls|rm> [pubkey hexstring]" );
      return;
    }

    byte[] key = null;

    if (    args[1].equalsIgnoreCase("add")
         || args[1].equalsIgnoreCase("rm") )
    {
      key = HexString.decode( args[2] );

      if (    ((byte)0x02 == key[0] || (byte)0x03 == key[0])
           && 33 != key.length )
        throw new Exception( "compressed key invalid length" );

      if ( (byte)0x04 == key[0]
           && 65 != key.length )
        throw new Exception( "uncompressed key invalid length" );
    }

    if (args[1].equalsIgnoreCase("add"))
    {
      new ACL( args[0] ).addKey( key );
    }

    if (args[0].equalsIgnoreCase("rm"))
    {
      new ACL( args[0] ).removeKey( key );
    }

    if (args[1].equalsIgnoreCase("ls"))
    {
      Vector<String> hkeys = new ACL( args[0] ).list();
      for (String s : hkeys)
        System.out.println( s );
    }

    // testing workaround since adding shutdown=true to the url not work ??
    org.hsqldb.DatabaseManager.closeDatabases(0);
  }
}

