// 0.4.21+commit.dfe3193c.Emscripten.clang
pragma solidity ^0.4.21;

interface Token {
  function transfer( address to, uint amount ) external; // assume ERC20+
  function transferFrom( address from, address to, uint amount ) external; // assume ERC20+
}

interface Membership {
  function isMember( address pusher ) external returns (bool);
}

contract Owned
{
  address public owner;
  function Owned() public { owner = msg.sender; }

  modifier isOwner {
    require( msg.sender == owner );
    _;
  }

  function changeOwner( address newOwner ) isOwner public {
    owner = newOwner;
  }
}

contract Publisher is Owned
{
  event Published( bytes indexed receiverpubkey,
                   string ipfshash,
                   string redmeta );

  Membership public membership;
  address public treasury;
  uint256 public fee;

  uint256 public tokenFee;
  Token   public token;

  function Publisher() public {}

  function setFee( uint256 _fee ) isOwner public {
    fee = _fee;
  }

  function setTreasury( address _treasury ) isOwner public {
    treasury = _treasury;
  }

  function setMembership( address _contract ) isOwner public {
    membership = Membership(_contract);
  }

  function setTokenFee( uint256 _fee ) isOwner public {
    tokenFee = _fee;
  }

  function setToken( address _token ) isOwner public {
    token = Token(_token);
  }

  function publish( bytes receiverpubkey,
                    string ipfshash,
                    string redmeta ) payable public {

    require(    fee > 0
             && msg.value >= fee
             && membership.isMember(msg.sender) );

    if (treasury != address(0))
      treasury.transfer( msg.value - msg.value / 100 );

    emit Published( receiverpubkey, ipfshash, redmeta );
  }

  function publish( address tokensca,
                    bytes receiverpubkey,
                    string ipfshash,
                    string redmeta ) public {

    require(    membership.isMember(msg.sender)
             && token != address(0)
             && tokensca == token );

    if (treasury != address(0)) {
      Token t = Token(tokensca);
      t.transferFrom( msg.sender, address(this), tokenFee );
      t.transfer( treasury, tokenFee - tokFee/100 );
    }

    emit Published( receiverpubkey, ipfshash, redmeta );
  }

  function withdraw( uint256 amount ) isOwner public {
    owner.transfer( amount );
  }

  function sendTok( address _tok, address _to, uint256 _qty ) isOwner public {
    Token(_tok).transfer( _to, _qty );
  }
}
