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
 *  - Neither the name of Sun Microsystems, Inc. nor the names of its contributors   *   may be used to endorse or promote products derived from this software
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

import org.apache.clerezza.ssl.keygen.Certificate;

import java.security.cert.CertificateEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A DER serialisation of a certificate
 * This is the one that Safari, Firefox and Opera understand out of the box
 *
 * @author Henry Story
 * @since Mar 12, 2010
 */
public class DERSerialisation extends DefaultCertSerialisation {
	final static transient Logger log = Logger.getLogger(DERSerialisation.class.getName());

	byte[] ser = null;

	DERSerialisation(Certificate cer) {
		super(cer);
	}

	@Override
	public byte[] getContent() {
		if (ser == null) {
			try {
				ser = cer.getCertificate().getEncoded();
			} catch (CertificateEncodingException e) {
				log.log(Level.WARNING, "could not DER encode the give certificate.");
			}
		}
		return ser;
	}

	public String getMimeType() {
		return "application/x-x509-user-cert";
	}
}
