/*
Copyright (c) 2008-2010, The University of Manchester, United Kingdom.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
 * Neither the name of the The University of Manchester nor the names of
      its contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

  Author........: Bruno Harbulot
  Contributor...: Henry Story
 */

package org.apache.clerezza.ssl.keygen.bouncy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.ssl.keygen.Certificate;
import org.apache.clerezza.ssl.keygen.KeygenService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.crmf.*;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.netscape.NetscapeCertRequest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Map;

import static org.apache.clerezza.ssl.keygen.bouncy.DefaultPubKey.create;

/**
 * <p>
 * Service that can then be called by server side components to create certificates
 * from <a href="http://en.wikipedia.org/wiki/Certification_request">Certification Requests</a>.
 * </p>
 * <p>This one uses bouncycastle encryption library.</p>
 *
 * Question: should the methods be throwing exceptions or should they log errors and return null?
 *
 * @author Bruno Harbulot
 * @author Henry Story
 * @since Feb 17, 2010
 */
// Annotations explained at http://felix.apache.org/site/apache-felix-maven-scr-plugin.html
@Component(specVersion = "1.1")
@Service
public class BouncyKeygenService implements KeygenService {
	KeyStore keyStore;
	PrivateKey privateKey;
	X509Certificate certificate;
	SecureRandom numberGenerator;
	static transient final Logger log = LoggerFactory.getLogger(BouncyKeygenService.class);

	static {
		//if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		//} else throw new Error("missing BouncyCastleProvider -- add jars to classpath");
	}

	/**
	 * partly taken from UUID class. Generates random numbers
	 *
	 * @return a UUID BigInteger
	 */
	BigInteger nextRandom() {
		SecureRandom ng = numberGenerator;
		if (ng == null) {
			numberGenerator = ng = new SecureRandom();
		}

		byte[] randomBytes = new byte[16];
		ng.nextBytes(randomBytes);
		return new BigInteger(randomBytes).abs();
	}

	/**
	 * OSGi activate method, taking properties in order to reduce dependencies.
	 *
	 * @param properties
	 * @see <a href="http://www.osgi.org/javadoc/r4v42/org/osgi/service/component/ComponentContext.html">the Component Context javadoc</a>
	 */
	protected void activate(Map properties) {
		log.info("in keygen activate");
		try {
			initialize();
		} catch (Exception e) {
			log.warn("could not activate keygen component", e);
			throw new Error("could not activate keygen component", e);
		}
	}

	public void initialize() throws Exception {
		log.info("initializing " + this.getClass().getCanonicalName());
		URL certFile = BouncyKeygenService.class.getResource("/cacert.p12");
		InputStream in;
		try {
			in = certFile.openStream();
		} catch (IOException e) {
			throw new Exception("could not load cert file " + certFile);
		}
		String alias = null;

		try {
			keyStore = KeyStore.getInstance("PKCS12");
		} catch (KeyStoreException e) {
			throw new Exception("could not get instance of PKCS12 keystore! SEVERE!", e);
		}
		try {
			keyStore.load(in, "testtest".toCharArray());  //the p12 keystore should really have no password! no need.
		} catch (CertificateException e) {
			throw new Exception("certificate extension found while loading store!", e);
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("missing algorithm for reading store!", e);
		} catch (IOException e) {
			throw new Exception("Could not read keystore shipped with jar!", e);
		}

		// for some reason we don't have a fixed alias...
		// some tools produce aliases, others don't, so we search, though really we should have this fixed
		try {
			Enumeration<String> aliases = keyStore.aliases();
			while (aliases.hasMoreElements()) {
				String tempAlias = aliases.nextElement();
				if (keyStore.isKeyEntry(tempAlias)) {
					alias = tempAlias;
					break;
				}
			}
		} catch (KeyStoreException e) {
			throw new Exception("could not find alias", e);
		}


		if (alias == null) {
			throw new Exception(
				"Invalid keystore configuration: alias unspecified ");
		}

		try {
			privateKey = (PrivateKey) keyStore.getKey(alias, "testtest".toCharArray());
		} catch (KeyStoreException e) {
			throw new Exception("could not get key with alias " + alias, e);
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("missing algorithm for reading store!", e);
		} catch (UnrecoverableKeyException e) {
			throw new Exception("could not recover private key in store", e);
		}


		try {
			certificate = (X509Certificate) keyStore.getCertificate(alias);
		} catch (KeyStoreException e) {
			throw new Exception("problem getting certificate with alias " + alias + "from keystore.", e);
		}
		log.info("Initialization of " + this.getClass().getCanonicalName() + " successful.");
	}

	@Override
	public Certificate createFromPEM(String pemCsr) {
        if (pemCsr == null) {
            log.warn("pemCsr was null");
            return null;
        }
		PEMReader pemReader = new PEMReader(new StringReader(pemCsr));
		Object pemObject;
		try {
			pemObject = pemReader.readObject();
			if (pemObject instanceof PKCS10CertificationRequest) {
				PKCS10CertificationRequest pkcs10Obj = (PKCS10CertificationRequest) pemObject;
				DefaultCertificate cert = new DefaultCertificate(this);
				cert.setDefaultSerialisation(new PEMSerialisation(cert));
				try {
					cert.setSubjectPublicKey(create(pkcs10Obj.getPublicKey()));
					return cert;
				} catch (NoSuchAlgorithmException e) {
					log.warn("Don't know algorithm required by certification request ", e);
				} catch (NoSuchProviderException e) {
					log.warn("Don't have provider for certification request ", e);
				} catch (InvalidKeyException e) {
					log.warn("Invalid key sent in certificate request", e);
				}
			}
		} catch (IOException e) {
			log.warn("How can this happen? Serious! An IOEXception on a StringReader?", e);
		}
		return null;
	}

	@Override
	public Certificate createFromSpkac(String spkac) {
		if (spkac == null) {
            log.warn("SPKAC parameter is null, should be checked before");
            return null;
        }
		try {
			NetscapeCertRequest certRequest = new NetscapeCertRequest(Base64.decode(spkac));
			DefaultCertificate cert = new DefaultCertificate(this);
			cert.setDefaultSerialisation(new DERSerialisation(cert));
			cert.setSubjectPublicKey(create(certRequest.getPublicKey()));
			return cert;
		} catch (IOException e) {
			log.warn("how can an IOError occur when reading a string?", e);
		}
		return null;
	}

	@Override
	public Certificate createFromCRMF(String crmfReq)  {
		ASN1InputStream asn1InputStream = new ASN1InputStream(Base64.decode(crmfReq));
		CertReqMessages certReqMessages = null;
		try {
			certReqMessages = CertReqMessages
				.getInstance(asn1InputStream.readObject());
			CertReqMsg certReqMsg = certReqMessages.toCertReqMsgArray()[0];
			CertRequest certRequest = certReqMsg.getCertReq();

			/* (see RFC 2511)

					  CertRequest ::= SEQUENCE {
					  certReqId     INTEGER,          -- ID for matching request and reply
					  certTemplate  CertTemplate,  -- Selected fields of cert to be issued
					  controls      Controls OPTIONAL }   -- Attributes affecting issuance

			*/


//			System.out.println("Certificate Request ID: "
//				+ certRequest.getCertReqId());

			CertTemplate certTemplate = certRequest.getCertTemplate();

			/* (see RFC 2511) --
			  [ note RFC rfc4211 obsoletes 2511
                https://datatracker.ietf.org/doc/rfc4211/ ]

					  CertTemplate ::= SEQUENCE {
					  version      [0] Version               OPTIONAL,
					  serialNumber [1] INTEGER               OPTIONAL,
					  signingAlg   [2] AlgorithmIdentifier   OPTIONAL,
					  issuer       [3] Name                  OPTIONAL,
					  validity     [4] OptionalValidity      OPTIONAL,
					  subject      [5] Name                  OPTIONAL,
					  publicKey    [6] SubjectPublicKeyInfo  OPTIONAL,
					  issuerUID    [7] UniqueIdentifier      OPTIONAL,
					  subjectUID   [8] UniqueIdentifier      OPTIONAL,
					  extensions   [9] Extensions            OPTIONAL }

			*/

			DERInteger serialNumber = null;
			AlgorithmIdentifier algorithmIdentifier = null;
			X509Name issuerNm = null;
			OptionalValidity optionalValidity = null;
			X509Name subject = null;
			SubjectPublicKeyInfo subjectPublicKeyInfo = null;
			X509Extensions extensions = null;

			ASN1Sequence certTemplateSeq = (ASN1Sequence) certTemplate
				.getDERObject();
			System.out.println("CertTemplate sequence: " + certTemplateSeq);

			@SuppressWarnings("unchecked")
			Enumeration<DEREncodable> certTemplateSeqEnum = certTemplateSeq
				.getObjects();
			while (certTemplateSeqEnum.hasMoreElements()) {
				DEREncodable seqItem = certTemplateSeqEnum.nextElement();
				DERTaggedObject taggedObj = (DERTaggedObject) seqItem;
				DEREncodable value = taggedObj.getObjectParser(
					taggedObj.getTagNo(), true);
				if (taggedObj.getTagNo() == 1) {
					serialNumber = (DERInteger) value;
				} else if (taggedObj.getTagNo() == 2) {
					algorithmIdentifier = AlgorithmIdentifier.getInstance(
						taggedObj, false);
				} else if (taggedObj.getTagNo() == 3) {
					issuerNm = X509Name.getInstance(taggedObj, true);
				} else if (taggedObj.getTagNo() == 4) {
					optionalValidity = OptionalValidity.getInstance(value
						.getDERObject());
				} else if (taggedObj.getTagNo() == 5) {
					subject = X509Name.getInstance(taggedObj, true);
				} else if (taggedObj.getTagNo() == 6) {
					subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(
						taggedObj, false);
				} //else if (taggedObj.getTagNo() == 9) {
				  //extensions = X509Extensions.getInstance(taggedObj, false);
				  // }
			}
			log.info("Serial number: " + serialNumber
			+" Algorithm identifier: " + algorithmIdentifier
			+" Issuer: " + issuerNm
			+" Optional validity: " + optionalValidity
			+" Subject: " + subject);

			AsymmetricKeyParameter keyParam = PublicKeyFactory.createKey(subjectPublicKeyInfo);
			
			// TODO: handle other types of keys.
			if (keyParam instanceof RSAKeyParameters) {
				RSAKeyParameters rsaKeyParam = (RSAKeyParameters) keyParam;
				DefaultCertificate cert = new DefaultCertificate(this);
				cert.setDefaultSerialisation(new DERSerialisation(cert));
				cert.setSubjectPublicKey(new DefaultRSAPubKey(rsaKeyParam.getExponent(),rsaKeyParam.getModulus()));
				return cert;
			} else {
                log.warn("KeyParam is not an RSA Key but of type"+keyParam.getClass()+" need to implement this.");
            }
//          A lot of potentially useful code developed by Bruno, that shows one one
//          could use the extra fields in the CRMF structure.
//			But use cases need to be found, and it really is not clear that any of this
//			cannot be better done by passing the values in a form.
//			(So we keep this here for the yet to be discovered use case)
//
//
//			@SuppressWarnings("unchecked")
//			Enumeration<DERObjectIdentifier> extensionsOids = extensions.oids();
//			while (extensionsOids.hasMoreElements()) {
//				DERObjectIdentifier oid = extensionsOids.nextElement();
//				X509Extension extension = extensions.getExtension(oid);
//				System.out.println(String.format("X.509 extension with OID=%s: %s",
//					oid, extension.getValue()));
//			}
//
//			System.out.println("POP type: " + certReqMsg.getPop().getType());
//			System.out.println("POP object: " + certReqMsg.getPop().getObject());
//			POPOSigningKey popoSigningKey = (POPOSigningKey) certReqMsg.getPop()
//				.getObject();
//
//			ASN1Sequence seq = (ASN1Sequence) popoSigningKey.toASN1Object();
//			AlgorithmIdentifier signatureAlgorithmIdentifier = null;
//			DERBitString signature = null;
//			@SuppressWarnings("unchecked")
//			Enumeration<DEREncodable> seqEnum = seq.getObjects();
//			while (seqEnum.hasMoreElements()) {
//				DEREncodable seqItem = seqEnum.nextElement();
//				if (signatureAlgorithmIdentifier == null) {
//					if (seqItem instanceof AlgorithmIdentifier) {
//						signatureAlgorithmIdentifier = (AlgorithmIdentifier) seqItem;
//					}
//				} else {
//					signature = (DERBitString) seqItem;
//				}
//			}
//
//			System.out.println("signatureAlgorithmIdentifier: "
//				+ signatureAlgorithmIdentifier.getObjectId());
//
//			// TODO: check that the signatureAlgorithmIdentifier is indeed SHA-1 and RSA key
//			// 1.2.840.113549.1.1.5 (otherwise, use other appropriate algorithm).
//			SHA1Digest digest = new SHA1Digest();
//			RSADigestSigner signer = new RSADigestSigner(digest);
//			signer.init(false, keyParam);
//			signer.update(certRequest.getDEREncoded(), 0, certRequest
//				.getDEREncoded().length);
//			System.out.println("Signature verified? "
//				+ signer.verifySignature(signature.getBytes()));
//
//			for (AttributeTypeAndValue attrTypeAndValue : certReqMsg.getCertReq()
//				.getControls().toAttributeTypeAndValueArray()) {
//				System.out.println("attrTypeAndValue: "
//					+ attrTypeAndValue.getType());
//			}
		} catch (Exception e) {
			log.warn("caught exception in CRMF code",e);
		}
		return null;
	}

}
