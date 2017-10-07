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

// interface we expect for a contract that helps us check if a given address
// is one of our members

contract Membership {
  function isMember( address pusher ) returns (bool);
}

// ---------------------------------------------------------------------------
// Smart contract that publishes "chunks", files published in IPFS
// ---------------------------------------------------------------------------

contract Publisher is owned
{
  event Published( bytes receiverpubkey, string ipfshash );

  address membershipContract;

  function Publisher() {}

  function setMembershipContract( address _contract ) onlyOwner
  {
    require( isContract(_contract) );
    membershipContract = _contract;
  }

  function() payable { revert(); }

  function publish( bytes receiverpubkey, string ipfshash )
  {
    require( Membership(membershipContract).isMember(msg.sender) );

    Published( receiverpubkey, ipfshash );
  }

  function isContract( address _addr ) private returns (bool)
  {
    uint length;
    assembly { length := extcodesize(_addr) }
    return (length > 0);
  }
}

