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

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map.Entry;

import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.utils.imageprocessing.metadataprocessing.DataSet.DataSetFormatPair;
import org.apache.clerezza.utils.imageprocessing.metadataprocessing.DataSet.DataSetSerializer;

/**
 * This class provides utilities to convert 
 * various meta data formats into each other.
 *
 * @author daniel
 */
public abstract class MetaDataUtils {

	/**
	 * Converts meta data in IPTC format to meta data in XMP format.
	 *
	 * @param metaData  IPTC metadata
	 * @return  A TripleCollection containg XMP meta data.
	 */
	public static TripleCollection convertIptcToXmp(MetaData<IptcDataSet> metaData) {
		return convertToXMP(metaData, IptcDataSet.IPTC_FORMAT_NAME);
	}

	/**
	 * Converts meta data in EXIF format to meta data in XMP format.
	 *
	 * @param metaData  EXIF metadata
	 * @return  A TripleCollection containg XMP meta data.
	 */
	public static TripleCollection convertExifToXmp(MetaData<ExifTagDataSet> metaData) {
		return convertToXMP(metaData, ExifTagDataSet.EXIF_FORMAT_NAME);
	}
	
	private static TripleCollection convertToXMP(MetaData<? extends DataSet> metaData, String formatName) {
		MultivaluedMapImpl<String, String> nameSpaceClusters = 
			new MultivaluedMapImpl<String, String>();
		
		for(List<? extends DataSet> dataSetList : metaData) {
			DataSetFormatPair key = new DataSetFormatPair(
					dataSetList.get(0).getKey(), formatName);
			if(DataSet.conversionMap.containsKey(key)) {
				DataSetSerializer xmpSerializer = DataSet.conversionMap.get(key);
				
				nameSpaceClusters.add(xmpSerializer.key, xmpSerializer.serialize(
						metaData.get(dataSetList.get(0).getKey())));
			}
		}
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>");
		
		for(Entry<String, String> entry : XmpSchemaDefinitions.uriToNameSpacePrefix.entrySet()) {
			if(nameSpaceClusters.containsKey(entry.getKey())) {
				sb.append("<rdf:Description rdf:about='' xmlns:" + 
						entry.getValue() + "='" + 
						entry.getKey() + "'>");
				
				for(String serializedString : nameSpaceClusters.get(entry.getKey())) {
					sb.append(serializedString);
				}
				
				sb.append("</rdf:Description>");			
			}
		}
		
		sb.append("</rdf:RDF>");
		
		return Parser.getInstance().parse(
				new ByteArrayInputStream(sb.toString().getBytes()), 
				"application/rdf+xml");
	}
}