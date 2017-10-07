//
// node transfer.js <SCA> <addressindex> <toaddress>
//

const cmmn = require('./common');
const membership = cmmn.instance( process.argv[2] );

const ix = process.argv[3];

cmmn.web3.eth.getAccounts( (err,arr) =>
{

  // sender must already be owner
  membership.methods.transferOwnership( process.argv[4] )
    .send( {from: arr[ix], gas: 48000} );

});
