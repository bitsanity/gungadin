//
// node drop.js <SCA> <address to drop>
//

const cmmn = require('./common');

const membership = cmmn.instance( process.argv[2] );

cmmn.web3.eth.getAccounts().then( (arr) => {
  membership.methods.suspend( process.argv[3] )
    .send( {from: arr[0], gas: 100000} );

} );

