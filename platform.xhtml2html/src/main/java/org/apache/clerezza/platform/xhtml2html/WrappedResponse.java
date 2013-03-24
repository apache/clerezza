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
import java.io.PrintWriter;
import java.io.Writer;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rbn
 */
class WrappedResponse extends HttpServletResponseWrapper implements ResponseStatusInfo {

    private String XHTML_TYPE = "application/xhtml+xml";
    private String HTML_TYPE = "text/html";
    private boolean convertXhtml2Html = false;
    private String contentLength = null;
    private final Logger log = LoggerFactory.getLogger(WrappedResponse.class);

    public WrappedResponse(HttpServletResponse response) {
        super(response);
    }

    //stupid servlet redundanny
    @Override
    public void setContentLength(int value) {
        if (convertXhtml2Html) {
                // do nothing
        } else {
            contentLength = Integer.toString(value);
        }
    }

    @Override
    public void setContentType(String type) {
        if (type.startsWith(XHTML_TYPE)) {
            super.setHeader("Content-Type", HTML_TYPE + type.substring(XHTML_TYPE.length()));
            convertXhtml2Html = true;
        } else {
            log.info("The original media type is "+type+" and is not being changed");
            super.setContentType(type);
        }
        
    }

    @Override
    public void setHeader(String headerName, String value) {
        if ("Content-Length".equalsIgnoreCase(headerName)) {
            if (convertXhtml2Html) {
                // do nothing
            } else {
                contentLength = value;
            }
            return;
        }

        if ("Content-Type".equalsIgnoreCase(headerName)) {
            setContentType(value);
            return;
        }
        super.setHeader(headerName, value);
    }

    @Override
    public void addHeader(String headerName, String value) {
        if ("Content-Length".equalsIgnoreCase(headerName)) {
            if (convertXhtml2Html) {
                // do nothing
            } else {
                contentLength = value;
            }
            return;
        }
        if ("Content-Type".equalsIgnoreCase(headerName) && value.startsWith(XHTML_TYPE)) {
            //more than one conten-type hearders make no sense
            setContentType(value);
        } else {
            super.addHeader(headerName, value);
        }
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        //return new ContentLengthSettingByteChannel(super.getOutputStream(), this);
        return new ContentLengthSettingOutputStream(new DocTypeFilteringOutputStream(
                new SelfClosing2ClosingTagsOutputStream(
                super.getOutputStream(), this), this), this);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        //this doesn't work, data is truncated
        //return new PrintWriter(new OutputStreamWriter(getOutputStream(), "utf-8"), true);
        final OutputStream stream = getOutputStream();
        return new PrintWriter(new Writer() {

            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                
                for (int i = 0; i < len; i++) {
                    char ch = cbuf[off+i];
                    if (ch > 127) {
                        byte[] bytes = (""+ch).getBytes("utf-8");
                        for (byte b : bytes) {
                            stream.write(b);
                        }
                    } else {
                        stream.write(ch);
                    }
                }                
            }

            @Override
            public void flush() throws IOException {
                stream.flush();
            }

            @Override
            public void close() throws IOException {
                stream.close();;
            }
        }, true);
        
    }


    @Override
    public boolean convertXhtml2Html() {
        return convertXhtml2Html;
    }

    void setContentLengthIfNoConversion() {
        if ((contentLength != null)  && !convertXhtml2Html) {
            super.setHeader("Content-Length", contentLength);
        }
    }
}
