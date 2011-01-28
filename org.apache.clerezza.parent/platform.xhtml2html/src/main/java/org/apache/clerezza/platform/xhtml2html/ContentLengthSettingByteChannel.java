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
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import org.wymiwyg.wrhapi.HandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Triggers the CONTENT-LENGTH header to be set in the wrapped response when the
 * first bytes are written to the <code>WritableByteChannel</code> and the content
 * (to be written to the channel) will not to be converted from xhtml to html.
 * 
 * @author mir
 */
class ContentLengthSettingByteChannel implements WritableByteChannel {

	final static private Logger logger = LoggerFactory.getLogger(ContentLengthSettingByteChannel.class);
	private WrappedResponse wrappedResponse;
	private WritableByteChannel wrappedByteChannel;
	private boolean contetLengthIsSet = false;

	ContentLengthSettingByteChannel(WritableByteChannel byteChannel,
			WrappedResponse wrappedResponse) {
		this.wrappedByteChannel = byteChannel;
		this.wrappedResponse = wrappedResponse;
	}

	@Override
	public int write(ByteBuffer bb) throws IOException {
		if (!contetLengthIsSet && bb.remaining() > 0) {
			try {
				wrappedResponse.setContentLengthIfNoConversion();
				contetLengthIsSet = true;
			} catch (HandlerException ex) {
				logger.error("Exception {}", ex.toString(), ex);
			}
		}
		return wrappedByteChannel.write(bb);
	}

	@Override
	public boolean isOpen() {
		return wrappedByteChannel.isOpen();




	}

	@Override
	public void close() throws IOException {
		wrappedByteChannel.close();


	}
}
