const fs = require('fs');
const Web3 = require('web3');
const web3 =
  new Web3(new Web3.providers.HttpProvider("http://localhost:8545"));
//new Web3(new Web3.providers.WebsocketProvider("ws://localhost:8545"));
//new Web3(new Web3.providers.WebsocketProvider("ws://localhost:8546"));

function getABI() {
  return JSON.parse(
    fs.readFileSync('./build/Publisher_sol_Publisher.abi').toString() );
}

function getBinary() {
  var binary =
    fs.readFileSync('./build/Publisher_sol_Publisher.bin').toString();

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
  if (evt.event == 'Fee' ) {
    var decoded = web3.eth.abi.decodeParameters(
                    ["uint256"], events[ii].raw.data );

    console.log( "Fee: " + decoded['0'] );
  }
  else if (evt.event == 'Published' ) {
    var decoded = web3.eth.abi.decodeParameters(
                    ["bytes", "string"], events[ii].raw.data );

    console.log( "Published\npubkey: " + decoded['0'] +
                 "\nipfshash: " + decoded['1'] );
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
   'publish',
   'sendTok',
   'setFee',
   'setMembership',
   'setTreasury',
   'withdraw',
  ];

function usage() {
  console.log(
    '\nUsage:\n$ node cli.js <acctindex> <gprice> <SCA> <command> [arg]*\n',
     'Commands:\n',
     '\tchown <new owner eoa> |\n',
     '\tdeploy |\n',
     '\tevents |\n',
     '\tpublish <receiverpubkey> <ipfshash> |\n',
     '\tsendTok <receiver address> <quantity> |\n',
     '\tsetFee <new fee wei> |\n',
     '\tsetMembership <sca> |\n',
     '\tsetTreasury <sca> |\n',
     '\twithdraw <amount in wei>\n'
  );
}

var cmd = process.argv[5];

let found = false;
for (let ii = 0; ii < cmds.length; ii++)
  if (cmds[ii] == cmd) found = true;

if (!found) {
  usage();
  process.exit(1);
}

var ebi = process.argv[2];
var gprice = '' + (process.argv[3] * 1e9);
var sca = process.argv[4];

var eb;
web3.eth.getAccounts().then( (res) => {
  eb = res[ebi];
  if (cmd == 'deploy')
  {
    let con = new web3.eth.Contract( getABI() );

    con
      .deploy({data:getBinary()} )
      .send({from: eb, gas: 1452525, gasPrice: gprice}, (err, txhash) => {
        if (txhash) console.log( "send txhash: ", txhash );
      } )
      .on('error', (err) => { console.log("err: ", err); })
      .on('transactionHash', (h) => { console.log( "hash: ", h ); } )
      .on('receipt', (r) => { console.log( 'rcpt: ' + r.contractAddress); } )
      .on('confirmation', (cn, rcpt) => { console.log( 'cn: ', cn ); } )
      .then( (con) => {
        console.log( "SCA", con.options.address );
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

    if (cmd == 'publish')
    {
      let pubkey = process.argv[7];
      let ipfshash = process.argv[8];
      con.methods.publish( pubkey, ipfshash )
                 .send( {from: eb, gas: 100000, gasPrice: gprice} );
    }
    if (cmd == 'sendTok')
    {
      let recip = process.argv[6];
      checkAddr( recip );
      let qty = process.argv[7];
      con.methods.sendTok( recip, qty )
                 .send( {from: eb, gas: 100000, gasPrice: gprice} );
    }
    if (cmd == 'setFee')
    {
      let newfee = process.argv[6];
      con.methods.setFee( newfee )
                 .send( {from: eb, gas: 100000, gasPrice: gprice} );
    }
    if (cmd == 'setMembership')
    {
      let mbrship = process.argv[6];
      checkAddr( mbrship );
      con.methods.setMembership( mbrship )
                 .send( {from: eb, gas: 120000, gasPrice: gprice} )
      .catch( err => { console.log(err) } );
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
  }
} );

