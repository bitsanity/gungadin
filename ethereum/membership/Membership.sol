// 0.4.21+commit.dfe3193c.Emscripten.clang
pragma solidity ^0.4.25;

interface Token {
  function transfer( address to, uint amount ) external;
}

contract Owned
{
  address public owner;

  modifier isOwner {
    require( msg.sender == owner );
    _;
  }

  constructor() public { owner = msg.sender; }
  function changeOwner( address newOwner ) isOwner public { owner = newOwner; }
}

contract Membership is Owned
{
  event Approval( address indexed member, bool status );
  event Fee( uint256 fee );
  event Receipt( address indexed member, uint256 amount );

  mapping( address => bool ) public approvals;
  mapping( address => uint256 ) public balances;

  address public treasury;
  uint256 public fee;
  uint256 dao;

  constructor() public {
    dao = uint256(100);
  }

  function setFee( uint256 _fee ) isOwner public {
    fee = _fee;
    emit Fee( fee );
  }

  function setDao( uint256 _dao ) isOwner public {
    dao = _dao;
  }

  function setTreasury( address _treasury ) isOwner public {
    treasury = _treasury;
  }

  function setApproval( address member, bool status ) isOwner public {
    approvals[ member] = status;
    emit Approval( member, status );
  }

  function isMember( address _addr ) constant public returns (bool) {
    return approvals[_addr] && 0 < balances[_addr];
  }

  function() payable public {
    require( approvals[msg.sender] && msg.value >= fee );
    balances[msg.sender] += msg.value;

    if (treasury != address(0))
      treasury.transfer( msg.value - msg.value / dao );

    emit Receipt( msg.sender, msg.value );
  }

  function withdraw( uint256 amount ) isOwner public {
    owner.transfer( amount );
  }

  function sendTok( address _tok, address _to, uint256 _qty ) isOwner public {
    Token(_tok).transfer( _to, _qty );
  }
}

