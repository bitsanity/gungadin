//
// node suicide.js <SCA>
//

const cmmn = require('./common');
const membership = cmmn.instance( process.argv[2] );

cmmn.web3.eth.getAccounts( (err,arr) =>
{
  console.log( "commented out for safety" );
  //membership.methods.closedown().send( {from: arr[0], gas: 48000} );
});
