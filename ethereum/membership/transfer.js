//
// node transfer.js <SCA> <toaddress>
//

const cmmn = require('./common');
const membership = cmmn.instance( process.argv[2] );

cmmn.web3.eth.getAccounts(arr) =>
{
  // sender must already be owner
  membership.methods.changeOwner( process.argv[4] )
    .send( {from: arr[0], gas: 48000} );

});
