const fs = require('fs');
const Web3 = require('web3');
const web3 = new Web3(new Web3.providers.HttpProvider("http://localhost:8545"));

function getABI() {
  var contents = fs.readFileSync('./build/' +
                   '/MembershipMock_sol_MembershipMock.abi').toString();
  var abiObj = JSON.parse(contents);
  return abiObj;
}

if (5 != process.argv.length)
{
  console.log( 'node setMock.js <SCA> <isMember> <isApproved>' );
  process.exit(1);
}

var sca = process.argv[2];
var mret = process.argv[3] === 'true';
var aret = process.argv[4] === 'true';

web3.eth.getAccounts().then( eb => {

  var con = new web3.eth.Contract( getABI(), sca );

  con.methods.setReturn( mret, aret )
     .send( {from: eb[0], gas: 250000}, (err,res) => {
       if (err) console.log(err);
       if (res) console.log(res);
     } )
     .catch( e => { console.log(e); } );
} )
.catch( e => { console.log(e.toString()); } );
