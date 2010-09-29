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

package org.apache.clerezza.ssl.keygen;

/**
 * A service to get Certificates from <a href="http://en.wikipedia.org/wiki/Certification_request">Certification Requests</a>
 * These can the be used to get information about the CSR's public key, and generate a certifiate to return to the server.
 *
 * Question: should the methods be throwing exceptions or should they log errors and return null?
 *
 * @author Henry J. Story
 */
public interface KeygenService {
	static final String issuer = "O=FOAF\\+SSL, OU=The Community of Self Signers, CN=Not a Certification Authority"; //the exact name for the FOAF+SSL issuer is still being decided

	/**
	 * Creates a certificate stub from the given <a href="http://en.wikipedia.org/wiki/PEM">PEM</a> <a href="http://en.wikipedia.org/wiki/Certification_request">CSR</a>.
	 * The returned certificate will be filled out with info from the CSR and a number of other defaults.
	 * Other information may then be added programatically before generating a certificate to return.
	 * <p/>
	 * Internet Explorer sends PEM CSRs to the server.
	 *
	 * @param csr a <a href="http://en.wikipedia.org/wiki/PEM">PEM</a> <a href="http://en.wikipedia.org/wiki/Certification_request">Certificate Signing Request</a>
	 * @return a certificate using the CSR, and cert defaults
	 */
	Certificate createFromPEM(String csr);

	/**
	 * Creates a certificate stub from the given <a href="http://en.wikipedia.org/wiki/Spkac">SPKAC</a>
	 * <a href="http://en.wikipedia.org/wiki/Certification_request">CSR</a>.
	 * The returned certificate will be filled out with info from the CSR and a number of other defaults.
	 * Other information may then be added programatically before generating a certificate to return.
	 * <p/>
	 * Safari, Firefox, Opera, return through the <code>&lt;keygen&gt;</code> element an SPKAC request
	 * (see the specification in html5)
	 *
	 * @param spkac a <a href="http://en.wikipedia.org/wiki/Spkac">SPKAC</a> <a href="http://en.wikipedia.org/wiki/Certification_request">Certificate Signing Request</a>
	 * @return a certificate using the CSR, and cert defaults
	 */
	Certificate createFromSpkac(String spkac);


	/**
	 * <p>CRMF requests are produced by the javascript <a href="https://developer.mozilla.org/en/GenerateCRMFRequest">generateCRMFRequest()</a>
	 * method in Netscape and are documented by <a href="http://tools.ietf.org/html/rfc2511">RFC 2511</a>.</p>
	 * <p>Using this method may be needed when the server has to produce XHTML (should be rare!) as the <code>keygen</code>
	 * tag in Netscape browsers is only supported by html. This should be fixed soon, now that html5 supports the <code>keygen</code>
	 * element. For progress on this issue check  <a href="https://bugzilla.mozilla.org/show_bug.cgi?id=101019">bug report 101019</a>.
	 * </p>
	 * <p>
	 * A CRMF request can contain more details about the certificate, but those would better be passed using a form,
	 * as in the keygen examples, the server then setting those fields directly on the returned request. Currently
	 * we extract only the public key to generate the returned Certificate.
	 * </p>
	 *
	 * @param crmfReq the request
	 * @return a certificate that may be filled in with some extra details in the requests such as webid
	 */
	Certificate createFromCRMF(String crmfReq);
}
