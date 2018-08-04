pragma solidity ^0.4.21;

interface Membership {
  function approvals( address member ) external returns (bool);
  function isMember( address pusher ) external returns (bool);
}

contract Owned {
  address public owner;

  function Owned() public { owner = msg.sender; }
  function changeOwner( address newOwner ) isOwner public { owner = newOwner; }

  modifier isOwner {
    require( msg.sender == owner );
    _;
  }
}

contract Votes is Owned {

  event Vote( int indexed blocknum, string ipfshash );

  Membership public membership_;
  uint256    public fee_;

  function Votes() public {}

  function setFee( uint _newfee ) isOwner public {
    fee_ = _newfee;
  }

  function setMembership( address _contract ) isOwner public {
    membership_ = Membership( _contract );
  }

  function vote( uint _blocknum, string _ipfshash ) payable public {

    require(    msg.value >= fee_
             && membership_.isMember(msg.sender)
             && membership_.approvals(msg.sender)
           );

    emit Vote( _blocknum, _ipfshash );
  }
}
