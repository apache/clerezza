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
package org.apache.clerezza.web.fileserver;

import java.io.IOException;
import java.net.URL;
import junit.framework.Assert;
import org.junit.Test;
import org.wymiwyg.commons.util.dirbrowser.PathNode;
import org.wymiwyg.commons.util.dirbrowser.PathNodeFactory;

/**
 *
 * @author reto
 */
public class TestFileServer {

	@Test
	public void testGetResource() throws IOException {
		URL testRoot = getClass().getResource("test-root");
		PathNode pathNode = PathNodeFactory.getPathNode(testRoot);
		FileServer fileServer = new FileServer(pathNode);
		PathNode file = fileServer.getNode("dir/subdir/file.txt");
		Assert.assertEquals(PathNodeFactory.
				getPathNode(getClass().
				getResource("test-root/dir/subdir/file.txt")).getPath(), file.getPath());

	}
}
