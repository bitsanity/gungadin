//
// node pay.js <SCA> <from: accountindex> <value in wei>
//

const cmmn = require('./common');

const membership = cmmn.instance( process.argv[2] );

var ix = process.argv[3];

cmmn.web3.eth.getAccounts( (err,arr) =>
{
  cmmn.web3.eth.sendTransaction( {from: arr[ix],
                                  to: process.argv[2],
                                  gas: 47000,
                                  value: process.argv[4]} );
} );
