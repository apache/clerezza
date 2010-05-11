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

import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests backup functionality.
 *
 * @author hasan
 */
public class ZipCreationUtilTest {

	@Test
	public void simple() throws Exception {
		Hierarchy root = new Hierarchy();
		root.addChild("/foo/bar/test", "my testdata".getBytes());
		root.addChild("/foo/bar/test1", "another testdata".getBytes());
		root.addChild("/foo/something", "something else".getBytes());
		byte[] zipData = ZipCreationUtil.createZip(root);
		File tmpFile = File.createTempFile("test", "zip");
		FileOutputStream fout = new FileOutputStream(tmpFile);
		fout.write(zipData);
		fout.close();
		ZipFile zipFile = new ZipFile(tmpFile);
		Enumeration<? extends ZipEntry> entriesEnum = zipFile.entries();
		Set<ZipEntry> entries = new HashSet<ZipEntry>();
		while (entriesEnum.hasMoreElements()) {
			final ZipEntry nextElement = entriesEnum.nextElement();
			System.out.println(nextElement);
			entries.add(nextElement);
		}
		Assert.assertEquals(3, entries.size());

	}
}