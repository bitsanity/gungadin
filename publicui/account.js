var PASSPHRASE = null;

function doAccount() {
}

function doLoadButton() {
  modal.style.display = "block";
}

function setPassphrase() {
  PASSPHRASE = document.getElementById("pphrasefield").value;
  console.log( 'passphrase: ' + PASSPHRASE );
  console.log( 'load file: ' + document.getElementById("keyfile").value );
  modal.style.display = "none";
}
