package org.apache.clerezza.scala.scripting;

import java.io.IOException;
import java.io.Writer;

/**
 * A Writer which stores written output between two reset calls.
 */
public class ResettableStringWriter extends Writer {
	/**
	 * Buffer to store written data of writer.
	 */
	private StringBuilder stringBuffer = new StringBuilder();

	public ResettableStringWriter() {
	}

	@Override
	public void close() throws IOException {
		// nothing todo
	}

	@Override
	public void flush() throws IOException {
		// nothing todo
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		stringBuffer.append(cbuf, off, len);
	}

	/**
	 * Resets the buffer.
	 * 
	 * @return the contents of buffer
	 */
	public String reset() {
		String data = stringBuffer.toString();
		stringBuffer.setLength(0);
		return data;
	}
}