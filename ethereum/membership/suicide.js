//
// node suicide.js <SCA> <owneraddressix>
//

const cmmn = require('./common');
const membership = cmmn.instance( process.argv[2] );

const ix = process.argv[3];

cmmn.web3.eth.getAccounts( (err,arr) =>
{
  console.log( "commented out for safety" );
  //membership.methods.closedown().send( {from: arr[ix], gas: 48000} );
});
