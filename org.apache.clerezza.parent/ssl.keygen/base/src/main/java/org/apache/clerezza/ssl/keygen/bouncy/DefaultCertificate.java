/*
 Copyright (c) 2009, The University of Manchester, United Kingdom.
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

  Author: Bruno Harbulot
  Author: Henry Story
 */

package org.apache.clerezza.ssl.keygen.bouncy;

import org.apache.clerezza.ssl.keygen.CertSerialisation;
import org.apache.clerezza.ssl.keygen.Certificate;
import org.apache.clerezza.ssl.keygen.PubKey;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.misc.NetscapeCertType;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation of Certificate
 *
 * @author Henry Story
 */

public class DefaultCertificate implements Certificate {
	static final Logger log = Logger.getLogger(DefaultCertificate.class.getName());

	String webId;
	String CN;
	Date startDate;
	Date endDate;
	double numDays = 0;
	double numHours = 0;
	double earlier = 0;
	PubKey subjectPubKey;
	private BouncyKeygenService service;
	X509Certificate cert = null;
	CertSerialisation serialization;

	public DefaultCertificate(BouncyKeygenService service) {
		this.service = service;
	}

	@Override
	public X509Certificate getCertificate() {
		return cert;
	}


	@Override
	public void setSubjectWebID(String urlStr) {
		URL url = null;
		try {
			url = new URL(urlStr);
			String protocol = url.getProtocol();
			if (protocol.equals("http") || protocol.equals("https") || protocol.equals("ftp") || protocol.equals("ftps")) {
				//everything probably ok, though really https should be the default
			} else {
				//could very well be a mistake
				log.log(Level.WARNING, "using WebId with protocol " + protocol + ". Could be a mistake. WebId=" + url);
			}

		} catch (MalformedURLException e) {
			log.log(Level.WARNING, "Malformed URL " + url, e);
		}
		this.webId = urlStr;
	}

	@Override
	public void setSubjectCommonName(String name) {
		CN = name;
	}

	@Override
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Override
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}


	@Override
	public void addDurationInDays(String days) {
		if (null == days || "".equals(days)) return;
		try {
			this.numDays += Double.valueOf(days);
		} catch (NumberFormatException e) {
			log.log(Level.WARNING, "unable to interpret the number of days passed as a float " + days);
		}
		//this.numDays = days;
	}

	@Override
	public void startEarlier(String hours) {
		if (null == hours  || "".equals(hours)) return;
		try {
			this.earlier += Double.valueOf(hours);
		} catch (NumberFormatException e) {
			log.log(Level.WARNING, "unable to interpret the number of days passed as a float " + hours);
		}
	}

	@Override
	public void addDurationInHours(String hours) {
		if (null ==hours || "".equals(hours)) return;
		try {
			this.numHours += Double.valueOf(hours);
		} catch (NumberFormatException e) {
			log.log(Level.WARNING, "unable to interpret the number of hours passed as a float" + hours);
		}
	}

	@Override
	public PubKey getSubjectPublicKey() {
		return subjectPubKey;
	}

	/**
	 * Set the <a href="http://en.wikipedia.org/wiki/Spkac">Spkac</a> data sent by browser
	 * One should set either this or the pemCSR.
	 *
	 * @param pubkey the public key for the subject
	 */
	public void setSubjectPublicKey(PubKey pubkey) {
		this.subjectPubKey = pubkey;
	}


	@Override
	public void setDefaultSerialisation(CertSerialisation ser) {
		serialization = ser;
	}

	@Override
	public CertSerialisation getSerialisation() throws Exception {
		if (cert == null) {
			generate();
		}
		return serialization;
	}

	public void generate() throws Exception {
		X509V3CertificateGenerator certGenerator = new X509V3CertificateGenerator();

		certGenerator.reset();
		/*
					* Sets up the subject distinguished name. Since it's a self-signed
					* certificate, issuer and subject are the same.
					*/
		certGenerator.setIssuerDN(new X509Name(BouncyKeygenService.issuer));


		Vector<DERObjectIdentifier> subjectDnOids = new Vector<DERObjectIdentifier>();
		Vector<String> subjectDnValues = new Vector<String>();

		subjectDnOids.add(X509Name.O);
		subjectDnValues.add("FOAF+SSL");
		subjectDnOids.add(X509Name.OU);
		subjectDnValues.add("The Community Of Self Signers");
		subjectDnOids.add(X509Name.UID);
		subjectDnValues.add(webId);
		subjectDnOids.add(X509Name.CN);
		subjectDnValues.add(CN);

		X509Name DName = new X509Name(subjectDnOids, subjectDnValues);
		certGenerator.setSubjectDN(DName);

		/*
					* Sets up the validity dates.
					*/
		certGenerator.setNotBefore(getStartDate());

		certGenerator.setNotAfter(getEndDate());

		/*
					* The serial-number of this certificate is 1. It makes sense because
					* it's self-signed.
					*/
		certGenerator.setSerialNumber(service.nextRandom());

		/*
					* Sets the public-key to embed in this certificate.
					*/
		certGenerator.setPublicKey(getSubjectPublicKey().getPublicKey());
		/*
					* Sets the signature algorithm.
					*/
//        String pubKeyAlgorithm = service.caPubKey.getAlgorithm();
//        if (pubKeyAlgorithm.equals("DSA")) {
//            certGenerator.setSignatureAlgorithm("SHA1WithDSA");
//        } else if (pubKeyAlgorithm.equals("RSA")) {
		certGenerator.setSignatureAlgorithm("SHA1WithRSAEncryption");
//        } else {
//            RuntimeException re = new RuntimeException(
//                    "Algorithm not recognised: " + pubKeyAlgorithm);
//            LOGGER.error(re.getMessage(), re);
//            throw re;
//        }

		/*
					* Adds the Basic Constraint (CA: false) extension.
					*/
		certGenerator.addExtension(X509Extensions.BasicConstraints, true,
			new BasicConstraints(false));

		/*
					* Adds the Key Usage extension.
					*/
		certGenerator.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(
			KeyUsage.digitalSignature | KeyUsage.nonRepudiation
				| KeyUsage.keyEncipherment | KeyUsage.keyAgreement
				| KeyUsage.keyCertSign));

		/*
					* Adds the Netscape certificate type extension.
					*/
		certGenerator.addExtension(MiscObjectIdentifiers.netscapeCertType,
			false, new NetscapeCertType(NetscapeCertType.sslClient
				| NetscapeCertType.smime));

		/*
					* Adds the authority key identifier extension.
					* Bruno pointed out that this is not needed, as the authority's key is never checked in this setup!
					* so I am commenting it out, to be removed at a later date.
					*

				  AuthorityKeyIdentifierStructure authorityKeyIdentifier;
				  try {
						authorityKeyIdentifier = new AuthorityKeyIdentifierStructure(
								  service.certificate.getPublicKey());
				  } catch (InvalidKeyException e) {
						throw new Exception("failed to parse CA cert. This should never happen", e);
				  }

				  certGenerator.addExtension(X509Extensions.AuthorityKeyIdentifier,
							 false, authorityKeyIdentifier);
				  */

		/*
					* Adds the subject key identifier extension.
					*/
		SubjectKeyIdentifier subjectKeyIdentifier = new SubjectKeyIdentifierStructure(
			getSubjectPublicKey().getPublicKey());
		certGenerator.addExtension(X509Extensions.SubjectKeyIdentifier, false,
			subjectKeyIdentifier);

		/*
					* Adds the subject alternative-name extension (critical).
					*/
		if (webId != null) {
			GeneralNames subjectAltNames = new GeneralNames(new GeneralName(
				GeneralName.uniformResourceIdentifier, webId));
			certGenerator.addExtension(X509Extensions.SubjectAlternativeName,
				true, subjectAltNames);
		} else throw new Exception("WebId not set!");

		/*
					* Creates and sign this certificate with the private key corresponding
					* to the public key of the FOAF+SSL DN
					*/
		cert = certGenerator.generate(service.privateKey);

		/*
					* Checks that this certificate has indeed been correctly signed.
					*/
		cert.verify(service.certificate.getPublicKey());

	}

	@Override
	public Date getEndDate() {
		if (endDate == null) {
			long endtime;
			if (numDays == 0 && numHours == 0) {
				numDays = 365;
			}
			endtime = getStartDate().getTime();
			endtime += (long) (numDays * DAY) + (long) ((numHours + earlier) * HOUR);
			endDate = new Date(endtime);
		}
		return endDate;
	}


	@Override
	public Date getStartDate() {
		if (startDate == null) {
			startDate = new Date(System.currentTimeMillis() - (long) (earlier * HOUR));
		}
		return startDate;
	}

}
