/*
 *  Copyright 2010 mir.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.clerezza.tools.offline.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The conditional output stream wraps an <code>OutputStream</code>. All bytes
 * written to the condition output stream are tested by the <code>StreamCondition</code>
 * given to the constructor of the conditional output stream. The stream condition
 * can modify the byte stream if it is satisfied.
 *
 * @author mir
 */
public class ConditionalOutputStream extends OutputStream {

	private OutputStream out;
	private StreamCondition condition;
	private ByteArrayOutputStream cachedBytes = new ByteArrayOutputStream();

	public ConditionalOutputStream(OutputStream out, StreamCondition condition) {
		this.out = out;
		this.condition = condition;
	}

	@Override
	public void write(int b) throws IOException {		
		if (condition.feed(b)) {
			cachedBytes.write(b);
		} else {
			if (condition.isSatisfied()) {
				out.write(condition.getBytes());
				cachedBytes.reset();
			} else {
				if (cachedBytes.size() > 0) {
					out.write(cachedBytes.toByteArray());
					cachedBytes.reset();					
				}
				out.write(b);
			}
		}
	}

}
