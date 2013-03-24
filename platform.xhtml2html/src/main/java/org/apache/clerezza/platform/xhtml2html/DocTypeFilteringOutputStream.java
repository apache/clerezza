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
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 *
 * @author mir
 */
class DocTypeFilteringOutputStream extends OutputStream {

    private final static byte[] DOCTYPE_DEF_BYTES = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"> ".getBytes();
    private final static byte[] DOCTYPE_TAG_BYTES = "<!DOCTYPE".getBytes();
    private final static byte[] HTML_TAG_BYTES = "<html".getBytes();
    private final static byte[] XML_DECLARATION_BYTES = "<?xml".getBytes();
    private final static byte GREATER_THAN = '>';
    private final static byte SPACE = ' ';
    private final static byte NEXTLINE = '\n';
    private final static byte CARRIAGE_RETURN = '\r';
    private boolean doctypeWritten = false;
    private int arrayPosition = 0;
    private ByteArrayOutputStream cachedBytes = new ByteArrayOutputStream();
    private ResponseStatusInfo wrappedResponse;
    private boolean isXmlDeclaration = true;
    private boolean isNotADoctypeDef = false;
    private boolean hasHtmlTag = false;
    private boolean lookingForHtmlTag = true;
    private OutputStream wrapped;

    public DocTypeFilteringOutputStream(OutputStream wrapped,
            ResponseStatusInfo wrappedResponse) {
        this.wrapped = wrapped;
        this.wrappedResponse = wrappedResponse;
    }

    @Override
    public void write(int b) throws IOException {
        if (!doctypeWritten && wrappedResponse.convertXhtml2Html()) {

            cachedBytes.write(b);
            if (arrayPosition == 0
                    && (b == SPACE || b == NEXTLINE || b == CARRIAGE_RETURN)) {
                return;
            }
            if (arrayPosition == (DOCTYPE_TAG_BYTES.length - 1)
                    && DOCTYPE_TAG_BYTES[arrayPosition] == b) {
                writeEverythingAndSetDoctypeWrittenToTrue(b);
                return;
            }
            if (arrayPosition < XML_DECLARATION_BYTES.length
                    && XML_DECLARATION_BYTES[arrayPosition] != b) {
                isXmlDeclaration = false;
            }

            if (lookingForHtmlTag) {
                if (arrayPosition < HTML_TAG_BYTES.length
                        && HTML_TAG_BYTES[arrayPosition] != b) {
                    lookingForHtmlTag = false;
                }
                if (arrayPosition >= HTML_TAG_BYTES.length) {
                    hasHtmlTag = true;
                }
            }

            if (arrayPosition >= XML_DECLARATION_BYTES.length && isXmlDeclaration) {
                if (b == GREATER_THAN) {
                    arrayPosition = 0;
                    isNotADoctypeDef = false;
                    lookingForHtmlTag = true;
                    cachedBytes.reset(); // dump XML Declaration
                }
                return;
            }
            if (DOCTYPE_TAG_BYTES[arrayPosition] != b || isNotADoctypeDef) {
                isNotADoctypeDef = true;
                if (!isXmlDeclaration && hasHtmlTag) {
                    writeToWrappedChannel(DOCTYPE_DEF_BYTES);
                    writeEverythingAndSetDoctypeWrittenToTrue(b);
                    return;
                } else if (!isXmlDeclaration && !hasHtmlTag && !lookingForHtmlTag) {
                    writeEverythingAndSetDoctypeWrittenToTrue(b);
                    return;
                }
            }
            arrayPosition++;

        } else {
            wrapped.write(b);
        }
    }

    private void writeEverythingAndSetDoctypeWrittenToTrue(int b) throws IOException {
        writeToWrappedChannel(cachedBytes.toByteArray());
        //wrapped.write(b);
        doctypeWritten = true;
    }

    @Override
    public void flush()
            throws IOException {
        if (!doctypeWritten) {
            writeToWrappedChannel(cachedBytes.toByteArray());
        }
        wrapped.flush();
    }
    
    @Override
    public void close()
            throws IOException {
        if (!doctypeWritten) {
            writeToWrappedChannel(cachedBytes.toByteArray());
        }
        wrapped.close();
    }

    private void writeToWrappedChannel(byte[] byteArray) throws IOException {
        wrapped.write(byteArray);
    }
}
