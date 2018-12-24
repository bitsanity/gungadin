// main
// NOTE: installation requirement - the private key for the first ethereum
//       account must be shared with the daemon as the key is also the node
//       identifier in the system

if ( process.argv.length != 7 ) {
  console.log(
    process.argv.length,
    'Args: <myport> ' +
          '<daemonport> ' +
          '<daemonpubkey> ' +
          '<publsca> ' +
          '<votesca>' );
  process.exit( 1 );
}

const respHeader = { 'Content-Type' : 'application/json; charset=utf-8' };

// TODO : should adjust itself dynamically per network conditions
const fileSizeFeeWei = 1e6;

// TODO : should read the fee from the smart contract directly on init and if
//        a vote is rejected due to insufficient fee
const votingFeeWei = 1e6;

const fs = require( 'fs' );
const http = require( 'http' );
const Web3 = require( 'web3' );
const web3 =
  new Web3( new Web3.providers.WebsocketProvider("ws://localhost:8546") );

var myPort = process.argv[2];
var daemonPort = process.argv[3];
var daemonPubkey = process.argv[4];
var publishSCA = process.argv[5];
var voteSCA = process.argv[6];

var myAddress;
web3.eth.getAccounts().then( arr => {

  myAddress = arr[0];

  let msg = 'content not important';

  web3.eth.sign( msg, myAddress ).then( sig => {
    myPubkey = web3.eth.personal.ecRecover( msg, sig );
    let derivedaddress = web3.eth.keccak256( myPubkey ).slice(12,32);
    console.log( 'ethgw public key:\n\t', myPubkey,
                 '\n\taddress', derivedAddress );
  } );
} );

var publisher = new web3.eth.Contract(
  JSON.parse( fs.readFileSync('../publisher/build/Publisher_sol_Publisher.abi')
                .toString() ), publishSCA );

var votes = new web3.eth.Contract(
  JSON.parse(
    fs.readFileSync('../votes/build/Votes_sol_Votes.abi').toString() ),
    voteSCA );

http.createServer( (req, resp) => {
  resp.on( 'error', (err) => { console.log( 'resp error: ' + err ); } );

  try
  {
    if ( req.method != 'POST' ) throw 'only POST supported';
    let body = [];

    req.on( 'error', (err) => {
      console.log( 'request error: ' + err );
      resp.writeHead( 400, respHeader );
      resp.end( '{"error" : "' + err + '"}' );

    } ).on( 'data', (data) => {

      body.push( data );

    } ).on( 'end', () => {

        body = Buffer.concat( body ).toString();
        handleMessage( JSON.parse(body) );

        resp.writeHead( 201, { respHeader } );
        resp.end( '{}' );

    } );
  }
  catch( ex )
  {
    resp.writeHead( 400, { respHeader } );
    resp.end( '{"error" : "' + ex + '" }' );
  }
} ).listen( myPort );

function handleMessage( cmd )
{
  var mth = cmd['method'];
  var msg = cmd['params'][0];
  var sig = cmd['params'][1];
  var pbk  = cmd['id'];

  var signer = web3.eth.personal.ecRecover( msg, sig );
  if ( !(signer === pbk) || !(signer === daemonPubkey) )
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

  web3.eth.sign( msg, myAddress ).then( sig => {
    var msgparams = [];
    msgparams.push( msg );
    msgparams.push( web3.utils.utf8ToHex(sig) );

    var msg = {};
    msg['method'] = method;
    msg['params'] = msgparams;
    msg['id'] = myPubkey;
  } );
}

