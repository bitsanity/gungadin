pragma solidity ^0.4.21;

interface Membership {
  function approvals( address who ) external returns (bool);
  function isMember( address who ) external returns (bool);
}

// assume ERC20 compatibility
interface Token {
  function transfer( address to, uint amount ) external;
  function transferFrom( address from, address to, uint amount ) external;
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
              string          hash );

  Membership public membership_;
  address    public treasury_;
  uint256    public fee_;
  uint256 dao_;

  uint256 public tokenFee_;
  Token   public token_;

  function Votes() public {
    dao_ = uint256(100);
  }

  function setFee( uint _newfee ) isOwner public {
    fee_ = _newfee;
  }

  function setDao( uint _dao ) isOwner public {
    dao_ = _dao;
  }

  function setMembership( address _contract ) isOwner public {
    membership_ = Membership( _contract );
  }

  function setTreasury( address _treasury ) isOwner public {
    treasury_ = _treasury;
  }

  function setTokenFee( uint256 _fee ) isOwner public {
    tokenFee_ = _fee;
  }

  function setToken( address _token ) isOwner public {
    token_ = Token(_token);
  }

  function vote( uint _blocknum, string _hash ) payable public {
    require(    fee_ != 0
             && msg.value >= fee_
             && membership_.isMember(msg.sender)
             && membership_.approvals(msg.sender)
           );

    if (treasury_ != address(0))
      treasury_.transfer( msg.value - msg.value / dao_ );

    emit Vote( msg.sender, _blocknum, _hash );
  }

  function vote( address tokensca,
                 bytes receiverpubkey,
                 string ipfshash ) public {

    require(    membership_.isMember(msg.sender)
             && membership_.approvals(msg.sender)
             && tokensca == token_
             && token_ != address(0)
           );

    if (treasury_ != address(0)) {
      Token t = Token(tokensca);
      t.transferFrom( msg.sender, address(this),tokenFee_ );
      t.transfer( treasury_, tokenFee_ - tokenFee_/dao_ );
    }

    emit Published( receiverpubkey, ipfshash );
  }

  function withdraw( uint amt ) isOwner public {
    owner_.transfer( amt );
  }

  function sendTok( address _tok, address _to, uint _qty ) isOwner public {
    Token(_tok).transfer( _to, _qty );
  }
}
