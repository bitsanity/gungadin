// main
// NOTE: installation requirement - the private key for the first ethereum
//       account must be shared with the daemon as the key is also the node
//       identifier in the system

if ( process.argv.length != 9 ) {
  console.log(
    process.argv.length,
    'Args: <myport> ' +
          '<daemonport> ' +
          '<daemonpubkey> ' +
          '<publsca> ' +
          '<votesca> ' +
          '<acct index> ' +
          '<acct password>',
    process.argv );
  process.exit( 1 );
}

const respHeader = { 'Content-Type' : 'application/json; charset=utf-8' };

// TODO : should adjust itself dynamically per network conditions
const fileSizeFeeWei = 1e6;

// TODO : should read the fee from the smart contract directly on init and if
//        a vote is rejected due to insufficient fee
const votingFeeWei = 1e6;

const fs = require( 'fs' );
const net = require( 'net' );
const Web3 = require( 'web3' );
const web3 =
  new Web3( new Web3.providers.WebsocketProvider("ws://localhost:8546") );
//new Web3( new Web3.providers.HttpProvider("http://localhost:8545") );

var EC = require( 'elliptic' ).ec;
var ec = new EC( 'secp256k1' );
var keythereum = require( 'keythereum' );

var myPort = process.argv[2];
var daemonPort = process.argv[3];
var daemonPubkey = process.argv[4];
var publishSCA = process.argv[5];
var voteSCA = process.argv[6];
var acctIndex = process.argv[7];
var acctPass = process.argv[8];

var myPrivkey;
var myPubkey;
var myAddress;
web3.eth.getAccounts().then( arr => {

  myAddress = arr[acctIndex];
  let keyObj = keythereum.importFromFile( myAddress );
  myPrivkey = keythereum.recover( acctPass, keyObj );
  let eckp = ec.keyFromPrivate( myPrivkey, "hex" );
  myPubkey = eckp.getPublic( "hex" );
  let tempaddr = keythereum.privateKeyToAddress( myPrivkey );

  console.log( "pubkey: " + myPubkey + "\n" +
               "config addr: " + myAddress + "\n" +
               "calc address: " + tempaddr );

} );

var publisher = new web3.eth.Contract(
  JSON.parse( fs.readFileSync(
    '../../../ethereum/publisher/build/Publisher_sol_Publisher.abi')
    .toString() ), publishSCA );

var votes = new web3.eth.Contract(
  JSON.parse( fs.readFileSync(
    '../../../ethereum/votes/build/Votes_sol_Votes.abi').toString() ),
    voteSCA );

const server = net.createServer( cx => {

  let rsp = {};

  cx.on( 'data', data => {
    try {
      handleMessage( JSON.parse(data) );

      rsp = {
        "jsonrpc":"2.0",
        "result":"0",
        "error":"null",
        "id":data.id };
    }
    catch( err ) {
      rsp = {
        "jsonrpc":"2.0",
        "result":"null",
        "error": {"code":"1","message":err.name,"data":err.message},
        "id":data.id };
    }

    cx.write( JSON.stringify(rsp) );
  } );
} );

server.on( 'error', (err) => {
  console.log(err);
} );

server.listen( myPort, () => {
  console.log( 'ethgw is listening on ' + myPort );
} );

function handleMessage( cmd )
{
  console.log( 'handleMessage:\n' + JSON.stringify(cmd) );

  var mth = cmd['method'];
  var msg = cmd['params'][0];
  var sig = cmd['params'][1];
  var pbk  = cmd['id'];

  var msgHash = web3.utils.sha3( msg );
  let dpubkey = ec.keyFromPublic( daemonPubkey, 'hex' );
  if (!dpubkey.verify(msgHash, sig) )
    throw "Daemon signature doesn't match public key.";

  var msgbod = JSON.parse( web3.hexToUtf8(msg) );

  if ('setHWM' === mth)
    handleNewHWM( msgbod['newhwm'] );
  else if ('publish' === mth)
    handlePublish( msgbod['recipkey'], msgbod['hash'], msgbod['fsize'] );
  else if ('vote' === mth)
    handleVote( msgbod['blocknum'], msgbod['hash'] );
}

function handleNewHWM( newhwm )
{
  publisher.getPastEvents( 'Published',
                           { fromBlock: newhwm, toBlock: 'latest' } )
  .then( (events) => {

    let data = [];
    for (var ii = 0; ii < events.length; ii++)
    {
      let elem = {};
      elem['pubkey'] =
        web3.eth.abi.decodeParameter( 'bytes', events[ii].raw.topics[1] );
      elem['ipfshash'] =
        web3.eth.abi.decodeParameter( 'string', events[ii].raw.data );
      elem['blocknum'] = events[ii].blockNumber;
      elem['logindex'] = events[ii].logIndex;

      data.push( elem );
    }
    sendToDaemon( 'Published', data );
  } ).catch( err => { console.log(err); } );
}

function handlePublish( recipkey, hash, fsize )
{
  // TODO: do an ipfs stat to confirm file size

  let price = '' + fsize * fileSizeFeeWei;

  web3.eth.getGasPrice().then( px => {
    publisher.methods.publish( recipkey, hash )
             .send( {from: myAddress,
                     gas: 50000,
                     gasPrice: px,
                     value: price} );
  } );
}

function handleVote( blocknum, hash )
{
  let price = '' + votingFeeWei;

  web3.eth.getGasPrice().then( px => {
    votes.methods.vote( blocknum, hash )
         .send( {from: myAddress,
                 gas: 50000,
                 gasPrice: px,
                 value: price} );
  } );
}

function sendToDaemon( method, msgbody )
{
  var msg = web3.utils.utf8ToHex( JSON.stringify(msgbody) );
  let msgHash = web3.utils.sha3( msg );
  let eckp = ec.keyFromPrivate( myPrivkey, "hex" );
  let sig = eckp.sign( msgHash );

  var msgparams = [];
  msgparams.push( msg );
  msgparams.push( web3.utils.utf8ToHex(sig) );

  var msg = {};
  msg['method'] = method;
  msg['params'] = msgparams;
  msg['id'] = '' + myPubkey;
}

