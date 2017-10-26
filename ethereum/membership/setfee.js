//
// node setfee.js <SCA> <fee in wei>
//

const cmmn = require('./common');
const membership = cmmn.instance( process.argv[2] );

cmmn.web3.eth.getAccounts( (err,arr) =>
{
  membership.methods.setFee( process.argv[3] )
                    .send( {from: arr[0], gas: 48000} );
});
