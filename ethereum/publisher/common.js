//
//
const fs = require('fs');
const Web3 = require('web3');
const web3 = new Web3(new Web3.providers.HttpProvider('http://localhost:8545'));
exports.web3 = web3;

const abifile = './build/Publisher_sol_Publisher.abi';
const binfile = './build/Publisher_sol_Publisher.bin';

const abi = JSON.parse( fs.readFileSync(abifile).toString() );
exports.abi = abi;

exports.bytecode = function() {
  var binary = fs.readFileSync(binfile).toString();
  if (!binary.startsWith('0x'))
    binary = '0x' + binary;
  return binary;
}

const contract = new web3.eth.Contract(abi);
exports.contract = contract;

exports.instance = function(sca) {
  return new web3.eth.Contract(abi,sca);
}

