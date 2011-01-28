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
import org.apache.clerezza.ssl.keygen.KeygenService;

import java.security.InvalidParameterException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A static class to deal with keygen requests
 * <p/>
 * The disadvantage of using it is that if it fails, it will probably need restarting
 * the whole container. The advantage is that one can get this going on non osgi frameworks
 * easily.
 * <p/>
 * If this class is never called it will never get loaded.
 *
 * @author Henry Story
 */
public class StaticKeygenService implements KeygenService {
	static transient final Logger log = Logger.getLogger(StaticKeygenService.class.getName());
	static BouncyKeygenService keygenService;

	static {
		keygenService = new BouncyKeygenService();
		try {
			keygenService.initialize();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Could not start static keygen service ", e);
		}
	}

	/**
	 * Create certificates from PEM requests, coming from Internet Explorer usually
	 *
	 * @param pemCsr
	 * @return A yet incomplete certificate
	 */
	@Override
	public  Certificate createFromPEM(String pemCsr) {
		return keygenService.createFromPEM(pemCsr);
	}


	/**
	 * Create Certificates from SPKAC requests coming from the other browsers
	 *
	 * @param spkac
	 * @return an as yet incomplete Certificate
	 * @throws InvalidParameterException
	 */
	@Override
	public  Certificate createFromSpkac(String spkac)  {
		return keygenService.createFromSpkac(spkac);
	}

	@Override
    public  Certificate createFromCRMF(String crmfReq) {
        return keygenService.createFromCRMF(crmfReq);
    }

}
