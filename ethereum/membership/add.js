//
// node add.js <SCA> <address to add>
//

const cmmn = require('./common');

const membership = cmmn.instance( process.argv[2] );

cmmn.web3.eth.getCoinbase().then( (cb) => {
  membership.methods.addMember(
    process.argv[3] ).send( {from: cb, gas: 100000} );

} );

