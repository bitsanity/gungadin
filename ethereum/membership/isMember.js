//
// node isMember.js <SCA> <address to check>
//

const cmmn = require('./common');

const membership = cmmn.instance( process.argv[2] );

membership.methods.isMember(process.argv[3]).call().then( (res) =>
{
  console.log( process.argv[3], ': ', res );
} );

