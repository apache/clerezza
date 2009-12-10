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
package org.apache.clerezza.templating.seedsnipe.simpleparser;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Stack;

/**
 * Line reader that can handle multiple marks.
 * 
 * @author reto
 * 
 */
public class MultiMarkLineNumberReader extends LineNumberReader {

	private Stack<Mark> marksInBuffer = new Stack<Mark>();
	private StringWriter buffer = new StringWriter();
	private boolean buffering = false;
	private boolean repeating = false;
	private int repeatingPosition;

	/**
	 * Constructor.
	 * 
	 * @param in  The Reader to wrap. 
	 */
	public MultiMarkLineNumberReader(Reader in) {
		super(in);
	}

	@Override
	public void mark(int i) {
		buffering = true; //there is something in the buffer
		Mark mark;
		if (!repeating) { //new mark
			mark = new Mark(buffer.getBuffer().length(), getLineNumber());
		} else {
			mark = new Mark(repeatingPosition, getLineNumber());
		}
		marksInBuffer.push(mark);
	}

	/**
	 * Remove the topmost mark.
	 */
	public void removeMark() {
		marksInBuffer.pop();
	}

	@Override
	public int read() throws IOException {
		if (repeating) {
			if (repeatingPosition < buffer.getBuffer().length()) {
				return buffer.getBuffer().charAt(repeatingPosition++);
			} else {
				repeating = false;
			}
		}
		int back = super.read();
		if (buffering) {
			buffer.write(back);
		}
		return back;

	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {

		int charactersRead;
		for (charactersRead = 0; charactersRead < len; charactersRead++) {
			int thisChar = read();
			if (thisChar == -1) {
				break;
			}
			cbuf[off + charactersRead] = (char) thisChar;
		}
		return charactersRead;
	}
	
	@Override
	public void reset() throws IOException {
		repeating = true;
		Mark mark = marksInBuffer.peek();
		repeatingPosition = mark.getPositionInBuffer();
		setLineNumber(mark.getLineNumber());
	}

}

/**
 * This mark is used by {@link MultiMarkLineNumberReader}
 * to save positions within a {@link Reader}.
 * 
 * @author reto
 */
class Mark {
	
	private int positionInBuffer;
	private int lineNumber;

	/**
	 * Constructor.
	 * 
	 * @param positionInBuffer
	 * @param lineNumber  The current line number.
	 */
	protected Mark(int positionInBuffer, int lineNumber) {
		this.positionInBuffer = positionInBuffer;
		this.lineNumber = lineNumber;
	}

	/**
	 * Returns the index of the mark within 
	 * the reader's buffer.
	 * 
	 * @return the index of the mark within the reader's buffer.
	 */
	protected int getPositionInBuffer() {
		return positionInBuffer;
	}
	
	/**
	 * Returns the line number this mark has been set on.
	 * 
	 * @return the line number.
	 */
	protected int getLineNumber() {
		return lineNumber;
	}

	@Override
	public String toString() {
		return "Mark: positionInBuffer=" + positionInBuffer + " lineNumber="
				+ lineNumber;
	}
}