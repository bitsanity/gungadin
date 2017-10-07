//
// node publish.js <publisherSCA> <pubkey> <hash>
//

const cmmn = require('./common');

const publisher = cmmn.instance( process.argv[2] );

cmmn.web3.eth.getCoinbase().then( (cb) =>
{

  publisher.methods.publish( process.argv[3], process.argv[4] )
    .send( {from: cb, gas: 100000} );

} );

