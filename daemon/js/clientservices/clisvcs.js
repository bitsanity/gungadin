// usage:
//   node <this.js> <publisher SCA>
//
const fs = require( 'fs' );
const http = require( 'http' );
const Web3 = require( 'web3' );
const web3 =
  new Web3( new Web3.providers.WebsocketProvider("ws://localhost:8546") );

const respHeader = { 'Content-Type' : 'application/json; charset=utf-8' };

var publishSCA = argv[2];
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

function handleMessage( JSONObject cmd )
{
  var mth = cmd['method'];
  var msg = cmd['params'][0];
  var sig = cmd['params'][1];
  var pbk = cmd['id'];

  var signer = web3.eth.personal.ecRecover( msg, sig );
  if ( !(signer === pbk) ) {
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

  for (let ii = 0; ii < pubs.length; ii++) {
    let decoded = web3.eth.abi.decodeParameter( 'string', pubs[ii].raw.data );
    result['hashes'].push( decoded );
  }

  return result;
}
