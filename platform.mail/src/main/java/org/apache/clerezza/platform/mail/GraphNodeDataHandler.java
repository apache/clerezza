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
package org.apache.clerezza.platform.mail;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import javax.activation.CommandInfo;
import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import org.apache.clerezza.platform.typerendering.Renderer;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * 
 * @author mir
 */
public class GraphNodeDataHandler extends DataHandler{

	private GraphNode graphNode;
	private Renderer renderer;
	
	
	public GraphNodeDataHandler(GraphNode graphNode,
			Renderer renderer) {
		super(null, null);
		this.graphNode = graphNode;
		this.renderer = renderer;
	}
	

	@Override
	public CommandInfo[] getAllCommands() {
		throw new UnsupportedOperationException("Operation not supported 1");
	}

	@Override
	public Object getBean(CommandInfo arg0) {
		throw new UnsupportedOperationException("Operation not supported 2");
	}

	@Override
	public CommandInfo getCommand(String arg0) {
		throw new UnsupportedOperationException("Operation not supported 3");
	}

	@Override
	public Object getContent() throws IOException {
		throw new UnsupportedOperationException("Operation not supported 4");
	}

	@Override
	public String getContentType() {
		return renderer.getMediaType().toString();
	}

	@Override
	public DataSource getDataSource() {
		throw new UnsupportedOperationException("Operation not supported 6");
	}

	@Override
	public InputStream getInputStream() throws IOException {
		throw new UnsupportedOperationException("Operation not supported 7");
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		throw new UnsupportedOperationException("Operation not supported 9");
	}

	@Override
	public CommandInfo[] getPreferredCommands() {
		throw new UnsupportedOperationException("Operation not supported 10");
	}

	@Override
	public Object getTransferData(DataFlavor arg0)
			throws UnsupportedFlavorException, IOException {
		throw new UnsupportedOperationException("Operation not supported 11");
	}

	@Override
	public synchronized DataFlavor[] getTransferDataFlavors() {
		throw new UnsupportedOperationException("Operation not supported 12");
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor arg0) {
		throw new UnsupportedOperationException("Operation not supported 13");
	}

	@Override
	public synchronized void setCommandMap(CommandMap arg0) {
		throw new UnsupportedOperationException("Operation not supported 14");
	}

	@Override
	public void writeTo(OutputStream os) throws IOException {
		renderer.render(graphNode, null, null, null, null, null, new HashMap<String, Object>(), os);
	}



}
