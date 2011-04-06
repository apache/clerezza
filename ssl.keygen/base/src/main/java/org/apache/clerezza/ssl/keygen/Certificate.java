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


import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * @author Henry J. Story
 */

public interface Certificate {
	public static final long SECOND = 1000; //milliseconds
	public static final long HOUR = 60 * 60 * SECOND;
	public static final long DAY = 24 * HOUR;
	public static final long YEAR = 365 * DAY;


	/**
	 * @return the X509 Certificate
	 */
	public X509Certificate getCertificate();


	/**
	 * Set the <a href="http://esw.w3.org/topic/webid">WebID</a> for the certificate.
     * The first SAN added will appear in the Distinguished Name
	 *
	 * @param san, a Subject Alternative Name to add. Must be a full URL, usually the WebID
	 */
	public void addSubjectAlternativeName(String san);


	/**
	 * @param name he user should set a himself a name that will be easily recognised in the browser.
	 *             Eg: "Henry-public"
	 */
	public void setSubjectCommonName(String name);


	/**
	 * a short comment for the certificate, such as what purpose it was made for, ...
	 * This does not appear in the certificate, but stays in the wiki.
	 *
	 * @param comment
	 */
	// I was hoping that one could use this object as a java bean too. This will rather have to appear in the wiki DB
	//public void setUserComment(String comment);


	/**
	 * @param startDate Set the start date for the validity of the certificate. If unset it will be now.
	 */
	public void setStartDate(Date startDate);

	/**
	 * @return the start validity date for the certificate
	 */
	public Date getStartDate();

	/**
	 * set the end date of validity of the certificate.
	 * If unset it will be one year from start date, if the duration is not set.
	 * Or the duration in days and hours from the start date.
	 *
	 * @param endDate end date
	 */
	public void setEndDate(Date endDate);


	/**
	 * @return end validity date for the certificate
	 */
	public Date getEndDate();


	/**
	 * set duration of cert in days. Easier to set than the end date.
	 *
	 * @param days the duration of the certificate in days, as a floating point number
	 */
	public void addDurationInDays(String days);


	/**
	 * in order to avoid synchronization problems it is good to set the
	 * start date to be somewhat earlier than the current time.
	 * This time is then added to the length of time the certificate is meant to last.
	 *
	 * @param hours a floating point number
	 */
	public void startEarlier(String hours);


	/**
	 * set the duration of the certificate in hours
	 * Useful for short durations ( for public kiosks for example )
	 * If days is set then this will be added to the days.
	 *
	 * @param hours as floats, encoded as strings (it is easier to get a string from velocity). Partial hours are possible
	 */
	public void addDurationInHours(String hours);


	/**
	 * @return the subject's public key
	 */
	public PubKey getSubjectPublicKey();

	/**
	 * When we create a certificate we already know the serialisation it is going to require.
	 * So this method is here to set it.
	 * <p/>
	 * note: this could perhaps better be done in the constructor. It is not clear that this will be needed
	 * by scripts, so perhaps it should not be here
	 *
	 * @param ser the serialisation implementation
	 */
	void setDefaultSerialisation(CertSerialisation ser);


	/**
	 * get the encoded version of this certificate in a binary form
	 *
	 * @return the serialisation object (which sets headers, mime types, length, etc..)
	 * @throws Exception why? //todo: is this really needed
	 */
	public CertSerialisation getSerialisation() throws Exception;


}
