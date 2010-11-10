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

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.StringReader;

/**
 * Test cases always useful
 * <p/>
 * User: hjs
 * Date: Feb 18, 2010
 * Time: 3:59:02 PM
 */
public class DefaultRSAPubKeyTest extends TestCase {
	public void testGetHexModulus() throws Exception {
	}

	public void testGetIntExponent() throws Exception {
	}

	public void testGetPublicKey() throws Exception {
	}

	public void testBeautify() throws Exception {
		String longStr = "The Velocity User Guide is intended to help page designers and content providers get acquainted with Velocity and the syntax of its simple yet powerful scripting language, the Velocity Template Language (VTL). Many of the examples in this guide deal with using Velocity to embed dynamic content in web sites, but all VTL examples are equally applicable to other pages and templates.";
		String res = DefaultRSAPubKey.beautify(longStr);
		BufferedReader sbuf = new BufferedReader(new StringReader(res));
		String line;
		while ((line = sbuf.readLine()) != null) {
			if (line.length() != 60) {
				assertTrue("only the last line can be less than 60 chars", sbuf.readLine() == null);
			}
		}
	}
}
