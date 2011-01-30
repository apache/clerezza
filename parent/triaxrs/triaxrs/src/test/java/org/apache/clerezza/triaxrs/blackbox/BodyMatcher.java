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
package org.apache.clerezza.triaxrs.blackbox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.wymiwyg.wrhapi.MessageBody;

public class BodyMatcher implements IArgumentMatcher {

	private byte[] bytes;

	BodyMatcher(MessageBody expected) {
		if (expected == null) {
			bytes = null;
			return;
		}
		try {
			InputStream in = Channels.newInputStream(expected.read());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			for (int ch = in.read(); ch != -1; ch = in.read()) {
				baos.write(ch);
			}
			this.bytes = baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void appendTo(StringBuffer buffer) {
		buffer.append("BodyMatchesBytes ");
		buffer.append(new String(bytes));

	}

	@Override
	public boolean matches(Object object) {
		if (object == null) {
			return bytes == null;
		}
		MessageBody body = (MessageBody) object;
		try {
			InputStream in = Channels.newInputStream(body.read());
			for (byte b : bytes) {
				int inByte = in.read();
				if (inByte != b) {
					return false;
				}
			}
			if (in.read() != -1) {
				return false;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return true;
	}

	public static MessageBody eqBody(MessageBody in) {
		EasyMock.reportMatcher(new BodyMatcher(in));
		return null;
	}

}