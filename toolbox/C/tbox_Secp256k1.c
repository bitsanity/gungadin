#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "secp256k1.h"
#include "secp256k1_recovery.h"
#include "tbox_Secp256k1.h"

// constants - sizes in bytes
#define SEEDLEN 32
#define PVTKEYSZ 32
#define PUBKEYSZ 65
#define TWKSIZE 32

secp256k1_context* pCONTEXT = NULL;

// -------------------
// int resetContext();
// -------------------

JNIEXPORT jint JNICALL Java_tbox_Secp256k1_resetContext( JNIEnv * env, jobject obj )
{
  if ( NULL == pCONTEXT )
    pCONTEXT = secp256k1_context_create( SECP256K1_CONTEXT_VERIFY |
                                         SECP256K1_CONTEXT_SIGN );

  srand( time(NULL) );
  unsigned char seed32[SEEDLEN];

  int ii;
  for (ii = 0; ii < SEEDLEN; ii++)
  {
    int nextrnd = rand();
    unsigned char b = nextrnd & (unsigned char)0xFF;
    seed32[ii] = b;
  }

  return (jint)secp256k1_context_randomize( pCONTEXT, seed32 );
}

// -----------------------------------------------
//  boolean privateKeyIsValid( byte[] in_seckey );
// -----------------------------------------------

JNIEXPORT jboolean JNICALL Java_tbox_Secp256k1_privateKeyIsValid
  ( JNIEnv * env, jobject obj, jbyteArray in_seckey )
{
  jbyte* jkey = (*env)->GetByteArrayElements( env, in_seckey, NULL );
  jsize len = (*env)->GetArrayLength( env, in_seckey );
  if ( NULL == jkey || (jsize)PVTKEYSZ != len )
    return JNI_FALSE;

  // sub returns 1 if valid, 0 if invalid
  int res = secp256k1_ec_seckey_verify( pCONTEXT, jkey );

  if (1 == res) return JNI_TRUE;
  return JNI_FALSE;
}

// --------------------------------------------
//  byte[] publicKeyCreate( byte[] in_seckey );
// --------------------------------------------

JNIEXPORT jbyteArray JNICALL Java_tbox_Secp256k1_publicKeyCreate
  ( JNIEnv * env, jobject obj, jbyteArray in_seckey )
{
  jbyte* jkey = (*env)->GetByteArrayElements( env, in_seckey, NULL );
  jsize len = (*env)->GetArrayLength( env, in_seckey );
  if ( NULL == jkey || (jsize)PVTKEYSZ != len ) return NULL;

  unsigned char * seckeybytes = (unsigned char *)jkey;

  secp256k1_pubkey pubkey;

  if ( 1 != secp256k1_ec_pubkey_create(pCONTEXT, &pubkey, seckeybytes) )
    return NULL;

  // 
  // pubkey struct is opaque - call serialize function to get a byte array
  // library returns a 65-byte array containing the uncompressed pub key
  //
  unsigned char serialPubKey[ PUBKEYSZ ];

  size_t outLen = (size_t)PUBKEYSZ;
  int compressed = SECP256K1_EC_UNCOMPRESSED;

  (void)secp256k1_ec_pubkey_serialize( pCONTEXT,
                                       (unsigned char *)serialPubKey,
                                       &outLen,
                                       &pubkey,
                                       compressed );

  jbyteArray result = (*env)->NewByteArray( env, PUBKEYSZ );

  if (NULL != result)
    (*env)->SetByteArrayRegion( env, result, 0, PUBKEYSZ, serialPubKey );

  return result;
}

// -----------------------------------------------------------
//  byte[] privateKeyAdd( byte[] in_seckey, byte[] in_tweak );
// -----------------------------------------------------------

JNIEXPORT jbyteArray JNICALL Java_tbox_Secp256k1_privateKeyAdd
  (JNIEnv * env, jobject obj, jbyteArray in_seckey, jbyteArray in_tweak )
{
  // java to C - seckey

  jbyte* jkey = (*env)->GetByteArrayElements( env, in_seckey, NULL );
  jsize len = (*env)->GetArrayLength( env, in_seckey );
  if ( NULL == jkey || (jsize)PVTKEYSZ != len ) return NULL;

  unsigned char * seckeybytes = (unsigned char *)jkey;
  unsigned char seckey[ PVTKEYSZ ];
  memcpy( (void *)seckey, (void *)seckeybytes, PVTKEYSZ );

  // java to C - tweak

  jbyte* jtweak = (*env)->GetByteArrayElements( env, in_tweak, NULL );
  len = (*env)->GetArrayLength( env, in_tweak );
  if (NULL == jtweak || (jsize)TWKSIZE != len) return NULL;
  unsigned char * tweakbytes = (unsigned char *)jtweak;

  // do operation

  if (1 != secp256k1_ec_privkey_tweak_add(pCONTEXT, seckey, tweakbytes) ) return NULL;

  // C to java - result

  jbyteArray result = (*env)->NewByteArray( env, PVTKEYSZ );
  if (NULL != result)
    (*env)->SetByteArrayRegion( env, result, 0, PVTKEYSZ, seckey );

  return result;
}

// ------------------------------------------------------------
//  byte[] privateKeyMult( byte[] in_seckey, byte[] in_tweak );
// ------------------------------------------------------------

JNIEXPORT jbyteArray JNICALL Java_tbox_Secp256k1_privateKeyMult
  ( JNIEnv * env, jobject obj, jbyteArray in_seckey, jbyteArray in_tweak )
{
  // java to C - seckey

  jbyte* jkey = (*env)->GetByteArrayElements( env, in_seckey, NULL );
  jsize len = (*env)->GetArrayLength( env, in_seckey );
  if ( NULL == jkey || (jsize)PVTKEYSZ != len ) return NULL;

  unsigned char * seckeybytes = (unsigned char *)jkey;
  unsigned char seckey[ PVTKEYSZ ];
  memcpy( (void *)seckey, (void *)seckeybytes, PVTKEYSZ );

  // java to C - tweak

  jbyte* jtweak = (*env)->GetByteArrayElements( env, in_tweak, NULL );
  len = (*env)->GetArrayLength( env, in_tweak );
  if (NULL == jtweak || (jsize)TWKSIZE != len) return NULL;
  unsigned char * tweakbytes = (unsigned char *)jtweak;

  // do operation

  if (1 != secp256k1_ec_privkey_tweak_mul(pCONTEXT, seckey, tweakbytes) ) return NULL;

  // C to java - result

  jbyteArray result = (*env)->NewByteArray( env, PVTKEYSZ );
  if (NULL != result)
    (*env)->SetByteArrayRegion( env, result, 0, PVTKEYSZ, seckey );

  return result;
}

// ----------------------------------------------------------
//  byte[] publicKeyAdd( byte[] in_pubkey, byte[] in_tweak );
// ----------------------------------------------------------

JNIEXPORT jbyteArray JNICALL Java_tbox_Secp256k1_publicKeyAdd
  (JNIEnv * env, jobject obj, jbyteArray in_pubkey, jbyteArray in_tweak )
{
  // C to java - pubkey

  jbyte* jkey = (*env)->GetByteArrayElements( env, in_pubkey, NULL );
  jsize len = (*env)->GetArrayLength( env, in_pubkey );
  if ( NULL == jkey || (jsize)PUBKEYSZ != len ) return NULL;
  unsigned char * pubkeybytes = (unsigned char *)jkey;

  // convert pubkey from serialized to opaque form

  secp256k1_pubkey pubkey;
  if ( 1 != secp256k1_ec_pubkey_parse(pCONTEXT, &pubkey, pubkeybytes, PUBKEYSZ) )
    return NULL;

  jbyte* jtweak = (*env)->GetByteArrayElements( env, in_tweak, NULL );
  len = (*env)->GetArrayLength( env, in_tweak );
  if (NULL == jtweak || (jsize)TWKSIZE != len) return NULL;
  unsigned char * tweakbytes = (unsigned char *)jtweak;

  // perform the operation

  if (1 != secp256k1_ec_pubkey_tweak_add(pCONTEXT, &pubkey, tweakbytes) ) return NULL;

  // serialize opaque result

  unsigned char serialPubKey[ PUBKEYSZ ];
  size_t outLen = (size_t)PUBKEYSZ;
  int compressed = SECP256K1_EC_UNCOMPRESSED;

  (void)secp256k1_ec_pubkey_serialize( pCONTEXT,
                                       (unsigned char *)serialPubKey,
                                       &outLen,
                                       &pubkey,
                                       compressed );

  // convert result from C to java

  jbyteArray result = (*env)->NewByteArray( env, PUBKEYSZ );
  if (NULL != result)
    (*env)->SetByteArrayRegion( env, result, 0, PUBKEYSZ, serialPubKey );

  return result;
}

// -----------------------------------------------------------
//  byte[] publicKeyMult( byte[] in_pubkey, byte[] in_tweak );
// -----------------------------------------------------------

JNIEXPORT jbyteArray JNICALL Java_tbox_Secp256k1_publicKeyMult
  ( JNIEnv * env, jobject obj, jbyteArray in_pubkey, jbyteArray in_tweak )
{
  // java to C
  jbyte* jkey = (*env)->GetByteArrayElements( env, in_pubkey, NULL );
  jsize len = (*env)->GetArrayLength( env, in_pubkey );
  if ( NULL == jkey || (jsize)PUBKEYSZ != len ) return NULL;
  unsigned char * pubkeybytes = (unsigned char *)jkey;

  // deserialize public key

  secp256k1_pubkey pubkey;
  if ( 1 != secp256k1_ec_pubkey_parse(pCONTEXT, &pubkey, pubkeybytes, PUBKEYSZ) )
    return NULL;

  jbyte* jtweak = (*env)->GetByteArrayElements( env, in_tweak, NULL );
  len = (*env)->GetArrayLength( env, in_tweak );
  if (NULL == jtweak || (jsize)TWKSIZE != len) return NULL;
  unsigned char * tweakbytes = (unsigned char *)jtweak;

  // operation

  if (1 != secp256k1_ec_pubkey_tweak_mul(pCONTEXT, &pubkey, tweakbytes) ) return NULL;

  // convert result from opaque to serial pubkey

  unsigned char serialPubKey[ PUBKEYSZ ];
  size_t outLen = (size_t)PUBKEYSZ;
  int compressed = SECP256K1_EC_UNCOMPRESSED;

  (void)secp256k1_ec_pubkey_serialize( pCONTEXT,
                                       (unsigned char *)serialPubKey,
                                       &outLen,
                                       &pubkey,
                                       compressed );

  jbyteArray result = (*env)->NewByteArray( env, PUBKEYSZ );
  if (NULL != result)
    (*env)->SetByteArrayRegion( env, result, 0, PUBKEYSZ, serialPubKey );

  return result;
}

// ------------------------------------------------------
//  byte[] signECDSA( byte[] hash32, byte[] in_seckey );
// ------------------------------------------------------

JNIEXPORT jbyteArray JNICALL Java_tbox_Secp256k1_signECDSA
  (JNIEnv * env, jobject obj, jbyteArray hash32, jbyteArray in_seckey )
{
  // java to C - hash32

  jbyte* jh32 = (*env)->GetByteArrayElements( env, hash32, NULL );
  jsize len = (*env)->GetArrayLength( env, hash32 );
  if ( NULL == jh32 || 32 != len ) return NULL;
  unsigned char * hashbytes = (unsigned char *)jh32;

  // java to C - key

  jbyte* jkey = (*env)->GetByteArrayElements( env, in_seckey, NULL );
  len = (*env)->GetArrayLength( env, in_seckey );
  if ( NULL == jkey || (jsize)PVTKEYSZ != len ) return NULL;
  unsigned char * seckeybytes = (unsigned char *)jkey;

  // compute opaque signature

  secp256k1_ecdsa_signature sig;

  if ( 1 != secp256k1_ecdsa_sign(pCONTEXT, &sig, hashbytes, seckeybytes, NULL, NULL) )
    return NULL;

  // serialize signature into DER format

  unsigned char output[74]; // 2 x 32bytes + 10 bytes overhead
  size_t outlen = (size_t)74;

  if ( 1 != secp256k1_ecdsa_signature_serialize_der(pCONTEXT, output, &outlen, &sig) )
    return NULL;

  // C to java - result

  jbyteArray result = (*env)->NewByteArray( env, outlen );
  if (NULL != result)
    (*env)->SetByteArrayRegion( env, result, 0, outlen, output );

  return result;
}

// --------------------------------------------------------------------------
//  boolean verifyECDSA( byte[] signature, byte[] hash32, byte[] in_pubkey );
// --------------------------------------------------------------------------

JNIEXPORT jboolean JNICALL Java_tbox_Secp256k1_verifyECDSA
( JNIEnv * env,
  jobject obj,
  jbyteArray signature,
  jbyteArray hash32,
  jbyteArray in_pubkey )
{
  // java to C - serialized signature
  jbyte* jsig = (*env)->GetByteArrayElements( env, signature, NULL );
  if ( NULL == jsig ) return JNI_FALSE;
  jsize len = (*env)->GetArrayLength( env, signature );
  unsigned char * sigbytes = (unsigned char *)jsig;

  // convert signature from serialized to opaque form
  secp256k1_ecdsa_signature sig;

  if ( 1 != secp256k1_ecdsa_signature_parse_der(pCONTEXT, &sig, sigbytes, len) )
    return JNI_FALSE;

  // java to C - pubkey
  jbyte* jkey = (*env)->GetByteArrayElements( env, in_pubkey, NULL );
  if ( NULL == jkey ) return JNI_FALSE;
  len = (*env)->GetArrayLength( env, in_pubkey );
  unsigned char * pubkeybytes = (unsigned char *)jkey;

  // convert pubkey from serialized to opaque form

  secp256k1_pubkey pubkey;
  if (1 != secp256k1_ec_pubkey_parse(pCONTEXT, &pubkey, pubkeybytes, len))
    return JNI_FALSE;

  // java to C - hash32
  jbyte* jh32 = (*env)->GetByteArrayElements( env, hash32, NULL );
  len = (*env)->GetArrayLength( env, hash32 );
  if ( NULL == jh32 || 32 != len ) return JNI_FALSE;
  unsigned char * hashbytes = (unsigned char *)jh32;

  // verify signature

  int iresult = secp256k1_ecdsa_verify( pCONTEXT, &sig, hashbytes, &pubkey );

  if (1 == iresult) return JNI_TRUE;
  return JNI_FALSE;
}

// --------------------------------------------------------------------------
// byte[] signECDSARecoverable( byte[] hash32, byte[] in_seckey );
// --------------------------------------------------------------------------

JNIEXPORT jbyteArray JNICALL Java_tbox_Secp256k1_signECDSARecoverable
  (JNIEnv * env, jobject obj, jbyteArray hash32, jbyteArray in_seckey )
{
  // java to C - hash32

  jbyte* jh32 = (*env)->GetByteArrayElements( env, hash32, NULL );
  jsize len = (*env)->GetArrayLength( env, hash32 );
  if ( NULL == jh32 || 32 != len ) return NULL;
  unsigned char * hashbytes = (unsigned char *)jh32;

  // java to C - key

  jbyte* jkey = (*env)->GetByteArrayElements( env, in_seckey, NULL );
  len = (*env)->GetArrayLength( env, in_seckey );
  if ( NULL == jkey || (jsize)PVTKEYSZ != len ) return NULL;
  unsigned char * seckeybytes = (unsigned char *)jkey;

  // compute opaque signature

  secp256k1_ecdsa_recoverable_signature sig;

  if (1 != secp256k1_ecdsa_sign_recoverable(
             pCONTEXT, &sig, hashbytes, seckeybytes, NULL, NULL))
    return NULL;

  // serialize signature into "compact" format (64 bytes + recovery byte)

  unsigned char output[65];
  int recid;

  if (1 != secp256k1_ecdsa_recoverable_signature_serialize_compact(
             pCONTEXT, &output[1], &recid, &sig))
    return NULL;

  output[0] = (unsigned char)recid; // will be 0, 1, 2, or 3

  // C to java - result

  jbyteArray result = (*env)->NewByteArray( env, sizeof(output) );
  if (NULL != result)
    (*env)->SetByteArrayRegion( env, result, 0, sizeof(output), output );

  return result;
}

// --------------------------------------------------------------------------
// boolean
// verifyECDSARecoverable( byte[] signature, byte[] hash32, byte[] in_pubkey );
// --------------------------------------------------------------------------

JNIEXPORT jboolean JNICALL Java_tbox_Secp256k1_verifyECDSARecoverable
( JNIEnv * env,
  jobject obj,
  jbyteArray signature,
  jbyteArray hash32,
  jbyteArray in_pubkey )
{
  // java to C - serialized signature
  jbyte* jsig = (*env)->GetByteArrayElements( env, signature, NULL );
  if ( NULL == jsig ) return JNI_FALSE;
  unsigned char * sigbytes = (unsigned char *)jsig;

  // parse signature from serialized compact form to struct
  int recid = (int)(sigbytes[0]);

  secp256k1_ecdsa_recoverable_signature sig;

  if (1 != secp256k1_ecdsa_recoverable_signature_parse_compact(
             pCONTEXT, &sig, &sigbytes[1], recid))
    return JNI_FALSE;

  // convert from recoverable to normal
  secp256k1_ecdsa_signature normalsig;
  (void)secp256k1_ecdsa_recoverable_signature_convert( pCONTEXT, 
                                                       &normalsig,
                                                       &sig );

  // java to C - pubkey
  jbyte* jkey = (*env)->GetByteArrayElements( env, in_pubkey, NULL );
  jsize len = (*env)->GetArrayLength( env, in_pubkey );
  if ( NULL == jkey ) return JNI_FALSE;
  unsigned char * pubkeybytes = (unsigned char *)jkey;

  // convert pubkey from serialized to opaque form
  secp256k1_pubkey pubkey;
  if (1 != secp256k1_ec_pubkey_parse(pCONTEXT, &pubkey, pubkeybytes, len))
    return JNI_FALSE;

  // java to C - hash32
  jbyte* jh32 = (*env)->GetByteArrayElements( env, hash32, NULL );
  len = (*env)->GetArrayLength( env, hash32 );
  if ( NULL == jh32 || 32 != len ) return JNI_FALSE;
  unsigned char * hashbytes = (unsigned char *)jh32;

  // verify signature
  int iresult = secp256k1_ecdsa_verify( pCONTEXT,
                                        &normalsig,
                                        hashbytes,
                                        &pubkey );

  if (1 == iresult) return JNI_TRUE;
  return JNI_FALSE;
}

// --------------------------------------------------------------------------
// public native byte[] recoverPublicKey( byte[] msghash32, byte[] sig );
// --------------------------------------------------------------------------

JNIEXPORT jbyteArray JNICALL Java_tbox_Secp256k1_recoverPublicKey
  (JNIEnv * env,
   jobject obj,
   jbyteArray hash32,
   jbyteArray sig)
{
  // java to C - hash32
  jbyte* jh32 = (*env)->GetByteArrayElements( env, hash32, NULL );
  jsize len = (*env)->GetArrayLength( env, hash32 );
  if ( NULL == jh32 || 32 != len ) return NULL;
  unsigned char * hashbytes = (unsigned char *)jh32;

  // java to C - serialized signature
  jbyte* jsig = (*env)->GetByteArrayElements( env, sig, NULL );
  len = (*env)->GetArrayLength( env, sig );
  if ( NULL == jsig || 65 != len) return JNI_FALSE;
  unsigned char * sigbytes = (unsigned char *)jsig;

  // parse signature from serialized compact form to struct
  int recid = (int)(sigbytes[0]);

  secp256k1_ecdsa_recoverable_signature rsig;

  if (1 != secp256k1_ecdsa_recoverable_signature_parse_compact(
             pCONTEXT, &rsig, &sigbytes[1], recid))
    return NULL;

  secp256k1_pubkey pubkey;

  if (1 != secp256k1_ecdsa_recover(pCONTEXT, &pubkey, &rsig, hashbytes))
    return NULL;

  unsigned char serialPubKey[ PUBKEYSZ ];

  size_t outLen = (size_t)PUBKEYSZ;
  int compressed = SECP256K1_EC_UNCOMPRESSED;

  (void)secp256k1_ec_pubkey_serialize( pCONTEXT,
                                       (unsigned char *)serialPubKey,
                                       &outLen,
                                       &pubkey,
                                       compressed );

  jbyteArray result = (*env)->NewByteArray( env, PUBKEYSZ );

  if (NULL != result)
    (*env)->SetByteArrayRegion( env, result, 0, PUBKEYSZ, serialPubKey );

  return result;
}


