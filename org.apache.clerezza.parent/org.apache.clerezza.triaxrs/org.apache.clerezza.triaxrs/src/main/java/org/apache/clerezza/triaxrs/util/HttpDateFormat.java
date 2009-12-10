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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Helper class for HTTP specified date formats.
 * 
 *  taken from jersey
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class HttpDateFormat {
    
    private HttpDateFormat() {
    }
    
    /**
     * The date format pattern for RFC 1123.
     */
    private static final String RFC1123_DATE_FORMAT_PATTERN = "EEE, dd MMM yyyy HH:mm:ss zzz";
    
    /**
     * The date format pattern for RFC 1036.
     */
    private static final String RFC1036_DATE_FORMAT_PATTERN = "EEEE, dd-MMM-yy HH:mm:ss zzz";
    
    /**
     * The date format pattern for ANSI C asctime().
     */
    private static final String ANSI_C_ASCTIME_DATE_FORMAT_PATTERN = "EEE MMM d HH:mm:ss yyyy";
    
    private static ThreadLocal<List<SimpleDateFormat>> dateFormats = new ThreadLocal<List<SimpleDateFormat>>() {
        @Override
        protected synchronized List<SimpleDateFormat> initialValue() {
            return createDateFormats();
        }
    };
    
    private static List<SimpleDateFormat> createDateFormats() {
        SimpleDateFormat[] dateFormats = new SimpleDateFormat[] {
            new SimpleDateFormat(RFC1123_DATE_FORMAT_PATTERN, Locale.US),
            new SimpleDateFormat(RFC1036_DATE_FORMAT_PATTERN, Locale.US),
            new SimpleDateFormat(ANSI_C_ASCTIME_DATE_FORMAT_PATTERN, Locale.US)
        };
        
        TimeZone tz = TimeZone.getTimeZone("GMT");
        dateFormats[0].setTimeZone(tz);
        dateFormats[1].setTimeZone(tz);
        dateFormats[2].setTimeZone(tz);
        
        return Collections.unmodifiableList(Arrays.asList(dateFormats));
    }
    
    /**
     * Return an unmodifiable list of HTTP specified date formats to use for 
     * parsing or formating {@link Date}.
     * <p>
     * The list of date formats are scoped to the current thread and may be
     * used without requiring to synchronize access to the instances when 
     * parsing or formatting.
     *
     * @return the list of data formats.
     */
    public static List<SimpleDateFormat> getDateFormats() {
        return dateFormats.get();
    }

    /**
     * Get the preferred HTTP specified date format (RFC 1123).
     * <p>
     * The date format is scoped to the current thread and may be
     * used without requiring to synchronize access to the instance when 
     * parsing or formatting.
     *
     * @return the preferred of data format.
     */
    public static SimpleDateFormat getPreferedDateFormat() {
        return dateFormats.get().get(0);
    }
}
