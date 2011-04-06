/*
 * New BSD license: http://opensource.org/licenses/bsd-license.php
 *
 *  Copyright (c) 2010.
 * Henry Story
 * http://bblfish.net/
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *  - Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */

package org.apache.clerezza.ssl.keygen.bouncy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.apache.clerezza.ssl.keygen.Certificate.*;

import junit.framework.TestCase;
import org.apache.clerezza.ssl.keygen.*;
import org.apache.clerezza.ssl.keygen.KeygenService;
import org.bouncycastle.asn1.x509.X509Name;


/**
 * Tests for the {@link org.jsslutils.keygen.KeygenService} component.
 *
 * @version $Id: $
 */
public class CertificateServiceTest extends TestCase {
	public static final String WEBID = "http://test.com/#me";
	static String spkac = "MIIBRzCBsTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAwWxHp09gHwgec98X\n" +
		"2hxynxlAlN9IeiSu7T1CSry4uMPCkujkcpTg0n7ofhHvke/kwlv9QpK/Ko4gcQTI\n" +
		"nWu3Sl5hcRdP1KvRTq+VdyPp0QUTStlri3uYMZcOC5yXFqAFVywRWvQDtBYMYtqp\n" +
		"KcyvaRpKKRC+lpWTIjbvOSgfy4UCAwEAARYNVGhlQ2hhbGxlbmdlMTANBgkqhkiG\n" +
		"9w0BAQQFAAOBgQClhG6itMJneOfwSt5gaCzg/HRt94WKtJivbLvlYwNi2NkZu014\n" +
		"308EhhG0onhBIy5hXopa7pvYzqMv2gbipj89ucqoUYybqaoP+qJ0eDbSlJOaISlB\n" +
		"2b6nVDYhlj/ihT40qv6+3WNdiUgayB+INLQW1hPvqPirjHfMJOfpfQcwIw==";

	/**
	 * test the creation of an spkac certificate
	 *
	 * @throws Exception
	 */
	public void testSpkac() throws Exception {
		BouncyKeygenService srvc = new BouncyKeygenService();
		srvc.initialize();
		Certificate cert = srvc.createFromSpkac(spkac);
		PubKey spk = cert.getSubjectPublicKey();
		assertNotNull(spk);
		assertTrue(spk instanceof RSAPubKey);
		assertEquals("the expected and real values don't match",
			"c16c47a74f601f081e73df17da1c729f194094df487a24aeed3d424abcb8\r\n" +
				"b8c3c292e8e47294e0d27ee87e11ef91efe4c25bfd4292bf2a8e207104c8\r\n" +
				"9d6bb74a5e6171174fd4abd14eaf957723e9d105134ad96b8b7b9831970e\r\n" +
				"0b9c9716a005572c115af403b4160c62daa929ccaf691a4a2910be969593\r\n" +
				"2236ef39281fcb85\r\n", ((RSAPubKey) spk).getHexModulus());
		assertEquals("int exponent is not correct", "65537", ((RSAPubKey) spk).getIntExponent());
		Date now = new Date();
		cert.addDurationInDays("3");
		cert.setSubjectCommonName("Test");
		cert.addSubjectAlternativeName(WEBID);
		CertSerialisation certByte = cert.getSerialisation();

		//test that the returned certificate contains the correct values...
		Date endDate = cert.getEndDate();
		assertTrue("end date is too early (we added 10 seconds)",
			endDate.getTime() < (now.getTime() + (3 * 24 * 60 * 60 * SECOND) + (10 * SECOND)));
		assertTrue("end date is too late (we removed 10 seconds)",
			endDate.getTime() > (now.getTime() + (3 * 24 * 60 * 60 * SECOND) - (10 * SECOND)));

		ByteArrayOutputStream bout = new ByteArrayOutputStream(certByte.getLength());
		certByte.writeTo(bout);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate x509 = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(bout.toByteArray()));

		Collection<List<?>> sanlst = x509.getSubjectAlternativeNames();
		assertNotNull(sanlst);

		assertEquals("only one SAN", 1, sanlst.size());
		List<?> next = sanlst.iterator().next();
		assertEquals("Uniform Resource identifiers is nbr 6", next.get(0), 6);
		assertEquals("testing WebId", next.get(1), WEBID);

		Date notAfter = x509.getNotAfter();
		assertTrue("end date is too early (we added 10 seconds)",
			notAfter.getTime() < (now.getTime() + (3 * 24 * HOUR) + (10 * SECOND)));
		assertTrue("end date is too late (we removed 10 seconds)",
			notAfter.getTime() > (now.getTime() + (3 * 24 * HOUR) - (10 * SECOND)));
		System.out.println("not after=" + notAfter);

		Date notbefore = x509.getNotBefore();
		assertTrue("start date is too early (we added 10 seconds)",
			notbefore.getTime() < (now.getTime() + (10 * SECOND)));
		assertTrue("start date is too late (we removed 10 seconds)",
			notbefore.getTime() > (now.getTime() - (10 * SECOND)));
		System.out.println("not before=" + notbefore);
	}


	/**
	 * test the creation of an spkac certificate
	 *
	 * @throws Exception
	 */
	public void testSpkacOneYear() throws Exception {
		BouncyKeygenService srvc = new BouncyKeygenService();
		srvc.initialize();
		Certificate cert = srvc.createFromSpkac(spkac);
		PubKey spk = cert.getSubjectPublicKey();
		assertNotNull(spk);
		assertTrue(spk instanceof RSAPubKey);
		assertEquals("the expected and real values don't match",
			"c16c47a74f601f081e73df17da1c729f194094df487a24aeed3d424abcb8\r\n" +
				"b8c3c292e8e47294e0d27ee87e11ef91efe4c25bfd4292bf2a8e207104c8\r\n" +
				"9d6bb74a5e6171174fd4abd14eaf957723e9d105134ad96b8b7b9831970e\r\n" +
				"0b9c9716a005572c115af403b4160c62daa929ccaf691a4a2910be969593\r\n" +
				"2236ef39281fcb85\r\n", ((RSAPubKey) spk).getHexModulus());
		assertEquals("int exponent is not correct", "65537", ((RSAPubKey) spk).getIntExponent());
		Date now = new Date();

		cert.setSubjectCommonName("Test");
		cert.addSubjectAlternativeName(WEBID);
		cert.startEarlier("2");
		CertSerialisation certByte = cert.getSerialisation();


		//test that the returned certificate contains the correct values...
		Date endDate = cert.getEndDate();
		long end10 = now.getTime() + YEAR + (10 * SECOND);
		assertTrue("end date (" + endDate + ") is too late . It should be before " + new Date(end10) + " - we added 10 seconds .",
			endDate.getTime() < end10);
		end10 = now.getTime() + YEAR - (10 * SECOND);
		assertTrue("end date (" + endDate + ") is too early. It should be after " + new Date(end10) + " - we removed 10 seconds .",
			endDate.getTime() > end10);

		Date startDate = cert.getStartDate();
		long start10 = now.getTime() - (2 * HOUR) - (10 * SECOND);
		assertTrue("start date (" + startDate + ") is too early. It should be after " + new Date(start10) + "- we removed 2 hours and 10 seconds.",
			startDate.getTime() > start10);
		assertTrue("start date (" + startDate + ") is too late It should be after " + new Date(start10) + "- we removed 10 secondes short of 2 hours.",
			startDate.getTime() < (now.getTime() - (2 * HOUR) + (10 * SECOND)));


		ByteArrayOutputStream bout = new ByteArrayOutputStream(certByte.getLength());
		certByte.writeTo(bout);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate x509 = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(bout.toByteArray()));

		Collection<List<?>> sanlst = x509.getSubjectAlternativeNames();
		assertNotNull(sanlst);

		assertEquals("only one SAN", 1, sanlst.size());
		List<?> next = sanlst.iterator().next();
		assertEquals("Uniform Resource identifiers is nbr 6", next.get(0), 6);
		assertEquals("testing WebId", next.get(1), WEBID);

		Date notAfter = x509.getNotAfter();
		assertTrue("end date is too early (we added 10 seconds)",
			notAfter.getTime() < (now.getTime() + YEAR + (10 * SECOND)));
		assertTrue("end date is too late (we removed 10 seconds)",
			notAfter.getTime() > (now.getTime() + YEAR - (10 * SECOND)));
		System.out.println("not after=" + notAfter);

		Date notbefore = x509.getNotBefore();
		end10 = now.getTime() - (2 * HOUR) - (10 * SECOND);
		assertTrue("NotBefore date of cert (" + notbefore + ") should be after " + new Date(end10) + "( ie, now less 2 hours and 10 sec )",
			notbefore.getTime() > end10);
		end10 = (now.getTime() - (2 * HOUR) + (10 * SECOND));
		assertTrue("NotBefore date of cert (" + notbefore + ") should be before " + new Date(end10) + "( ie, now less 2 hours less 10 sec )",
			notbefore.getTime() < end10);
		System.out.println("not before=" + notbefore);
	}


	public void testDN() throws Exception {
		X509Name x509Name = new X509Name(KeygenService.issuer);
		//todo some testing on this way of doing things.
	}

	public void testInit() throws Exception {
		BouncyKeygenService srvc = new BouncyKeygenService();
		srvc.initialize();
	}

}
