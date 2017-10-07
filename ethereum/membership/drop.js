//
// node drop.js <SCA> <address to drop>
//

const cmmn = require('./common');

const membership = cmmn.instance( process.argv[2] );

cmmn.web3.eth.getCoinbase().then( (cb) => {
  membership.methods.dropMember( process.argv[3] )
    .send( {from: cb, gas: 100000} );

} );

