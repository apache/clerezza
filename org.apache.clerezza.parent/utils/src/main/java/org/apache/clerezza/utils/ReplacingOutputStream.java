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
package org.apache.clerezza.utils;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An OutputStream replacing a specified byte-sequence to another before 
 * writing to the undelying base stream.
 *
 * @author reto
 */
public class ReplacingOutputStream extends FilterOutputStream {

	private byte[] replaceFrom;
	private byte[] replaceTo;
	private int posInFrom = 0;

	/**
	 * Constructs a ReplacingOutputStream replacing replaceFrom with replaceTo
	 *
	 * @param out The base outputream to which the data is to be written after
	 * the replacement
	 * @param replaceFrom the byte-sequence to be replaced
	 * @param replaceTo the replacement byte sequence
	 */
	public ReplacingOutputStream(OutputStream out, byte[] replaceFrom, byte[] replaceTo) {
		super(out);
		this.replaceFrom = replaceFrom;
		this.replaceTo = replaceTo;
	}

	@Override
	public void write(int b) throws IOException {
		if (b == replaceFrom[posInFrom]) {
			posInFrom++;
			if (posInFrom == replaceFrom.length) {
				for (int i = 0; i < replaceTo.length; i++) {
					super.write(replaceTo[i]);
				}
				posInFrom = 0;
			}
		} else {
			for (int i = 0; i < posInFrom; i++) {
				super.write(replaceFrom[i]);
			}
			posInFrom = 0;
			super.write(b);
		}
	}

	@Override
	public void close() throws IOException {
		if (posInFrom == replaceFrom.length) {
			for (int i = 0; i < replaceTo.length; i++) {
				super.write(replaceTo[i]);
			}
			posInFrom = 0;
		}
		super.close();
	}
}
