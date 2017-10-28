const cmmn = require('./common');

var cb;
cmmn.web3.eth.getAccounts().then( (arr) =>
{
  cmmn.contract.deploy( {data: cmmn.bytecode(),arguments:[]} )
               .send( {from:arr[0], gas:2000000}, (err,res) =>
               {
                 if (err) console.log(err);
                 if (res) console.log(res);
               } )
               .then( (receipt) => {
                 console.log( 'SCA: ', receipt.options.address );
               } );
} );
