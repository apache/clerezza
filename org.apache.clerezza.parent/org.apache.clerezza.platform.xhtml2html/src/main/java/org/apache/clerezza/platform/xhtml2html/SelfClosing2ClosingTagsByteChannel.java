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
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

/**
 * Changes self-closing tags to tags with closing tags. Self-closing tags that
 * are allowed in HTML are left unchanged. The allowed self-closing tags are:
 * area, base, basefont, br, hr, input img, link and meta.
 * 
 * @author mir
 */
class SelfClosing2ClosingTagsByteChannel implements WritableByteChannel {
	
	
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

	private WritableByteChannel wrappedByteChannel;
	private ResponseStatusInfo responseStatusInfo;
	
	private ByteArrayOutputStream tagNameStream = new ByteArrayOutputStream();
	private OutputStream bytes = new ByteArrayOutputStream();

	private enum Status {SEARCH_TAG, DETERMINE_IF_IS_OPENING_TAG, READ_TAG_NAME,
		SEARCH_SLASH,SEARCH_GREATER_THAN, FOUND}
	
	private Status status = Status.SEARCH_TAG;
	
	public SelfClosing2ClosingTagsByteChannel(WritableByteChannel byteChannel,
			ResponseStatusInfo responseStatusInfo) {
		this.wrappedByteChannel = byteChannel;
		this.responseStatusInfo = responseStatusInfo;
		bytes = Channels.newOutputStream(wrappedByteChannel);
	}	

	@Override
	public int write(ByteBuffer byteBuffer) throws IOException {
		if (responseStatusInfo.convertXhtml2Html()) {
			int bytesWritten = byteBuffer.remaining();
			while (byteBuffer.remaining() > 0) {
				byte b = byteBuffer.get();
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
							continue;
						} else {
							tagNameStream.write(b);
						}
						break;
					case SEARCH_SLASH:
						if (b == SLASH) {
							status = Status.SEARCH_GREATER_THAN;
							continue;
						}
						if (b == GREATER_THAN) {
							reset();
						}
						break;

					case SEARCH_GREATER_THAN:
						if (b == GREATER_THAN) {
							status = Status.FOUND;
						} else {
							bytes.write(SLASH); // write the slash that we didn't write when we found it
							status = Status.SEARCH_SLASH;
						}
						break;
				}
				if (status == Status.FOUND) {
					byte[] tagNameBytes = tagNameStream.toByteArray();
					if (isAllowedTagName(tagNameBytes)) {
						bytes.write(SLASH);
						bytes.write(GREATER_THAN);
					} else {
						bytes.write(GREATER_THAN);
						bytes.write(LESS_THAN);
						bytes.write(SLASH);
						bytes.write(tagNameBytes);
						bytes.write(GREATER_THAN);					
					}
					reset();
				} else {
					bytes.write(b);
				}
			}
			return bytesWritten - byteBuffer.remaining();
		} else {
			return wrappedByteChannel.write(byteBuffer);
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
	public boolean isOpen() {
		return wrappedByteChannel.isOpen();
	}

	@Override
	public void close()	throws IOException {
		wrappedByteChannel.close();
	}
}
