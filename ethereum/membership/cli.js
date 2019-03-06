const fs = require('fs');
const Web3 = require('web3');
const web3 =
  new Web3(new Web3.providers.HttpProvider("http://localhost:8545"));
//new Web3(new Web3.providers.WebsocketProvider("ws://localhost:8545"));
//new Web3(new Web3.providers.WebsocketProvider("ws://localhost:8546"));

function getABI() {
  return JSON.parse(
    fs.readFileSync('./build/Membership_sol_Membership.abi').toString() );
}

function getBinary() {
  var binary =
    fs.readFileSync('./build/Membership_sol_Membership.bin').toString();

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

  if (evt.event == 'Approval' ) {
    let member = web3.eth.abi.decodeParameter( "address", evt.raw.topics[1] );
    let astats = web3.eth.abi.decodeParameter( "bool", evt.raw.data );
    console.log( "Approval member: " + member + " = " + astats );
  }
  else if (evt.event == 'Receipt' ) {
    let member = web3.eth.abi.decodeParameter( "address", evt.raw.topics[1] );
    let qty = web3.eth.abi.decodeParameter( "uint256", evt.raw.data );
    console.log( "Receipt member: " + member + ", amount: " + qty );
  }
  else if (evt.event == 'ReceiptTokens' ) {
    let member = web3.eth.abi.decodeParameter( "address", evt.raw.topics[1] );
    let qty = web3.eth.abi.decodeParameter( "uint256", evt.raw.data );
    console.log( "ReceiptTokens member: " + member + ", amount: " + qty );
  }
  else {
    console.log( evt );
  }
}

const cmds =
  [
   'chown',
   'deploy',
   'events',
   'approval',
   'balance',
   'isMember',
   'paydues',
   'payWithTokens',
   'setApproval',
   'setFee',
   'setToken',
   'setTokenFee',
   'setTreasury',
   'sendTok',
   'variables',
   'withdraw'
  ];

function usage() {
  console.log(
    '\nUsage:\n$ node cli.js <acctindex> <gasprice> <SCA> <command> [arg]*\n',
     'Commands:\n',
     '\tchown <new owner eoa> |\n',
     '\tdeploy |\n',
     '\tevents |\n',
     '\tapproval <address> |\n',
     '\tbalance <address> |\n',
     '\tisMember <address> |\n',
     '\tpaydues <amountwei> |\n',
     '\tpayWithTokens |\n',
     '\tsetApproval <address> <true|false> |\n',
     '\tsetFee <new fee wei> |\n',
     '\tsetToken <toksca> |\n',
     '\tsetTokenFee <quantity> |\n',
     '\tsetTreasury <sca> |\n',
     '\tsendTok <tokensca> <to address> <quantity> |\n',
     '\tvariables |\n',
     '\twithdraw <amount in wei>\n'
  );
}

var ebi = process.argv[2];
var gprice = '' + (process.argv[3] * 1e9);
var sca = process.argv[4];
var cmd = process.argv[5];

let found = false;
for (let ii = 0; ii < cmds.length; ii++)
  if (cmds[ii] == cmd) found = true;

if (!found) {
  usage();
  process.exit(1);
}

var eb;
web3.eth.getAccounts().then( (res) => {
  eb = res[ebi];
  if (cmd == 'deploy')
  {
    let con = new web3.eth.Contract( getABI() );

    con
      .deploy({data:getBinary()} )
      .send({from: eb, gas: 1000000, gasPrice: gprice}, (err, txhash) => {
        if (err) console.log( err );
      } )
      .on('error', (err) => { console.log("borked: ", err); })
      .on('transactionHash', (h) => { console.log( "hash: ", h ); } )
      .on('receipt', (r) => { console.log( 'rcpt: ' + r.contractAddress); } )
      .on('confirmation', (cn, rcpt) => { console.log( 'cn: ', cn ); } )
      .then( (tract) => {
        console.log( "SCA", tract.options.address );
        process.exit(0);
      } )
      .catch( err => { console.log } );
  }
  else
  {
    let con = new web3.eth.Contract( getABI(), sca );

    if (cmd == 'chown')
    {
      let addr = process.argv[6];
      checkAddr(addr);
      con.methods.changeOwner( addr )
                 .send( {from: eb, gas: 30000, gasPrice: gprice} );
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
      let addr = process.argv[6];
      checkAddr( addr );

      con.methods.approval(addr).call().then( res => {
        console.log( "approval(" + addr + "): " + res );
      } );
    }
    if (cmd == 'balance')
    {
      let addr = process.argv[6];
      checkAddr( addr );

      web3.eth.getBalance(addr).then( res => {
        console.log( "balance(" + addr + "): " + res );
      } );
    }
    if (cmd == 'isMember')
    {
      let addr = process.argv[6];
      checkAddr( addr );

      con.methods.isMember(addr).call().then( res => {
        console.log( "isMember(" + addr + "): " + res );
      } );
    }
    if (cmd == 'paydues') {
      let val = process.argv[6];

      web3.eth.sendTransaction(
        {from: eb, to: sca, value: val, gas: 100000, gasPrice: gprice} )
      .catch( err => { console.log } );
    }
    if (cmd == 'payWithTokens') {
      con.methods.payWithTokens()
         .send( {from: eb, gas: 100000, gasPrice: gprice} )
         .catch( err => { console.log } );
    }
    if (cmd == 'setApproval') {
      let addr = process.argv[6];
      let val = JSON.parse( process.argv[7] );

      con.methods.setApproval( addr, val )
       .send( {from: eb, gas: 100000, gasPrice: gprice} )
       .then( receipt => {
          process.exit(0);
        } )
       .catch( err => { console.log(err) } );
    }
    if (cmd == 'sendTok')
    {
      let tok = process.argv[6];
      checkAddr( tok );
      let toaddr = process.argv[7];
      checkAddr( toaddr );
      let qty = process.argv[8];
      con.methods.sendTok( tok, toaddr, qty )
                 .send( {from: eb, gas: 100000, gasPrice: gprice} );
    }
    if (cmd == 'setFee')
    {
      let newfee = process.argv[6];
      con.methods.setFee( newfee )
                 .send( {from: eb, gas: 100000, gasPrice: gprice} );
    }
    if (cmd == 'setToken')
    {
      let tok = '' + process.argv[6];
      con.methods.setToken( tok )
                 .send( {from: eb, gas: 100000, gasPrice: gprice} );
    }
    if (cmd == 'setTokenFee')
    {
      let newfee = process.argv[6];
      con.methods.setTokenFee( newfee )
                 .send( {from: eb, gas: 100000, gasPrice: gprice} );
    }
    if (cmd == 'setTreasury')
    {
      let trs = process.argv[6];
      checkAddr( trs );
      con.methods.setTreasury( trs )
                 .send( {from: eb, gas: 120000, gasPrice: gprice} );
    }
    if (cmd == 'withdraw')
    {
      let amt = process.argv[6];
      con.methods.withdraw( amt )
                 .send( {from: eb, gas: 120000, gasPrice: gprice} );
    }
    if (cmd == 'variables')
    {
      con.methods.owner().call().then( res => {
        console.log( "owner: " + res );
      } );

      con.methods.treasury().call().then( res => {
        console.log( "treasury: " + res );
      } );

      con.methods.fee().call().then( res => {
        console.log( "fee: " + res );
      } );

      con.methods.token().call().then( res => {
        console.log( "token: " + res );
      } );

      con.methods.tokenFee().call().then( res => {
        console.log( "tokenFee: " + res );
      } );
    }
  }
} );

