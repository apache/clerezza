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
package org.apache.clerezza.tools.offline;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.wymiwyg.commons.util.dirbrowser.PathNode;

/**
 *
 * @author reto
 */
class ZipCreationUtil {
	static byte[] createZip(PathNode rootNode) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
			ZipOutputStream compressedTcs = new ZipOutputStream(result);
			archive(compressedTcs, rootNode);
			compressedTcs.close();
		return result.toByteArray();
	}

	private static void archive(ZipOutputStream compressedTcs,
			PathNode pathNode) throws IOException {
		if (pathNode.isDirectory()) {
			//multi-path node doesn't prevent duplicate in versions previous to
			//0.8
			Set<String> childNames = new HashSet<String>(Arrays.asList(pathNode.list()));
			for (String childName : childNames) {
				archive(compressedTcs, pathNode.getSubPath(childName));
			}
		} else {
			compressedTcs.putNextEntry(new ZipEntry(removeLeadingSlash(pathNode.getPath())));
			final int BUF_SIZE = 2048;
			byte buffer[] = new byte[BUF_SIZE];
			InputStream in = pathNode.getInputStream();
			int count;
			while ((count = in.read(buffer, 0, BUF_SIZE)) != -1) {
				compressedTcs.write(buffer, 0, count);
			}
		}
	}

	private static String removeLeadingSlash(String string) {
		if (string.length() > 0 && string.charAt(0) == '/') {
			return string.substring(1);
		} else {
			return string;
		}
	}
}
