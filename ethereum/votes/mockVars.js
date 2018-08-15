const fs = require('fs');
const Web3 = require('web3');
const web3 = new Web3(new Web3.providers.HttpProvider("http://localhost:8545"));

function getABI() {
  var contents = fs.readFileSync('./build/' +
                   '/MembershipMock_sol_MembershipMock.abi').toString();
  var abiObj = JSON.parse(contents);
  return abiObj;
}

var sca = process.argv[2];

var con = new web3.eth.Contract( getABI(), sca );

con.methods.isMember_().call().then( (ismem) => {
  console.log( 'ismember: ' + ismem );
} );

con.methods.approved_().call().then( (appr) => {
  console.log( 'approved: ' + appr );
} );
