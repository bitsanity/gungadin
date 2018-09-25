
function hexToBytes(hex) {

  if (hex.startsWith("0x"))
    hex = hex.substring(2);

  for (var bytes = [], c = 0; c < hex.length; c += 2)
    bytes.push(parseInt(hex.substr(c, 2), 16));

  return bytes;
}

function bytesToHex(bytes) {

  for (var hex = [], i = 0; i < bytes.length; i++) {
    hex.push((bytes[i] >>> 4).toString(16));
    hex.push((bytes[i] & 0xF).toString(16));
  }

  return "0x" + hex.join("");
}

