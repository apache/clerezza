/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.clerezza.triaxrs.headerDelegate;

import java.util.Locale;

import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;
/**
 * Converts a RFC 1766 language tag into java.util.Locale Object and vice versa.
 * 
 * @author mir
 *
 */
public class LocaleProvider implements HeaderDelegate<Locale> {

	@Override
	public Locale fromString(String header) throws IllegalArgumentException {
		if (header == null) {
			throw new IllegalArgumentException();
		}

		String[] parts = header.split(";");
		parts = parts[0].split("-");

		String language = "";
		String country = "";
		String variant = "";

		
		/* (RFC 1766) 
		   In the primary language tag:

		    -    All 2-letter tags are interpreted according to ISO standard
		         639, "Code for the representation of names of languages" [ISO
		         639].
		
		    -    The value "i" is reserved for IANA-defined registrations
		
		    -    The value "x" is reserved for private use. Subtags of "x"
		         will not be registered by the IANA.
		
		    -    Other values cannot be assigned except by updating this
		         standard.

		 */
		if (parts.length > 0) {
			if (parts[0].length() == 2) {
				language = parts[0];
			} else {
				throw new IllegalArgumentException();
			}
		}
		
		/*  (RFC 1766) 
			In the first subtag:

		    -    All 2-letter codes are interpreted as ISO 3166 alpha-2
		         country codes denoting the area in which the language is
		         used.

		    -    Codes of 3 to 8 letters may be registered with the IANA by
		         anyone who feels a need for it, according to the rules in
		         -    Country identification, such as en-US (this usage is
		         described in ISO 639)
		
		    -    Dialect or variant information, such as no-nynorsk or en-
		         cockney
		
		    -    Languages not listed in ISO 639 that are not variants of
		         any listed language, which can be registered with the i-
		         prefix, such as i-cherokee
		
		    -    Script variations, such as az-arabic and az-cyrillic

		 */
		if (parts.length > 1) {
			if (parts[1].length() == 2) {
				country = parts[1];
			} else if (parts[1].length() >= 3 && parts[1].length() <= 8){
				variant = parts[1];
			} else {
				throw new IllegalArgumentException();
			}
		}
		
		// (RFC 1766) 
		// In the second and subsequent subtag, any value can be registered.
		if (parts.length > 2) {
			variant = parts[2];
		}

		return new Locale(language, country, variant);
	}

	@Override
	public String toString(Locale locale) {
		String result = "";
		if (!locale.getLanguage().equals("")) {
			result += locale.getLanguage();
		}

		if (!locale.getCountry().equals("")) {
			result += "-" + locale.getCountry();
		}

		if (!locale.getVariant().equals("")) {
			result += "-" + locale.getVariant();
		}

		return result;
	}
}
