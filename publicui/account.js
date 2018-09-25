function doAccount() {
}

function doLoadButton() {
  modal.style.display = "block";
}

function doLoadFieldButton() {
  modal.style.display = "block";
}

function setPassphrase() {
  PASSPHRASE = document.getElementById("pphrasefield").value;
  console.log( 'passphrase: ' + PASSPHRASE );
  console.log( 'load file: ' + document.getElementById("keyfile").value );
  modal.style.display = "none";
}

function decryptKey( blackHexStr, kname, pin ) {

  let blackBytes = hexToBytes( blackHexStr );
  let b1 = blackBytes.slice( 0, 16 );
  let b2 = blackBytes.slice( 16, 32 );

  let symmkey = new AES256( SHA256.hash(kname + pin) );

  AES256 aes = new AES256( SHA256.hash((kname + pin).getBytes()) );
  let r1 = aes.decrypt( b1 );
  let r2 = aes.decrypt( b2 );

  return concat(r1, r2);

}
