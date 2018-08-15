pragma solidity ^0.4.21;

interface Membership {
  function approvals( address who ) external returns (bool);
  function isMember( address who ) external returns (bool);
}

interface Token {
  function transfer( address to, uint amount ) external; // assume ERC20+
}

contract Owned {
  address public owner_;
  function Owned() public { owner_ = msg.sender; }
  function changeOwner( address newOwner ) isOwner public {
    owner_ = newOwner;
  }

  modifier isOwner {
    require( msg.sender == owner_ );
    _;
  }
}

contract Votes is Owned {

  event Vote( address indexed voter,
              uint    indexed blocknum,
              string          ipfshash );

  Membership public membership_;
  address    public treasury_;
  uint256    public fee_;

  function Votes() public {}

  function setFee( uint _newfee ) isOwner public {
    fee_ = _newfee;
  }

  function setMembership( address _contract ) isOwner public {
    membership_ = Membership( _contract );
  }

  function setTreasury( address _treasury ) isOwner public {
    treasury_ = _treasury;
  }

  function vote( uint _blocknum, string _ipfshash ) payable public {
    require(    msg.value >= fee_
             && membership_.isMember(msg.sender)
             && membership_.approvals(msg.sender)
           );

    if (treasury_ != address(0)) {
      uint dao = msg.value / 500;
      treasury_.transfer( msg.value - dao );
    }

    emit Vote( msg.sender, _blocknum, _ipfshash );
  }

  function withdraw( uint amt ) isOwner public {
    owner_.transfer( amt );
  }

  function sendTok( address _tok, address _to, uint _qty ) isOwner public {
    Token(_tok).transfer( _to, _qty );
  }
}
