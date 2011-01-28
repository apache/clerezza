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
package org.apache.clerezza.utils.imageprocessing.metadataprocessing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents an abstract meta data set. 
 * Instances of these class hold format specific information  
 * and information how to convert a data set into XMP format.
 * 
 * @author daniel
 *
 */
public abstract class DataSet {
	
	/**
	 * This class is used as a container for the <code>conversionMap</code>.
	 * It associates a key and the corresponding format name
	 * (e.g. The 2:80 is associated with the format name IPTC).
	 * 
	 * Note: XMP does not have its own data set class. 
	 * The key used for XMP data sets is the schema namespace 
	 * (e.g. XMP key is http://ns.adobe.com/xap/1.0/). XMP "data sets" are 
	 * defined by the schema used and not by the tag itself 
	 * (each schema creates its own rdf:Description structure).
	 * 
	 * @author daniel
	 *
	 * @see DataSet#conversionMap
	 */
	static class DataSetFormatPair {
		/**
		 * The data set key.
		 */
		final String key;
		
		/**
		 * The name of the metadata format the key belongs to (e.g. IPTC).
		 */
		final String formatName;
		
		
		/**
		 * Constructor.
		 * 
		 * @param key  the data set key.
		 * @param formatName  the name of the metadata format the key belongs to.
		 */
		DataSetFormatPair(String key, String formatName) {
			this.key = key;
			this.formatName = formatName;
		}
		
		@Override
		public int hashCode() {
			return (key + formatName).hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj.getClass().getName().equals(this.getClass().getName())) {
				DataSetFormatPair o = (DataSetFormatPair) obj;
				return this.key.equals(o.key) 
				&& this.formatName.equals(o.formatName);
			}
			return false;
		}
	}
	
	/**
	 * This class is used in the <code>conversionMap</code>. 
	 * It defines how to serialize DataSets.
	 * 
	 * This class' serialize method is meant to be overwritten 
	 * by instances of this class in order to define correct serialization.
	 * 
	 * @author daniel
	 * 
	 * @see DataSet#conversionMap
	 *
	 */
	static class DataSetSerializer extends DataSetFormatPair {
		
		/**
		 * Constructor.
		 * 
		 * @param key  the data set key.
		 * @param formatName  the name of the metadata format the key belongs to.
		 */
		DataSetSerializer(String key, String formatName) {
			super(key, formatName);
		}

		/**
		 * Returns a serialized representation of the supplied DataSets.
		 * 
		 * @param dataSets  The DataSet instances that should be serialized.
		 * @return   a serialized representation of <code>dataSets</code>.
		 */
		String serialize(List<? extends DataSet> dataSets) {
			String str = "";
			
			Iterator<? extends DataSet> it = dataSets.iterator();
			if(it.hasNext()) {
				str += it.next();
			}
			while(it.hasNext()) {
				str += ", " + it.next().getValue();
			}
			return str;
		}
	}
	
	/**
	 * Maps a key-format pair to a list of serializers.
	 */
	static final HashMap<DataSetFormatPair, DataSetSerializer> conversionMap = 
		new HashMap<DataSetFormatPair, DataSetSerializer>();
	
	/**
	 * Logger instance.
	 */
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Returns the value of the DataSet.
	 * 
	 * @return  the value
	 */
	public abstract String getValue();
	
	/**
	 * Sets the value.
	 * 
	 * @param value  the value
	 */
	public abstract void set(String value);

	/**
	 * Returns the a key that identifies this data set.
	 * 
	 * @return  the key
	 */
	public abstract String getKey();
	
	
	@Override
	public String toString() {
		return "(" + getKey() + ") " + getValue();
	}
}