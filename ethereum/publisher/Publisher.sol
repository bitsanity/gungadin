pragma solidity ^0.4.15;

contract owned
{
  address public owner;
  function owned() { owner = msg.sender; }

  modifier onlyOwner {
    require( msg.sender == owner );
    _;
  }

  function changeOwner( address newOwner ) onlyOwner { owner = newOwner; }
  function closedown() onlyOwner { selfdestruct( owner ); }
}

interface Membership {
  function isMember( address pusher ) returns (bool);
}

contract Publisher is owned
{
  event Published( bytes receiverpubkey, string ipfshash );

  Membership public membership;

  function Publisher() {}

  function setMembershipContract( address _contract ) onlyOwner
  {
    membership = Membership(_contract);
  }

  function() payable { revert(); }

  function publish( bytes receiverpubkey, string ipfshash )
  {
    require( membership.isMember(msg.sender) );
    Published( receiverpubkey, ipfshash );
  }
}

