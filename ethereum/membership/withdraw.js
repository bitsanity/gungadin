//
// node withdraw.js <SCA> <value in wei>
//

const cmmn = require('./common');
const membership = cmmn.instance( process.argv[2] );

cmmn.web3.eth.getCoinbase().then( (cb) => {

  membership.methods.withdraw( process.argv[3] ).send({from:cb, gas:47000});
} );
