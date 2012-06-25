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

/**
 * This class defines serializers for the properties defined 
 * by various XMP schema specifications.
 * 
 * @author daniel
 *
 */
final class XmpSchemaDefinitions {
	
	//Namespace constants
	private static final String DC_NS_URI = "http://purl.org/dc/elements/1.1/";
	private static final String XMP_NS_URI = "http://ns.adobe.com/xap/1.0/";
	private static final String XMPIDQ_NS_URI = "http://ns.adobe.com/xmp/Identifier/qual/1.0/";
	private static final String XMPRIGHTS_NS_URI = "http://ns.adobe.com/xap/1.0/rights/";
	private static final String XMPMM_NS_URI = "http://ns.adobe.com/xap/1.0/mm/";
	private static final String XMPBJ_NS_URI = "http://ns.adobe.com/xap/1.0/bj/";
	private static final String XMPTPG_NS_URI = "http://ns.adobe.com/xap/1.0/t/pg/";
	private static final String XMPDM_NS_URI = "http://ns.adobe.com/xmp/1.0/DynamicMedia/";
	private static final String PDF_NS_URI = "http://ns.adobe.com/pdf/1.3/";
	private static final String PHOTOSHOP_NS_URI = "http://ns.adobe.com/photoshop/1.0/";
	private static final String CRS_NS_URI = "http://ns.adobe.com/camera-raw-settings/1.0/";
	private static final String TIFF_NS_URI = "http://ns.adobe.com/tiff/1.0/";
	private static final String EXIF_NS_URI = "http://ns.adobe.com/exif/1.0/";
	private static final String AUX_NS_URI = "http://ns.adobe.com/exif/1.0/aux/";
	private static final String IPTC_4_XMP_CORE_NS_URI = "http://iptc.org/std/Iptc4xmpCore/1.0/xmlns/";
	private static final String IPTC_4_XMP_EXT_NS_URI = "http://iptc.org/std/Iptc4xmpExt/2008-02-29/";
	
	
	/**
	 * Maps namespace URIs to namespace prefixes (e.g. "http://ns.adobe.com/xap/1.0/" to "xmp")
	 */
	static HashMap<String, String> uriToNameSpacePrefix = new HashMap<String, String>();
	static {
		//Namespace mappings
		uriToNameSpacePrefix.put(DC_NS_URI,"dc");
		uriToNameSpacePrefix.put(XMP_NS_URI, "xmp");
		uriToNameSpacePrefix.put(XMPIDQ_NS_URI, "xmpidq");
		uriToNameSpacePrefix.put(XMPRIGHTS_NS_URI, "xmpRights");
		uriToNameSpacePrefix.put(XMPMM_NS_URI, "xmpMM");
		uriToNameSpacePrefix.put(XMPBJ_NS_URI, "xmpBJ");
		uriToNameSpacePrefix.put(XMPTPG_NS_URI, "xmpTPg");
		uriToNameSpacePrefix.put(XMPDM_NS_URI, "xmpDM");
		uriToNameSpacePrefix.put(PDF_NS_URI, "pdf");
		uriToNameSpacePrefix.put(PHOTOSHOP_NS_URI, "photoshop");
		uriToNameSpacePrefix.put(CRS_NS_URI, "crs");
		uriToNameSpacePrefix.put(TIFF_NS_URI, "tiff");
		uriToNameSpacePrefix.put(EXIF_NS_URI, "exif");
		uriToNameSpacePrefix.put(AUX_NS_URI, "aux");
		uriToNameSpacePrefix.put(IPTC_4_XMP_CORE_NS_URI, "Iptc4xmpCore");
		uriToNameSpacePrefix.put(IPTC_4_XMP_EXT_NS_URI, "Iptc4xmpExt");
	}
	
	//Dublin Core schema
	
	/**
	 * dc:contributor serializer
	 * 
	 * Contributors to the resource (other than authors).
	 */
	static DataSet.DataSetSerializer DcContributor = 
		new DataSet.DataSetSerializer(DC_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<dc:contributor>" + rdfBag(dataSets) + "</dc:contributor>";
		};
	};

	/**
	 * dc:coverage serializer
	 * 
	 * Extend or scope of the resource.
	 */
	static DataSet.DataSetSerializer DcCoverage = 
		new DataSet.DataSetSerializer(DC_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<dc:coverage>" + dataSets.get(0).getValue() + "</dc:coverage>";
		};
	};
	
	/**
	 * dc:creator serializer
	 * 
	 * The authors of the resource 
	 * (listed in order of precedence, if significant).
	 */
	static DataSet.DataSetSerializer DcCreator = 
		new DataSet.DataSetSerializer(DC_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<dc:creator>" + rdfSeq(dataSets) + "</dc:creator>";
		};
	};
	
	/**
	 * dc:date serializer
	 * 
	 * Date(s) that something interesting happened to the resource.
	 */
	static DataSet.DataSetSerializer DcDate = 
		new DataSet.DataSetSerializer(DC_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<dc:date>" + rdfSeq(dataSets) + "</dc:date>";
		};
	};
	
	/**
	 * dc:description serializer
	 * 
	 * A textual description of the content of the resource.
	 * Multiple values may be present for different languages.
	 */
	static DataSet.DataSetSerializer DcDescription = 
		new DataSet.DataSetSerializer(DC_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<dc:description>" + rdfAlt(dataSets, "x-default") + 
					"</dc:description>";
		};
	};
	
	/**
	 * dc:format serializer
	 * 
	 * The file format used when saving the resource.
	 */
	static DataSet.DataSetSerializer DcFormat = 
		new DataSet.DataSetSerializer(DC_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<dc:format>" + dataSets.get(0).getValue() + "</dc:format>";
		};
	};
	
	/**
	 * dc:identifier serializer
	 * 
	 * Unique identifier of the resource.
	 */
	static DataSet.DataSetSerializer DcIdentifier = 
		new DataSet.DataSetSerializer(DC_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<dc:identifier>" + dataSets.get(0).getValue() + 
					"</dc:identifier>";
		};
	};
	
	/**
	 * dc:language serializer
	 * 
	 * An unordered array specifying the languages used in the resource.
	 */
	static DataSet.DataSetSerializer DcLanguage = 
		new DataSet.DataSetSerializer(DC_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<dc:language>" + rdfBag(dataSets) + "</dc:language>";
		};
	};
	
	/**
	 * dc:publisher serializer
	 * 
	 * Publishers.
	 */
	static DataSet.DataSetSerializer DcPublisher = 
		new DataSet.DataSetSerializer(DC_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<dc:publisher>" + rdfBag(dataSets) + "</dc:publisher>";
		};
	};
	
	/**
	 * dc:relation serializer
	 * 
	 * Relationships to other documents.
	 */
	static DataSet.DataSetSerializer DcRelation = 
		new DataSet.DataSetSerializer(DC_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<dc:relation>" + rdfBag(dataSets) + "</dc:relation>";
		};
	};
	
	/**
	 * dc:rights serializer
	 * 
	 * Informal rights statement, selected by language.
	 */
	static DataSet.DataSetSerializer DcRights = 
		new DataSet.DataSetSerializer(DC_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<dc:rights>" + rdfAlt(dataSets, "x-default") + "</dc:rights>";
		};
	};
	
	/**
	 * dc:source serializer
	 * 
	 * Unique identifier of the work from which this resource was derived.
	 */
	static DataSet.DataSetSerializer DcSource = 
		new DataSet.DataSetSerializer(DC_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<dc:source>" + dataSets.get(0).getValue() + "</dc:source>";
		};
	};
	
	/**
	 * dc:subject serializer
	 * 
	 * An unordered array of descriptive phrases or keywords 
	 * that specify the topic of the content of the resource.
	 */
	static DataSet.DataSetSerializer DcSubject = 
		new DataSet.DataSetSerializer(DC_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<dc:subject>" + rdfBag(dataSets) + "</dc:subject>";
		};
	};
	
	/**
	 * dc:title serializer
	 * 
	 * The title of the document, or the name given to the resource.
	 * Typically, it will be a name by which the resource is formally known.
	 */
	static DataSet.DataSetSerializer DcTitle = 
		new DataSet.DataSetSerializer(DC_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<dc:title>" + rdfAlt(dataSets, "x-default") + "</dc:title>";
		};
	};
	
	/**
	 * dc:type serializer
	 * 
	 * A document type; for example, novel, poem, or working paper.
	 */
	static DataSet.DataSetSerializer DcType = 
		new DataSet.DataSetSerializer(DC_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<dc:type>" + rdfBag(dataSets) + "</dc:type>";
		};
	};
	
	//XMP Basic schema
	
	/**
	 * xmp:Advisory serializer
	 * 
	 * An unordered array specifying properties 
	 * that were edited outside the authoring application.
	 * Each item should contain a single namespace and XPath 
	 * separated by one ASCII space (U+0020).
	 */
	static DataSet.DataSetSerializer XmpAdvisory = 
		new DataSet.DataSetSerializer(XMP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmp:Advisory>" + rdfBag(dataSets) + "</xmp:Advisory>";
		};
	};
	
	/**
	 * xmp:BaseURL serializer
	 * 
	 * The base URL for relative URLs in the document content. 
	 * If this document contains Internet links, and those links are relative, 
	 * they are relative to this base URL. 
	 * 
	 * This property provides a standard way for embedding relative URLs 
	 * to be interpreted by tools. 
	 * Web authoring tools should set the value based on their notion 
	 * of where URLs will be interpreted.
	 */
	static DataSet.DataSetSerializer XmpBaseUrl = 
		new DataSet.DataSetSerializer(XMP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmp:BaseURL>" + dataSets.get(0).getValue() + 
					"</xmp:BaseURL>";
		};
	};
	
	/**
	 * xmp:CreateDate serializer
	 * 
	 * The date and time the resource was originally created.
	 */
	static DataSet.DataSetSerializer XmpCreateDate = 
		new DataSet.DataSetSerializer(XMP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmp:CreateDate>" + dataSets.get(0).getValue() + 
					"</xmp:CreateDate>";
		};
	};
	
	/**
	 * xmp:CreatorTool serializer
	 * 
	 * The name of the first known tool used to create the resource.
	 * If history is present in the metadata, this value should be 
	 * equivalent to that of xmpMM:History's softwareAgent property.
	 */
	static DataSet.DataSetSerializer XmpCreatorTool = 
		new DataSet.DataSetSerializer(XMP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmp:CreatorTool>" + dataSets.get(0).getValue() + 
			"</xmp:CreatorTool>";
		};
	};
	
	/**
	 * xmp:Identifier serializer
	 * 
	 * An unordered array of text strings that unambiguously 
	 * identify the resource within a given context.
	 * An array item may be qualified with xmpidq:Scheme 
	 * to denote the formal identification system to which that identifier
	 * conforms.
	 * 
	 * The dc:identifier property is not used because it lacks a defined scheme
	 * qualifier and has been defined in the XMP Specification as a simple
	 * (single valued) property.
	 */
	static DataSet.DataSetSerializer XmpIdentifier = 
		new DataSet.DataSetSerializer(XMP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmp:Identifier>" + rdfBag(dataSets) + "</xmp:Identifier>";
		};
	};
	
	/**
	 * xmp:Label serializer
	 * 
	 * A word or short phrase that identifies a document 
	 * as a member of a user-defined collection. 
	 * Used to organize documents in a file browser.
	 */
	static DataSet.DataSetSerializer XmpLabel = 
		new DataSet.DataSetSerializer(XMP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmp:Label>" + dataSets.get(0).getValue() + "</xmp:Label>";
		};
	};
	
	/**
	 * xmp:MetadataDate serializer
	 * 
	 * The date and time that any metadata for this resource was last changed.
	 * It should be the same as or more than xmp:ModifyDate.
	 */
	static DataSet.DataSetSerializer XmpMetadataDate = 
		new DataSet.DataSetSerializer(XMP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmp:MetadataDate>" + dataSets.get(0).getValue() + 
					"</xmp:MetadataDate>";
		};
	};
	
	/**
	 * xmp:ModifyDate serializer
	 * 
	 * The date and time the resource was last modified.
	 * 
	 * The value of this property is not necessarily the same as the file's
	 * system modification date because it is set before the file is saved.
	 */
	static DataSet.DataSetSerializer XmpModifyDate = 
		new DataSet.DataSetSerializer(XMP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmp:ModifyDate>" + dataSets.get(0).getValue() + 
					"</xmp:ModifyDate>";
		};
	};
	
	/**
	 * xmp:Nickname serializer
	 * 
	 * A short informal name for the resource.
	 */
	static DataSet.DataSetSerializer XmpNickname = 
		new DataSet.DataSetSerializer(XMP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmp:Nickname>" + dataSets.get(0).getValue() + 
					"</xmp:Nickname>";
		};
	};
	
	/**
	 * xmp:Rating serializer
	 * 
	 * A number that indicates a document's status relative to other documents, 
	 * used to organize documents in a file browser.
	 * Values are user-defined within an application-defined range.
	 */
	static DataSet.DataSetSerializer XmpRating = 
		new DataSet.DataSetSerializer(XMP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmp:Rating>" + dataSets.get(0).getValue() + 
					"</xmp:Rating>";
		};
	};
	
	/**
	 * xmp:Thumbnails serializer
	 * 
	 * An alternative array of thumbnail images for a file, 
	 * which can differ in characteristics such as size or image encoding.
	 */
	static DataSet.DataSetSerializer XmpThumbnails = 
		new DataSet.DataSetSerializer(XMP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmp:Thumbnails>" + rdfAlt(dataSets, null) + 
					"</xmp:Thumbnails>";
		};
	};
	
	//XMP Identifier Qualifier
	
	/**
	 * xmpidq:Scheme serializer
	 * 
	 * The name of the formal identification system 
	 * used in the value of the associated xmp:Identifier item.
	 */
	static DataSet.DataSetSerializer XmpidqScheme = 
		new DataSet.DataSetSerializer(XMPIDQ_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpidq:Scheme>" + dataSets.get(0).getValue() + 
					"</xmpidq:Scheme>";
		};
	};
	
	//XMP Rights Management schema
	
	/**
	 * xmpRights:Certificate serializer
	 * 
	 * Online rights management certificate.
	 */
	static DataSet.DataSetSerializer XmpRightsCertificate = 
		new DataSet.DataSetSerializer(XMPRIGHTS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpRights:Certificate>" + dataSets.get(0).getValue() + 
					"</xmpRights:Certificate>";
		};
	};
	
	/**
	 * xmpRights:Marked serializer
	 * 
	 * Indicates that this is a rights-managed resource.
	 */
	static DataSet.DataSetSerializer XmpRightsMarked = 
		new DataSet.DataSetSerializer(XMPRIGHTS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpRights:Marked>" + dataSets.get(0).getValue() + 
				"</xmpRights:Marked>";
		};
	};
	
	/**
	 * xmpRights:Owner serializer
	 * 
	 * An unordered array specifying the legal owner(s) of a resource.
	 */
	static DataSet.DataSetSerializer XmpRightsOwner = 
		new DataSet.DataSetSerializer(XMPRIGHTS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpRights:Owner>" + rdfBag(dataSets) + 
					"</xmpRights:Owner>";
		};
	};
	
	/**
	 * xmpRights:UsageTerms serializer
	 * 
	 * Text instructions on how a resource can be legally used.
	 */
	static DataSet.DataSetSerializer XmpRightsUsageTerms = 
		new DataSet.DataSetSerializer(XMPRIGHTS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpRights:UsageTerms>" + rdfAlt(dataSets, "x-default") + 
					"</xmpRights:UsageTerms>";
		};
	};
	
	/**
	 * xmpRights:WebStatement serializer
	 * 
	 * The location of a web page describing the owner 
	 * and/or rights statement for this resource.
	 */
	static DataSet.DataSetSerializer XmpRightsWebStatement = 
		new DataSet.DataSetSerializer(XMPRIGHTS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpRights:WebStatement>" + dataSets.get(0).getValue() + 
					"</xmpRights:WebStatement>";
		};
	};
	
	//XMP Media Management schema
	
	/**
	 * xmpMM:DerivedFrom serializer
	 * 
	 * A reference to the original document from which this one is derived. 
	 * It is a minimal reference, missing components can be assumed 
	 * to be unchanged. For example, a new version might only need to specify 
	 * the instance ID and version number of the previous version, 
	 * or a rendition might only need to specify the instance ID 
	 * and rendition class of the original.
	 */
	static DataSet.DataSetSerializer XmpMmDerivedFrom = 
		new DataSet.DataSetSerializer(XMPMM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpMM:DerivedFrom>" + dataSets.get(0).getValue() + 
					"</xmpMM:DerivedFrom>";
		};
	};
	
	/**
	 * xmpMM:DocumentID serializer
	 * 
	 * The common identifier for all versions and rendition of a document.
	 */
	static DataSet.DataSetSerializer XmpMmDocumentID = 
		new DataSet.DataSetSerializer(XMPMM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpMM:DocumentID>" + dataSets.get(0).getValue() + 
			"</xmpMM:DocumentID>";
		};
	};
	
	/**
	 * xmpMM:History serializer
	 * 
	 * An ordered array of high-level user actions that resulted in this resource.
	 * It is intended to give human readers a description of the steps taken 
	 * to make the changes from the previous version to this one.
	 * The list should be at an abstract level; 
	 * it not intended to be an exhaustive keystroke or other detailed history.
	 * The description should be sufficient for metadata management, as well as
	 * workflow enhancement.
	 */
	static DataSet.DataSetSerializer XmpMmHistory = 
		new DataSet.DataSetSerializer(XMPMM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpMM:History>" + rdfSeq(dataSets) + 
				"</xmpMM:History>";
		};
	};
	
	/**
	 * xmpMM:Ingredients serializer
	 * 
	 * An unordered array of references to resources that were incorporated,
	 * by inclusion or reference, into this document.
	 */
	static DataSet.DataSetSerializer XmpMmIngredients = 
		new DataSet.DataSetSerializer(XMPMM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpMM:Ingredients>" + rdfBag(dataSets) + 
				"</xmpMM:Ingredients>";
		};
	};
	
	/**
	 * xmpMM:InstanceID serializer
	 * 
	 * An identifier for a specific incarnation of a document, 
	 * updated each time a file is saved. It should be based on a UUID.
	 * 
	 */
	static DataSet.DataSetSerializer XmpMmInstanceId = 
		new DataSet.DataSetSerializer(XMPMM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpMM:InstanceID>" + dataSets.get(0).getValue() + 
				"</xmpMM:InstanceID>";
		};
	};
	
	/**
	 * xmpMM:ManagedFrom serializer
	 * 
	 * A reference to the document as it was prior to becoming managed. 
	 * It is set when a managed document is introduced 
	 * to an asset management system that does not currently own it. 
	 * It may or may not include references to different management systems.
	 * 
	 */
	static DataSet.DataSetSerializer XmpMmManagedFrom = 
		new DataSet.DataSetSerializer(XMPMM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpMM:ManagedFrom>" + dataSets.get(0).getValue() + 
					"</xmpMM:ManagedFrom>";
		};
	};
	
	/**
	 * xmpMM:Manager serializer
	 * 
	 * The name of the asset management system that manages this resource. 
	 * Along with xmpMM:ManagerVariant, it tells applications 
	 * which asset management system to contact concerning this document.
	 * 
	 */
	static DataSet.DataSetSerializer XmpMmManager = 
		new DataSet.DataSetSerializer(XMPMM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpMM:Manager>" + dataSets.get(0).getValue() + 
					"</xmpMM:Manager>";
		};
	};
	
	/**
	 * xmpMM:ManageTo serializer
	 * 
	 * A URI identifying the managed resource to the asset management system; 
	 * the presence of this property is the formal indication 
	 * that this resource is managed. 
	 * The form and content of this URI is private to the asset management system.
	 * 
	 */
	static DataSet.DataSetSerializer XmpMmManageTo = 
		new DataSet.DataSetSerializer(XMPMM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpMM:ManageTo>" + dataSets.get(0).getValue() + 
					"</xmpMM:ManageTo>";
		};
	};
	
	/**
	 * xmpMM:ManageUI serializer
	 * 
	 * A URI that can be used to access information about the managed resource 
	 * through a web browser. It might require a custom browser plug-in.
	 * 
	 */
	static DataSet.DataSetSerializer XmpMmManageUI = 
		new DataSet.DataSetSerializer(XMPMM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpMM:ManageUI>" + dataSets.get(0).getValue() + 
				"</xmpMM:ManageUI>";
		};
	};
	
	/**
	 * xmpMM:ManagerVariant serializer
	 * 
	 * Specifies a particular variant of the asset management system. 
	 * The format of this property is private to the specific 
	 * asset management system.
	 * 
	 */
	static DataSet.DataSetSerializer XmpMmManagerVariant = 
		new DataSet.DataSetSerializer(XMPMM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpMM:ManagerVariant>" + dataSets.get(0).getValue() + 
				"</xmpMM:ManagerVariant>";
		};
	};
	
	/**
	 * xmpMM:OriginalDocumentID serializer
	 * 
	 * The common identifier for all versions and renditions of a document.
	 * 
	 */
	static DataSet.DataSetSerializer XmpMmOriginalDocumentId = 
		new DataSet.DataSetSerializer(XMPMM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpMM:OriginalDocumentID>" + dataSets.get(0).getValue() + 
					"</xmpMM:OriginalDocumentID>";
		};
	};
	
	/**
	 * xmpMM:Pantry serializer
	 * 
	 * Each array item is a struct with a potentially unique set of fields, 
	 * containing the full XMP from a component. 
	 * Each field is a top level property from the XMP of a contained document 
	 * component, with all substructure preserved. 
	 * 
	 * Each pantry entry must contain an xmpMM:InstanceID. 
	 * Only one copy of the pantry entry for any given instance ID 
	 * should be retained in the pantry. 
	 * 
	 * Nested pantry items are removed from the individual pantry item 
	 * and promoted to the top level of the pantry.
	 * 
	 */
	static DataSet.DataSetSerializer XmpMmPantry = 
		new DataSet.DataSetSerializer(XMPMM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpMM:Pantry>" + rdfBag(dataSets) + 
					"</xmpMM:Pantry>";
		};
	};
	
	/**
	 * xmpMM:RenditionClass serializer
	 * 
	 * The rendition class name for this resource. 
	 * This property should be absent or set to default 
	 * for a document version that is not a derived rendition.
	 * 
	 */
	static DataSet.DataSetSerializer XmpMmRenditionClass = 
		new DataSet.DataSetSerializer(XMPMM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpMM:RenditionClass>" + dataSets.get(0).getValue() + 
					"</xmpMM:RenditionClass>";
		};
	};
	
	/**
	 * xmpMM:RenditionParams serializer
	 * 
	 * Can be used to provide additional rendition parameters 
	 * that are too complex or verbose to encode in xmpMM:RenditionClass.
	 * 
	 */
	static DataSet.DataSetSerializer XmpMmRenditionParams = 
		new DataSet.DataSetSerializer(XMPMM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpMM:RenditionParams>" + dataSets.get(0).getValue() + 
					"</xmpMM:RenditionParams>";
		};
	};
	
	/**
	 * xmpMM:VersionID serializer
	 * 
	 * The document version identifier for this resource. 
	 * 
	 * Each version of a document gets a new identifier, 
	 * usually simply by incrementing integers 1, 2, 3 . . . and so on. 
	 * Media management systems can have other conventions or support 
	 * branching which requires a more complex scheme.
	 * 
	 */
	static DataSet.DataSetSerializer XmpMmVersionId = 
		new DataSet.DataSetSerializer(XMPMM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpMM:VersionID>" + dataSets.get(0).getValue() + 
					"</xmpMM:VersionID>";
		};
	};
	
	/**
	 * xmpMM:Versions serializer
	 * 
	 * The version history associated with this resource. 
	 * Entry[1] is the oldest known version for this document, 
	 * entry [last()] is the most recent version. 
	 * 
	 * Typically, a media management system would fill in 
	 * the version information in the metadata on check-in. 
	 * 
	 * It is not guaranteed that a complete history of versions 
	 * from the first to this one will be present in the 
	 * xmpMM:Versions property. Interior version information 
	 * can be compressed or eliminated and the version history 
	 * can be truncated at some point.
	 * 
	 */
	static DataSet.DataSetSerializer XmpMmVersions = 
		new DataSet.DataSetSerializer(XMPMM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpMM:Versions>" + rdfSeq(dataSets) + 
					"</xmpMM:Versions>";
		};
	};
	
	/**
	 * xmpMM:LastURL serializer
	 * 
	 * Deprecated for privacy protection.
	 * 
	 */
	@Deprecated
	static DataSet.DataSetSerializer XmpMmLastUrl = 
		new DataSet.DataSetSerializer(XMPMM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpMM:LastURL>" + dataSets.get(0).getValue() + 
					"</xmpMM:LastURL>";
		};
	};
	
	/**
	 * xmpMM:RenditionOf serializer
	 * 
	 * Deprecated in favor of xmpMM:DerivedFrom. 
	 * A reference to the document of which this is a rendition.
	 * 
	 */
	@Deprecated
	static DataSet.DataSetSerializer XmpMmRenditionOf = 
		new DataSet.DataSetSerializer(XMPMM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpMM:RenditionOf>" + dataSets.get(0).getValue() + 
					"</xmpMM:RenditionOf>";
		};
	};
	
	/**
	 * xmpMM:SaveID serializer
	 * 
	 * Deprecated. Previously used only to support the xmpMM:LastURL property.
	 * 
	 */
	@Deprecated
	static DataSet.DataSetSerializer XmpMmSaveId = 
		new DataSet.DataSetSerializer(XMPMM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpMM:SaveID>" + dataSets.get(0).getValue() + 
					"</xmpMM:SaveID>";
		};
	};
	
	//XMP Basic Job Ticket schema
	
	/**
	 * xmpBJ:JobRef serializer
	 * 
	 * References an external job management file for a job process 
	 * in which the document is being used. Use of job names is under 
	 * user control. Typical use would be to identify all documents 
	 * that are part of a particular job or contract. 
	 * 
	 * There are multiple values because there can be more than one 
	 * job using a particular document at any time, 
	 * and it can also be useful to keep historical information 
	 * about what jobs a document was part of previously.
	 * 
	 */
	static DataSet.DataSetSerializer XmpBjJobRef = 
		new DataSet.DataSetSerializer(XMPBJ_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpBJ:JobRef>" + rdfBag(dataSets) + "</xmpBJ:JobRef>";
		};
	};
	
	//XMP Paged-text schema
	
	/**
	 * xmpTPg:MaxPageSize serializer
	 * 
	 * The size of the largest page in the document 
	 * (including any in contained documents).
	 * 
	 */
	static DataSet.DataSetSerializer XmpTpgMaxPageSize = 
		new DataSet.DataSetSerializer(XMPTPG_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpTPg:MaxPageSize>" + dataSets.get(0).getValue() + 
					"</xmpTPg:MaxPageSize>";
		};
	};
	
	/**
	 * xmpTPg:NPages serializer
	 * 
	 * The number of pages in the document 
	 * (including any in contained documents).
	 * 
	 */
	static DataSet.DataSetSerializer XmpTpgNPages = 
		new DataSet.DataSetSerializer(XMPTPG_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpTPg:NPages>" + dataSets.get(0).getValue() + 
					"</xmpTPg:NPages>";
		};
	};
	
	/**
	 * xmpTPg:Fonts serializer
	 * 
	 * An unordered array of fonts that are used in the document 
	 * (including any in contained documents).
	 * 
	 */
	static DataSet.DataSetSerializer XmpTpgFonts = 
		new DataSet.DataSetSerializer(XMPTPG_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpTPg:Fonts>" + rdfBag(dataSets) + "</xmpTPg:Fonts>";
		};
	};
	
	/**
	 * xmpTPg:Colorants serializer
	 * 
	 * An ordered array of colorants (swatches) that are used in the document 
	 * (including any in contained documents).
	 * 
	 */
	static DataSet.DataSetSerializer XmpTpgColorants = 
		new DataSet.DataSetSerializer(XMPTPG_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpTPg:Colorants>" + rdfSeq(dataSets) + 
					"</xmpTPg:Colorants>";
		};
	};
	
	/**
	 * xmpTPg:PlateNames serializer
	 * 
	 * An ordered array of plate names that are needed to print the document 
	 * (including any in contained documents).
	 * 
	 */
	static DataSet.DataSetSerializer XmpTpgPlateNames = 
		new DataSet.DataSetSerializer(XMPTPG_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpTPg:PlateNames>" + rdfSeq(dataSets) + 
				"</xmpTPg:PlateNames>";
		};
	};
	
	//XMP Dynamic Media schema
	
	/**
	 * xmpDM:absPeakAudioFilePath serializer
	 * 
	 * The absolute path to the file’s peak audio file. 
	 * If empty, no peak file exists.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmAbsPeakAudioFilePath = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:absPeakAudioFilePath>" + dataSets.get(0).getValue() + 
					"</xmpDM:absPeakAudioFilePath>";
		};
	};
	
	/**
	 * xmpDM:album serializer
	 * 
	 * The name of the album.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmAlbum = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:album>" + dataSets.get(0).getValue() + 
					"</xmpDM:album>";
		};
	};
	
	/**
	 * xmpDM:altTapeName serializer
	 * 
	 * An alternative tape name, set via the project window 
	 * or timecode dialog in Premiere. If an alternative name 
	 * has been set and has not been reverted, that name is displayed.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmAltTapeName = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:altTapeName>" + dataSets.get(0).getValue() + 
					"</xmpDM:altTapeName>";
		};
	};
	
	/**
	 * xmpDM:altTimecode serializer
	 * 
	 * A timecode set by the user. 
	 * When specified, it is used instead of the startTimecode.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmAltTimeCode = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:altTimecode>" + dataSets.get(0).getValue() + 
					"</xmpDM:altTimecode>";
		};
	};
	
	/**
	 * xmpDM:artist serializer
	 * 
	 * The name of the artist or artists.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmArtist = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:artist>" + dataSets.get(0).getValue() + 
					"</xmpDM:artist>";
		};
	};
	
	/**
	 * xmpDM:audioModDate serializer
	 * 
	 * The date and time when the audio was last modified.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmAudioModDate = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:audioModDate>" + dataSets.get(0).getValue() + 
				"</xmpDM:audioModDate>";
		};
	};
	
	/**
	 * xmpDM:audioSampleRate serializer
	 * 
	 * The audio sample rate. Can be any value, 
	 * but commonly 32000, 41100, or 48000.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmAudioSampleRate = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:audioSampleRate>" + dataSets.get(0).getValue() + 
					"</xmpDM:audioSampleRate>";
		};
	};
	
	/**
	 * xmpDM:audioSampleType serializer
	 * 
	 * The audio sample type. One of: 8Int, 16Int, 32Int, 32Float
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmAudioSampleType = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:audioSampleType>" + dataSets.get(0).getValue() + 
					"</xmpDM:audioSampleType>";
		};
	};
	
	/**
	 * xmpDM:audioChannelType serializer
	 * 
	 * The audio channel type. One of: Mono, Stereo, 5.1, 7.1
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmAudioChannelType = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:audioChannelType>" + dataSets.get(0).getValue() + 
					"</xmpDM:audioChannelType>";
		};
	};
	
	/**
	 * xmpDM:audioCompressor serializer
	 * 
	 * The audio compression used. For example, MP3.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmAudioCompressor = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:audioCompressor>" + dataSets.get(0).getValue() + 
					"</xmpDM:audioCompressor>";
		};
	};
	
	/**
	 * xmpDM:beatSpliceParams serializer
	 * 
	 * Additional parameters for BeatSplice stretch mode.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmBeatSpliceParams = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:beatSpliceParams>" + dataSets.get(0).getValue() + 
					"</xmpDM:beatSpliceParams>";
		};
	};
	
	/**
	 * xmpDM:composer serializer
	 * 
	 * The composer’s name.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmComposer = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:composer>" + dataSets.get(0).getValue() + 
					"</xmpDM:composer>";
		};
	};
	
	/**
	 * xmpDM:contributedMedia serializer
	 * 
	 * An unordered list of all media used to create this media.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmContributedMedia = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:contributedMedia>" + rdfBag(dataSets) + 
					"</xmpDM:contributedMedia>";
		};
	};
	
	/**
	 * xmpDM:copyright serializer
	 * 
	 * The copyright information.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmCopyright = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:copyright>" + dataSets.get(0).getValue() + 
					"</xmpDM:copyright>";
		};
	};
	
	/**
	 * xmpDM:duration serializer
	 * 
	 * The duration of the media file.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmDuration = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:duration>" + dataSets.get(0).getValue() + 
					"</xmpDM:duration>";
		};
	};
	
	/**
	 * xmpDM:engineer serializer
	 * 
	 * The engineer’s name.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmEngineer = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:engineer>" + dataSets.get(0).getValue() + 
					"</xmpDM:engineer>";
		};
	};
	
	/**
	 * xmpDM:fileDataRate serializer
	 * 
	 * The file data rate in megabytes per second. 
	 * For example: “36/10” = 3.6 MB/sec
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmFileDataRate = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:fileDataRate>" + dataSets.get(0).getValue() + 
					"</xmpDM:fileDataRate>";
		};
	};
	
	/**
	 * xmpDM:genre serializer
	 * 
	 * The name of the genre.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmGenre = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:genre>" + dataSets.get(0).getValue() + 
					"</xmpDM:genre>";
		};
	};
	
	/**
	 * xmpDM:instrument serializer
	 * 
	 * The musical instrument.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmInstrument = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:instrument>" + dataSets.get(0).getValue() + 
				"</xmpDM:instrument>";
		};
	};
	
	/**
	 * xmpDM:introTime serializer
	 * 
	 * The duration of lead time for queuing music.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmIntroTime = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:introTime>" + dataSets.get(0).getValue() + 
					"</xmpDM:introTime>";
		};
	};
	
	/**
	 * xmpDM:key serializer
	 * 
	 * The audio’s musical key. 
	 * One of: C, C#, D, D#, E, F, F#, G, G#, A, A#, B
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmKey = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:key>" + dataSets.get(0).getValue() + 
					"</xmpDM:key>";
		};
	};
	
	/**
	 * xmpDM:logComment serializer
	 * 
	 * User’s log comments.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmLongComment = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:logComment>" + dataSets.get(0).getValue() + 
					"</xmpDM:logComment>";
		};
	};
	
	/**
	 * xmpDM:loop serializer
	 * 
	 * When true, the clip can be looped seamlessly.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmLoop = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:loop>" + dataSets.get(0).getValue() + 
					"</xmpDM:loop>";
		};
	};
	
	/**
	 * xmpDM:numberOfBeats serializer
	 * 
	 * The number of beats.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmNumberOfBeats = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:numberOfBeats>" + dataSets.get(0).getValue() + 
					"</xmpDM:numberOfBeats>";
		};
	};
	
	/**
	 * xmpDM:markers serializer
	 * 
	 * An ordered list of markers. See also xmpDM:Tracks.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmMarkers = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:markers>" + rdfSeq(dataSets) + 
					"</xmpDM:markers>";
		};
	};
	
	/**
	 * xmpDM:metadataModDate serializer
	 * 
	 * The date and time when the metadata was last modified.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmMetadataModDate = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:metadataModDate>" + dataSets.get(0).getValue() + 
					"</xmpDM:metadataModDate>";
		};
	};
	
	/**
	 * xmpDM:outCue serializer
	 * 
	 * The time at which to fade out.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmOutCue = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:outCue>" + dataSets.get(0).getValue() + 
					"</xmpDM:outCue>";
		};
	};
	
	/**
	 * xmpDM:projectRef serializer
	 * 
	 * A reference to the project that created this file.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmProjectRef = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:projectRef>" + dataSets.get(0).getValue() + 
					"</xmpDM:projectRef>";
		};
	};
	
	/**
	 * xmpDM:pullDown serializer
	 * 
	 * The sampling phase of film to be converted to video (pull-down). 
	 * One of: WSSWW, SSWWW, SWWWS, WWWSS, WWSSW, WSSWW_24p, SSWWW_24p, 
	 * SWWWS_24p, WWWSS_24p, WWSSW_24p
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmPullDown = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:pullDown>" + dataSets.get(0).getValue() + 
					"</xmpDM:pullDown>";
		};
	};
	
	/**
	 * xmpDM:relativePeakAudioFilePath serializer
	 * 
	 * The relative path to the file’s peak audio file. 
	 * If empty, no peak file exists.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmRelativePeakAudioFilePath = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:relativePeakAudioFilePath>" + 
					dataSets.get(0).getValue() + 
					"</xmpDM:relativePeakAudioFilePath>";
		};
	};
	
	/**
	 * xmpDM:relativeTimestamp serializer
	 * 
	 * The start time of the media inside the audio project.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmRelativeTimestamp = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:relativeTimestamp>" + dataSets.get(0).getValue() + 
			"</xmpDM:relativeTimestamp>";
		};
	};
	
	/**
	 * xmpDM:releaseDate serializer
	 * 
	 * The date the title was released.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmReleaseDate = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:releaseDate>" + dataSets.get(0).getValue() + 
					"</xmpDM:releaseDate>";
		};
	};
	
	/**
	 * xmpDM:resampleParams serializer
	 * 
	 * Additional parameters for Resample stretch mode.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmResampleParams = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:resampleParams>" + dataSets.get(0).getValue() + 
				"</xmpDM:resampleParams>";
		};
	};
	
	/**
	 * xmpDM:scaleType serializer
	 * 
	 * The musical scale used in the music. 
	 * 
	 * One of: Major, Minor, Both, Neither 
	 * 
	 * Neither is most often used for instruments with no associated scale, 
	 * such as drums.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmScaleType = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:scaleType>" + dataSets.get(0).getValue() + 
					"</xmpDM:scaleType>";
		};
	};
	
	/**
	 * xmpDM:scene serializer
	 * 
	 * The name of the scene.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmScene = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:scene>" + dataSets.get(0).getValue() + 
					"</xmpDM:scene>";
		};
	};
	
	/**
	 * xmpDM:shotDate serializer
	 * 
	 * The date and time when the video was shot.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmShotDate = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:shotDate>" + dataSets.get(0).getValue() + 
					"</xmpDM:shotDate>";
		};
	};
	
	/**
	 * xmpDM:shotLocation serializer
	 * 
	 * The name of the location where the video was shot. 
	 * For example: “Oktoberfest, Munich Germany” 
	 * 
	 * For more accurate positioning, use the EXIF GPS values.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmShotLocation = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:shotLocation>" + dataSets.get(0).getValue() + 
			"</xmpDM:shotLocation>";
		};
	};
	
	/**
	 * xmpDM:shotName serializer
	 * 
	 * The name of the shot or take.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmShotName = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:shotName>" + dataSets.get(0).getValue() + 
					"</xmpDM:shotName>";
		};
	};
	
	/**
	 * xmpDM:speakerPlacement serializer
	 * 
	 * A description of the speaker angles from center front in degrees. 
	 * 
	 * For example: “Left = -30, Right = 30, Center = 0, 
	 * LFE = 45, Left Surround = -110, Right Surround = 110”
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmSpeakerPlacement = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:speakerPlacement>" + dataSets.get(0).getValue() + 
					"</xmpDM:speakerPlacement>";
		};
	};
	
	/**
	 * xmpDM:startTimecode serializer
	 * 
	 * The timecode of the first frame of video in the file, 
	 * as obtained from the device control.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmStartTimecode = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:startTimecode>" + dataSets.get(0).getValue() + 
					"</xmpDM:startTimecode>";
		};
	};
	
	/**
	 * xmpDM:stretchMode serializer
	 * 
	 * The audio stretch mode. 
	 * One of: Fixed, length, Time-Scale, Resample, Beat Splice, Hybrid
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmStretchMode = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:stretchMode>" + dataSets.get(0).getValue() + 
					"</xmpDM:stretchMode>";
		};
	};
	
	/**
	 * xmpDM:tapeName serializer
	 * 
	 * The name of the tape from which the clip was captured, 
	 * as set during the capture process.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmTapeName = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:tapeName>" + dataSets.get(0).getValue() + 
				"</xmpDM:tapeName>";
		};
	};
	
	/**
	 * xmpDM:tempo serializer
	 * 
	 * The audio’s tempo.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmTempo = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:tempo>" + dataSets.get(0).getValue() + 
					"</xmpDM:tempo>";
		};
	};
	
	/**
	 * xmpDM:timeScaleParams serializer
	 * 
	 * Additional parameters for Time-Scale stretch mode.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmTimeScaleParams = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:timeScaleParams>" + dataSets.get(0).getValue() + 
					"</xmpDM:timeScaleParams>";
		};
	};
	
	/**
	 * xmpDM:timeSignature serializer
	 * 
	 * The time signature of the music. 
	 * One of: 2/4, 3/4, 4/4, 5/4, 7/4, 6/8, 9/8, 12/8, other
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmTimeSignature = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:timeSignature>" + dataSets.get(0).getValue() + 
					"</xmpDM:timeSignature>";
		};
	};
	
	/**
	 * xmpDM:trackNumber serializer
	 * 
	 * A numeric value indicating the order 
	 * of the audio file within its original recording.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmTrackNumber = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:trackNumber>" + dataSets.get(0).getValue() + 
					"</xmpDM:trackNumber>";
		};
	};
	
	/**
	 * xmpDM:Tracks serializer
	 * 
	 * An unordered list of tracks. 
	 * A track is a named set of markers, which can specify 
	 * a frame rate for all markers in the set. 
	 * 
	 * See also xmpDM:markers.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmTracks = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:Tracks>" + rdfBag(dataSets) + "</xmpDM:Tracks>";
		};
	};
	
	/**
	 * xmpDM:videoAlphaMode serializer
	 * 
	 * The alpha mode. 
	 * 
	 * One of: straight, pre-multiplied
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmVideoAlphaMode = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:videoAlphaMode>" + dataSets.get(0).getValue() + 
					"</xmpDM:videoAlphaMode>";
		};
	};
	
	/**
	 * xmpDM:videoAlphaPremultipleColor serializer
	 * 
	 * A color in CMYK or RGB to be used as the pre-multiple color 
	 * when alpha mode is pre-multiplied.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmVideoAlphaPremultipleColor = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:videoAlphaPremultipleColor>" + 
					dataSets.get(0).getValue() + 
					"</xmpDM:videoAlphaPremultipleColor>";
		};
	};
	
	/**
	 * xmpDM:videoAlphaUnityIsTransparent serializer
	 * 
	 * When true, unity is clear, when false, it is opaque.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmVideoAlphaUnityIsTransparent = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:videoAlphaUnityIsTransparent>" + 
					dataSets.get(0).getValue() + 
					"</xmpDM:videoAlphaUnityIsTransparent>";
		};
	};
	
	/**
	 * xmpDM:videoColorSpace serializer
	 * 
	 * The color space. 
	 * One of: sRGB (used by Photoshop), CCIR-601 (used for NTSC), 
	 * CCIR-709 (used for HD)
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmVideoColorSpace = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:videoColorSpace>" + dataSets.get(0).getValue() + 
					"</xmpDM:videoColorSpace>";
		};
	};
	
	/**
	 * xmpDM:videoCompressor serializer
	 * 
	 * Video compression used. For example, jpeg.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmVideoCompressor = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:videoCompressor>" + dataSets.get(0).getValue() + 
					"</xmpDM:videoCompressor>";
		};
	};
	
	/**
	 * xmpDM:videoFieldOrder serializer
	 * 
	 * The field order for video. 
	 * One of: Upper, Lower, Progressive
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmVideoFieldOrder = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:videoFieldOrder>" + dataSets.get(0).getValue() + 
					"</xmpDM:videoFieldOrder>";
		};
	};
	
	/**
	 * xmpDM:videoFrameRate serializer
	 * 
	 * The video frame rate. 
	 * One of: 24, NTSC, PAL
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmVideoFrameRate = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:videoFrameRate>" + dataSets.get(0).getValue() + 
					"</xmpDM:videoFrameRate>";
		};
	};
	
	/**
	 * xmpDM:videoFrameSize serializer
	 * 
	 * The frame size. 
	 * For example: w:720, h: 480, unit:pixels
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmVideoFrameSize = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:videoFrameSize>" + dataSets.get(0).getValue() + 
					"</xmpDM:videoFrameSize>";
		};
	};
	
	/**
	 * xmpDM:videoModDate serializer
	 * 
	 * The date and time when the video was last modified.
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmVideoModDate = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:videoModDate>" + dataSets.get(0).getValue() + 
					"</xmpDM:videoModDate>";
		};
	};
	
	/**
	 * xmpDM:videoPixelDepth serializer
	 * 
	 * The size in bits of each color component of a pixel. 
	 * Standard Windows 32-bit pixels have 8 bits per component. 
	 * 
	 * One of: 8Int, 16Int, 32Int, 32Float
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmVideoPixelDepth = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:videoPixelDepth>" + dataSets.get(0).getValue() + 
					"</xmpDM:videoPixelDepth>";
		};
	};
	
	/**
	 * xmpDM:videoPixelAspectRatio serializer
	 * 
	 * The aspect ratio, expressed as wd/ht. 
	 * 
	 * For example: “648/720” = 0.9
	 * 
	 */
	static DataSet.DataSetSerializer XmpDmVideoPixelAspectRatio = 
		new DataSet.DataSetSerializer(XMPDM_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<xmpDM:videoPixelAspectRatio>" + dataSets.get(0).getValue() + 
					"</xmpDM:videoPixelAspectRatio>";
		};
	};
	
	//Adobe PDF schema
	
	/**
	 * pdf:Keywords serializer
	 * 
	 * Keywords.
	 * 
	 */
	static DataSet.DataSetSerializer PdfKeywords = 
		new DataSet.DataSetSerializer(PDF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<pdf:Keywords>" + dataSets.get(0).getValue() + 
					"</pdf:Keywords>";
		};
	};
	
	/**
	 * pdf:PDFVersion serializer
	 * 
	 * The PDF file version (for example: 1.0, 1.3, and so on).
	 * 
	 */
	static DataSet.DataSetSerializer PdfVersion = 
		new DataSet.DataSetSerializer(PDF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<pdf:PDFVersion>" + dataSets.get(0).getValue() + 
					"</pdf:PDFVersion>";
		};
	};
	
	/**
	 * pdf:Producer serializer
	 * 
	 * The name of the tool that created the PDF document.
	 * 
	 */
	static DataSet.DataSetSerializer PdfProducer = 
		new DataSet.DataSetSerializer(PDF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<pdf:Producer>" + dataSets.get(0).getValue() + 
					"</pdf:Producer>";
		};
	};
	
	//Photoshop schema
	
	/**
	 * photoshop:AuthorsPosition serializer
	 * 
	 * By-line title.
	 * 
	 */
	static DataSet.DataSetSerializer PhotoshopAuthorsPosition = 
		new DataSet.DataSetSerializer(PHOTOSHOP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<photoshop:AuthorsPosition>" + dataSets.get(0).getValue() + 
					"</photoshop:AuthorsPosition>";
		};
	};
	
	/**
	 * photoshop:CaptionWriter serializer
	 * 
	 * Writer/editor.
	 * 
	 */
	static DataSet.DataSetSerializer PhotoshopCaptionWriter = 
		new DataSet.DataSetSerializer(PHOTOSHOP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<photoshop:CaptionWriter>" + dataSets.get(0).getValue() + 
					"</photoshop:CaptionWriter>";
		};
	};
	
	/**
	 * photoshop:Category serializer
	 * 
	 * Category. 
	 * 
	 * Limited to 3 7-bit ASCII characters.
	 * 
	 */
	static DataSet.DataSetSerializer PhotoshopCategory = 
		new DataSet.DataSetSerializer(PHOTOSHOP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<photoshop:Category>" + dataSets.get(0).getValue() + 
					"</photoshop:Category>";
		};
	};
	
	/**
	 * photoshop:City serializer
	 * 
	 * City.
	 * 
	 */
	static DataSet.DataSetSerializer PhotoshopCity = 
		new DataSet.DataSetSerializer(PHOTOSHOP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<photoshop:City>" + dataSets.get(0).getValue() + 
					"</photoshop:City>";
		};
	};
	
	/**
	 * photoshop:Country serializer
	 * 
	 * Country/primary location.
	 * 
	 */
	static DataSet.DataSetSerializer PhotoshopCountry = 
		new DataSet.DataSetSerializer(PHOTOSHOP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<photoshop:Country>" + dataSets.get(0).getValue() + 
					"</photoshop:Country>";
		};
	};
	
	/**
	 * photoshop:Credit serializer
	 * 
	 * Credit.
	 * 
	 */
	static DataSet.DataSetSerializer PhotoshopCredit = 
		new DataSet.DataSetSerializer(PHOTOSHOP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<photoshop:Credit>" + dataSets.get(0).getValue() + 
					"</photoshop:Credit>";
		};
	};
	
	/**
	 * photoshop:DateCreated serializer
	 * 
	 * The date the intellectual content of the document was created 
	 * (rather than the creation date of the physical representation), 
	 * following IIM conventions. 
	 * 
	 * For example, a photo taken during the American Civil War 
	 * would have a creation date during that epoch (1861-1865) 
	 * rather than the date the photo was digitized for archiving.
	 * 
	 */
	static DataSet.DataSetSerializer PhotoshopDateCreated = 
		new DataSet.DataSetSerializer(PHOTOSHOP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<photoshop:DateCreated>" + dataSets.get(0).getValue() + 
					"</photoshop:DateCreated>";
		};
	};
	
	/**
	 * photoshop:Headline serializer
	 * 
	 * Headline.
	 * 
	 */
	static DataSet.DataSetSerializer PhotoshopHeadline = 
		new DataSet.DataSetSerializer(PHOTOSHOP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<photoshop:Headline>" + dataSets.get(0).getValue() + 
					"</photoshop:Headline>";
		};
	};
	
	/**
	 * photoshop:Instructions serializer
	 * 
	 * Special instructions.
	 * 
	 */
	static DataSet.DataSetSerializer PhotoshopInstructions = 
		new DataSet.DataSetSerializer(PHOTOSHOP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<photoshop:Instructions>" + dataSets.get(0).getValue() + 
					"</photoshop:Instructions>";
		};
	};
	
	/**
	 * photoshop:Source serializer
	 * 
	 * Source.
	 * 
	 */
	static DataSet.DataSetSerializer PhotoshopSource = 
		new DataSet.DataSetSerializer(PHOTOSHOP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<photoshop:Source>" + dataSets.get(0).getValue() + 
					"</photoshop:Source>";
		};
	};
	
	/**
	 * photoshop:State serializer
	 * 
	 * Province/state.
	 * 
	 */
	static DataSet.DataSetSerializer PhotoshopState = 
		new DataSet.DataSetSerializer(PHOTOSHOP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<photoshop:State>" + dataSets.get(0).getValue() + 
					"</photoshop:State>";
		};
	};

	/**
	 * photoshop:SupplementalCategories serializer
	 * 
	 * Supplemental category.
	 * 
	 */
	static DataSet.DataSetSerializer PhotoshopSupplementalCategories = 
		new DataSet.DataSetSerializer(PHOTOSHOP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<photoshop:SupplementalCategories>" + rdfBag(dataSets) + 
					"</photoshop:SupplementalCategories>";
		};
	};

	/**
	 * photoshop:TransmissionReference serializer
	 * 
	 * Original transmission reference.
	 * 
	 */
	static DataSet.DataSetSerializer PhotoshopTransmissionReference = 
		new DataSet.DataSetSerializer(PHOTOSHOP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<photoshop:TransmissionReference>" + 
					dataSets.get(0).getValue() + 
					"</photoshop:TransmissionReference>";
		};
	};
	
	/**
	 * photoshop:Urgency serializer
	 * 
	 * Urgency. Valid range is 1-8.
	 * 
	 */
	static DataSet.DataSetSerializer PhotoshopUrgency = 
		new DataSet.DataSetSerializer(PHOTOSHOP_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<photoshop:Urgency>" + dataSets.get(0).getValue() + 
					"</photoshop:Urgency>";
		};
	};
	
	//Camera Raw schema
	
	/**
	 * crs:AutoBrightness serializer
	 * 
	 * When true, "Brightness" is automatically adjusted.
	 * 
	 */
	static DataSet.DataSetSerializer CrsAutoBrightness = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:AutoBrightness>" + dataSets.get(0).getValue() + 
					"</crs:AutoBrightness>";
		};
	};
	
	/**
	 * crs:AutoContrast serializer
	 * 
	 * When true, "Contrast" is automatically adjusted.
	 * 
	 */
	static DataSet.DataSetSerializer CrsAutoContrast = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:AutoContrast>" + dataSets.get(0).getValue() + 
					"</crs:AutoContrast>";
		};
	};
	
	/**
	 * crs:AutoExposure serializer
	 * 
	 * When true, "Exposure" is automatically adjusted.
	 * 
	 */
	static DataSet.DataSetSerializer CrsAutoExposure = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:AutoExposure>" + dataSets.get(0).getValue() + 
					"</crs:AutoExposure>";
		};
	};

	/**
	 * crs:AutoShadows serializer
	 * 
	 * When true, "Shadows" is automatically adjusted.
	 * 
	 */
	static DataSet.DataSetSerializer CrsAutoShadows = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:AutoShadows>" + dataSets.get(0).getValue() + 
					"</crs:AutoShadows>";
		};
	};
	
	/**
	 * crs:BlueHue serializer
	 * 
	 * "Blue Hue" setting. Range -100 to 100.
	 * 
	 */
	static DataSet.DataSetSerializer CrsBlueHue = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:BlueHue>" + dataSets.get(0).getValue() + 
					"</crs:BlueHue>";
		};
	};
	
	/**
	 * crs:BlueSaturation serializer
	 * 
	 * "Blue Saturation" setting. Range -100 to 100.
	 * 
	 */
	static DataSet.DataSetSerializer CrsBlueSaturation = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:BlueSaturation>" + dataSets.get(0).getValue() + 
					"</crs:BlueSaturation>";
		};
	};
	
	/**
	 * crs:Brightness serializer
	 * 
	 * "Brightness" setting. Range 0 to 150.
	 * 
	 */
	static DataSet.DataSetSerializer CrsBrightness = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:Brightness>" + dataSets.get(0).getValue() + 
					"</crs:Brightness>";
		};
	};
	
	/**
	 * crs:CameraProfile serializer
	 * 
	 * "Camera Profile" setting.
	 * 
	 */
	static DataSet.DataSetSerializer CrsCameraProfile = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:CameraProfile>" + dataSets.get(0).getValue() + 
					"</crs:CameraProfile>";
		};
	};
	
	/**
	 * crs:ChromaticAberrationB serializer
	 * 
	 * "Chromatic Aberration, Fix Blue/Yellow Fringe" setting. 
	 * Range -100 to 100.
	 * 
	 */
	static DataSet.DataSetSerializer CrsChromaticAberrationB = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:ChromaticAberrationB>" + dataSets.get(0).getValue() + 
					"</crs:ChromaticAberrationB>";
		};
	};
	
	/**
	 * crs:ChromaticAberrationR serializer
	 * 
	 * "Chromatic Aberration, Fix Red/Cyan Fringe" setting. 
	 * Range -100 to 100.
	 * 
	 */
	static DataSet.DataSetSerializer CrsChromaticAberrationR = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:ChromaticAberrationR>" + dataSets.get(0).getValue() + 
					"</crs:ChromaticAberrationR>";
		};
	};
	
	/**
	 * crs:ColorNoiseReduction serializer
	 * 
	 * "Color Noise Reduction" setting. Range 0 to 100.
	 * 
	 */
	static DataSet.DataSetSerializer CrsColorNoiseReduction = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:ColorNoiseReduction>" + dataSets.get(0).getValue() + 
					"</crs:ColorNoiseReduction>";
		};
	};
	
	/**
	 * crs:Contrast serializer
	 * 
	 * "Contrast" setting. Range -50 to 100.
	 * 
	 */
	static DataSet.DataSetSerializer CrsContrast = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:Contrast>" + dataSets.get(0).getValue() + 
					"</crs:Contrast>";
		};
	};
	
	/**
	 * crs:CropTop serializer
	 * 
	 * When HasCrop is true, top of crop rectangle.
	 * 
	 */
	static DataSet.DataSetSerializer CrsCropTop = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:CropTop>" + dataSets.get(0).getValue() + 
					"</crs:CropTop>";
		};
	};
	
	/**
	 * crs:CropLeft serializer
	 * 
	 * When HasCrop is true, left of crop rectangle.
	 * 
	 */
	static DataSet.DataSetSerializer CrsCropLeft = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:CropLeft>" + dataSets.get(0).getValue() + 
					"</crs:CropLeft>";
		};
	};
	
	/**
	 * crs:CropBottom serializer
	 * 
	 * When HasCrop is true, bottom of crop rectangle.
	 * 
	 */
	static DataSet.DataSetSerializer CrsCropBottom = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:CropBottom>" + dataSets.get(0).getValue() + 
					"</crs:CropBottom>";
		};
	};
	
	/**
	 * crs:CropRight serializer
	 * 
	 * When HasCrop is true, right of crop rectangle.
	 * 
	 */
	static DataSet.DataSetSerializer CrsCropRight = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:CropRight>" + dataSets.get(0).getValue() + 
					"</crs:CropRight>";
		};
	};
	
	/**
	 * crs:CropAngle serializer
	 * 
	 * When HasCrop is true, angle of crop rectangle.
	 * 
	 */
	static DataSet.DataSetSerializer CrsCropAngle = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:CropAngle>" + dataSets.get(0).getValue() + 
					"</crs:CropAngle>";
		};
	};
	
	/**
	 * crs:CropWidth serializer
	 * 
	 * Width of resulting cropped image in CropUnits units.
	 * 
	 */
	static DataSet.DataSetSerializer CrsCropWidth = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:CropWidth>" + dataSets.get(0).getValue() + 
					"</crs:CropWidth>";
		};
	};
	
	/**
	 * crs:CropHeight serializer
	 * 
	 * Height of resulting cropped image in CropUnits units.
	 * 
	 */
	static DataSet.DataSetSerializer CrsCropHeight = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:CropHeight>" + dataSets.get(0).getValue() + 
					"</crs:CropHeight>";
		};
	};
	
	/**
	 * crs:CropUnits serializer
	 * 
	 * Units for CropWidth and CropHeight. 
	 * 
	 * One of: 0 = pixels, 1 = inches, 2 = cm
	 * 
	 */
	static DataSet.DataSetSerializer CrsCropUnits = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:CropUnits>" + dataSets.get(0).getValue() + 
					"</crs:CropUnits>";
		};
	};
	
	/**
	 * crs:Exposure serializer
	 * 
	 * "Exposure" setting. Range -4.0 to 4.0.
	 * 
	 */
	static DataSet.DataSetSerializer CrsExposure = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:Exposure>" + dataSets.get(0).getValue() + 
					"</crs:Exposure>";
		};
	};
	
	/**
	 * crs:GreenHue serializer
	 * 
	 * "Green Hue" setting. Range -100 to 100.
	 * 
	 */
	static DataSet.DataSetSerializer CrsGreenHue = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:GreenHue>" + dataSets.get(0).getValue() + 
					"</crs:GreenHue>";
		};
	};
	
	/**
	 * crs:GreenSaturation serializer
	 * 
	 * "Green Saturation" setting. Range -100 to 100.
	 * 
	 */
	static DataSet.DataSetSerializer CrsGreenSaturation = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:GreenSaturation>" + dataSets.get(0).getValue() + 
					"</crs:GreenSaturation>";
		};
	};
	
	/**
	 * crs:HasCrop serializer
	 * 
	 * When true, image has a cropping rectangle.
	 * 
	 */
	static DataSet.DataSetSerializer CrsHasCrop = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:HasCrop>" + dataSets.get(0).getValue() + 
					"</crs:HasCrop>";
		};
	};
	
	/**
	 * crs:HasSettings serializer
	 * 
	 * When true, non-default camera raw settings.
	 * 
	 */
	static DataSet.DataSetSerializer CrsHasSettings = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:HasSettings>" + dataSets.get(0).getValue() + 
					"</crs:HasSettings>";
		};
	};
	
	/**
	 * crs:LuminanceSmoothing serializer
	 * 
	 * "Luminance Smoothing" setting. Range 0 to 100.
	 * 
	 */
	static DataSet.DataSetSerializer CrsLuminanceSmoothing = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:LuminanceSmoothing>" + dataSets.get(0).getValue() + 
					"</crs:LuminanceSmoothing>";
		};
	};
	
	/**
	 * crs:RawFileName serializer
	 * 
	 * File name for raw file (not a complete path).
	 * 
	 */
	static DataSet.DataSetSerializer CrsRawFileName = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:RawFileName>" + dataSets.get(0).getValue() + 
					"</crs:RawFileName>";
		};
	};
	
	/**
	 * crs:RedHue serializer
	 * 
	 * "Red Hue" setting. Range -100 to 100.
	 * 
	 */
	static DataSet.DataSetSerializer CrsRedHue = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:RedHue>" + dataSets.get(0).getValue() + 
					"</crs:RedHue>";
		};
	};
	
	/**
	 * crs:RedSaturation serializer
	 * 
	 * "Red Saturation" setting. Range -100 to 100.
	 * 
	 */
	static DataSet.DataSetSerializer CrsRedSaturation = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:RedSaturation>" + dataSets.get(0).getValue() + 
					"</crs:RedSaturation>";
		};
	};
	
	/**
	 * crs:Saturation serializer
	 * 
	 * "Saturation" setting. Range -100 to 100.
	 * 
	 */
	static DataSet.DataSetSerializer CrsSaturation = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:Saturation>" + dataSets.get(0).getValue() + 
					"</crs:Saturation>";
		};
	};
	
	/**
	 * crs:Shadows serializer
	 * 
	 * "Shadows" setting. Range 0 to 100.
	 * 
	 */
	static DataSet.DataSetSerializer CrsShadows = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:Shadows>" + dataSets.get(0).getValue() + 
					"</crs:Shadows>";
		};
	};
	
	/**
	 * crs:ShadowTint serializer
	 * 
	 * "Shadow Tint" setting. Range -100 to 100.
	 * 
	 */
	static DataSet.DataSetSerializer CrsShadowTint = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:ShadowTint>" + dataSets.get(0).getValue() + 
					"</crs:ShadowTint>";
		};
	};
	
	/**
	 * crs:Sharpness serializer
	 * 
	 * "Sharpness" setting. Range 0 to 100.
	 * 
	 */
	static DataSet.DataSetSerializer CrsSharpness = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:Sharpness>" + dataSets.get(0).getValue() + 
					"</crs:Sharpness>";
		};
	};
	
	/**
	 * crs:Temperature serializer
	 * 
	 * "Temperature" setting. Range 2000 to 50000.
	 * 
	 */
	static DataSet.DataSetSerializer CrsTemperature = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:Temperature>" + dataSets.get(0).getValue() + 
					"</crs:Temperature>";
		};
	};
	
	/**
	 * crs:Tint serializer
	 * 
	 * "Tint" setting. Range -150 to 150.
	 * 
	 */
	static DataSet.DataSetSerializer CrsTint = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:Tint>" + dataSets.get(0).getValue() + "</crs:Tint>";
		};
	};
	
	/**
	 * crs:ToneCurve serializer
	 * 
	 * Array of points (Integer, Integer) defining a “Tone Curve.”
	 * 
	 */
	static DataSet.DataSetSerializer CrsToneCurve = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:ToneCurve>" + rdfSeq(dataSets) + 
					"</crs:ToneCurve>";
		};
	};
	
	/**
	 * crs:ToneCurveName serializer
	 * 
	 * The name of the Tone Curve described by ToneCurve. 
	 * 
	 * One of: Linear, Medium Contrast, Strong Contrast, Custom, 
	 * or a user-defined preset name
	 * 
	 */
	static DataSet.DataSetSerializer CrsToneCurveName = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:ToneCurveName>" + dataSets.get(0).getValue() + 
					"</crs:ToneCurveName>";
		};
	};
	
	/**
	 * crs:Version serializer
	 * 
	 * Version of Camera Raw plug-in.
	 * 
	 */
	static DataSet.DataSetSerializer CrsVersion = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:Version>" + dataSets.get(0).getValue() + 
					"</crs:Version>";
		};
	};
	
	/**
	 * crs:VignetteAmount serializer
	 * 
	 * "Vignetting Amount" setting. Range -100 to 100.
	 * 
	 */
	static DataSet.DataSetSerializer CrsVignetteAmount = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:VignetteAmount>" + dataSets.get(0).getValue() + 
					"</crs:VignetteAmount>";
		};
	};
	
	/**
	 * crs:VignetteMidpoint serializer
	 * 
	 * "Vignetting Midpoint" setting. Range 0 to 100.
	 * 
	 */
	static DataSet.DataSetSerializer CrsVignetteMidpoint = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:VignetteMidpoint>" + dataSets.get(0).getValue() + 
					"</crs:VignetteMidpoint>";
		};
	};
	
	/**
	 * crs:WhiteBalance serializer
	 * 
	 * "White Balance" setting. 
	 * 
	 * One of: As Shot, Auto, Daylight, Cloudy, Shade, Tungsten, 
	 * Fluorescent, Flash, Custom
	 * 
	 */
	static DataSet.DataSetSerializer CrsWhiteBalance = 
		new DataSet.DataSetSerializer(CRS_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<crs:WhiteBalance>" + dataSets.get(0).getValue() + 
					"</crs:WhiteBalance>";
		};
	};
	
	//EXIF schema for TIFF properties (Version 2.2)
	
	/**
	 * tiff:ImageWidth serializer
	 * 
	 * TIFF tag 256, 0x100. Image width in pixels.
	 * 
	 */
	static DataSet.DataSetSerializer TiffImageWidth = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:ImageWidth>" + dataSets.get(0).getValue() + 
					"</tiff:ImageWidth>";
		};
	};
	
	/**
	 * tiff:ImageLength serializer
	 * 
	 * TIFF tag 257, 0x101. Image height in pixels.
	 * 
	 */
	static DataSet.DataSetSerializer TiffImageLength = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:ImageLength>" + dataSets.get(0).getValue() + 
					"</tiff:ImageLength>";
		};
	};
	
	/**
	 * tiff:BitsPerSample serializer
	 * 
	 * TIFF tag 257, 0x101. Image height in pixels.
	 * 
	 */
	static DataSet.DataSetSerializer TiffBitsPerSample = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:BitsPerSample>" + rdfSeq(dataSets) + 
					"</tiff:BitsPerSample>";
		};
	};
	
	/**
	 * tiff:Compression serializer
	 * 
	 * TIFF tag 259, 0x103. Compression scheme:
	 * 	1 = uncompressed 
	 * 	6 = JPEG
	 * 
	 */
	static DataSet.DataSetSerializer TiffCompression = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:Compression>" + dataSets.get(0).getValue() + 
					"</tiff:Compression>";
		};
	};
	
	/**
	 * tiff:PhotometricInterpretation serializer
	 * 
	 * TIFF tag 262, 0x106. Pixel Composition: 
	 * 	2 = RGB
	 * 	6 = YCbCr
	 * 
	 */
	static DataSet.DataSetSerializer TiffPhotometricInterpretation = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:PhotometricInterpretation>" + 
					dataSets.get(0).getValue() + 
					"</tiff:PhotometricInterpretation>";
		};
	};
	
	/**
	 * tiff:Orientation serializer
	 * 
	 * TIFF tag 274, 0x112. Orientation: 
	 * 	1 = 0th row at top, 0th column at left 
	 * 	2 = 0th row at top, 0th column at right 
	 * 	3 = 0th row at bottom, 0th column at right 
	 * 	4 = 0th row at bottom, 0th column at left 
	 * 	5 = 0th row at left, 0th column at top 
	 * 	6 = 0th row at right, 0th column at top 
	 * 	7 = 0th row at right, 0th column at bottom 
	 * 	8 = 0th row at left, 0th column at bottom
	 * 
	 */
	static DataSet.DataSetSerializer TiffOrientation = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:Orientation>" + dataSets.get(0).getValue() + 
					"</tiff:Orientation>";
		};
	};
	
	/**
	 * tiff:SamplesPerPixel serializer
	 * 
	 * TIFF tag 277, 0x115. Number of components per pixel.
	 * 
	 */
	static DataSet.DataSetSerializer TiffSamplesPerPixel = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:SamplesPerPixel>" + dataSets.get(0).getValue() + 
					"</tiff:SamplesPerPixel>";
		};
	};
	
	/**
	 * tiff:PlanarConfiguration serializer
	 * 
	 * TIFF tag 284, 0x11C. Data layout 
	 * 	1 = chunky 
	 * 	2 = planar
	 * 
	 */
	static DataSet.DataSetSerializer TiffPlanarConfiguration = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:PlanarConfiguration>" + dataSets.get(0).getValue() + 
					"</tiff:PlanarConfiguration>";
		};
	};

	/**
	 * tiff:YCbCrSubSampling serializer
	 * 
	 * TIFF tag 530, 0x212. 
	 * Sampling ratio of chrominance components: 
	 * 	[2, 1] = YCbCr4:2:2 
	 * 	[2, 2] = YCbCr4:2:0
	 * 
	 */
	static DataSet.DataSetSerializer TiffYCbCrSubSampling = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:YCbCrSubSampling>" + rdfSeq(dataSets) + 
					"</tiff:YCbCrSubSampling>";
		};
	};
	
	/**
	 * tiff:YCbCrPositioning serializer
	 * 
	 * TIFF tag 531, 0x213. 
	 * Position of chrominance vs. luminance components: 
	 * 	1 = centered 
	 * 	2 = co-sited
	 * 
	 */
	static DataSet.DataSetSerializer TiffYCbCrPositioning = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:YCbCrPositioning>" + dataSets.get(0).getValue() + 
					"</tiff:YCbCrPositioning>";
		};
	};
	
	/**
	 * tiff:XResolution serializer
	 * 
	 * TIFF tag 282, 0x11A. Horizontal resolution in pixels per unit.
	 * 
	 */
	static DataSet.DataSetSerializer TiffXResolution = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:XResolution>" + dataSets.get(0).getValue() + 
					"</tiff:XResolution>";
		};
	};
	
	/**
	 * tiff:YResolution serializer
	 * 
	 * TIFF tag 283, 0x11B. Vertical resolution in pixels per unit.
	 * 
	 */
	static DataSet.DataSetSerializer TiffYResolution = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:YResolution>" + dataSets.get(0).getValue() + 
					"</tiff:YResolution>";
		};
	};
	
	/**
	 * tiff:ResolutionUnit serializer
	 * 
	 * TIFF tag 296, 0x128. Unit used for XResolution and YResolution. 
	 * Value is one of: 
	 * 	2 = inches 
	 * 	3 = centimeters
	 * 
	 */
	static DataSet.DataSetSerializer TiffResolutionUnit = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:ResolutionUnit>" + dataSets.get(0).getValue() + 
					"</tiff:ResolutionUnit>";
		};
	};
	
	/**
	 * tiff:TransferFunction serializer
	 * 
	 * TIFF tag 301, 0x12D. 
	 * Transfer function for image described in tabular style 
	 * with 3 * 256 entries.
	 * 
	 */
	static DataSet.DataSetSerializer TiffTransferFunction = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:TransferFunction>" + rdfSeq(dataSets) + 
					"</tiff:TransferFunction>";
		};
	};
	
	/**
	 * tiff:WhitePoint serializer
	 * 
	 * TIFF tag 318, 0x13E. Chromaticity of white point.
	 * 
	 */
	static DataSet.DataSetSerializer TiffWhitePoint = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:WhitePoint>" + rdfSeq(dataSets) + 
					"</tiff:WhitePoint>";
		};
	};
	
	/**
	 * tiff:PrimaryChromaticities serializer
	 * 
	 * TIFF tag 319, 0x13F. Chromaticity of the three primary colors.
	 * 
	 */
	static DataSet.DataSetSerializer TiffPrimaryChromaticities = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:PrimaryChromaticities>" + rdfSeq(dataSets) + 
					"</tiff:PrimaryChromaticities>";
		};
	};
	
	/**
	 * tiff:YCbCrCoefficients serializer
	 * 
	 * TIFF tag 529, 0x211. 
	 * Matrix coefficients for RGB to YCbCr transformation.
	 * 
	 */
	static DataSet.DataSetSerializer TiffYCbCrCoefficients = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:YCbCrCoefficients>" + rdfSeq(dataSets) + 
					"</tiff:YCbCrCoefficients>";
		};
	};
	
	/**
	 * tiff:ReferenceBlackWhite serializer
	 * 
	 * TIFF tag 532, 0x214. Reference black and white point values.
	 * 
	 */
	static DataSet.DataSetSerializer TiffReferenceBlackWhite = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:ReferenceBlackWhite>" + rdfSeq(dataSets) + 
					"</tiff:ReferenceBlackWhite>";
		};
	};
	
	/**
	 * tiff:DateTime serializer
	 * 
	 * TIFF tag 306, 0x132 (primary) and EXIF tag 37520, 0x9290 (subseconds). 
	 * Date and time of image creation (no time zone in EXIF), 
	 * stored in ISO 8601 format, not the original EXIF format. 
	 * 
	 * This property includes the value for the EXIF SubSecTime attribute. 
	 * This property is stored in XMP as xmp:ModifyDate.
	 * 
	 */
	static DataSet.DataSetSerializer TiffDateTime = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:DateTime>" + dataSets.get(0).getValue() + 
					"</tiff:DateTime>";
		};
	};
	
	/**
	 * tiff:ImageDescription serializer
	 * 
	 * TIFF tag 270, 0x10E. Description of the image. 
	 * This property is stored in XMP as dc:description.
	 * 
	 */
	static DataSet.DataSetSerializer TiffImageDescription = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:ImageDescription>" + rdfAlt(dataSets, "x-default") + 
					"</tiff:ImageDescription>";
		};
	};
	
	/**
	 * tiff:Make serializer
	 * 
	 * TIFF tag 271, 0x10F. Manufacturer of recording equipment.
	 * 
	 */
	static DataSet.DataSetSerializer TiffMake = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:Make>" + dataSets.get(0).getValue() + "</tiff:Make>";
		};
	};
	
	/**
	 * tiff:Model serializer
	 * 
	 * TIFF tag 272, 0x110. Model name or number of equipment.
	 * 
	 */
	static DataSet.DataSetSerializer TiffModel = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:Model>" + dataSets.get(0).getValue() + "</tiff:Model>";
		};
	};
	
	/**
	 * tiff:Software serializer
	 * 
	 * TIFF tag 305, 0x131. Software or firmware used to generate image. 
	 * 
	 * This property is stored in XMP as xmp:CreatorTool.
	 * 
	 */
	static DataSet.DataSetSerializer TiffSoftware = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:Software>" + dataSets.get(0).getValue() + 
					"</tiff:Software>";
		};
	};
	
	/**
	 * tiff:Artist serializer
	 * 
	 * TIFF tag 315, 0x13B. Camera owner, photographer or image creator. 
	 * This property is stored in XMP as the first item 
	 * in the dc:creator array.
	 * 
	 */
	static DataSet.DataSetSerializer TiffArtist = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:Artist>" + dataSets.get(0).getValue() + 
					"</tiff:Artist>";
		};
	};
	
	/**
	 * tiff:Copyright serializer
	 * 
	 * TIFF tag 33432, 0x8298. Copyright information. 
	 * This property is stored in XMP as dc:rights.
	 * 
	 */
	static DataSet.DataSetSerializer TiffCopyright = 
		new DataSet.DataSetSerializer(TIFF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<tiff:Copyright>" + rdfAlt(dataSets, "x-default") + 
					"</tiff:Copyright>";
		};
	};
	
	//EXIF schema for EXIF-specific properties (Version 2.2)
	
	
	
	/**
	 * exif:ExifVersion serializer
	 * 
	 * EXIF tag 36864, 0x9000. EXIF version number.
	 * 
	 */
	static DataSet.DataSetSerializer ExifVersion = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:ExifVersion>" + dataSets.get(0).getValue() + 
					"</exif:ExifVersion>";
		};
	};
	
	/**
	 * exif:FlashpixVersion serializer
	 * 
	 * EXIF tag 40960, 0xA000. Version of FlashPix.
	 * 
	 */
	static DataSet.DataSetSerializer ExifFlashpixVersion = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:FlashpixVersion>" + dataSets.get(0).getValue() + 
					"</exif:FlashpixVersion>";
		};
	};
	
	/**
	 * exif:ColorSpace serializer
	 * 
	 * EXIF tag 40961, 0xA001. Color space information: 
	 * 	1 = sRGB 
	 * 	65535 = uncalibrated
	 * 
	 */
	static DataSet.DataSetSerializer ExifColorSpace = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:ColorSpace>" + dataSets.get(0).getValue() + 
					"</exif:ColorSpace>";
		};
	};
	
	/**
	 * exif:ComponentsConfiguration serializer
	 * 
	 * EXIF tag 37121, 0x9101. 
	 * 
	 * Configuration of components in data: 4 5 6 0 (if RGB compressed data), 
	 * 1 2 3 0 (other cases). 
	 * 	0 = does not exist 
	 * 	1 = Y 
	 * 	2 = Cb 
	 * 	3 = Cr 
	 * 	4 = R 
	 * 	5 = G 
	 * 	6 = B
	 * 
	 */
	static DataSet.DataSetSerializer ExifComponentsConfiguration = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:ComponentsConfiguration>" + rdfSeq(dataSets) + 
					"</exif:ComponentsConfiguration>";
		};
	};
	
	/**
	 * exif:CompressedBitsPerPixel serializer
	 * 
	 * EXIF tag 37122, 0x9102. 
	 * Compression mode used for a compressed image 
	 * is indicated in unit bits per pixel.
	 * 
	 */
	static DataSet.DataSetSerializer ExifCompressedBitsPerPixel = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:CompressedBitsPerPixel>" + dataSets.get(0).getValue() + 
					"</exif:CompressedBitsPerPixel>";
		};
	};
	
	/**
	 * exif:PixelXDimension serializer
	 * 
	 * EXIF tag 40962, 0xA002. Valid image width, in pixels.
	 * 
	 */
	static DataSet.DataSetSerializer ExifPixelXDimension = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:PixelXDimension>" + dataSets.get(0).getValue() + 
					"</exif:PixelXDimension>";
		};
	};
	
	/**
	 * exif:PixelYDimension serializer
	 * 
	 * EXIF tag 40963, 0xA003. Valid image height, in pixels.
	 * 
	 */
	static DataSet.DataSetSerializer ExifPixelYDimension = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:PixelYDimension>" + dataSets.get(0).getValue() + 
					"</exif:PixelYDimension>";
		};
	};
	
	/**
	 * exif:UserComment serializer
	 * 
	 * EXIF tag 37510, 0x9286. Comments from user.
	 * 
	 */
	static DataSet.DataSetSerializer ExifUserComment = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:UserComment>" + rdfAlt(dataSets, "x-default") + 
					"</exif:UserComment>";
		};
	};
	
	/**
	 * exif:RelatedSoundFile serializer
	 * 
	 * EXIF tag 40964, 0xA004. 
	 * An “8.3” file name for the related sound file.
	 * 
	 */
	static DataSet.DataSetSerializer ExifRelatedSoundFile = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:RelatedSoundFile>" + dataSets.get(0).getValue() + 
					"</exif:RelatedSoundFile>";
		};
	};
	
	/**
	 * exif:DateTimeOriginal serializer
	 * 
	 * EXIF tags 36867, 0x9003 (primary) and 37521, 0x9291 (subseconds). 
	 * Date and time when original image was generated, 
	 * in ISO 8601 format. Includes the EXIF SubSecTimeOriginal data. 
	 * 
	 * Note that EXIF date-time values have no time zone information.
	 * 
	 */
	static DataSet.DataSetSerializer ExifDateTimeOriginal = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:DateTimeOriginal>" + dataSets.get(0).getValue() + 
					"</exif:DateTimeOriginal>";
		};
	};
	
	/**
	 * exif:DateTimeDigitized serializer
	 * 
	 * EXIF tag 36868, 0x9004 (primary) and 37522, 0x9292 (subseconds). 
	 * Date and time when image was stored as digital data, 
	 * can be the same as DateTimeOriginal if originally stored in digital form. 
	 * Stored in ISO 8601 format. Includes the EXIF SubSecTimeDigitized data.
	 * 
	 */
	static DataSet.DataSetSerializer ExifDateTimeDigitized = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:DateTimeDigitized>" + dataSets.get(0).getValue() + 
					"</exif:DateTimeDigitized>";
		};
	};
	
	/**
	 * exif:ExposureTime serializer
	 * 
	 * EXIF tag 33434, 0x829A. Exposure time in seconds.
	 * 
	 */
	static DataSet.DataSetSerializer ExifExposureTime = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:ExposureTime>" + dataSets.get(0).getValue() + 
					"</exif:ExposureTime>";
		};
	};
	
	/**
	 * exif:FNumber serializer
	 * 
	 * EXIF tag 33437, 0x829D. F number.
	 * 
	 */
	static DataSet.DataSetSerializer ExifFNumber = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:FNumber>" + dataSets.get(0).getValue() + 
					"</exif:FNumber>";
		};
	};
	
	/**
	 * exif:ExposureProgram serializer
	 * 
	 * EXIF tag 34850, 0x8822. 
	 * Class of program used for exposure: 
	 * 	0 = not defined 
	 * 	1 = Manual 
	 * 	2 = Normal program 
	 * 	3 = Aperture priority 
	 * 	4 = Shutter priority 
	 * 	5 = Creative program 
	 * 	6 = Action program 
	 * 	7 = Portrait mode 
	 * 	8 = Landscape mode
	 * 
	 */
	static DataSet.DataSetSerializer ExifExposureProgram = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:ExposureProgram>" + dataSets.get(0).getValue() + 
					"</exif:ExposureProgram>";
		};
	};
	
	/**
	 * exif:SpectralSensitivity serializer
	 * 
	 * EXIF tag 34852, 0x8824. Spectral sensitivity of each channel.
	 * 
	 */
	static DataSet.DataSetSerializer ExifSpectralSensitivity = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:SpectralSensitivity>" + dataSets.get(0).getValue() + 
					"</exif:SpectralSensitivity>";
		};
	};
	
	/**
	 * exif:ISOSpeedRatings serializer
	 * 
	 * EXIF tag 34855, 0x8827. ISO Speed and ISO Latitude of 
	 * the input device as specified in ISO 12232.
	 * 
	 */
	static DataSet.DataSetSerializer ExifISOSpeedRatings = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:ISOSpeedRatings>" + rdfSeq(dataSets) + 
					"</exif:ISOSpeedRatings>";
		};
	};
	
	/**
	 * exif:OECF serializer
	 * 
	 * EXIF tag 34856, 0x8828. Opto-Electronic Conversion Function 
	 * as specified in ISO 14524.
	 * 
	 */
	static DataSet.DataSetSerializer ExifOECF = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:OECF>" + dataSets.get(0).getValue() + "</exif:OECF>";
		};
	};
	
	/**
	 * exif:ShutterSpeedValue serializer
	 * 
	 * EXIF tag 37377, 0x9201. Shutter speed, unit is APEX. 
	 * 
	 * See Annex C of the EXIF specification.
	 * 
	 */
	static DataSet.DataSetSerializer ExifShutterSpeedValue = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:ShutterSpeedValue>" + dataSets.get(0).getValue() + 
					"</exif:ShutterSpeedValue>";
		};
	};
	
	/**
	 * exif:ApertureValue serializer
	 * 
	 * EXIF tag 37378, 0x9202. Lens aperture, unit is APEX.
	 * 
	 */
	static DataSet.DataSetSerializer ExifApertureValue = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:ApertureValue>" + dataSets.get(0).getValue() + 
					"</exif:ApertureValue>";
		};
	};
	
	/**
	 * exif:BrightnessValue serializer
	 * 
	 * EXIF tag 37379, 0x9203. Brightness, unit is APEX.
	 * 
	 */
	static DataSet.DataSetSerializer ExifBrightnessValue = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:BrightnessValue>" + dataSets.get(0).getValue() + 
					"</exif:BrightnessValue>";
		};
	};
	
	/**
	 * exif:ExposureBiasValue serializer
	 * 
	 * EXIF tag 37380, 0x9204. Exposure bias, unit is APEX.
	 * 
	 */
	static DataSet.DataSetSerializer ExifExposureBiasValue = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:ExposureBiasValue>" + dataSets.get(0).getValue() + 
					"</exif:ExposureBiasValue>";
		};
	};
	
	/**
	 * exif:MaxApertureValue serializer
	 * 
	 * EXIF tag 37381, 0x9205. Smallest F number of lens, in APEX.
	 * 
	 */
	static DataSet.DataSetSerializer ExifMaxApertureValue = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:MaxApertureValue>" + dataSets.get(0).getValue() + 
					"</exif:MaxApertureValue>";
		};
	};
	
	/**
	 * exif:SubjectDistance serializer
	 * 
	 * EXIF tag 37382, 0x9206. Distance to subject, in meters.
	 * 
	 */
	static DataSet.DataSetSerializer ExifSubjectDistance = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:SubjectDistance>" + dataSets.get(0).getValue() + 
					"</exif:SubjectDistance>";
		};
	};
	
	/**
	 * exif:MeteringMode serializer
	 * 
	 * EXIF tag 37383, 0x9207. 
	 * Metering mode: 
	 * 	0 = unknown 
	 * 	1 = Average 
	 * 	2 = CenterWeightedAverage 
	 * 	3 = Spot 
	 * 	4 = MultiSpot 
	 * 	5 = Pattern 
	 * 	6 = Partial 
	 * 	255 = other
	 * 
	 */
	static DataSet.DataSetSerializer ExifMeteringMode = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:MeteringMode>" + dataSets.get(0).getValue() + 
					"</exif:MeteringMode>";
		};
	};
	
	/**
	 * exif:LightSource serializer
	 * 
	 * EXIF tag 37384, 0x9208. EXIF tag, 0x. 
	 * Light source: 
	 * 	0 = unknown 
	 * 	1 = Daylight 
	 * 	2 = Fluorescent 
	 * 	3 = Tungsten 
	 * 	4 = Flash 
	 * 	9 = Fine weather 
	 * 	10 = Cloudy weather 
	 * 	11 = Shade 
	 * 	12 = Daylight fluorescent (D 5700 – 7100K) 
	 * 	13 = Day white fluorescent (N 4600 – 5400K) 
	 * 	14 = Cool white fluorescent (W 3900 – 4500K) 
	 * 	15 = White fluorescent (WW 3200 – 3700K) 
	 * 	17 = Standard light A 
	 * 	18 = Standard light B 
	 * 	19 = Standard light C 
	 * 	20 = D55 
	 * 	21 = D65 
	 * 	22 = D75 
	 * 	23 = D50 
	 * 	24 = ISO studio tungsten 
	 * 	255 = other
	 * 
	 */
	static DataSet.DataSetSerializer ExifLightSource = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:LightSource>" + dataSets.get(0).getValue() + 
					"</exif:LightSource>";
		};
	};
	
	/**
	 * exif:Flash serializer
	 * 
	 * EXIF tag 37385, 0x9209. Strobe light (flash) source data.
	 * 
	 */
	static DataSet.DataSetSerializer ExifFlash = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:Flash>" + dataSets.get(0).getValue() + 
					"</exif:Flash>";
		};
	};
	
	/**
	 * exif:FocalLength serializer
	 * 
	 * EXIF tag 37386, 0x920A. Focal length of the lens, in millimeters.
	 * 
	 */
	static DataSet.DataSetSerializer ExifFocalLength = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:FocalLength>" + dataSets.get(0).getValue() + 
					"</exif:FocalLength>";
		};
	};
	
	/**
	 * exif:SubjectArea serializer
	 * 
	 * EXIF tag 37396, 0x9214. 
	 * The location and area of the main subject in the overall scene.
	 * 
	 */
	static DataSet.DataSetSerializer ExifSubjectArea = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:SubjectArea>" + rdfSeq(dataSets) + 
					"</exif:SubjectArea>";
		};
	};
	
	/**
	 * exif:FlashEnergy serializer
	 * 
	 * EXIF tag 41483, 0xA20B. Strobe energy during image capture.
	 * 
	 */
	static DataSet.DataSetSerializer ExifFlashEnergy = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:FlashEnergy>" + dataSets.get(0).getValue() + 
					"</exif:FlashEnergy>";
		};
	};
	
	/**
	 * exif:SpatialFrequencyResponse serializer
	 * 
	 * EXIF tag 41484, 0xA20C. Input device spatial frequency table and 
	 * SFR values as specified in ISO 12233.
	 * 
	 */
	static DataSet.DataSetSerializer ExifSpatialFrequencyResponse = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:SpatialFrequencyResponse>" + 
					dataSets.get(0).getValue() + 
					"</exif:SpatialFrequencyResponse>";
		};
	};
	
	/**
	 * exif:FocalPlaneXResolution serializer
	 * 
	 * EXIF tag 41486, 0xA20E. Horizontal focal resolution, 
	 * measured pixels per unit.
	 * 
	 */
	static DataSet.DataSetSerializer ExifFocalPlaneXResolution = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:FocalPlaneXResolution>" + dataSets.get(0).getValue() + 
					"</exif:FocalPlaneXResolution>";
		};
	};
	
	/**
	 * exif:FocalPlaneYResolution serializer
	 * 
	 * EXIF tag 41487, 0xA20F. Vertical focal resolution, 
	 * measured in pixels per unit.
	 * 
	 */
	static DataSet.DataSetSerializer ExifFocalPlaneYResolution = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:FocalPlaneYResolution>" + dataSets.get(0).getValue() + 
					"</exif:FocalPlaneYResolution>";
		};
	};
	
	/**
	 * exif:FocalPlaneResolutionUnit serializer
	 * 
	 * EXIF tag 41488, 0xA210. 
	 * Unit used for FocalPlaneXResolution and FocalPlaneYResolution. 
	 * 	2 = inches 
	 * 	3 = centimeters
	 * 
	 */
	static DataSet.DataSetSerializer ExifFocalPlaneResolutionUnit = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:FocalPlaneResolutionUnit>" + 
					dataSets.get(0).getValue() + 
					"</exif:FocalPlaneResolutionUnit>";
		};
	};
	
	/**
	 * exif:SubjectLocation serializer
	 * 
	 * EXIF tag 41492, 0xA214. 
	 * Location of the main subject of the scene. 
	 * The first value is the horizontal pixel and the second value 
	 * is the vertical pixel at which the main subject appears.
	 * 
	 */
	static DataSet.DataSetSerializer ExifSubjectLocation = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:SubjectLocation>" + rdfSeq(dataSets) + 
					"</exif:SubjectLocation>";
		};
	};
	
	/**
	 * exif:ExposureIndex serializer
	 * 
	 * EXIF tag 41493, 0xA215. Exposure index of input device.
	 * 
	 */
	static DataSet.DataSetSerializer ExifExposureIndex = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:ExposureIndex>" + dataSets.get(0).getValue() + 
					"</exif:ExposureIndex>";
		};
	};
	
	/**
	 * exif:SensingMethod serializer
	 * 
	 * EXIF tag 41495, 0xA217. 
	 * Image sensor type on input device: 
	 * 	1 = Not defined 
	 * 	2 = One-chip color area sensor 
	 * 	3 = Two-chip color area sensor 
	 * 	4 = Three-chip color area sensor 
	 * 	5 = Color sequential area sensor 
	 * 	7 = Trilinear sensor 
	 * 	8 = Color sequential linear sensor
	 * 
	 */
	static DataSet.DataSetSerializer ExifSensingMethod = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:SensingMethod>" + dataSets.get(0).getValue() + 
					"</exif:SensingMethod>";
		};
	};
	
	/**
	 * exif:FileSource serializer
	 * 
	 * EXIF tag 41728, 0xA300. 
	 * Indicates image source: 
	 * 	3 (DSC) is the only choice.
	 * 
	 */
	static DataSet.DataSetSerializer ExifFileSource = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:FileSource>" + dataSets.get(0).getValue() + 
					"</exif:FileSource>";
		};
	};
	
	/**
	 * exif:SceneType serializer
	 * 
	 * EXIF tag 41729, 0xA301. 
	 * Indicates the type of scene: 
	 * 	1 (directly photographed image) is the only choice.
	 * 
	 */
	static DataSet.DataSetSerializer ExifSceneType = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:SceneType>" + dataSets.get(0).getValue() + 
					"</exif:SceneType>";
		};
	};
	
	/**
	 * exif:CFAPattern serializer
	 * 
	 * EXIF tag 41730, 0xA302. Color filter array geometric 
	 * pattern of the image sense.
	 * 
	 */
	static DataSet.DataSetSerializer ExifCFAPattern = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:CFAPattern>" + dataSets.get(0).getValue() + 
					"</exif:CFAPattern>";
		};
	};
	
	/**
	 * exif:CustomRendered serializer
	 * 
	 * EXIF tag 41985, 0xA401. Indicates the use of special processing 
	 * on image data: 
	 * 	0 = Normal process 
	 * 	1 = Custom process
	 * 
	 */
	static DataSet.DataSetSerializer ExifCustomRendered = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:CustomRendered>" + dataSets.get(0).getValue() + 
					"</exif:CustomRendered>";
		};
	};
	
	/**
	 * exif:ExposureMode serializer
	 * 
	 * EXIF tag 41986, 0xA402. 
	 * Indicates the exposure mode set when the image was shot: 
	 * 	0 = Auto exposure 
	 * 	1 = Manual exposure 
	 * 	2 = Auto bracket
	 * 
	 */
	static DataSet.DataSetSerializer ExifExposureMode = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:ExposureMode>" + dataSets.get(0).getValue() + 
					"</exif:ExposureMode>";
		};
	};
	
	/**
	 * exif:WhiteBalance serializer
	 * 
	 * EXIF tag 41987, 0xA403. 
	 * Indicates the white balance mode set when the image was shot: 
	 * 	0 = Auto white balance 
	 * 	1 = Manual white balance
	 * 
	 */
	static DataSet.DataSetSerializer ExifWhiteBalance = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:WhiteBalance>" + dataSets.get(0).getValue() + 
					"</exif:WhiteBalance>";
		};
	};
	
	/**
	 * exif:DigitalZoomRatio serializer
	 * 
	 * EXIF tag 41988, 0xA404. 
	 * Indicates the digital zoom ratio when the image was shot.
	 * 
	 */
	static DataSet.DataSetSerializer ExifDigitalZoomRatio = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:DigitalZoomRatio>" + dataSets.get(0).getValue() + 
					"</exif:DigitalZoomRatio>";
		};
	};
	
	/**
	 * exif:FocalLengthIn35mmFilm serializer
	 * 
	 * EXIF tag 41989, 0xA405. 
	 * Indicates the equivalent focal length assuming a 35mm film camera, 
	 * in mm. A value of 0 means the focal length is unknown. 
	 * 
	 * Note that this tag differs from the FocalLength tag.
	 * 
	 */
	static DataSet.DataSetSerializer ExifFocalLengthIn35mmFilm = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:FocalLengthIn35mmFilm>" + dataSets.get(0).getValue() + 
					"</exif:FocalLengthIn35mmFilm>";
		};
	};
	
	/**
	 * exif:SceneCaptureType serializer
	 * 
	 * EXIF tag 41990, 0xA406. Indicates the type of scene that was shot: 
	 * 	0 = Standard 
	 * 	1 = Landscape 
	 * 	2 = Portrait 
	 * 	3 = Night scene
	 * 
	 */
	static DataSet.DataSetSerializer ExifSceneCaptureType = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:SceneCaptureType>" + dataSets.get(0).getValue() + 
					"</exif:SceneCaptureType>";
		};
	};
	
	/**
	 * exif:GainControl serializer
	 * 
	 * EXIF tag 41991, 0xA407. 
	 * Indicates the degree of overall image gain adjustment: 
	 * 	0 = None 
	 * 	1 = Low gain up 
	 * 	2 = High gain up 
	 * 	3 = Low gain down 
	 * 	4 = High gain down
	 * 
	 */
	static DataSet.DataSetSerializer ExifGainControl = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GainControl>" + dataSets.get(0).getValue() + 
					"</exif:GainControl>";
		};
	};
	
	/**
	 * exif:Contrast serializer
	 * 
	 * EXIF tag 41992, 0xA408. 
	 * Indicates the direction of contrast processing applied by the camera: 
	 * 	0 = Normal 
	 * 	1 = Soft 
	 * 	2 = Hard
	 * 
	 */
	static DataSet.DataSetSerializer ExifContrast = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:Contrast>" + dataSets.get(0).getValue() + 
					"</exif:Contrast>";
		};
	};
	
	/**
	 * exif:Saturation serializer
	 * 
	 * EXIF tag 41993, 0xA409. 
	 * Indicates the direction of saturation processing applied by the camera: 
	 * 	0 = Normal 
	 * 	1 = Low saturation 
	 * 	2 = High saturation
	 * 
	 */
	static DataSet.DataSetSerializer ExifSaturation = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:Saturation>" + dataSets.get(0).getValue() + 
					"</exif:Saturation>";
		};
	};
	
	/**
	 * exif:Sharpness serializer
	 * 
	 * EXIF tag 41994, 0xA40A. 
	 * Indicates the direction of sharpness processing applied by the camera: 
	 * 	0 = Normal 
	 * 	1 = Soft 
	 * 	2 = Hard
	 * 
	 */
	static DataSet.DataSetSerializer ExifSharpness = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:Sharpness>" + dataSets.get(0).getValue() + 
					"</exif:Sharpness>";
		};
	};
	
	/**
	 * exif:DeviceSettingDescription serializer
	 * 
	 * EXIF tag 41995, 0xA40B. 
	 * Indicates information on the picture-taking conditions 
	 * of a particular camera model.
	 * 
	 */
	static DataSet.DataSetSerializer ExifDeviceSettingDescription = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:DeviceSettingDescription>" + 
					dataSets.get(0).getValue() + 
					"</exif:DeviceSettingDescription>";
		};
	};
	
	/**
	 * exif:SubjectDistanceRange serializer
	 * 
	 * EXIF tag 41996, 0xA40C. 
	 * Indicates the distance to the subject: 
	 * 	0 = Unknown 
	 * 	1 = Macro 
	 * 	2 = Close view 
	 * 	3 = Distant view
	 * 
	 */
	static DataSet.DataSetSerializer ExifSubjectDistanceRange = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:SubjectDistanceRange>" + dataSets.get(0).getValue() + 
					"</exif:SubjectDistanceRange>";
		};
	};
	
	/**
	 * exif:ImageUniqueID serializer
	 * 
	 * EXIF tag 42016, 0xA420. 
	 * An identifier assigned uniquely to each image. 
	 * It is recorded as a 32 character ASCII string, 
	 * equivalent to hexadecimal notation and 128-bit fixed length.
	 * 
	 */
	static DataSet.DataSetSerializer ExifImageUniqueID = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:ImageUniqueID>" + dataSets.get(0).getValue() + 
					"</exif:ImageUniqueID>";
		};
	};
	
	/**
	 * exif:GPSVersionID serializer
	 * 
	 * GPS tag 0, 0x00. 
	 * A decimal encoding of each of the four EXIF bytes 
	 * with period separators. The current value is “2.0.0.0”.
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSVersionID = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSVersionID>" + dataSets.get(0).getValue() + 
					"</exif:GPSVersionID>";
		};
	};
	
	/**
	 * exif:GPSLatitude serializer
	 * 
	 * GPS tag 2, 0x02 (position) and 1, 0x01 (North/South). 
	 * Indicates latitude.
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSLatitude = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSLatitude>" + dataSets.get(0).getValue() + 
					"</exif:GPSLatitude>";
		};
	};
	
	/**
	 * exif:GPSLongitude serializer
	 * 
	 * GPS tag 4, 0x04 (position) and 3, 0x03 (East/West). 
	 * Indicates longitude.
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSLongitude = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSLongitude>" + dataSets.get(0).getValue() + 
					"</exif:GPSLongitude>";
		};
	};
	
	/**
	 * exif:GPSAltitudeRef serializer
	 * 
	 * GPS tag 5, 0x5. 
	 * Indicates whether the altitude is above or below sea level: 
	 * 	0 = Above sea level 
	 * 	1 = Below sea level
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSAltitudeRef = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSAltitudeRef>" + dataSets.get(0).getValue() + 
					"</exif:GPSAltitudeRef>";
		};
	};
	
	/**
	 * exif:GPSAltitude serializer
	 * 
	 * GPS tag 6, 0x06. Indicates altitude in meters.
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSAltitude = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSAltitude>" + dataSets.get(0).getValue() + 
					"</exif:GPSAltitude>";
		};
	};
	
	/**
	 * exif:GPSTimeStamp serializer
	 * 
	 * GPS tag 29 (date), 0x1D, and, and GPS tag 7 (time), 0x07. 
	 * Time stamp of GPS data, in Coordinated Universal Time. 
	 * The GPSDateStamp tag is new in EXIF 2.2. 
	 * The GPS timestamp in EXIF 2.1 does not include a date. 
	 * If not present, the date component for the XMP should be taken 
	 * from exif:DateTimeOriginal, or if that is also lacking 
	 * from exif:DateTimeDigitized. 
	 * 
	 * If no date is available, do not write exif:GPSTimeStamp to XMP.
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSTimeStamp = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSTimeStamp>" + dataSets.get(0).getValue() + 
					"</exif:GPSTimeStamp>";
		};
	};
	
	/**
	 * exif:GPSSatellites serializer
	 * 
	 * GPS tag 8, 0x08. Satellite information, format is unspecified.
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSSatellites = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSSatellites>" + dataSets.get(0).getValue() + 
					"</exif:GPSSatellites>";
		};
	};
	
	/**
	 * exif:GPSStatus serializer
	 * 
	 * GPS tag 9, 0x09. 
	 * Status of GPS receiver at image creation time: 
	 * 	A = measurement in progress 
	 * 	V = measurement is interoperability
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSStatus = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSStatus>" + dataSets.get(0).getValue() + 
					"</exif:GPSStatus>";
		};
	};
	
	/**
	 * exif:GPSMeasureMode serializer
	 * 
	 * GPS tag 10, 0x0A. 
	 * GPS measurement mode, Text type: 
	 * 	2 = two-dimensional measurement 
	 * 	3 = three-dimensional measurement
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSMeasureMode = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSMeasureMode>" + dataSets.get(0).getValue() + 
					"</exif:GPSMeasureMode>";
		};
	};
	
	/**
	 * exif:GPSDOP serializer
	 * 
	 * GPS tag 11, 0x0B. Degree of precision for GPS data.
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSDOP = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSDOP>" + dataSets.get(0).getValue() + 
					"</exif:GPSDOP>";
		};
	};
	
	/**
	 * exif:GPSSpeedRef serializer
	 * 
	 * GPS tag 12, 0x0C. 
	 * Units used to speed measurement: 
	 * 	K = kilometers per hour 
	 * 	M = miles per hour 
	 * 	N = knots
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSSpeedRef = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSSpeedRef>" + dataSets.get(0).getValue() + 
					"</exif:GPSSpeedRef>";
		};
	};
	
	/**
	 * exif:GPSSpeed serializer
	 * 
	 * GPS tag 13, 0x0D. Speed of GPS receiver movement.
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSSpeed = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSSpeed>" + dataSets.get(0).getValue() + 
					"</exif:GPSSpeed>";
		};
	};
	
	/**
	 * exif:GPSTrackRef serializer
	 * 
	 * GPS tag 14, 0x0E. 
	 * Reference for movement direction: 
	 * 	T = true direction 
	 * 	M = magnetic direction
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSTrackRef = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSTrackRef>" + dataSets.get(0).getValue() + 
					"</exif:GPSTrackRef>";
		};
	};
	
	/**
	 * exif:GPSTrack serializer
	 * 
	 * GPS tag 15, 0x0F. 
	 * Direction of GPS movement, values range from 0 to 359.99.
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSTrack = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSTrack>" + dataSets.get(0).getValue() + 
					"</exif:GPSTrack>";
		};
	};
	
	/**
	 * exif:GPSImgDirectionRef serializer
	 * 
	 * GPS tag 16, 0x10. 
	 * Reference for movement direction: 
	 * 	T = true direction 
	 * 	M = magnetic direction
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSImgDirectionRef = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSImgDirectionRef>" + dataSets.get(0).getValue() + 
					"</exif:GPSImgDirectionRef>";
		};
	};
	
	/**
	 * exif:GPSImgDirection serializer
	 * 
	 * GPS tag 17, 0x11. 
	 * Direction of image when captured, values range from 0 to 359.99.
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSImgDirection = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSImgDirection>" + dataSets.get(0).getValue() + 
					"</exif:GPSImgDirection>";
		};
	};
	
	/**
	 * exif:GPSMapDatum serializer
	 * 
	 * GPS tag 18, 0x12. Geodetic survey data.
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSMapDatum = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSMapDatum>" + dataSets.get(0).getValue() + 
					"</exif:GPSMapDatum>";
		};
	};
	
	/**
	 * exif:GPSDestLatitude serializer
	 * 
	 * GPS tag 20, 0x14 (position) and 19, 0x13 (North/South). 
	 * Indicates destination latitude.
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSDestLatitude = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSDestLatitude>" + dataSets.get(0).getValue() + 
					"</exif:GPSDestLatitude>";
		};
	};
	
	/**
	 * exif:GPSDestLongitude serializer
	 * 
	 * GPS tag 22, 0x16 (position) and 21, 0x15 (East/West). 
	 * Indicates destination longitude.
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSDestLongitude = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSDestLongitude>" + dataSets.get(0).getValue() + 
					"</exif:GPSDestLongitude>";
		};
	};
	
	/**
	 * exif:GPSDestBearingRef serializer
	 * 
	 * GPS tag 23, 0x17. 
	 * Reference for movement direction: 
	 * 	T = true direction 
	 * 	M = magnetic direction
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSDestBearingRef = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSDestBearingRef>" + dataSets.get(0).getValue() + 
					"</exif:GPSDestBearingRef>";
		};
	};
	
	/**
	 * exif:GPSDestBearing serializer
	 * 
	 * GPS tag 24, 0x18. 
	 * Destination bearing, values from 0 to 359.99.
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSDestBearing = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSDestBearing>" + dataSets.get(0).getValue() + 
					"</exif:GPSDestBearing>";
		};
	};
	
	/**
	 * exif:GPSDestDistanceRef serializer
	 * 
	 * GPS tag 25, 0x19. 
	 * Units used for speed measurement: 
	 * 	K = kilometers 
	 * 	M = miles 
	 * 	N = knots
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSDestDistanceRef = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSDestDistanceRef>" + dataSets.get(0).getValue() + 
					"</exif:GPSDestDistanceRef>";
		};
	};
	
	/**
	 * exif:GPSDestDistance serializer
	 * 
	 * GPS tag 26, 0x1A. Distance to destination.
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSDestDistance = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSDestDistance>" + dataSets.get(0).getValue() + 
					"</exif:GPSDestDistance>";
		};
	};
	
	/**
	 * exif:GPSProcessingMethod serializer
	 * 
	 * GPS tag 27, 0x1B. 
	 * A character string recording the name of the method used 
	 * for location finding.
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSProcessingMethod = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSProcessingMethod>" + dataSets.get(0).getValue() + 
					"</exif:GPSProcessingMethod>";
		};
	};
	
	/**
	 * exif:GPSAreaInformation serializer
	 * 
	 * GPS tag 28, 0x1C.
	 * A character string recording the name of the GPS area.
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSAreaInformation = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSAreaInformation>" + dataSets.get(0).getValue() + 
					"</exif:GPSAreaInformation>";
		};
	};
	
	/**
	 * exif:GPSDifferential serializer
	 * 
	 * GPS tag 30, 0x1E. 
	 * Indicates whether differential correction is applied 
	 * to the GPS receiver: 
	 * 	0 = Without correction 
	 * 	1 = Correction applied
	 * 
	 */
	static DataSet.DataSetSerializer ExifGPSDifferential = 
		new DataSet.DataSetSerializer(EXIF_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<exif:GPSDifferential>" + dataSets.get(0).getValue() + 
					"</exif:GPSDifferential>";
		};
	};
	
	//EXIF schema for additional EXIF properties (Version 2.2)
	
	/**
	 * aux:Lens serializer
	 * 
	 * A description of the lens used to take the photograph. 
	 * For example, “70-200 mm f/2.8-4.0”.
	 * 
	 */
	static DataSet.DataSetSerializer AuxLens = 
		new DataSet.DataSetSerializer(AUX_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<aux:Lens>" + dataSets.get(0).getValue() + "</aux:Lens>";
		};
	};
	
	/**
	 * aux:SerialNumber serializer
	 * 
	 * The serial number of the camera or camera body 
	 * used to take the photograph.
	 * 
	 */
	static DataSet.DataSetSerializer AuxSerialNumber = 
		new DataSet.DataSetSerializer(AUX_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<aux:SerialNumber>" + dataSets.get(0).getValue() + 
					"</aux:SerialNumber>";
		};
	};
	
	//IPTC For XMP Core schema (Version 1.1 - July 2009)
	
	/**
	 * Iptc4xmpCore:CountryCode serializer
	 * 
	 * Code of the country the content is focussing on.
	 * Either the country shown in visual media or referenced in text 
	 * or audio media. 
	 * 
	 * This element is at the top/first level of a top-down 
	 * geographical hierarchy. The code should be taken from ISO 3166 
	 * two or three letter code. 
	 * 
	 * The full name of a country should go to the "Country" element.
	 * 
	 */
	@Deprecated
	static DataSet.DataSetSerializer Iptc4XmpCoreCountryCode = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_CORE_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpCore:CountryCode>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpCore:CountryCode>";
		};
	};
	
	/**
	 * Iptc4xmpCore:IntellectualGenre serializer
	 * 
	 * Describes the nature, intellectual, artistic or journalistic 
	 * characteristic of a item, not specifically its content.
	 * 
	 * Optional: IPTC Genre NewsCodes
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpCoreIntellectualGenre = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_CORE_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpCore:IntellectualGenre>" + 
					dataSets.get(0).getValue() + 
					"</Iptc4xmpCore:IntellectualGenre>";
		};
	};
	
	/**
	 * Iptc4xmpCore:Scene serializer
	 * 
	 * Describes the scene of a news content. 
	 * Specifies one or more terms from the IPTC "Scene-NewsCodes". 
	 * Each Scene is represented as a string of 6 digits in an unordered list.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpCoreScene = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_CORE_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpCore:Scene>" + rdfBag(dataSets) + 
					"</Iptc4xmpCore:Scene>";
		};
	};
	
	/**
	 * Iptc4xmpCore:SubjectCode serializer
	 * 
	 * Specifies one or more Subjects from the IPTC Subject-NewsCodes 
	 * taxonomy to categorise the content. 
	 * Each Subject is represented as a string of 8 digits in an unordered list.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpCoreSubjectCode = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_CORE_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpCore:SubjectCode>" + rdfBag(dataSets) + 
					"</Iptc4xmpCore:SubjectCode>";
		};
	};
	
	/**
	 * Iptc4xmpCore:Location serializer
	 * 
	 * Name of a sublocation the content is focussing on.
	 * Either the location shown in visual media or referenced by text 
	 * or audio media. This location name could either be the name of a 
	 * sublocation to a city or the name of a well known location 
	 * or (natural) monument outside a city. 
	 * 
	 * In the sense of a sublocation to a city this element is at 
	 * the fourth level of a top-down geographical hierarchy.
	 * 
	 */
	@Deprecated
	static DataSet.DataSetSerializer Iptc4XmpCoreLocation = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_CORE_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpCore:Location>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpCore:Location>";
		};
	};
	
	/**
	 * Iptc4xmpCore:CreatorContactInfo serializer
	 * 
	 * The creator's contact information provides all necessary information 
	 * to get in contact with the creator of this item and comprises 
	 * a set of sub-properties for proper addressing.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpCoreCreatorContactInfo = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_CORE_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpCore:CreatorContactInfo>" + 
					dataSets.get(0).getValue() + 
					"</Iptc4xmpCore:CreatorContactInfo>";
		};
	};
	
	/**
	 * Iptc4xmpCore:ContactInfoDetails serializer
	 * 
	 * A generic structure providing a basic set of information 
	 * to get in contact with a person or organisation.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpCoreContactInfoDetails = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_CORE_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpCore:ContactInfoDetails>" + 
					dataSets.get(0).getValue() + 
					"</Iptc4xmpCore:ContactInfoDetails>";
		};
	};
	
	/**
	 * Iptc4xmpCore:CiAdrExtadr serializer
	 * 
	 * The contact information address part. 
	 * Comprises an optional company name and all required information 
	 * to locate the building or postbox to which mail should be sent. 
	 * To that end, the address is a multiline field.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpCoreCiAdrExtadr = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_CORE_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpCore:CiAdrExtadr>" + 
					dataSets.get(0).getValue() + 
					"</Iptc4xmpCore:CiAdrExtadr>";
		};
	};
	
	/**
	 * Iptc4xmpCore:CiAdrCity serializer
	 * 
	 * The contact information city part.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpCoreCiAdrCity = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_CORE_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpCore:CiAdrCity>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpCore:CiAdrCity>";
		};
	};
	
	/**
	 * Iptc4xmpCore:CiAdrCtry serializer
	 * 
	 * The contact information country part.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpCoreCiAdrCtry = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_CORE_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpCore:CiAdrCtry>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpCore:CiAdrCtry>";
		};
	};
	
	/**
	 * Iptc4xmpCore:CiEmailWork serializer
	 * 
	 * The contact information email address part.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpCoreCiEmailWork = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_CORE_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpCore:CiEmailWork>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpCore:CiEmailWork>";
		};
	};
	
	/**
	 * Iptc4xmpCore:CiTelWork serializer
	 * 
	 * The contact information phone number part.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpCoreCiTelWork = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_CORE_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpCore:CiTelWork>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpCore:CiTelWork>";
		};
	};
	
	/**
	 * Iptc4xmpCore:CiAdrPcode serializer
	 * 
	 * The contact information part denoting the local postal code.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpCoreCiAdrPcode = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_CORE_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpCore:CiAdrPcode>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpCore:CiAdrPcode>";
		};
	};
	
	/**
	 * Iptc4xmpCore:CiAdrRegion serializer
	 * 
	 * The contact information part denoting regional information 
	 * like state or province.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpCoreCiAdrRegion = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_CORE_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpCore:CiAdrRegion>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpCore:CiAdrRegion>";
		};
	};
	
	/**
	 * Iptc4xmpCore:CiUrlWork serializer
	 * 
	 * The contact information web address part. 
	 * 
	 * Multiple addresses can be given, separated by a comma.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpCoreCiUrlWork = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_CORE_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpCore:CiUrlWork>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpCore:CiUrlWork>";
		};
	};
	
	//IPTC Extension schema (Version 1.1 - July 2009)
	
	/**
	 * Iptc4xmpExt:AddlModelInfo serializer
	 * 
	 * Information about the ethnicity and other facets of the model(s) 
	 * in a model-released image.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtAddlModelInfo = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:AddlModelInfo>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:AddlModelInfo>";
		};
	};
	
	/**
	 * Iptc4xmpExt:ArtworkOrObject serializer
	 * 
	 * A set of metadata about artwork or an object in the item.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtArtworkOrObject = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:ArtworkOrObject>" + rdfBag(dataSets) + 
					"</Iptc4xmpExt:ArtworkOrObject>";
		};
	};
	
	/**
	 * Iptc4xmpExt:OrganisationInImageCode serializer
	 * 
	 * Code from a controlled vocabulary for identifying the organisation 
	 * or company which is featured in the content.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtOrganisationInImageCode = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:OrganisationInImageCode>" + rdfBag(dataSets) + 
					"</Iptc4xmpExt:OrganisationInImageCode>";
		};
	};
	
	/**
	 * Iptc4xmpExt:CVterm serializer
	 * 
	 * A term to describe the content of the image by a value 
	 * from a Controlled Vocabulary.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtCVterm = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:CVterm>" + rdfBag(dataSets) + 
					"</Iptc4xmpExt:CVterm>";
		};
	};
	
	/**
	 * Iptc4xmpExt:LocationShown serializer
	 * 
	 * A location the content of the item is about. 
	 * For photos that is a location shown in the image.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtLocationShown = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:LocationShown>" + rdfBag(dataSets) + 
					"</Iptc4xmpExt:LocationShown>";
		};
	};
	
	/**
	 * Iptc4xmpExt:ModelAge serializer
	 * 
	 * Age of the human model(s) at the time this image was taken 
	 * in a model released image.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtModelAge = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:ModelAge>" + rdfBag(dataSets) + 
					"</Iptc4xmpExt:ModelAge>";
		};
	};
	
	/**
	 * Iptc4xmpExt:OrganisationInImageName serializer
	 * 
	 * Name of the organisation or company which is featured in the content.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtOrganisationInImageName = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:OrganisationInImageName>" + rdfBag(dataSets) + 
					"</Iptc4xmpExt:OrganisationInImageName>";
		};
	};
	
	/**
	 * Iptc4xmpExt:PersonInImage serializer
	 * 
	 * Name of a person the content of the item is about. 
	 * For photos that is a person shown in the image.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtPersonInImage = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:PersonInImage>" + rdfBag(dataSets) + 
					"</Iptc4xmpExt:PersonInImage>";
		};
	};
	
	/**
	 * Iptc4xmpExt:DigImageGUID serializer
	 * 
	 * Globally unique identifier for the item. 
	 * 
	 * It is created and applied by the creator of the item at the time 
	 * of its creation. This value shall not be changed after that time.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtDigImageGUID = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:DigImageGUID>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:DigImageGUID>";
		};
	};
	
	/**
	 * Iptc4xmpExt:DigitalSourcefileType serializer
	 * 
	 * The type of the source digital file.
	 * 
	 * Values are: 
	 * 	Scan from film {scanfilm}, 
	 * 	scan from transparency (including slide) {scantransparancy}, 
	 * 	scan from print {scanprint}, 
	 * 	camera raw {cameraraw}, 
	 * 	camera tiff {cameratiff}, 
	 * 	camera jpeg {camerajpeg}
	 * 
	 */
	@Deprecated
	static DataSet.DataSetSerializer Iptc4XmpExtDigitalSourcefileType = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:DigitalSourcefileType>" + 
					dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:DigitalSourcefileType>";
		};
	};
	
	/**
	 * Iptc4xmpExt:DigitalSourceType serializer
	 * 
	 * The type of the source of this digital image.
	 * 
	 * Digital Source Type NewsCodes 
	 * {@link http://cv.iptc.org/newscodes/digitalsourcetype/}
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtDigitalSourceType = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:DigitalSourceType>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:DigitalSourceType>";
		};
	};
	
	/**
	 * Iptc4xmpExt:Event serializer
	 * 
	 * Names or describes the specific event the content relates to.
	 * 
	 * Examples are: a press conference, dedication ceremony, etc. 
	 * 
	 * If this is a sub-event of a larger event both can be provided 
	 * by the field: e.g. XXXIX Olympic Summer Games (Beijing): opening ceremony. 
	 * 
	 * Unplanned events could be named by this property too.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtEvent = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:Event>" + rdfAlt(dataSets, "x-default") + 
					"</Iptc4xmpExt:Event>";
		};
	};
	
	/**
	 * Iptc4xmpExt:RegistryId serializer
	 * 
	 * Both a Registry Item Id and a Registry Organisation Id 
	 * to record any registration of this item with a registry.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtRegistryId = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:RegistryId>" + rdfBag(dataSets) + 
					"</Iptc4xmpExt:RegistryId>";
		};
	};
	
	/**
	 * Iptc4xmpExt:IptcLastEdited serializer
	 * 
	 * The date and optionally time when any of the 
	 * IPTC photo metadata fields has been last edited.
	 * 
	 */
	@Deprecated
	static DataSet.DataSetSerializer Iptc4XmpExtIptcLastEdited = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:IptcLastEdited>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:IptcLastEdited>";
		};
	};
	
	/**
	 * Iptc4xmpExt:LocationCreated serializer
	 * 
	 * The location the content of the item was created.
	 * 
	 * If the location in the image is different from the location 
	 * the photo was taken the IPTC Extension property Location Shown 
	 * in the Image should be used.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtLocationCreated = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:LocationCreated>" + rdfBag(dataSets) + 
					"</Iptc4xmpExt:LocationCreated>";
		};
	};
	
	/**
	 * Iptc4xmpExt:MaxAvailHeight serializer
	 * 
	 * The maximum available height in pixels of the original photo 
	 * from which this photo has been derived by downsizing.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtMaxAvailHeight = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:MaxAvailHeight>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:MaxAvailHeight>";
		};
	};
	
	/**
	 * Iptc4xmpExt:MaxAvailWidth serializer
	 * 
	 * The maximum available width in pixels of the original photo 
	 * from which this photo has been derived by downsizing.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtMaxAvailWidth = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:MaxAvailWidth>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:MaxAvailWidth>";
		};
	};
	
	/**
	 * Iptc4xmpExt:ArtworkOrObjectDetails serializer
	 * 
	 * A structured datatype for details about artwork or an object in an image.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtArtworkOrObjectDetails = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:ArtworkOrObjectDetails>" + 
					dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:ArtworkOrObjectDetails>";
		};
	};
	
	/**
	 * Iptc4xmpExt:AOCopyrightNotice serializer
	 * 
	 * Contains any necessary copyright notice for claiming the 
	 * intellectual property for artwork or an object in the image 
	 * and should identify the current owner of the copyright of this work 
	 * with associated intellectual property rights.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtAOCopyrightNotice = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:AOCopyrightNotice>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:AOCopyrightNotice>";
		};
	};
	
	/**
	 * Iptc4xmpExt:AOCreator serializer
	 * 
	 * Contains the name of the artist who has created artwork 
	 * or an object in the image.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtAOCreator = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:AOCreator>" + rdfSeq(dataSets) + 
					"</Iptc4xmpExt:AOCreator>";
		};
	};
	
	/**
	 * Iptc4xmpExt:AODateCreated serializer
	 * 
	 * Designates the date and optionally the time the artwork or object 
	 * in the image was created. 
	 * 
	 * This relates to artwork or objects with associated 
	 * intellectual property rights.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtAODateCreated = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:AODateCreated>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:AODateCreated>";
		};
	};
	
	/**
	 * Iptc4xmpExt:AOSource serializer
	 * 
	 * The organisation or body holding and registering the artwork 
	 * or object in the image for inventory purposes.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtAOSource = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:AOSource>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:AOSource>";
		};
	};
	
	/**
	 * Iptc4xmpExt:AOSourceInvNo serializer
	 * 
	 * The inventory number issued by the organisation 
	 * or body holding and registering the artwork or object in the image.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtAOSourceInvNo = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:AOSourceInvNo>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:AOSourceInvNo>";
		};
	};
	
	/**
	 * Iptc4xmpExt:AOTitle serializer
	 * 
	 * A reference for the artwork or object in the image.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtAOTitle = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:AOTitle>" + rdfAlt(dataSets, "x-default") + 
					"</Iptc4xmpExt:AOTitle>";
		};
	};
	
	/**
	 * Iptc4xmpExt:City serializer
	 * 
	 * Name of the city of a location. 
	 * This element is at the fourth level of a top-down geographical hierarchy.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtCity = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:City>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:City>";
		};
	};
	
	/**
	 * Iptc4xmpExt:CountryCode serializer
	 * 
	 * The ISO code of a country of a location. 
	 * This element is at the second level of a top-down geographical hierarchy.
	 * 
	 * ISO 3166-1 - 2 or 3 characters
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtCountryCode = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:CountryCode>" + rdfBag(dataSets) + 
					"</Iptc4xmpExt:CountryCode>";
		};
	};
	
	/**
	 * Iptc4xmpExt:CountryName serializer
	 * 
	 * The name of a country of a location. 
	 * This element is at the second level of a top-down geographical hierarchy.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtCountryName = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:CountryName>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:CountryName>";
		};
	};
	
	/**
	 * Iptc4xmpExt:LocationDetails serializer
	 * 
	 * A structured datatype for details of a location.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtLocationDetails = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:LocationDetails>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:LocationDetails>";
		};
	};
	
	/**
	 * Iptc4xmpExt:ProvinceState serializer
	 * 
	 * The name of a subregion of a country (a province or state) of a location. 
	 * This element is at the third level of a top-down geographical hierarchy.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtProvinceState = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:ProvinceState>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:ProvinceState>";
		};
	};
	
	/**
	 * Iptc4xmpExt:Sublocation serializer
	 * 
	 * Name of a sublocation. 
	 * 
	 * This sublocation name could either be the name of a sublocation 
	 * to a city or the name of a well known location or (natural) 
	 * monument outside a city. 
	 * 
	 * In the sense of a sublocation to a city this element is 
	 * at the fifth level of a top-down geographical hierarchy.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtSublocation = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:Sublocation>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:Sublocation>";
		};
	};
	
	/**
	 * Iptc4xmpExt:WorldRegion serializer
	 * 
	 * The name of a world region of a location. 
	 * This element is at the first (topI) level of a top-down geographical 
	 * hierarchy.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtWorldRegion = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:WorldRegion>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:WorldRegion>";
		};
	};
	
	/**
	 * Iptc4xmpExt:RegItemId serializer
	 * 
	 * A unique identifier created by a registry and applied by the creator 
	 * of the item. This value shall not be changed after being applied. 
	 * 
	 * This identifier is linked to a corresponding 
	 * Registry Organisation Identifier.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtRegItemId = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:RegItemId>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:RegItemId>";
		};
	};
	
	/**
	 * Iptc4xmpExt:RegOrgId serializer
	 * 
	 * An identifier for the registry which issued 
	 * the corresponding Registry Image Id.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtRegOrgId = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:RegOrgId>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:RegOrgId>";
		};
	};
	
	/**
	 * Iptc4xmpExt:RegistryEntryDetails serializer
	 * 
	 * A structured datatype for an entry in a registry, 
	 * includes the id issued by the registry and the registry's id.
	 * 
	 */
	static DataSet.DataSetSerializer Iptc4XmpExtRegistryEntryDetails = 
		new DataSet.DataSetSerializer(IPTC_4_XMP_EXT_NS_URI, "XMP") {
		@Override
		String serialize(List<? extends DataSet> dataSets) {
			return "<Iptc4xmpExt:RegistryEntryDetails>" + dataSets.get(0).getValue() + 
					"</Iptc4xmpExt:RegistryEntryDetails>";
		};
	};
	
	private static String rdfBag(List<? extends DataSet> dataSets) {
		Iterator<? extends DataSet> it = dataSets.iterator();
		String str = "<rdf:Bag>";
		while(it.hasNext()) {
			str += "<rdf:li>" + it.next().getValue() + "</rdf:li>";
		}	
		str += "</rdf:Bag>";
		return str;
	}
	
	private static String rdfSeq(List<? extends DataSet> dataSets) {
		Iterator<? extends DataSet> it = dataSets.iterator();
		String str = "<rdf:Seq>";
		while(it.hasNext()) {
			str += "<rdf:li>" + it.next().getValue() + "</rdf:li>";
		}	
		str += "</rdf:Seq>";
		return str;
	}
	
	/**
	 * @param language 	if null, don't add "xml:lang" qualifier.
	 * 					Use specified language otherwise.
	 */
	private static String rdfAlt(List<? extends DataSet> dataSets, String language) {
		String langQualifier = "";
		if(language != null) {
			langQualifier = " xml:lang='" + language + "'";
		}
		
		Iterator<? extends DataSet> it = dataSets.iterator();
		String str = "<rdf:Alt>";
		while(it.hasNext()) {
			str += "<rdf:li" + langQualifier + ">" + 
					it.next().getValue() + "</rdf:li>";
		}	
		str += "</rdf:Alt>";
		return str;
	}
}
