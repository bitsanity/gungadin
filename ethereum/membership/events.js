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

    if (events[ii].event == 'AddedMember' )
    {
      console.log( "AddedMember " + shorten(events[ii].raw.topics[1]) );
    }
    if (events[ii].event == 'DroppedMember')
    {
      console.log( "DroppedMember " + shorten(events[ii].raw.topics[1]) );
    }
  }
});

