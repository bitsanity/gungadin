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
               "calc'd addr: " + tempaddr );

} );

// ===========================================================================
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

    console.log( 'returning: ' + JSON.stringify(rsp) );

    cx.write( JSON.stringify(rsp) );
    cx.end();
  } );
} );

server.on( 'error', (err) => {
  console.log(err);
} );

server.listen( myPort, () => {
  console.log( 'ethgw is listening on ' + myPort );
} );

// ===========================================================================
var publisher = new web3.eth.Contract(
  JSON.parse( fs.readFileSync(
    '../../../ethereum/publisher/build/Publisher_sol_Publisher.abi')
    .toString() ), publishSCA );

console.log( 'watching ' + publishSCA + ' for publication events.' );

publisher.events.Published()
.on( 'data', (evt) => {

  let oneevt = pubEventToObj( evt );
  let msgbody = {};
  msgbody.events = oneevt;
  sendToDaemon( 'Published', msgbody );
} )
.on( 'error', console.error );

// ===========================================================================
var votes = new web3.eth.Contract(
  JSON.parse( fs.readFileSync(
    '../../../ethereum/votes/build/Votes_sol_Votes.abi').toString() ),
    voteSCA );

console.log( 'watching ' + voteSCA + ' for votes.' );

votes.events.Vote()
.on( 'data', (evt) => {

  let blocknum = parseInt( evt.topics[2] );
  let hash = web3.eth.abi.decodeParameters( ["string"], evt.raw.data )['0'];

  let msgbody = {};
  msgbody.blockNum = blocknum;
  msgbody.ipfsHash = hash;
  msgbody.logindex = evt.logIndex;
  msgbody.voteraddr = evt.topics[1];

  console.log( 'vote block ' + blocknum + ', hash ' + hash );
  sendToDaemon( 'Voted', msgbody );

} )
.on( 'error', console.error );

// ===========================================================================
function handleMessage( cmd )
{
  console.log( 'handleMessage: ' + JSON.stringify(cmd) );

  var mth = cmd['method'];
  var msg = cmd['params'][0];
  var sig = cmd['params'][1];
  var pbk = cmd['id'];

  var msgHash = web3.utils.sha3(msg).slice(2);
  let dpubkey = ec.keyFromPublic( daemonPubkey, 'hex' );

  if (!dpubkey.verify(msgHash, sig) )
    throw "Daemon signature doesn't match public key.";

  var msgbod = JSON.parse( hexToText(msg) );

  if ('setHWM' === mth)
    handleNewHWM( msgbod['newhwm'] );

  else if ('publish' === mth)
  {
    let redmeta = msgbod['redmeta'];
    if (!redmeta) redmeta = "";

    handlePublish( msgbod['recipkey'],
                   msgbod['hash'],
                   redmeta,
                   msgbod['fsize'] );
  }
  else if ('vote' === mth)
    handleVote( msgbod['blocknum'],
                msgbod['hash'] );
}

function handleNewHWM( newhwm )
{
  publisher.getPastEvents( 'Published',
                           { fromBlock: newhwm, toBlock: 'latest' } )
  .then( (events) => {

    let data = [];
    for (var ii = 0; ii < events.length; ii++)
    {
      let evt = pubEventToObj( events[ii] );
      console.log( 'handleNewHWM evt: ' + JSON.stringify(evt) );
      data.push( evt );
    }

    let msgbody = {};
    msgbody.events = data;

    sendToDaemon( 'Published', msgbody );

  } ).catch( err => { console.log(err); } );
}

function handlePublish( recipkey, hash, redmeta, fsize )
{
  // TODO: do an ipfs stat to confirm file size

  let price = '' + fsize * fileSizeFeeWei;

  web3.eth.getGasPrice().then( px => {

    console.log( '\nhandlePublish:\n' +
                 '\n\trecipient key: ' + recipkey +
                 '\n\tfrom: ' + myAddress +
                 '\n\tgasPrice: ' + px +
                 '\n\tvalue: ' + price + '\n\n' );

    publisher.methods.publish( recipkey, hash, redmeta )
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
  var msg = textToHex( JSON.stringify(msgbody) );

  let msgHash = web3.utils.sha3( msg ).slice( 2 );
  let eckp = ec.keyFromPrivate( myPrivkey, "hex" );
  let sig = bytesToHex( eckp.sign(msgHash).toDER() );

  var msgparams = [];
  msgparams.push( msg );
  msgparams.push( sig );

  var outbound = {};
  outbound['method'] = method;
  outbound['params'] = msgparams;
  outbound['id'] = '' + myPubkey;

  console.log( 'sendToDaemon on port: ' + daemonPort +
               ', input: ' + JSON.stringify(msgbody) +
               ', message: ' + JSON.stringify(outbound) +
               ', msgHash: ' + msgHash +
               ', sig: ' + sig );

  let client = new net.Socket();
  client.connect( daemonPort, '127.0.0.1', () => {
    client.write( JSON.stringify(outbound) );
    client.destroy();
  } );
}

function pubEventToObj( evt )
{
  let result = {};
  result.receiverpublickey = '' + evt.raw.topics[1];

  let decoded = web3.eth.abi.decodeParameters(
    ["string", "string"], evt.raw.data );

  result.ipfshash = decoded[0];
  result.redmeta = decoded[1];

  result.blocknum = evt.blockNumber;
  result.logindex = evt.logIndex;

  return result;
}

function textToHex( txt )
{
  let result = '';
  for (let ii = 0; ii < txt.length; ii++)
  {
    let it = txt.charCodeAt(ii).toString(16);
    if (it.length < 2) it = "0" + it;
    result += it;
  }
  return result;
}

function hexToText( hexs )
{
	let result = '';

	for (let ii = 0; ii < hexs.length; ii += 2)
		result += String.fromCharCode(parseInt(hexs.substr(ii, 2), 16));

	return result;
}

function hexToBytes(hex)
{
  let bytes = [];

  for (ii = 0; ii < hex.length; ii += 2)
    bytes.push( parseInt(hex.substr(ii, 2), 16) );

  return bytes;
}

function bytesToHex(bytes)
{
  let hex = [];

  for (ii = 0; ii < bytes.length; ii++)
  {
    var current = bytes[ii] < 0 ? bytes[ii] + 256 : bytes[ii];
    hex.push( (current >>> 4).toString(16) );
    hex.push( (current & 0xF).toString(16) );
  }

  return hex.join("");
}

