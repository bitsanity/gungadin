package pubui;

public class Onions
{
  private static Onions instance_ = null;

  private String[] onions_;

  private Onions()
  {
    onions_ = new String[3];
    onions_[0] = "blafflewag.onion";
    onions_[1] = "puwectamist.onion";
    onions_[2] = "actquedux.onion";
  }

  public static Onions instance()
  {
    if (null == instance_)
      instance_ = new Onions();
    return instance_;
  }

  public int count()
  {
    return onions_.length;
  }

  public String pickRandom()
  {
    int ix = (int)(Math.random() * 3.0d);
    return onions_[ix];
  }

  public String get( int ix )
  {
    return onions_[ix];
  }

  public void set( int ix, String value )
  {
    onions_[ix] = value;
  }

}

