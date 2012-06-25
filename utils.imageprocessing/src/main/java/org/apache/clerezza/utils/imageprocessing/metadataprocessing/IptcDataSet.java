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

import java.lang.reflect.Field;

/**
 * This class represents an IPTC DataSet and defines 
 * IPTC specific functionality and data.
 * 
 * @author daniel
 *
 */
public class IptcDataSet extends DataSet {
	
	/**
	 * @see DataSet#FORMAT_NAME
	 */
	static final String IPTC_FORMAT_NAME = "IPTC";
	
	private final int recordNumber;
	private final int dataSetNumber;
	private final String dataSetName;
	private String value;
	
	/**
	 * Creates a new IPTCDataSet.
	 * 
	 * @param recordNumber  IPTC record number (e.g. 2).
	 * @param dataSetNumber  IPTC data set number (e.g. 80 for the Author).
	 * @param value  the value.
	 */
	public IptcDataSet(int recordNumber, int dataSetNumber, String value) {
		this(recordNumber + ":" + dataSetNumber, value);
	}
	
	/**
	 * Creates a new IPTCDataSet.
	 * 
	 * @param key IPTC data set key (e.g. 2:80 for the Author).
	 * @param value  the value.
	 */
	public IptcDataSet(String key, String value) {
		String[] sa = key.split(":");
		this.recordNumber = Integer.parseInt(sa[0]);
		this.dataSetNumber = Integer.parseInt(sa[1]);
		this.dataSetName = extractDataSetName(this.getKey());
		this.value = value;
	}
	
	/**
	 * Returns the IPTC record number.
	 * 
	 * @return  the record number.
	 */
	public int getRecordNumber() {
		return recordNumber;
	}
	
	/**
	 * Returns the IPTC data set number.
	 * 
	 * @return  the data set number.
	 */
	public int getDataSetNumber() {
		return dataSetNumber;
	}
	
	/**
	 * Returns the IPTC data set name.
	 * 
	 * @return  the data set name.
	 */
	public String getDataSetName() {
		return dataSetName;
	}
	
	private String extractDataSetName(String value) {
		String name = "";
		for(Field field : getClass().getDeclaredFields()) {
			if(field.getType().getName().equals(String.class.getName())) {
				try {
					if(field.get(null).equals(value)) {
						String[] sa = field.getName().split("_");
						
						for(int j = 0; j < sa.length; ++j) {
							if(j > 0) {
								name += " ";
							}
							name += sa[j].substring(0, 1);
							name += sa[j].substring(1).toLowerCase();
						}
					}
				} catch (Exception ex) {
					//IllegalAccess or IllegalFormatException
					continue;
				}
			}
		}
		
		return name;
	}
	
	@Override
	public String getValue() {
		return value;
	}
	
	@Override
	public void set(String value) {
		this.value = value;
	}
	
	@Override
	public String getKey() {
		return recordNumber + ":" + dataSetNumber;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other != null && this.getClass().equals(other.getClass())) {
			IptcDataSet o = (IptcDataSet) other;
			return (o.recordNumber == recordNumber) && 
					(o.dataSetNumber == dataSetNumber) && 
					(o.value.equals(this.value));
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return value.hashCode();
	}
	
	/**
	 * 	IPTC Record Version key.
	 */
	public static final String RECORD_VERSION = "2:0";
	
	/**
	 * 	IPTC Object Type Reference key.
	 */
	public static final String OBJECT_TYPE_REFERENCE = "2:3";
	
	/**
	 * 	IPTC Object Attribute Reference key.
	 */
	public static final String OBJECT_ATTRIBUTE_REFERENCE = "2:4";
	
	/**
	 * 	IPTC Object Name key.
	 */
	public static final String OBJECT_NAME = "2:5";
	
	/**
	 * 	IPTC Edit Status key.
	 */
	public static final String EDIT_STATUS = "2:7";
	
	/**
	 * 	IPTC Editorial Update key.
	 */
	public static final String EDITORIAL_UPDATE = "2:8";
	
	/**
	 * 	IPTC Urgency key.
	 */
	public static final String URGENCY = "2:10";
	
	/**
	 * 	IPTC Subject Reference key.
	 */
	public static final String SUBJECT_REFERENCE = "2:12";
	
	/**
	 * 	IPTC Category key.
	 */
	public static final String CATEGORY = "2:15";
	
	/**
	 * 	IPTC Supplemental Category key.
	 */
	public static final String SUPPLEMENTAL_CATEGORY = "2:20";
	
	/**
	 * 	IPTC Fixture Identifier key.
	 */
	public static final String FIXTURE_IDENTIFIER = "2:22";
	
	/**
	 * 	IPTC Keywords key.
	 */
	public static final String KEYWORDS = "2:25";
	
	/**
	 * 	IPTC Content Location Code key.
	 */
	public static final String CONTENT_LOCATION_CODE = "2:26";
	
	/**
	 * 	IPTC Content Location Name key.
	 */
	public static final String CONTENT_LOCATION_NAME = "2:27";
	
	/**
	 * 	IPTC Release Date key.
	 */
	public static final String RELEASE_DATE = "2:30";
	
	/**
	 * 	IPTC Release Time key.
	 */
	public static final String RELEASE_TIME = "2:35";
	
	/**
	 * 	IPTC Expiration Date key.
	 */
	public static final String EXPIRATION_DATE = "2:37";
	
	/**
	 * 	IPTC Expiration Time key.
	 */
	public static final String EXPIRATION_TIME = "2:38";
	
	/**
	 * 	IPTC Special Instructions key.
	 */
	public static final String SPECIAL_INSTRUCTIONS = "2:40";
	
	/**
	 * 	IPTC Action Advised key.
	 */
	public static final String ACTION_ADVISED = "2:42";
	
	/**
	 * 	IPTC Reference Service key.
	 */
	public static final String REFERENCE_SERVICE = "2:45";
	
	/**
	 * 	IPTC Reference Date key.
	 */
	public static final String REFERENCE_DATE = "2:47";
	
	/**
	 * 	IPTC Reference Number key.
	 */
	public static final String REFERENCE_NUMBER = "2:50";
	
	/**
	 * 	IPTC Date Created key.
	 */
	public static final String DATE_CREATED = "2:55";
	
	/**
	 * 	IPTC Time created key.
	 */
	public static final String TIME_CREATED = "2:60";
	
	/**
	 * 	IPTC Digital Creation Date key.
	 */
	public static final String DIGITAL_CREATION_DATE = "2:62";
	
	/**
	 * 	IPTC Digital Creation Time key.
	 */
	public static final String DIGITAL_CREATION_TIME = "2:63";
	
	/**
	 * 	IPTC Originating Program key.
	 */
	public static final String ORIGINATING_PROGRAM = "2:65";
	
	/**
	 * 	IPTC Program Version key.
	 */
	public static final String PROGRAM_VERSION = "2:70";
	
	/**
	 * 	IPTC Object Cycle key.
	 */
	public static final String OBJECT_CYCLE = "2:75";
	
	/**
	 * 	IPTC By-Line key.
	 */
	public static final String BY_LINE = "2:80";
	
	/**
	 * 	IPTC By-Line Title key.
	 */
	public static final String BY_LINE_TITLE = "2:85";
	
	/**
	 * 	IPTC City key.
	 */
	public static final String CITY = "2:90";
	
	/**
	 * 	IPTC Sub Location key.
	 */
	public static final String SUB_LOCATION = "2:92";
	
	/**
	 * 	IPTC Province key.
	 */
	public static final String PROVINCE = "2:95";
	
	/**
	 * 	IPTC Country Code key.
	 */
	public static final String COUNTRY_CODE = "2:100";
	
	/**
	 * 	IPTC Country Name key.
	 */
	public static final String COUNTRY_NAME = "2:101";
	
	/**
	 * 	IPTC Original Transmission Reference key.
	 */
	public static final String ORIGINAL_TRANSMISSION_REFERENCE = "2:103";
	
	/**
	 * 	IPTC Headline key.
	 */
	public static final String HEADLINE = "2:105";
	
	/**
	 * 	IPTC Credit key.
	 */
	public static final String CREDIT = "2:110";
	
	/**
	 * 	IPTC Source key.
	 */
	public static final String SOURCE = "2:115";
	
	/**
	 * 	IPTC Copyright Notice key.
	 */
	public static final String COPYRIGHT_NOTICE = "2:116";
	
	/**
	 * 	IPTC Contact key.
	 */
	public static final String CONTACT = "2:118";
	
	/**
	 * 	IPTC Caption key.
	 */
	public static final String CAPTION = "2:120";
	
	/**
	 * 	IPTC Editor key.
	 */
	public static final String EDITOR = "2:122";
	
	/**
	 * 	IPTC Rasterized Caption key.
	 */
	public static final String RASTERIZED_CAPTION = "2:125";
	
	/**
	 * 	IPTC Image Type Key.
	 */
	public static final String IMAGE_TYPE = "2:130";
	
	/**
	 * 	IPTC Image Orientation key.
	 */
	public static final String IMAGE_ORIENTATION = "2:131";
	
	/**
	 * 	IPTC Language Identifier key.
	 */
	public static final String LANGUAGE_IDENTIFIER = "2:135";
	
	/**
	 * 	IPTC Audio Type key.
	 */
	public static final String AUDIO_TYPE = "2:150";
	
	/**
	 * 	IPTC Audio Sampling Rate key.
	 */
	public static final String AUDIO_SAMPLING_RATE = "2:151";
	
	/**
	 * 	IPTC Audio Sampling Resolution key.
	 */
	public static final String AUDIO_SAMPLING_RESOLUTION = "2:152";
	
	/**
	 * 	IPTC Audio Duration key.
	 */
	public static final String AUDIO_DURATION = "2:153";
	
	/**
	 * 	IPTC Audio Outcue key.
	 */
	public static final String AUDIO_OUTCUE = "2:154";
	
	/**
	 * 	IPTC Objectdata Preview File Format key.
	 */
	public static final String OBJECTDATA_PREVIEW_FILE_FORMAT = "2:200";
	
	/**
	 * 	IPTC Objectdata Preview File Format Version key.
	 */
	public static final String OBJECTDATA_PREVIEW_FILE_FORMAT_VERSION = "2:201";
	
	/**
	 * 	IPTC Objectdata Preview Data key.
	 */
	public static final String OBJECTDATA_PREVIEW_DATA = "2:202";
	
	static {
		//definitions for converting IPTC to XMP
		conversionMap.put(new DataSetFormatPair(OBJECT_ATTRIBUTE_REFERENCE,
				IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.Iptc4XmpCoreIntellectualGenre);
		conversionMap.put(new DataSetFormatPair(OBJECT_NAME, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.DcTitle);
		conversionMap.put(new DataSetFormatPair(URGENCY, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.PhotoshopUrgency);
		conversionMap.put(
				new DataSetFormatPair(SUBJECT_REFERENCE, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.Iptc4XmpCoreSubjectCode);
		conversionMap.put(new DataSetFormatPair(CATEGORY, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.PhotoshopCategory);
		conversionMap.put(new DataSetFormatPair(SUPPLEMENTAL_CATEGORY,
				IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.PhotoshopSupplementalCategories);
		conversionMap.put(new DataSetFormatPair(KEYWORDS, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.DcSubject);
		conversionMap.put(new DataSetFormatPair(SPECIAL_INSTRUCTIONS,
				IPTC_FORMAT_NAME), XmpSchemaDefinitions.PhotoshopInstructions);
		conversionMap.put(new DataSetFormatPair(DATE_CREATED, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.PhotoshopDateCreated);
		conversionMap.put(new DataSetFormatPair(BY_LINE, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.DcCreator);
		conversionMap.put(new DataSetFormatPair(BY_LINE_TITLE, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.PhotoshopAuthorsPosition);
		conversionMap.put(new DataSetFormatPair(CITY, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.PhotoshopCity);
		conversionMap.put(new DataSetFormatPair(SUB_LOCATION, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.Iptc4XmpCoreLocation);
		conversionMap.put(new DataSetFormatPair(PROVINCE, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.PhotoshopState);
		conversionMap.put(new DataSetFormatPair(COUNTRY_CODE, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.Iptc4XmpCoreCountryCode);
		conversionMap.put(new DataSetFormatPair(COUNTRY_NAME, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.PhotoshopCountry);
		conversionMap.put(new DataSetFormatPair(
				ORIGINAL_TRANSMISSION_REFERENCE, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.PhotoshopTransmissionReference);
		conversionMap.put(new DataSetFormatPair(HEADLINE, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.PhotoshopHeadline);
		conversionMap.put(new DataSetFormatPair(CREDIT, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.PhotoshopCredit);
		conversionMap.put(new DataSetFormatPair(SOURCE, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.PhotoshopSource);
		conversionMap.put(new DataSetFormatPair(COPYRIGHT_NOTICE, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.DcRights);
		conversionMap.put(new DataSetFormatPair(CONTACT, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.Iptc4XmpCoreContactInfoDetails);
		conversionMap.put(new DataSetFormatPair(CAPTION, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.DcDescription);
		conversionMap.put(new DataSetFormatPair(EDITOR, IPTC_FORMAT_NAME),
				XmpSchemaDefinitions.PhotoshopCaptionWriter);
	}
}
