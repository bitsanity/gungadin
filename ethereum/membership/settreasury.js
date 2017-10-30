//
// node this.js <SCA> <Treasury address>
//

const cmmn = require('./common');
const membership = cmmn.instance( process.argv[2] );

cmmn.web3.eth.getAccounts( (err,arr) =>
{
  membership.methods.setTreasury( process.argv[3] )
                    .send( {from: arr[0], gas: 48000} );
});
