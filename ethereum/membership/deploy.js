const cmmn = require('./common');

var cb;
cmmn.web3.eth.getAccounts().then( (arr) =>
{
  cmmn.contract.deploy( {data: cmmn.bytecode(),arguments:[]} )
               .send( {from:arr[0], gas:2000000}, (err,obj) =>
               {
                 if (err) console.log(err);
                 if (obj) console.log(obj);
               } )
               .then( (receipt) => {
                 console.log( 'SCA: ', receipt.options.address );
               } );
} );
