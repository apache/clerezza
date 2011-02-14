//taken from GVS MillisDateFormat.java, modified to support different precision

/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: W3CDateFormat.java,v 1.6 2007/05/07 18:45:22 rebach Exp $
 */
package org.apache.clerezza.rdf.core.impl.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * @author reto implements http://www.w3.org/TR/NOTE-datetime with the
 *         limitation that it expects exactly a three digits decimal fraction of
 *         seconds. if a time zone designator other than 'Z' is present it must
 *         contain a column
 */
public class W3CDateFormat extends DateFormat {
	/**
	 * An instance of this class
	 */
	public static final W3CDateFormat instance = new W3CDateFormat();

	private static final SimpleDateFormat dateFormatWithMillis = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	private static final SimpleDateFormat dateFormatNoMillis = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");

	private static final long serialVersionUID = 3258407344076372025L;

	private static final TimeZone utcTZ = new SimpleTimeZone(0, "UTC");

	static {
		dateFormatWithMillis.setTimeZone(utcTZ);
		dateFormatNoMillis.setTimeZone(utcTZ);
	}

	@Override
	public void setTimeZone(TimeZone zone) {
		super.setTimeZone(zone);
	}


	/**
	 * @see java.text.DateFormat#format(java.util.Date, java.lang.StringBuffer,
	 *      java.text.FieldPosition)
	 */
	@Override
	public StringBuffer format(Date date, StringBuffer toAppendTo,
			FieldPosition fieldPosition) {

		final DateFormat dateFormat = (date.getTime() % 1000) == 0 ?
			dateFormatNoMillis : dateFormatWithMillis;
		String string = dateFormat.format(date);
		if (string.endsWith("0000")) {
			StringBuffer result = new StringBuffer(string.substring(0, string.length()-5));
			result.append('Z');
			return result;
		} else {
			StringBuffer result = new StringBuffer(string);
			result.insert(string.length() - 2, ':');
			return result;
		}
	}

	/**
	 * @see java.text.DateFormat#parse(java.lang.String,
	 *      java.text.ParsePosition)
	 */
	public Date parse(String dateString, ParsePosition parsePos) {

		int position = parsePos.getIndex();

		int y1 = dateString.charAt(position++) - '0';
		int y2 = dateString.charAt(position++) - '0';
		int y3 = dateString.charAt(position++) - '0';
		int y4 = dateString.charAt(position++) - '0';
		int year = 1000 * y1 + 100 * y2 + 10 * y3 + y4;
		position++; // skip '-'
		int m1 = dateString.charAt(position++) - '0';
		int m2 = dateString.charAt(position++) - '0';
		int month = 10 * m1 + m2;
		position++; // skip '-'
		int d1 = dateString.charAt(position++) - '0';
		int d2 = dateString.charAt(position++) - '0';
		int day = 10 * d1 + d2;
		position++; // skip 'T'
		int h1 = dateString.charAt(position++) - '0';
		int h2 = dateString.charAt(position++) - '0';
		int hour = 10 * h1 + h2;
		position++; // skip ':'
		int min1 = dateString.charAt(position++) - '0';
		int min2 = dateString.charAt(position++) - '0';
		int minutes = 10 * min1 + min2;
		position++; // skip ':'
		int s1 = dateString.charAt(position++) - '0';
		int s2 = dateString.charAt(position++) - '0';
		int secs = 10 * s1 + s2;
		Calendar resultCalendar = new GregorianCalendar(year, month - 1, day,
				hour, minutes, secs);
		resultCalendar.setTimeZone(utcTZ);
		char afterSecChar = dateString.charAt(position++);
		int msecs = 0;
		char tzd1;
		if (afterSecChar == '.') {
			int startPos = position;
			//read decimal part, this is till there is a 'Z', a '+' or a '-'
			char nextChar = dateString.charAt(position++);
			while ((nextChar != 'Z') && (nextChar != '-') && (nextChar != '+')) {
				msecs += (nextChar - '0')*Math.pow(10, 3+startPos-position);
				nextChar = dateString.charAt(position++);
			}
			tzd1 = nextChar;
		} else {
			tzd1 = afterSecChar;
		}
		long timeInMillis = resultCalendar.getTimeInMillis() + msecs;
		if (tzd1 != 'Z') {
			int htz1 = dateString.charAt(position++) - '0';
			int htz2 = dateString.charAt(position++) - '0';
			int hourtz = 10 * htz1 + htz2;
			position++; // skip ':'
			int mintz1 = dateString.charAt(position++) - '0';
			int mintz2 = dateString.charAt(position++) - '0';
			int minutestz = 10 * mintz1 + mintz2;
			int offSetInMillis = (hourtz * 60 + minutestz) * 60000;
			if (tzd1 == '+') {
				timeInMillis -= offSetInMillis;
			} else {
				timeInMillis += offSetInMillis;
			}
		}
		parsePos.setIndex(position);
		return new Date(timeInMillis);

	}
}

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

