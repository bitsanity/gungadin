package daemon;

import org.json.simple.*;
import org.json.simple.parser.*;

// creates/sends OUTGOING JSON-RPC messages to the Ethereum gateway

public class EthGateway
{
  private port_;

  public EthGateway( int port ) throws Exception
  {
    port_ = port;
  }

  public void setHWM( long newhwm ) throws Exception
  {
  }

  public void vote( int blocknum, String hashResult ) throws Exception
  {
  }

  public static void main( String[] args ) throws Exception
  {
  }
}
