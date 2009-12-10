/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License"). You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above. However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 * 
 * trialox.org (trialox AG, Switzerland) elects to include this software in this
 * distribution under the CDDL license.
 */ 

package org.apache.clerezza.triaxrs.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */

public abstract class HttpHeaderReader {
    
    public enum Event {
        Token, QuotedString, Comment, Separator, Control;
    };
    
    public abstract boolean hasNext();
    
    public abstract boolean hasNextSeparator(char separator, boolean skipWhiteSpace);
    
    public abstract Event next() throws ParseException;

    public abstract Event next(boolean skipWhiteSpace) throws ParseException;
    
    public abstract Event getEvent();
    
    public abstract String getEventValue();
    
    public abstract String getRemainder();
    
    public abstract int getIndex();
    
    public String nextToken() throws ParseException {
        Event e = next(false);
        if (e != Event.Token)
            throw new ParseException("Next event is not a Token", getIndex());
        
        return getEventValue();
    }
    
    public char nextSeparator() throws ParseException {         
        Event e = next(false);
        if (e != Event.Separator)
            throw new ParseException("Next event is not a Separator", getIndex());
        
        return getEventValue().charAt(0);
    }
    
    public void nextSeparator(char c) throws ParseException {         
        Event e = next(false);
        if (e != Event.Separator)
            throw new ParseException("Next event is not a Separator", getIndex());
        
        if (c != getEventValue().charAt(0)) {
            throw new ParseException("Expected separator '" + c + "' instead of '" 
                    + getEventValue().charAt(0) + "'", getIndex());
        }
    }
    
    public String nextQuotedString() throws ParseException { 
        Event e = next(false);
        if (e != Event.QuotedString)
            throw new ParseException("Next event is not a Quoted String", getIndex());
        
        return getEventValue();
    }
        
    public String nextTokenOrQuotedString() throws ParseException {
        Event e = next(false);
        if (e != Event.Token && e != Event.QuotedString)
            throw new ParseException("Next event is not a Token or a Quoted String, " + 
                    getEventValue(), getIndex());
        
        return getEventValue();
    }
    
    
    
    
    public static HttpHeaderReader newHttpHeaderReader(String header) {
        return new HttpHeaderReaderImpl(header);
    }
    
    public static HttpHeaderReader newHttpHeaderReader(String header, boolean processComments) {
        return new HttpHeaderReaderImpl(header, processComments);
    }
        
    public static Date readDate(String date) throws ParseException {
        ParseException pe = null;
        for (SimpleDateFormat f : HttpDateFormat.getDateFormats()) {
            try {
                return f.parse(date);
            } catch (ParseException e) {
                pe = (pe == null) ? e : pe;
            }
        }
        
        throw pe;
    }
    
    public static int readQualityFactor(String q) throws ParseException {
        if (q == null || q.length() == 0)
            throw new ParseException("Quality value cannot be null or an empty String", 0);
        
        int index = 0;
        final int length = q.length();
        if (length > 5) {
            throw new ParseException("Quality value is greater than the maximum length, 5", 0);
        }
        
        // Parse the whole number and decimal point
        final char wholeNumber;
        char c = wholeNumber = q.charAt(index++);
        if (c == '0' || c == '1') {
            if (index == length)
                return (c - '0') * 1000;
            c = q.charAt(index++);
            if (c != '.') {
                throw new ParseException("Error parsing Quality value: a decimal place is expected rather than '" + 
                        c + "'", index);
            }
            if (index == length)
                return (c - '0') * 1000;
        } else if (c == '.') {
            // This is not conformant to the HTTP specification but some implementations
            // do this, for example HttpURLConnection.
            if (index == length)
                throw new ParseException("Error parsing Quality value: a decimal numeral is expected after the decimal point", index);
            
        } else {
            throw new ParseException("Error parsing Quality value: a decimal numeral '0' or '1' is expected rather than '" + 
                    c + "'", index);
        }

        // Parse the fraction
        int value = 0;
        int exponent = 100;
        while (index < length) {
            c = q.charAt(index++);
            if (c >= '0' && c <= '9') {
                value += (c - '0') * exponent;
                exponent /= 10;
            } else {
                throw new ParseException("Error parsing Quality value: a decimal numeral is expected rather than '" + 
                        c + "'", index);
            }
        }
        
        if (wholeNumber == '1' && value > 0) {
            throw new ParseException("The Quality value, " + q + ", is greater than 1", index);
        }
        
        return value;
    }
    
    public static int readQualityFactorParameter(HttpHeaderReader reader) throws ParseException {
        int q = -1;
        while (reader.hasNext()) {
            reader.nextSeparator(';');
            
            // Ignore a ';' with no parameters
            if (!reader.hasNext())
                return QualityFactor.DEFAULT_QUALITY_FACTOR;
            
            // Get the parameter name
            String name = reader.nextToken();
            reader.nextSeparator('=');
            // Get the parameter value
            String value = reader.nextTokenOrQuotedString();
            
            if (q == -1 && name.equalsIgnoreCase(QualityFactor.QUALITY_FACTOR)) {
                q = readQualityFactor(value);
            }
        }
        
        return (q == -1) ? QualityFactor.DEFAULT_QUALITY_FACTOR : q;
    }
    
    public static Map<String, String> readParameters(HttpHeaderReader reader) throws ParseException {
        Map<String, String> m = null;
        
        while (reader.hasNext()) {
            reader.nextSeparator(';');
            
            // Ignore a ';' with no parameters
            if (!reader.hasNext())
                break;
            
            // Get the parameter name
            String name = reader.nextToken();
            reader.nextSeparator('=');
            // Get the parameter value
            String value = reader.nextTokenOrQuotedString();
    
            if (m == null)
                m = new LinkedHashMap<String, String>();
            
            // Lower case the parameter name
            m.put(name.toLowerCase(), value);
        }
        
        return m;
    }
 
    private static final Comparator<QualityFactor> QUALITY_COMPARATOR = new Comparator<QualityFactor>() {
        public int compare(QualityFactor o1, QualityFactor o2) {
            return o2.getQuality() - o1.getQuality();
        }
    };

    public static interface ListElementCreator<T> {
        T create(HttpHeaderReader reader)  throws ParseException;
    }
    
    public static <T> List<T> readList(ListElementCreator<T> c, 
            String header) throws ParseException {
        List<T> l = new ArrayList<T>();
        HttpHeaderReader reader = new HttpHeaderReaderImpl(header);
        HttpHeaderListAdapter adapter = new HttpHeaderListAdapter(reader);
        while(reader.hasNext()) {
            l.add(c.create(adapter));
            adapter.reset();
            if (reader.hasNext())
                reader.next();
        }
        
        return l;
    }   
    
    public static <T extends QualityFactor> List<T> readAcceptableList(
            ListElementCreator<T> c, 
            String header) throws ParseException {
        List<T> l = readList(c, header);
        Collections.sort(l, QUALITY_COMPARATOR);
        return l;
    }
}
