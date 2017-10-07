const cmmn = require('./common');

var cb;
cmmn.web3.eth.getCoinbase().then( (res) =>
{
  cmmn.contract.deploy( {data: cmmn.bytecode,arguments:[]} )
               .send( {from:res, gas:2000000}, (err,res) =>
               {
                 if (err) console.log(err);
                 if (res) console.log(res);
               } )
               .then( (receipt) => {
                 console.log( 'SCA: ', receipt.options.address );
               } );
} );
