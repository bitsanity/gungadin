// NOTES:
//
// 1. script uses hardcoded gasPrice -- CHECK ethgasstation.info

const fs = require('fs');
const Web3 = require('web3');
const web3 =
  new Web3(new Web3.providers.HttpProvider("http://localhost:8545"));
//new Web3(new Web3.providers.WebsocketProvider("ws://localhost:8545"));
//new Web3(new Web3.providers.WebsocketProvider("ws://localhost:8546"));

const MYGASPRICE = '' + 1 * 1e9;

function getABI() {
  return JSON.parse(
    fs.readFileSync('./build/Membership_sol_Membership.abi').toString() );
}

function getBinary() {
  var binary =
    fs.readFileSync('../build/Membership_sol_Membership.bin').toString();

  if (!binary.startsWith('0x')) binary = '0x' + binary;
  return binary;
}

function getContract(sca) {
  return new web3.eth.Contract( getABI(), sca );
}

function checkAddr(addr) {
  try {
    let isaddr = parseInt( addr );
  } catch( e ) {
    usage();
    process.exit(1);
  }
}

function shorten(addr) {
  var saddr = "" + addr;
  return "0x" + saddr.substring(26);
}

function printEvent(evt) {

  event Approval( address indexed member, bool status );
  event Fee( uint256 fee );
  event Receipt( address indexed member, uint256 amount );
}

const cmds =
  [
   'chown',
   'deploy',
   'events',
   'approval',
   'balance',
   'isMember',
   'setApproval',
   'setFee',
   'setTreasury',
   'sendTok',
   'withdraw'
  ];

function usage() {
  console.log(
    '\nUsage:\n$ node cli.js <acctindex> <SCA> <command> [arg]*\n',
     'Commands:\n',
     '\tchown <new owner eoa> |\n',
     '\tdeploy |\n',
     '\tevents |\n',
     '\tapproval <address> |\n',
     '\tbalance <address> |\n',
     '\tisMember <address> |\n',
     '\tsetApproval <address> <true|false> |\n',
     '\tsetFee <new fee wei> |\n',
     '\tsetTreasury <sca> |\n',
     '\tsendTok <address> <quantity> |\n',
     '\twithdraw <amount in wei>\n'
  );
}

var cmd = process.argv[4];

let found = false;
for (let ii = 0; ii < cmds.length; ii++)
  if (cmds[ii] == cmd) found = true;

if (!found) {
  usage();
  process.exit(1);
}

var ebi = process.argv[2];
var sca = process.argv[3];

var eb;
web3.eth.getAccounts().then( (res) => {
  eb = res[ebi];
  if (cmd == 'deploy')
  {
    let con = new web3.eth.Contract( getABI() );

    con
      .deploy({data:getBinary()} )
      .send({from: eb, gas: 1452525, gasPrice: MYGASPRICE}, (err, txhash) => {
        if (txhash) console.log( "send txhash: ", txhash );
      } )
      .on('error', (err) => { console.log("err: ", err); })
      .on('transactionHash', (h) => { console.log( "hash: ", h ); } )
      .on('receipt', (r) => { console.log( 'rcpt: ' + r.contractAddress); } )
      .on('confirmation', (cn, rcpt) => { console.log( 'cn: ', cn ); } )
      .then( (con) => {
        console.log( "SCA", con.options.address );
        process.exit(0);
      } );
  }
  else
  {
    let con = new web3.eth.Contract( getABI(), sca );

    if (cmd == 'chown')
    {
      let addr = process.argv[5];
      checkAddr(addr);
      con.methods.setTreasurer( addr )
                 .send( {from: eb, gas: 30000, gasPrice: MYGASPRICE} );
    }

    if (cmd == 'events')
    {
      con.getPastEvents('allEvents', {fromBlock: 0, toBlock: 'latest'})
         .then( (events) => {
        for (var ii = 0; ii < events.length; ii++) {
          printEvent( events[ii] );
        }
      });
    }

    if (cmd == 'approval')
    {
      let addr = process.argv[5];
      checkAddr( addr );

      con.methods.approval(addr).call().then( res => {
        console.log( "approval(" + addr + "): " + res );
      } );
    }
    if (cmd == 'balance')
    {
      let addr = process.argv[5];
      checkAddr( addr );

      con.methods.balance(addr).call().then( res => {
        console.log( "balance(" + addr + "): " + res );
      } );
    }
    if (cmd == 'isMember')
    {
      let addr = process.argv[5];
      checkAddr( addr );

      con.methods.isMember(addr).call().then( res => {
        console.log( "isMember(" + addr + "): " + res );
      } );
    }
    if (cmd == 'sendTok')
    {
      let recip = process.argv[5];
      checkAddr( recip );
      let qty = process.argv[6];
      con.methods.sendTok( recip, qty )
                 .send( {from: eb, gas: 100000, gasPrice: MYGASPRICE} );
    }
    if (cmd == 'setFee')
    {
      let newfee = process.argv[5];
      con.methods.setFee( newfee )
                 .send( {from: eb, gas: 100000, gasPrice: MYGASPRICE} );
    }
    if (cmd == 'setTreasury')
    {
      let trs = process.argv[5];
      checkAddr( trs );
      con.methods.setTreasury( trs )
                 .send( {from: eb, gas: 120000, gasPrice: MYGASPRICE} );
    }
    if (cmd == 'withdraw')
    {
      let amt = process.argv[5];
      con.methods.withdraw( amt )
                 .send( {from: eb, gas: 120000, gasPrice: MYGASPRICE} );
    }
  }
} );
