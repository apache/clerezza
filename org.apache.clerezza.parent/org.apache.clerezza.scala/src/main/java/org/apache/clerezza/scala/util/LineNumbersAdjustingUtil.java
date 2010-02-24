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
package org.apache.clerezza.scala.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.wymiwyg.commons.util.dirbrowser.FilePathNode;

/**
 * A util to adjust the line numbers in byte code.
 *
 * @author mir
 */
public class LineNumbersAdjustingUtil {

	/**
	 * Adjusts the line numbers of all classes at the specified path starting
	 * with the specified fileName. The line numbers are adjusted by the
	 * the specified lineOffset.
	 *
	 * @param path The path where the class files are located
	 * @param fileName Filter string that determines which class files are to be
	 *		adjusted. Only those starting with fileName are adjusted.
	 * @param lineOffset The offset used for adjusting the line numbers.
	 * @throws IOException
	 */
	public static void adjustAllFilesStartingWithFileName(String path, String fileName, int lineOffset) throws IOException {
		FilePathNode pathNode = new FilePathNode(new File(path));
		for (String string : pathNode.list()) {
			if(string.startsWith(fileName)) {
				byte[] transformedBytes = transform(pathNode.getSubPath(string).getInputStream(), lineOffset);
				FileOutputStream fout = new FileOutputStream(pathNode.getPath() + "/" + string);
				fout.write(transformedBytes);
				fout.close();
			}
		}
	}

	private static byte[] transform(InputStream bytecodeInputStream, int lineOffset) throws IOException {
		ClassWriter cw = new ClassWriter(0);
		ClassVisitor lineNumberCorretingCV = new LineNumbersAdjustingClassVisitor(cw, lineOffset);
		ClassReader cr = new ClassReader(bytecodeInputStream);
		cr.accept(lineNumberCorretingCV, 0);
		return cw.toByteArray();
	}
}
