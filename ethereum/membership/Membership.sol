pragma solidity ^0.4.15;

contract owned
{
  address public owner;

  modifier onlyOwner {
    require( msg.sender == owner );
    _;
  }

  function owned() { owner = msg.sender; }
  function changeOwner( address newOwner ) onlyOwner { owner = newOwner; }
  function closedown() onlyOwner { selfdestruct( owner ); }
}

// ==========================================================================
// List of members known by Ethereum address. Balance must be greater than
// zero to be valid. Owner may adjust fees.
// ==========================================================================

contract Membership is owned
{
  event Added( address indexed newmember );
  event Dropped( address indexed newmember );
  event Fee( uint256 fee );

  mapping( address => uint256 ) public balances;
  uint256 public fee;

  function Membership() { fee = 0; }

  function setFee( uint256 _fee ) onlyOwner {
    fee = _fee;
    Fee( fee );
  }

  function addMember( address newMember ) onlyOwner
  {
    Added( newMember );
  }

  function dropMember( address oldMember ) onlyOwner
  {
    balances[oldMember] = 0;
    Dropped( oldMember );
  }

  function isMember( address _addr ) private constant returns (bool)
  {
    return 0 < balances[_addr];
  }

  function withdraw( uint256 amount ) onlyOwner returns (bool)
  {
    return owner.send( amount );
  }

  function() payable
  {
    require( msg.value >= fee );
    balances[msg.sender] += msg.value;
  }
}

