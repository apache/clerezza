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

import org.apache.clerezza.ssl.keygen.CertSerialisation;
import org.apache.clerezza.ssl.keygen.Certificate;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Implementation of CertSerialisation
 *
 * @author Henry Story
 */
public abstract class DefaultCertSerialisation implements CertSerialisation {
	Certificate cer;

	DefaultCertSerialisation(Certificate cer) {
		this.cer = cer;
	}

	@Override
	public int getLength() {
		return getContent().length;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(getContent());
	}

	/**
	 * Should not be used, only for testing!
	 *
	 * @return a string representation of the output
	 */
	@Override
	public String toString() {
		return "DO NOT USE FOR OUTPUT! use write(ServletResponse res) instead!\r\n" +
			new String(getContent(), Charset.forName("UTF-8"));

	}
}
