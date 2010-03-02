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
package org.apache.clerezza.triaxrs.blackbox.jaf.resources;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;

/**
 * 
 * @author mir
 */
public class JsoDataContentHandler implements DataContentHandler {
	private static ActivationDataFlavor myDF = new ActivationDataFlavor(
			org.apache.clerezza.triaxrs.blackbox.jaf.resources.JafSerializableObj.class,
			"application/testobj", "Test Object");

	@Override
	public Object getContent(DataSource ds) throws IOException {
		InputStream is = ds.getInputStream();		
		ObjectInput oi = new ObjectInputStream(is);		
		Object obj = null;		
		try{
			obj = oi.readObject();
			oi.close();
		} catch (ClassNotFoundException e) {
			System.out.println("Class not found");
		}
		return obj;
	}
	
	@Override
	public Object getTransferData(DataFlavor df, DataSource ds)
			throws UnsupportedFlavorException, IOException {
		if (myDF.equals(df)) {
			return getContent(ds);
		}
		return null;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { myDF };
	}

	@Override
	public void writeTo(Object obj, String type, OutputStream os)
			throws IOException {
		if (!(obj instanceof JafSerializableObj)) {
			throw new IOException("\"" + myDF.getMimeType()
					+ "\" DataContentHandler requires TestObj object, "
					+ "was given object of type " + obj.getClass().toString());
		}
		try {
	        // Serialize testObj
	        ObjectOutput out = new ObjectOutputStream(os);
	        out.writeObject(obj);
	        out.close();      
		} catch (Exception e) {	}
	}
}
