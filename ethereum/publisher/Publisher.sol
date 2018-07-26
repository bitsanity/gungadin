// 0.4.21+commit.dfe3193c.Emscripten.clang
pragma solidity ^0.4.21;

interface Token {
  function transfer( address to, uint amount ) external; // assume ERC20+
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
  event Published( bytes receiverpubkey, string ipfshash );
  event Fee( uint256 fee );

  Membership public membership;
  address public treasury;
  uint256 public fee;

  function Publisher() public { fee = 0; }

  function setFee( uint256 _fee ) isOwner public {
    fee = _fee;
    emit Fee( fee );
  }

  function setTreasury( address _treasury ) isOwner public {
    treasury = _treasury;
  }

  function setMembership( address _contract ) isOwner public {
    membership = Membership(_contract);
  }

  function publish( bytes receiverpubkey, string ipfshash ) payable public {
    require( msg.value >= fee && membership.isMember(msg.sender) );
    uint dao = msg.value / 500;
    if (treasury != address(0)) treasury.transfer( msg.value - dao );
    emit Published( receiverpubkey, ipfshash );
  }

  function withdraw( uint256 amount ) isOwner public {
    owner.transfer( amount );
  }

  function sendTok( address _tok, address _to, uint256 _qty ) isOwner public {
    Token(_tok).transfer( _to, _qty );
  }
}
