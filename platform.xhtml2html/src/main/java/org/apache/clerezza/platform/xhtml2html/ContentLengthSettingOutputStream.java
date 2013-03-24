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
package org.apache.clerezza.platform.xhtml2html;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Triggers the CONTENT-LENGTH header to be set in the wrapped response when the
 * first bytes are written to the
 * <code>WritableByteChannel</code> and the content (to be written to the
 * channel) will not to be converted from xhtml to html.
 *
 * @author mir
 */
class ContentLengthSettingOutputStream extends ServletOutputStream {

    final static private Logger logger = LoggerFactory.getLogger(ContentLengthSettingOutputStream.class);
    private WrappedResponse wrappedResponse;
    private OutputStream base;
    private boolean contetLengthIsSet = false;
    int charCount = 0;

    ContentLengthSettingOutputStream(OutputStream base,
            WrappedResponse wrappedResponse) {
        this.base = base;
        this.wrappedResponse = wrappedResponse;
    }

    @Override
    public void write(int b) throws IOException {
        if (!contetLengthIsSet) {
            wrappedResponse.setContentLengthIfNoConversion();
            contetLengthIsSet = true;
        }
        base.write(b);
    }
    /**
     * This does was the abstract superclass would be expected to do
     */
    @Override
    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * This does was the abstract superclass would be expected to do
     */
    @Override
    public void write(byte b[], int off, int len) throws IOException {
        for (int i = off; i < (off + len); i++) {
            write(b[i]);
        }
    }

    @Override
    public void flush() throws IOException {
        super.flush();
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
    
    
}
