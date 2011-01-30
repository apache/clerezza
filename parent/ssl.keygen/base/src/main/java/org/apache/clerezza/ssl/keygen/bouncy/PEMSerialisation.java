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
import org.bouncycastle.openssl.PEMWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PEM serialisation of a certificate
 * <p/>
 * This is a base64 encoding of a DER encoding of a certificate. It is delimited like this:
 * <p/>
 * -----BEGIN CERTIFICATE-----
 * base64 of DER
 * ----END ... -----
 *
 * @author Henry Story
 * @since Mar 12, 2010
 */
public class PEMSerialisation extends DefaultCertSerialisation {
	byte[] ser = null;
	final transient Logger log = Logger.getLogger(DERSerialisation.class.getName());

	PEMSerialisation(Certificate cer) {
		super(cer);
	}

	@Override
	public byte[] getContent() {
		if (ser == null) {
			try {
				StringWriter sw = new StringWriter();
				PEMWriter pemWriter = new PEMWriter(sw);
				pemWriter.writeObject(cer.getCertificate());
				pemWriter.close();
				ser = sw.toString().getBytes("UTF-8");
			} catch (IOException e) {
				log.log(Level.SEVERE, "could not write PEM Serialisation");
			}
		}
		return ser;
	}


	@Override
	public String getMimeType() {
		return "application/x-pem-file";
	}

}
