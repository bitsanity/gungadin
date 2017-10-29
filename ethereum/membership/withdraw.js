//
// node withdraw.js <SCA> <value in wei>
//

const cmmn = require('./common');
const membership = cmmn.instance( process.argv[2] );

cmmn.web3.eth.getAccounts().then( (arr) => {

  membership.methods.withdraw( process.argv[3] ).send({from:arr[0], gas:47000});
} );
