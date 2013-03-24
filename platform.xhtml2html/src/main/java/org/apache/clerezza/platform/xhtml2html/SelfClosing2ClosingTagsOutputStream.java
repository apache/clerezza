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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import javax.servlet.ServletOutputStream;

/**
 * Changes self-closing tags to tags with closing tags. Self-closing tags that
 * are allowed in HTML are left unchanged. The allowed self-closing tags are:
 * area, base, basefont, br, hr, input img, link and meta.
 *
 * @author mir
 */
class SelfClosing2ClosingTagsOutputStream extends ServletOutputStream {

    private final static byte SPACE = " ".getBytes()[0];
    private final static byte SLASH = "/".getBytes()[0];
    private final static byte LESS_THAN = "<".getBytes()[0];
    private final static byte GREATER_THAN = ">".getBytes()[0];
    private static byte[][] allowedTagNamesBytes = {
        "area".getBytes(),
        "base".getBytes(),
        "basefont".getBytes(),
        "br".getBytes(),
        "hr".getBytes(),
        "input".getBytes(),
        "img".getBytes(),
        "link".getBytes(),
        "meta".getBytes()
    };
    private OutputStream wrapped;
    private ResponseStatusInfo responseStatusInfo;
    private ByteArrayOutputStream tagNameStream = new ByteArrayOutputStream();
    //private OutputStream bytes = new ByteArrayOutputStream();

    private enum Status {

        SEARCH_TAG, DETERMINE_IF_IS_OPENING_TAG, READ_TAG_NAME,
        SEARCH_SLASH, SEARCH_GREATER_THAN, FOUND
    }
    private Status status = Status.SEARCH_TAG;

    public SelfClosing2ClosingTagsOutputStream(OutputStream wrapped,
            ResponseStatusInfo responseStatusInfo) {
        this.wrapped = wrapped;
        this.responseStatusInfo = responseStatusInfo;
    }

    @Override
    public void write(int b) throws IOException {
        if (responseStatusInfo.convertXhtml2Html()) {

            switch (status) {
                case SEARCH_TAG:
                    if (b == LESS_THAN) {
                        status = Status.DETERMINE_IF_IS_OPENING_TAG;
                    }
                    break;

                case DETERMINE_IF_IS_OPENING_TAG:
                    if (b != SLASH) {
                        status = Status.READ_TAG_NAME;
                    } else {
                        status = Status.SEARCH_TAG;
                        break;
                    }
                case READ_TAG_NAME:
                    if (b == SPACE) {
                        status = Status.SEARCH_SLASH;
                    } else if (b == GREATER_THAN) {
                        reset();
                    } else if (b == SLASH) {
                        status = Status.SEARCH_GREATER_THAN;
                        return;
                    } else {
                        tagNameStream.write(b);
                    }
                    break;
                case SEARCH_SLASH:
                    if (b == SLASH) {
                        status = Status.SEARCH_GREATER_THAN;
                        return;
                    }
                    if (b == GREATER_THAN) {
                        reset();
                    }
                    break;

                case SEARCH_GREATER_THAN:
                    if (b == GREATER_THAN) {
                        status = Status.FOUND;
                    } else {
                        wrapped.write(SLASH); // write the slash that we didn't write when we found it
                        status = Status.SEARCH_SLASH;
                    }
                    break;
            }
            if (status == Status.FOUND) {
                byte[] tagNameBytes = tagNameStream.toByteArray();
                if (isAllowedTagName(tagNameBytes)) {
                    wrapped.write(SLASH);
                    wrapped.write(GREATER_THAN);
                } else {
                    wrapped.write(GREATER_THAN);
                    wrapped.write(LESS_THAN);
                    wrapped.write(SLASH);
                    wrapped.write(tagNameBytes);
                    wrapped.write(GREATER_THAN);
                }
                reset();
            } else {
                wrapped.write(b);
            }

        } else {
            wrapped.write(b);
            flush();
        }
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

    private void reset() {
        tagNameStream.reset();
        status = Status.SEARCH_TAG;
    }

    private boolean isAllowedTagName(byte[] tagNameBytes) {
        for (int i = 0; i < allowedTagNamesBytes.length; i++) {
            byte[] allowedTagNameBytes = allowedTagNamesBytes[i];
            if (Arrays.equals(allowedTagNameBytes, tagNameBytes)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void flush() throws IOException {
        wrapped.flush();
    }

    @Override
    public void close() throws IOException {
        wrapped.close();
    }
}
