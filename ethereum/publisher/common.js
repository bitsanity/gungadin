//
//
const fs = require('fs');
const Web3 = require('web3');
const web3 = new Web3(new Web3.providers.HttpProvider('http://localhost:8545'));
exports.web3 = web3;

const abifile = 'Publisher_sol_Publisher.abi';
const binfile = 'Publisher_sol_Publisher.bin';

const abi = JSON.parse( fs.readFileSync(abifile).toString() );
exports.abi = abi;

const bytecode = fs.readFileSync(binfile).toString();
exports.bytecode = bytecode;

const contract = new web3.eth.Contract(abi);
exports.contract = contract;

exports.instance = function(sca) {
  return new web3.eth.Contract(abi,sca);
}

