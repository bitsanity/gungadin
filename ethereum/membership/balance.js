//
// node balance.js <SCA>
//

const cmmn = require('./common');

cmmn.web3.eth.getBalance(process.argv[2]).then( (bal) =>
{
  console.log( bal );
});
