//
// node setMembershipContract.js <publisherSCA> <membershipSCA>
//

const cmmn = require('./common');

const publisher = cmmn.instance( process.argv[2] );

cmmn.web3.eth.getAccounts().then( (arr) => {

  publisher.methods.setMembershipContract( process.argv[3] ).send(
    {from:arr[0],gas:100000} );

} );

