// usage:
//   node <this.js> <publisher SCA>
//
const fs = require( 'fs' );
const http = require( 'http' );
const Web3 = require( 'web3' );
const web3 =
  new Web3( new Web3.providers.WebsocketProvider("ws://localhost:8546") );

const respHeader = { 'Content-Type' : 'application/json; charset=utf-8' };

var EC = require( 'elliptic' ).ec;
var ec = new EC( 'secp256k1' );

var publishSCA = process.argv[2];
var publisher = new web3.eth.Contract(
  JSON.parse( fs.readFileSync('../publisher/build/Publisher_sol_Publisher.abi')
                .toString() ), publishSCA );

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
        let answer = handleMessage( JSON.parse(body) );
        resp.writeHead( 201, { respHeader } );
        resp.end( JSON.stringify(answer) );

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
  var pbk = cmd['id'];

  // id could be anyone - just confirm they signed using the provided pubkey
  let msgHash = web3.utils.sha3( msg );
  let dpubk = ec.keyFromPublic( pbk, 'hex' );
  if (!dpubk.verify(msgHash,sig)) {
    console.log( "invalid sig by " + pbk );
    return;
  }

  var msgbod = JSON.parse( web3.hexToUtf8(msg) );

  if ('getIPFSHashes' === mth)
    return handleGetHashes( msgbod['pubkey'] );
}

async function handleGetHashes( pubkey ) {

  let pubs = await publisher.getPastEvents(
    'allEvents',
    {
      filter: {receiverpubkey:"'" + pubkey + "'"},
      fromBlock:0,
      toBlock:'latest'
    } );

  let result = {};

  result['pubkey'] = pubkey;
  result['hashes'] = [];
  result['timestamps'] = [];

  for (let ii = 0; ii < pubs.length; ii++) {
    let decoded = web3.eth.abi.decodeParameter( 'string', pubs[ii].raw.data );
    result['hashes'].push( decoded );
    result['timestamps'].push(
      web3.eth.getBlock(pubs[ii].blockNumber).timestamp );
  }

  return result;
}
