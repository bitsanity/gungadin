package gungadaemon;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

public class EthAccount
{
  private Credentials credentials_;

  private static EthAccount instance_ = null;

  public static EthAccount instance() throws Exception
  {
    if (null == instance_)
      instance_ = new EthAccount(
        Globals.instance().get("ethphrase"),
        Globals.instance().get("ethwalletpath") );

    return instance_;
  }

  private EthAccount( String pphrase, String walletfpath ) throws Exception
  {
    if (null == pphrase || 0 == pphrase.length())
      throw new Exception("no ethphrase spec'ed");

    if (null == walletfpath || 0 == walletfpath.length())
      throw new Exception("no wallet file spec'ed");

    credentials_ = WalletUtils.loadCredentials( pphrase, walletfpath );
  }

  public Credentials credentials() throws Exception
  {
    return credentials_;
  }
}

