package daemon;

import java.util.*;

public class Globals
{
  // singleton
  private static Globals instance_ = new Globals();
  public static Globals instance() { return instance_; }

  private Hashtable<String,String> table_;

  private Globals()
  {
    table_ = new Hashtable<String,String>();
  }

  public String get( String name ) throws Exception
  {
    if (null == name || 0 == name.length()) throw new Exception( "no name" );
    return table_.get(name);
  }

  public void put( String name, String val ) throws Exception
  {
    if (null == name || 0 == name.length() || null == val || 0 == val.length())
      throw new Exception( "invalid param" );
    table_.put( name, val );
  }

}
