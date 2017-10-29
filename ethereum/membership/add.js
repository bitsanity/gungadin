//
// node add.js <SCA> <address to add>
//

const cmmn = require('./common');

const membership = cmmn.instance( process.argv[2] );

cmmn.web3.eth.getAccounts().then( (arr) => {
  membership.methods.approve(
    process.argv[3] ).send( {from: arr[0], gas: 100000} );

} );

