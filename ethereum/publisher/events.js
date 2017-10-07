//
// node events.js <SCA>
//

// event Published( bytes receiverpubkey, string ipfshash );

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
      '---------------------------------------------------------------------' );

    if (events[ii].event == 'Published' )
    {
      var arg2off = parseInt( events[ii].raw.data.slice(66, 130), 16 );
      var bytelen = parseInt( events[ii].raw.data.slice(130, 194), 16 );
      var bytearg = events[ii].raw.data.slice(194, 194+2*bytelen);

      var hashlenstart = arg2off * 2 + 2;
      var hashlen = parseInt(
        events[ii].raw.data.slice(hashlenstart, hashlenstart+64), 16 );

      var hashbytes =
        events[ii].raw.data.slice( hashlenstart + 64,
                                   hashlenstart + 64 + hashlen * 2 );

      var hashstr = new Buffer( hashbytes,'hex' );

      console.log( "Published\npubkey is " +
                   bytelen + ' bytes: 0x' + bytearg +
                   "\nhashstr: " + hashstr
                   );
    }
  }
});

/*
 * How events are formatted:
 *

Format:

"0x" +

length of header, in characters
offset to start of second parameter, in chars
number of bytes in public key parameter (65 = uncompressed public key)
public key (65 bytes = 130 chars, zero-padded to next 32 byte/64-char boundary)
number of bytes in ipfshash parameter (42, 2 characters per byte = 84 chars)
hashstring as hexstring, underlying encoding UTF-8

Example:

0x
0000000000000000000000000000000000000000000000000000000000000040  64    64
00000000000000000000000000000000000000000000000000000000000000c0 128   192=>384
0000000000000000000000000000000000000000000000000000000000000041 192    65
000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f 256
202122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f 320
ff00000000000000000000000000000000000000000000000000000000000000 384
000000000000000000000000000000000000000000000000000000000000002a 448    42
516d303030303030303030303131313131313131313132323232323232323232 512
3333333333333333333300000000000000000000000000000000000000000000 576

*/
