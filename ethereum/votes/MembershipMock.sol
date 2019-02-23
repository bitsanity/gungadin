pragma solidity ^0.5.3;

contract MembershipMock {

  bool public approved_;
  bool public isMember_;

  constructor() public {}

  function approvals( address who ) public view returns (bool)
  {
    require( who != address(0) );
    return approved_;
  }

  function isMember( address who ) public view returns (bool)
  {
    require( who != address(0) );
    return isMember_;
  }

  function setReturn( bool mbr, bool approved ) public
  {
    isMember_ = mbr;
    approved_ = approved;
  }
}

