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

package org.apache.clerezza.triaxrs.headerDelegate;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class CacheControlProvider implements HeaderDelegate<CacheControl> {
    private static Pattern WHITESPACE = Pattern.compile("\\s");
    
    public boolean supports(Class<?> type) {
        return type == CacheControl.class;
    }

    @Override
    public String toString(CacheControl header) {
        StringBuffer b = new StringBuffer();
        if (!header.isPrivate())
            appendWithSeparator(b, "public");
        if (header.isPrivate())
            appendQuotedWithSeparator(b, "private", buildListValue(header.getPrivateFields()));
        if (header.isNoCache())
            appendQuotedWithSeparator(b, "no-cache", buildListValue(header.getNoCacheFields()));
        if (header.isNoStore())
            appendWithSeparator(b, "no-store");
        if (header.isNoTransform())
            appendWithSeparator(b, "no-transform");
        if (header.isMustRevalidate())
            appendWithSeparator(b, "must-revalidate");
        if (header.isProxyRevalidate())
            appendWithSeparator(b, "proxy-revalidate");
        if (header.getMaxAge() != -1)
            appendWithSeparator(b, "max-age", header.getMaxAge());
        if (header.getSMaxAge() != -1)
            appendWithSeparator(b, "s-maxage", header.getSMaxAge());
        
        for (Map.Entry<String, String> e : header.getCacheExtension().entrySet()) {
            appendWithSeparator(b, e.getKey(), quoteIfWhitespace(e.getValue()));
        }
                    
        return b.toString();        
    }

    @Override
    public CacheControl fromString(String header) {
        throw new UnsupportedOperationException();
    }
    
    private void appendWithSeparator(StringBuffer b, String field) {
        if (b.length()>0)
            b.append(", ");
        b.append(field);
    }
    
    private void appendQuotedWithSeparator(StringBuffer b, String field, String value) {
        appendWithSeparator(b, field);
        if (value != null && value.length() > 0) {
            b.append("=\"");
            b.append(value);
            b.append("\"");
        }
    }

    private void appendWithSeparator(StringBuffer b, String field, String value) {
        appendWithSeparator(b, field);
        if (value != null && value.length() > 0) {
        b.append("=");
            b.append(value);
        }
    }

    private void appendWithSeparator(StringBuffer b, String field, int value) {
        appendWithSeparator(b, field);
        b.append("=");
        b.append(value);
    }

    private String buildListValue(List<String> values) {
        StringBuffer b = new StringBuffer();
        for (String value: values)
            appendWithSeparator(b, value);
        return b.toString();
    }
    
    private String quoteIfWhitespace(String value) {
        if (value==null)
            return null;
        Matcher m = WHITESPACE.matcher(value);
        if (m.find()) {
            return "\""+value+"\"";
        }
        return value;
    }    
}
