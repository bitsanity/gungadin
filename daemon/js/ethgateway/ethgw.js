// main
// NOTE: installation requirement - the private key for the first ethereum
//       account must be shared with the daemon since it is also the node
//       identifier in the system

if ( process.argv.length != 3 ) {
  console.log(
    'Args: <myport> <daemonport> <daemonpubkey> <publsca> <votesca>' );
  process.exit( 1 );
}

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

var publisher = new web3.eth.Contract(
  JSON.parse( fs.readFileSync('../publisher/build/Publisher_sol_Publisher.abi')
                .toString() ),
  publishSCA );

var votes = new web3.eth.Contract(
  JSON.parse(
    fs.readFileSync('../votes/build/Votes_sol_Votes.abi').toString() ),
  voteSCA );

const respHeader = { 'Content-Type' : 'application/json; charset=utf-8' };

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

function handleMessage( JSONObject cmd )
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
    handlePublish( msgbod['recipkey', msgbod['hash'], msgbod['fsize'] );
  else if ('vote' === mth)
    handleVote( msgbod['blocknum'], msgbod['hash'] );
}

function handleNewHWM( newhwm )
{
}

function handlePublish( recipkey, hash, fsize )
{
}

function handleVote( blocknum, hash )
{
}

