pragma solidity ^0.4.25;

// assume basic ERC20 compatibility
interface Token {
  function transfer( address to, uint amount ) external;
  function transferFrom( address from, address to, uint amount ) external;
}

interface Membership {
  function isMember( address pusher ) external returns (bool);
}

contract Owned
{
  address public owner;
  constructor() public { owner = msg.sender; }

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
  uint256 dao;

  uint256 public tokenFee;
  Token   public token;

  constructor() public {
    dao = uint256(100);
  }

  function setFee( uint256 _fee ) isOwner public {
    fee = _fee;
  }

  function setDao( uint256 _dao ) isOwner public {
    dao = _dao;
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

    require(    msg.value >= fee
             && membership.isMember(msg.sender) );

    if (treasury != address(0))
      treasury.transfer( msg.value - msg.value / dao );

    emit Published( receiverpubkey, ipfshash, redmeta );
  }

  function publish_t( bytes receiverpubkey,
                      string ipfshash,
                      string redmeta ) public {

    require( membership.isMember(msg.sender) );

    token.transferFrom( msg.sender, address(this), tokenFee );

    if (treasury != address(0)) {
      token.transfer( treasury, tokenFee - tokenFee/dao );
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
