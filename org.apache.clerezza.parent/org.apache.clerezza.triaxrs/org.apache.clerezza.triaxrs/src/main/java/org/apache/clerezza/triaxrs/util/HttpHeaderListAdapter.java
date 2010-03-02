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

/** 
 *  taken from jersey
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class HttpHeaderListAdapter extends HttpHeaderReader {
    private HttpHeaderReader reader;
            
    boolean isTerminated;
        
    public HttpHeaderListAdapter(HttpHeaderReader reader) {
        this.reader = reader;
    }
    
    public void reset() {
        isTerminated = false;
    }

    
    public boolean hasNext() {
        if (isTerminated)
            return false;
        
        if (reader.hasNext()) {
            if (reader.hasNextSeparator(',', true)) {
                isTerminated = true;
                return false;
            } else
                return true;
        }
        
        return false;
    }

    public boolean hasNextSeparator(char separator, boolean skipWhiteSpace) {
        if (isTerminated)
            return false;
        
        if (reader.hasNextSeparator(',', skipWhiteSpace)) {
            isTerminated = true;
            return false;
        } else
            return reader.hasNextSeparator(separator, skipWhiteSpace);
    }
    
    public Event next() throws ParseException {
        return next(true);
    }

    public HttpHeaderReader.Event next(boolean skipWhiteSpace) throws ParseException {
        if (isTerminated)
            throw new ParseException("End of header", getIndex());
        
        if (reader.hasNextSeparator(',', skipWhiteSpace)) {
            isTerminated = true;
            throw new ParseException("End of header", getIndex());
        }
        
        return reader.next(skipWhiteSpace);
    }

    public HttpHeaderReader.Event getEvent() {
        return reader.getEvent();
    }

    public String getEventValue() {
        return reader.getEventValue();
    }

    public String getRemainder() {
        return reader.getRemainder();
    }    

    public int getIndex() {
        return reader.getIndex();
    }
}
