/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 *
 * trialox.org (trialox AG, Switzerland) elects to include this software in this
 * distribution under the CDDL license.
 */

package org.apache.clerezza.triaxrs.providers.provided;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.clerezza.triaxrs.util.MultivaluedMapImpl;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@Produces("application/x-www-form-urlencoded")
@Consumes("application/x-www-form-urlencoded")
public final class FormMultivaluedMapProvider extends 
        AbstractMessageReaderWriterProvider<MultivaluedMap<String, String>> {
    
    private final Type mapType;
    
    @Override
    public int compareTo(Object o) {

        if (o instanceof FormMultivaluedMapProvider) {
            return 0;
        }

        return -1;
    }
     

    public FormMultivaluedMapProvider() {
        ParameterizedType iface = (ParameterizedType)this.getClass().getGenericSuperclass();
        mapType = iface.getActualTypeArguments()[0];
    }
    
	@Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        // Only allow types MultivaluedMap<String, String> and MultivaluedMap.
        return type == MultivaluedMap.class && 
                (type == genericType || mapType.equals(genericType));
    }
    
	@Override
    public MultivaluedMap<String, String> readFrom(
            Class<MultivaluedMap<String, String>> type, 
            Type genericType, 
            Annotation annotations[],
            MediaType mediaType, 
            MultivaluedMap<String, String> httpHeaders, 
            InputStream entityStream) throws IOException {
        String encoded = readFromAsString(entityStream, mediaType);
    
        MultivaluedMap<String, String> map = new MultivaluedMapImpl();
        StringTokenizer tokenizer = new StringTokenizer(encoded, "&");
        String token;
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            int idx = token.indexOf('=');
            if (idx < 0) {
                map.add(URLDecoder.decode(token, "UTF-8"), null);
            } else if (idx > 0) {
                map.add(URLDecoder.decode(token.substring(0, idx), "UTF-8"), 
                        URLDecoder.decode(token.substring(idx+1), "UTF-8"));
            }
        }
        return map;
    }

	@Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return MultivaluedMap.class.isAssignableFrom(type);
    }
    
	@Override
    public void writeTo(
            MultivaluedMap<String, String> t, 
            Class<?> type, 
            Type genericType, 
            Annotation annotations[], 
            MediaType mediaType, 
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> e : t.entrySet()) {
            for (String value : e.getValue()) {
                if (sb.length() > 0)
                    sb.append('&');
                sb.append(URLEncoder.encode(e.getKey(), "UTF-8"));
                if (value != null) {
                    sb.append('=');
                    sb.append(URLEncoder.encode(value, "UTF-8"));
                }
            }
        }
                
        writeToAsString(sb.toString(), entityStream, mediaType);
    }    
}
