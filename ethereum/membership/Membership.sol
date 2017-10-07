pragma solidity ^0.4.15;

contract owned
{
  address public owner;
  function owned() { owner = msg.sender; }

  modifier onlyOwner {
    require( msg.sender == owner );
    _;
  }

  function transferOwnership( address newOwner ) onlyOwner { owner = newOwner; }
  function closedown() onlyOwner { selfdestruct( owner ); }
}

contract Membership is owned
{
  event AddedMember( address indexed newmember );
  event DroppedMember( address indexed newmember );

  mapping( address => uint256 ) public balances; // valid if balance > 0

  function Membership() {}

  function() payable
  {
    require( isMember(msg.sender) );
    balances[msg.sender] += msg.value;
  }

  function withdraw( uint256 amount ) onlyOwner returns (bool)
  {
    return owner.send( amount );
  }

  function addMember( address newMember ) onlyOwner
  {
    balances[newMember] = 1;
    AddedMember( newMember );
  }

  function dropMember( address oldMember ) onlyOwner
  {
    balances[oldMember] = 0;
    DroppedMember( oldMember );
  }

  function isMember( address _addr ) constant returns (bool)
  {
    return 0 < balances[_addr];
  }
}

