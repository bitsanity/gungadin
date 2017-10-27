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

// interface to check if given address is a member
interface Membership {
  function isMember( address pusher ) returns (bool);
}

// ---------------------------------------------------------------------------
// contract that enables members to broadcast pubkey/ipfs hash pairs
// ---------------------------------------------------------------------------
contract Publisher is owned
{
  event Published( bytes receiverpubkey, string ipfshash );

  Membership public membership;

  function Publisher() {}

  function setMembershipContract( address _contract ) onlyOwner
  {
    require( isContract(_contract) );
    membership = Membership(_contract);
  }

  function() payable { revert(); }

  function publish( bytes receiverpubkey, string ipfshash )
  {
    require( membership.isMember(msg.sender) );
    Published( receiverpubkey, ipfshash );
  }

  function isContract( address _addr ) private constant returns (bool)
  {
    uint length;
    assembly { length := extcodesize(_addr) }
    return (length > 0);
  }
}

