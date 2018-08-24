// Usage:
//
//   node <this.js> <votes SCA>
//
// Assumptions:
// [a] the first account available on the local ethereum client (geth) is
//     funded and able to send rewards to lottery winners

const DELAYBLOCKS = 100;

const fs = require('fs');
const loki = require('lokijs');
const Web3 = require('web3');
const web3 =
  new Web3(new Web3.providers.HttpProvider("http://localhost:8545"));
//new Web3(new Web3.providers.WebsocketProvider("ws://localhost:8545"));
//new Web3(new Web3.providers.WebsocketProvider("ws://localhost:8546"));

function getABI() {
  return JSON.parse(
    fs.readFileSync('../build/Votes_sol_Votes.abi').toString() );
}

var votes = new web3.eth.Contract( getABI(), process.argv[2] );
var table = null;
var highBlock = 0;
var lowBlock = 0;

var db = new loki('lottery.db', {
  autoload: true,
  autoloadCallback: dbInit,
  autosave: true, 
  autosaveInterval: 4000
});

function dbInit() {
  table = db.getCollection("Votes");
  if (table === null) {
    table = db.addCollection("Votes");
  }
}

votes.Vote().watch( (err, evt) => {
  if (!err) {

    let eventBlockNum = evt.args['blocknum'];

    table.insert(
      {
        'block' : eventBlockNum,
        'voter' : evt.args['voter'],
        'shard' : evt.args['voter'].toLowerCase().slice( -1 ),
        'hash'  : evt.args['hash']
      }
    );

    if (eventBlockNum > (highBlock - DELAYBLOCKS)) {

      let winner = doLottery();

      table.removeWhere( obj => {
        return obj.block == lowBlock;
      } );

      lowBlock += 1;

      doReward( winner );
    }
  }
} );

var blockFilter = web3.eth.filter( 'latest' );
blockFilter.watch( (err, res) => {
  if (!err) {
    web3.eth.getBlock(res).then( block => {
      highBlock = block.number;
    } )
    .catch( ex => { console.log } );
  }
} );

function doLottery() {

  //
  // eliminate aberrant votes
  //

  for (let shard = 'a'; shard <= 'f'; shard++) {

    let shardVotes = table.where( obj => {
      return    obj.shard == shard
             && obj.block == lowBlock;
    } ).data();

    let totalVotes = 0;
    let tally = {}; // mapping hash -> count

    for (let ii = 0; ii < shardVotes.length; ii++) {
      tally[ shardVotes[ii]['hash'] ] += 1;
      totalVotes += 1;
    }

    let sortedHashes = Object.keys(tally).sort( (a,b) => {
      return tally[b] - tally[a];
    } );

    if (tally[sortedHashes[0]] > totalVotes / 2) {
      let winningHash = tally[sortedHashes[0]];

      table.removeWhere( obj => {
        return    obj.shard == shard
               && obj.block == lowBlock
               && obj.hash != winningHash;
      } );
    }
  } // end foreach shard

  //
  // select a voter at random
  //

  let allVotesInBlock = table.where( obj => {
      return obj.block == lowBlock;
  } ).data();

  let winningIndex = Math.floor( (Math.random() * allVotesInBlock.length) );

  return allVotesInBlock[winningIndex]['voter'];
}

function doReward( winner ) {

  web3.eth.getAccounts().then( accts => {
    web3.eth.getGasPrice().then( prx => {
      web3.eth.getBalance( accts[0] ).then( bal => {

        let weiToSend = bal - 21000 * prx - 1;

        web3.eth.sendTransaction(
          { from: accts[0],
            to: winner,
            value: weiToSend,
            gas: 21000,
            gasPrice: prx
          } );
      } );
    } );
  } );
}

