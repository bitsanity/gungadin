package pubui;

// TODO - instead of hardcoding fetch three Online endpoints from:
//          https://ipfs.github.io/public-gateway-checker/

public class Endpoints
{
  private static Endpoints instance_ = null;

  private String[] endpoints_;

  private Endpoints()
  {
    endpoints_ = new String[3];
    endpoints_[0] = "https://ipfs.io/ipfs/";
    endpoints_[1] = "https://siderus.io/ipfs/";
    endpoints_[2] = "https://ipfs.infura.io/ipfs/";
  }

  public static Endpoints instance()
  {
    if (null == instance_)
      instance_ = new Endpoints();
    return instance_;
  }

  public int count()
  {
    return endpoints_.size;
  }

  public String pickRandom()
  {
    int ix = (int)(Math.random() * 3.0d);
    return endpoints_[ix];
  }

  public String get( int ix )
  {
    return endpoints_[ix];
  }

  public void set( int ix, String value )
  {
    endpoints_[ix] = value;
  }

}

