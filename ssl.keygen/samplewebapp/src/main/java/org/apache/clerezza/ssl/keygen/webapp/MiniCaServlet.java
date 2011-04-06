/**

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

 */
package org.apache.clerezza.ssl.keygen.webapp;

import org.apache.clerezza.ssl.keygen.Certificate;
import org.apache.clerezza.ssl.keygen.bouncy.BouncyKeygenService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * A small certificate authority service. Creates WebID enabled certificates.
 * 
 * TODO: add XHTML functionality developed in Clerezza
 * 
 * @author Bruno Harbulot
 * @author Henry Story
 *
 */
public class MiniCaServlet extends HttpServlet {
    private static final long serialVersionUID = -1103006284486954147L;
    private final transient BouncyKeygenService keygen = new BouncyKeygenService();

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            keygen.initialize();
        } catch (Exception e) {
            throw new ServletException("could not initialise keygen ", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        try {
            String webId = request.getParameter("webid");
            String spkacData = request.getParameter("spkac");
            String pemCsrData = request.getParameter("csrdata");
            String cn = request.getParameter("cn");
            if (cn == null || cn.length() == 0) {
                // really this should be tested at the UI level, the user should be made to
                // enter a name for his certificate that is easy to understand when he selects
                // it in the browser (so it has to be somewhat different from his others)
                // a webid is not a good idea, but this will at least be something.
                if (webId != null && webId.length() > 0) cn = webId;
                else cn = "default name (please improve keygen UI code)";
            }

            Certificate cert;
            if ((spkacData == null) || spkacData.isEmpty()) {
                cert = keygen.createFromSpkac(spkacData);

            } else {
                cert = keygen.createFromPEM(pemCsrData);
            }
            cert.setSubjectCommonName(cn);
            cert.addSubjectAlternativeName(webId);
            cert.addDurationInDays("365");
            cert.startEarlier("1"); //always start one hour earlier at least, to avoid clock synchronisation issues
            cert.getSerialisation().writeTo(response);
        } catch (Exception e) {
           throw new ServletException("could not create certificate",e);
        }
    }
}
