package pubui;

import java.util.*;
import javax.swing.ImageIcon;

public class Resources
{
  private static Resources instance_ = null;
  private static ResourceBundle res_;

  private Resources()
  {
    // expects file "Resources.properties" is findable in the classpath
    res_ = ResourceBundle.getBundle( "Resources" );
  }

  public String get( String key )
  {
    return res_.getString( key );
  }

  public ImageIcon icon( String path )
  {
    try {
      return new ImageIcon( Resources.class.getResource(path) );
    }
    catch( Exception e ) {
      return null;
    }
  }

  public static Resources instance()
  {
    if (null == instance_)
      instance_ = new Resources();

    return instance_;
  }
}
