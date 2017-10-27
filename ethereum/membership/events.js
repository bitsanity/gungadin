//
// node events.js <SCA>
//

const cmmn = require('./common');

function shorten(addr) {
  var saddr = "" + addr;
  return "0x" + saddr.substring(26);
}

cmmn.instance( process.argv[2] )
  .getPastEvents('allEvents',{fromBlock:0, toBlock:'latest'})
  .then( (events) =>
{
  for (var ii =0; ii < events.length; ii++) {
    console.log(
      '--------------------------------------------------------------------' );

    if (events[ii].event == 'Added' )
    {
      console.log( "Added " + shorten(events[ii].raw.topics[1]) );
    }
    else if (events[ii].event == 'DroppedMember')
    {
      console.log( "Dropped " + shorten(events[ii].raw.topics[1]) );
    }
    else if (events[ii].event == 'Fee')
    {
      var decoded =
        cmmn.web3.eth.abi.decodeParameters( ["uint256"],
                                            events[ii].raw.data );

      console.log( "Fee set to: " + decoded['0'] );
    }
  }
});

