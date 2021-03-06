const fs = require('fs');
const Web3 = require('web3');
const web3 =
  new Web3(new Web3.providers.HttpProvider("http://localhost:8545"));
//new Web3(new Web3.providers.WebsocketProvider("ws://localhost:8545"));
//new Web3(new Web3.providers.WebsocketProvider("ws://localhost:8546"));

function getABI() {
  return JSON.parse(
    fs.readFileSync('./build/Votes_sol_Votes.abi').toString() );
}

function getBinary() {
  var binary =
    fs.readFileSync('./build/Votes_sol_Votes.bin').toString();
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
  var hash = web3.eth.abi.decodeParameters( ["string"], evt.raw.data );

  console.log( 'Voted:\n\tvoter = ' + shorten(evt.raw.topics[1]) +
               '\n\tblock = '       + parseInt(evt.raw.topics[2]) +
               '\n\tipfshash = '    + hash['0'] );
}

const cmds =
  [
   'chown',
   'deploy',
   'events',
   'setFee',
   'setMembership',
   'setTreasury',
   'vote'
  ];

function usage() {
  console.log(
    '\nUsage:\n$ node cli.js <acctindex> <SCA> <command> [arg]*\n',
     'Commands:\n',
     '\tchown <new owner eoa> |\n',
     '\tdeploy |\n',
     '\tevents |\n',
     '\tsetFee <new fe wei> |\n',
     '\tsetMembership <address> |\n',
     '\tsetTreasury <address> |\n',
     '\tvote <blocknum> <hash> |\n'
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
      .catch( e =>  { console.log(e); } );
  }
  else
  {
    let con = new web3.eth.Contract( getABI(), sca );

    if (cmd == 'chown')
    {
      let addr = process.argv[6];
      checkAddr(addr);
      con.methods.setTreasurer( addr )
                 .send( {from: eb, gas: 30000, gasPrice: gprice} );
    }

    if (cmd == 'events')
    {
      con.getPastEvents('allEvents', {fromBlock: 0, toBlock: 'latest'})
         .then( events => {
        for (var ii = 0; ii < events.length; ii++) {
          printEvent( events[ii] );
        }
      });
    }

    if (cmd == 'setFee')
    {
      let newfee = process.argv[6];
      con.methods.setFee( newfee )
                 .send( {from: eb, gas: 100000, gasPrice: gprice} );
    }
    if (cmd == 'setMembership')
    {
      let mbrs = process.argv[6];
      checkAddr( mbrs );
      con.methods.setMembership( mbrs )
                 .send( {from: eb, gas: 120000, gasPrice: gprice} );
    }
    if (cmd == 'setTreasury')
    {
      let trs = process.argv[6];
      checkAddr( trs );
      con.methods.setTreasury( trs )
                 .send( {from: eb, gas: 120000, gasPrice: gprice} );
    }
    if (cmd == 'vote')
    {
      let blocknum = process.argv[6];
      let hash = process.argv[7];

      con.methods.fee_().call().then( fee => {
        con.methods
           .vote( blocknum, hash )
           .send( {from: eb, gas: 120000, value: fee, gasPrice: gprice} );
      } )
      .catch( e => { console.log } );
    }
    if (cmd == 'vote_t')
    {
      let blocknum = process.argv[6];
      let hash = process.argv[7];

      con.methods.vote_t( blocknum, hash )
      .send( {from: eb, gas: 120000, gasPrice: gprice} )
      .catch( e => { console.log } );
    }
  }
} );

