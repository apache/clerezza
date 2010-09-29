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

import org.apache.clerezza.ssl.keygen.PubKey;
import org.apache.clerezza.ssl.keygen.RSAPubKey;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import static java.security.KeyFactory.getInstance;

/**
 * Implementation of PubKey
 *
 * @author Henry J. Story
 */
public abstract class DefaultPubKey implements PubKey {

	static public PubKey create(PublicKey p) {
		if (p instanceof RSAPublicKey) {
			return new DefaultRSAPubKey((RSAPublicKey) p);
		} else return null; //we don't deal with other keys yet
	}


}

class DefaultRSAPubKey implements RSAPubKey {
	RSAPublicKey rpk;
	BigInteger mod, exp;

	DefaultRSAPubKey(RSAPublicKey pk) {
		rpk = pk;
		exp = pk.getPublicExponent();
		mod = pk.getModulus();
	}

	DefaultRSAPubKey(BigInteger exponent, BigInteger modulus) throws NoSuchAlgorithmException, InvalidKeySpecException {
		this.exp = exponent;
		this.mod = modulus;

		rpk =  (RSAPublicKey)getInstance("RSA").
				generatePublic( new RSAPublicKeySpec(mod,exp));
	}

	/**
	 * A very simple beautify script to cut a large hex string into 60 character lengths
	 * (this should not really be in this class but somewhere else)
	 *
	 * @param s a string to beautify
	 * @return a beautified string
	 */
	static String beautify(String s) {
		StringBuilder answer = new StringBuilder();
		int start = 0;
		while (start < s.length()) {
			int end = start + 60;
			if (end > s.length()) end = s.length();

			String line = s.substring(start, end);
			answer.append(line);
			answer.append("\r\n");
			start = end;
		}
		return answer.toString();
	}

	@Override
	public String getHexModulus() {
		return beautify(mod.toString(16));  
	}

	@Override
	public String getIntExponent() {
		return exp.toString();
	}

	@Override
	public PublicKey getPublicKey() {
		return rpk;
	}
}