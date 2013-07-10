/*
 * Copyright  2002-2004 WYMIWYG (www.wymiwyg.org)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.clerezza.jaxrs.utils.form;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author reto
 *
 */
public class DelimiterNotFoundException extends IOException {

    /**
     * 
     */
    private static final long serialVersionUID = -2002878162243872583L;
    byte[] remainingBytes;
    /**
     * @param remainingBytes
     */
    public DelimiterNotFoundException(byte[] remainingBytes) {
        super("Delimiter not found");
        this.remainingBytes = remainingBytes;
    }

    /**
     * @return Returns the remainingBytes.
     */
    public byte[] getRemainingBytes() {
        return remainingBytes;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        try {
            return  "The following "+remainingBytes.length+" bytes remain: "+new String(remainingBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
