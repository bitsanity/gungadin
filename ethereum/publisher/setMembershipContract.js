//
// node setMembershipContract.js <publisherSCA> <membershipSCA>
//

const cmmn = require('./common');

const publisher = cmmn.instance( process.argv[2] );

cmmn.web3.eth.getCoinbase().then( (cb) => {

  publisher.methods.setMembershipContract( process.argv[3] ).send(
    {from:cb,gas:100000} );

} );

