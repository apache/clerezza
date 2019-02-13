package org.apache.clerezza.ontologies;

import org.apache.clerezza.api.IRI;

public class DCTERMS {
	// Classes

	/**
	 * description: Examples of Agent include person, organization, and software agent.
comment: A resource that acts or has the power to act.

	 */
	public static final IRI Agent = new IRI("http://purl.org/dc/terms/Agent");

	/**
	 * description: Examples of Agent Class include groups seen as classes, such as students, women, charities, lecturers.
comment: A group of agents.

	 */
	public static final IRI AgentClass = new IRI("http://purl.org/dc/terms/AgentClass");

	/**
	 * comment: A book, article, or other documentary resource.

	 */
	public static final IRI BibliographicResource = new IRI("http://purl.org/dc/terms/BibliographicResource");

	/**
	 * comment: The set of regions in space defined by their geographic coordinates according to the DCMI Box Encoding Scheme.

	 */
	public static final IRI Box = new IRI("http://purl.org/dc/terms/Box");

	/**
	 * description: Examples include the formats defined by the list of Internet Media Types.
comment: A digital resource format.

	 */
	public static final IRI FileFormat = new IRI("http://purl.org/dc/terms/FileFormat");

	/**
	 * comment: A rate at which something recurs.

	 */
	public static final IRI Frequency = new IRI("http://purl.org/dc/terms/Frequency");

	/**
	 * comment: The set of codes listed in ISO 3166-1 for the representation of names of countries.

	 */
	public static final IRI ISO3166 = new IRI("http://purl.org/dc/terms/ISO3166");

	/**
	 * comment: The three-letter alphabetic codes listed in ISO639-2 for the representation of names of languages.

	 */
	public static final IRI ISO639_2 = new IRI("http://purl.org/dc/terms/ISO639-2");

	/**
	 * comment: The set of three-letter codes listed in ISO 639-3 for the representation of names of languages.

	 */
	public static final IRI ISO639_3 = new IRI("http://purl.org/dc/terms/ISO639-3");

	/**
	 * comment: The extent or range of judicial, law enforcement, or other authority.

	 */
	public static final IRI Jurisdiction = new IRI("http://purl.org/dc/terms/Jurisdiction");

	/**
	 * comment: A legal document giving official permission to do something with a Resource.

	 */
	public static final IRI LicenseDocument = new IRI("http://purl.org/dc/terms/LicenseDocument");

	/**
	 * description: Examples include written, spoken, sign, and computer languages.
comment: A system of signs, symbols, sounds, gestures, or rules used in communication.

	 */
	public static final IRI LinguisticSystem = new IRI("http://purl.org/dc/terms/LinguisticSystem");

	/**
	 * comment: A spatial region or named place.

	 */
	public static final IRI Location = new IRI("http://purl.org/dc/terms/Location");

	/**
	 * comment: A location, period of time, or jurisdiction.

	 */
	public static final IRI LocationPeriodOrJurisdiction = new IRI("http://purl.org/dc/terms/LocationPeriodOrJurisdiction");

	/**
	 * comment: A file format or physical medium.

	 */
	public static final IRI MediaType = new IRI("http://purl.org/dc/terms/MediaType");

	/**
	 * comment: A media type or extent.

	 */
	public static final IRI MediaTypeOrExtent = new IRI("http://purl.org/dc/terms/MediaTypeOrExtent");

	/**
	 * comment: A method by which resources are added to a collection.

	 */
	public static final IRI MethodOfAccrual = new IRI("http://purl.org/dc/terms/MethodOfAccrual");

	/**
	 * comment: A process that is used to engender knowledge, attitudes, and skills.

	 */
	public static final IRI MethodOfInstruction = new IRI("http://purl.org/dc/terms/MethodOfInstruction");

	/**
	 * comment: The set of time intervals defined by their limits according to the DCMI Period Encoding Scheme.

	 */
	public static final IRI Period = new IRI("http://purl.org/dc/terms/Period");

	/**
	 * comment: An interval of time that is named or defined by its start and end dates.

	 */
	public static final IRI PeriodOfTime = new IRI("http://purl.org/dc/terms/PeriodOfTime");

	/**
	 * description: Examples include paper, canvas, or DVD.
comment: A physical material or carrier.

	 */
	public static final IRI PhysicalMedium = new IRI("http://purl.org/dc/terms/PhysicalMedium");

	/**
	 * comment: A material thing.

	 */
	public static final IRI PhysicalResource = new IRI("http://purl.org/dc/terms/PhysicalResource");

	/**
	 * comment: The set of points in space defined by their geographic coordinates according to the DCMI Point Encoding Scheme.

	 */
	public static final IRI Point = new IRI("http://purl.org/dc/terms/Point");

	/**
	 * comment: A plan or course of action by an authority, intended to influence and determine decisions, actions, and other matters.

	 */
	public static final IRI Policy = new IRI("http://purl.org/dc/terms/Policy");

	/**
	 * comment: A statement of any changes in ownership and custody of a resource since its creation that are significant for its authenticity, integrity, and interpretation.

	 */
	public static final IRI ProvenanceStatement = new IRI("http://purl.org/dc/terms/ProvenanceStatement");

	/**
	 * comment: The set of tags, constructed according to RFC 1766, for the identification of languages.

	 */
	public static final IRI RFC1766 = new IRI("http://purl.org/dc/terms/RFC1766");

	/**
	 * description: RFC 3066 has been obsoleted by RFC 4646.
comment: The set of tags constructed according to RFC 3066 for the identification of languages.

	 */
	public static final IRI RFC3066 = new IRI("http://purl.org/dc/terms/RFC3066");

	/**
	 * description: RFC 4646 obsoletes RFC 3066.
comment: The set of tags constructed according to RFC 4646 for the identification of languages.

	 */
	public static final IRI RFC4646 = new IRI("http://purl.org/dc/terms/RFC4646");

	/**
	 * comment: A statement about the intellectual property rights (IPR) held in or over a Resource, a legal document giving official permission to do something with a resource, or a statement about access rights.

	 */
	public static final IRI RightsStatement = new IRI("http://purl.org/dc/terms/RightsStatement");

	/**
	 * description: Examples include a number of pages, a specification of length, width, and breadth, or a period in hours, minutes, and seconds.
comment: A dimension or extent, or a time taken to play or execute.

	 */
	public static final IRI SizeOrDuration = new IRI("http://purl.org/dc/terms/SizeOrDuration");

	/**
	 * comment: A basis for comparison; a reference point against which other things can be evaluated.

	 */
	public static final IRI Standard = new IRI("http://purl.org/dc/terms/Standard");

	/**
	 * comment: The set of identifiers constructed according to the generic syntax for Uniform Resource Identifiers as specified by the Internet Engineering Task Force.

	 */
	public static final IRI URI = new IRI("http://purl.org/dc/terms/URI");

	/**
	 * comment: The set of dates and times constructed according to the W3C Date and Time Formats Specification.

	 */
	public static final IRI W3CDTF = new IRI("http://purl.org/dc/terms/W3CDTF");

	// Properties

	/**
	 * comment: A summary of the resource.

	 */
	public static final IRI abstract_ = new IRI("http://purl.org/dc/terms/abstract");

	/**
	 * description: Access Rights may include information regarding access or restrictions based on privacy, security, or other policies.
comment: Information about who can access the resource or an indication of its security status.

	 */
	public static final IRI accessRights = new IRI("http://purl.org/dc/terms/accessRights");

	/**
	 * comment: The method by which items are added to a collection.

	 */
	public static final IRI accrualMethod = new IRI("http://purl.org/dc/terms/accrualMethod");

	/**
	 * comment: The frequency with which items are added to a collection.

	 */
	public static final IRI accrualPeriodicity = new IRI("http://purl.org/dc/terms/accrualPeriodicity");

	/**
	 * comment: The policy governing the addition of items to a collection.

	 */
	public static final IRI accrualPolicy = new IRI("http://purl.org/dc/terms/accrualPolicy");

	/**
	 * description: The distinction between titles and alternative titles is application-specific.
comment: An alternative name for the resource.
note: In current practice, this term is used primarily with literal values; however, there are important uses with non-literal values as well.  As of December 2007, the DCMI Usage Board is leaving this range unspecified pending an investigation of options.

	 */
	public static final IRI alternative = new IRI("http://purl.org/dc/terms/alternative");

	/**
	 * comment: A class of entity for whom the resource is intended or useful.

	 */
	public static final IRI audience = new IRI("http://purl.org/dc/terms/audience");

	/**
	 * comment: Date (often a range) that the resource became or will become available.

	 */
	public static final IRI available = new IRI("http://purl.org/dc/terms/available");

	/**
	 * description: Recommended practice is to include sufficient bibliographic detail to identify the resource as unambiguously as possible.
comment: A bibliographic reference for the resource.

	 */
	public static final IRI bibliographicCitation = new IRI("http://purl.org/dc/terms/bibliographicCitation");

	/**
	 * comment: An established standard to which the described resource conforms.

	 */
	public static final IRI conformsTo = new IRI("http://purl.org/dc/terms/conformsTo");

	/**
	 * description: Examples of a Contributor include a person, an organization, or a service. Typically, the name of a Contributor should be used to indicate the entity.
comment: An entity responsible for making contributions to the resource.

	 */
	public static final IRI contributor = new IRI("http://purl.org/dc/terms/contributor");

	/**
	 * description: Spatial topic and spatial applicability may be a named place or a location specified by its geographic coordinates. Temporal topic may be a named period, date, or date range. A jurisdiction may be a named administrative entity or a geographic place to which the resource applies. Recommended best practice is to use a controlled vocabulary such as the Thesaurus of Geographic Names [TGN]. Where appropriate, named places or time periods can be used in preference to numeric identifiers such as sets of coordinates or date ranges.
comment: The spatial or temporal topic of the resource, the spatial applicability of the resource, or the jurisdiction under which the resource is relevant.

	 */
	public static final IRI coverage = new IRI("http://purl.org/dc/terms/coverage");

	/**
	 * comment: Date of creation of the resource.

	 */
	public static final IRI created = new IRI("http://purl.org/dc/terms/created");

	/**
	 * description: Examples of a Creator include a person, an organization, or a service. Typically, the name of a Creator should be used to indicate the entity.
comment: An entity primarily responsible for making the resource.

	 */
	public static final IRI creator = new IRI("http://purl.org/dc/terms/creator");

	/**
	 * description: Date may be used to express temporal information at any level of granularity.  Recommended best practice is to use an encoding scheme, such as the W3CDTF profile of ISO 8601 [W3CDTF].
comment: A point or period of time associated with an event in the lifecycle of the resource.

	 */
	public static final IRI date = new IRI("http://purl.org/dc/terms/date");

	/**
	 * description: Examples of resources to which a Date Accepted may be relevant are a thesis (accepted by a university department) or an article (accepted by a journal).
comment: Date of acceptance of the resource.

	 */
	public static final IRI dateAccepted = new IRI("http://purl.org/dc/terms/dateAccepted");

	/**
	 * comment: Date of copyright.

	 */
	public static final IRI dateCopyrighted = new IRI("http://purl.org/dc/terms/dateCopyrighted");

	/**
	 * description: Examples of resources to which a Date Submitted may be relevant are a thesis (submitted to a university department) or an article (submitted to a journal).
comment: Date of submission of the resource.

	 */
	public static final IRI dateSubmitted = new IRI("http://purl.org/dc/terms/dateSubmitted");

	/**
	 * description: Description may include but is not limited to: an abstract, a table of contents, a graphical representation, or a free-text account of the resource.
comment: An account of the resource.

	 */
	public static final IRI description = new IRI("http://purl.org/dc/terms/description");

	/**
	 * comment: A class of entity, defined in terms of progression through an educational or training context, for which the described resource is intended.

	 */
	public static final IRI educationLevel = new IRI("http://purl.org/dc/terms/educationLevel");

	/**
	 * comment: The size or duration of the resource.

	 */
	public static final IRI extent = new IRI("http://purl.org/dc/terms/extent");

	/**
	 * description: Examples of dimensions include size and duration. Recommended best practice is to use a controlled vocabulary such as the list of Internet Media Types [MIME].
comment: The file format, physical medium, or dimensions of the resource.

	 */
	public static final IRI format = new IRI("http://purl.org/dc/terms/format");

	/**
	 * comment: A related resource that is substantially the same as the pre-existing described resource, but in another format.
note: This term is intended to be used with non-literal values as defined in the DCMI Abstract Model (http://dublincore.org/documents/abstract-model/).  As of December 2007, the DCMI Usage Board is seeking a way to express this intention with a formal range declaration.

	 */
	public static final IRI hasFormat = new IRI("http://purl.org/dc/terms/hasFormat");

	/**
	 * comment: A related resource that is included either physically or logically in the described resource.
note: This term is intended to be used with non-literal values as defined in the DCMI Abstract Model (http://dublincore.org/documents/abstract-model/).  As of December 2007, the DCMI Usage Board is seeking a way to express this intention with a formal range declaration.

	 */
	public static final IRI hasPart = new IRI("http://purl.org/dc/terms/hasPart");

	/**
	 * comment: A related resource that is a version, edition, or adaptation of the described resource.
note: This term is intended to be used with non-literal values as defined in the DCMI Abstract Model (http://dublincore.org/documents/abstract-model/).  As of December 2007, the DCMI Usage Board is seeking a way to express this intention with a formal range declaration.

	 */
	public static final IRI hasVersion = new IRI("http://purl.org/dc/terms/hasVersion");

	/**
	 * description: Recommended best practice is to identify the resource by means of a string conforming to a formal identification system. 
comment: An unambiguous reference to the resource within a given context.

	 */
	public static final IRI identifier = new IRI("http://purl.org/dc/terms/identifier");

	/**
	 * description: Instructional Method will typically include ways of presenting instructional materials or conducting instructional activities, patterns of learner-to-learner and learner-to-instructor interactions, and mechanisms by which group and individual levels of learning are measured.  Instructional methods include all aspects of the instruction and learning processes from planning and implementation through evaluation and feedback.
comment: A process, used to engender knowledge, attitudes and skills, that the described resource is designed to support.

	 */
	public static final IRI instructionalMethod = new IRI("http://purl.org/dc/terms/instructionalMethod");

	/**
	 * comment: A related resource that is substantially the same as the described resource, but in another format.
note: This term is intended to be used with non-literal values as defined in the DCMI Abstract Model (http://dublincore.org/documents/abstract-model/).  As of December 2007, the DCMI Usage Board is seeking a way to express this intention with a formal range declaration.

	 */
	public static final IRI isFormatOf = new IRI("http://purl.org/dc/terms/isFormatOf");

	/**
	 * comment: A related resource in which the described resource is physically or logically included.
note: This term is intended to be used with non-literal values as defined in the DCMI Abstract Model (http://dublincore.org/documents/abstract-model/).  As of December 2007, the DCMI Usage Board is seeking a way to express this intention with a formal range declaration.

	 */
	public static final IRI isPartOf = new IRI("http://purl.org/dc/terms/isPartOf");

	/**
	 * comment: A related resource that references, cites, or otherwise points to the described resource.
note: This term is intended to be used with non-literal values as defined in the DCMI Abstract Model (http://dublincore.org/documents/abstract-model/).  As of December 2007, the DCMI Usage Board is seeking a way to express this intention with a formal range declaration.

	 */
	public static final IRI isReferencedBy = new IRI("http://purl.org/dc/terms/isReferencedBy");

	/**
	 * comment: A related resource that supplants, displaces, or supersedes the described resource.
note: This term is intended to be used with non-literal values as defined in the DCMI Abstract Model (http://dublincore.org/documents/abstract-model/).  As of December 2007, the DCMI Usage Board is seeking a way to express this intention with a formal range declaration.

	 */
	public static final IRI isReplacedBy = new IRI("http://purl.org/dc/terms/isReplacedBy");

	/**
	 * comment: A related resource that requires the described resource to support its function, delivery, or coherence.
note: This term is intended to be used with non-literal values as defined in the DCMI Abstract Model (http://dublincore.org/documents/abstract-model/).  As of December 2007, the DCMI Usage Board is seeking a way to express this intention with a formal range declaration.

	 */
	public static final IRI isRequiredBy = new IRI("http://purl.org/dc/terms/isRequiredBy");

	/**
	 * description: Changes in version imply substantive changes in content rather than differences in format.
comment: A related resource of which the described resource is a version, edition, or adaptation.
note: This term is intended to be used with non-literal values as defined in the DCMI Abstract Model (http://dublincore.org/documents/abstract-model/).  As of December 2007, the DCMI Usage Board is seeking a way to express this intention with a formal range declaration.

	 */
	public static final IRI isVersionOf = new IRI("http://purl.org/dc/terms/isVersionOf");

	/**
	 * comment: Date of formal issuance (e.g., publication) of the resource.

	 */
	public static final IRI issued = new IRI("http://purl.org/dc/terms/issued");

	/**
	 * description: Recommended best practice is to use a controlled vocabulary such as RFC 4646 [RFC4646].
comment: A language of the resource.

	 */
	public static final IRI language = new IRI("http://purl.org/dc/terms/language");

	/**
	 * comment: A legal document giving official permission to do something with the resource.

	 */
	public static final IRI license = new IRI("http://purl.org/dc/terms/license");

	/**
	 * description: In an educational context, a mediator might be a parent, teacher, teaching assistant, or care-giver.
comment: An entity that mediates access to the resource and for whom the resource is intended or useful.

	 */
	public static final IRI mediator = new IRI("http://purl.org/dc/terms/mediator");

	/**
	 * comment: The material or physical carrier of the resource.

	 */
	public static final IRI medium = new IRI("http://purl.org/dc/terms/medium");

	/**
	 * comment: Date on which the resource was changed.

	 */
	public static final IRI modified = new IRI("http://purl.org/dc/terms/modified");

	/**
	 * description: The statement may include a description of any changes successive custodians made to the resource.
comment: A statement of any changes in ownership and custody of the resource since its creation that are significant for its authenticity, integrity, and interpretation.

	 */
	public static final IRI provenance = new IRI("http://purl.org/dc/terms/provenance");

	/**
	 * description: Examples of a Publisher include a person, an organization, or a service. Typically, the name of a Publisher should be used to indicate the entity.
comment: An entity responsible for making the resource available.

	 */
	public static final IRI publisher = new IRI("http://purl.org/dc/terms/publisher");

	/**
	 * comment: A related resource that is referenced, cited, or otherwise pointed to by the described resource.
note: This term is intended to be used with non-literal values as defined in the DCMI Abstract Model (http://dublincore.org/documents/abstract-model/).  As of December 2007, the DCMI Usage Board is seeking a way to express this intention with a formal range declaration.

	 */
	public static final IRI references = new IRI("http://purl.org/dc/terms/references");

	/**
	 * description: Recommended best practice is to identify the related resource by means of a string conforming to a formal identification system. 
comment: A related resource.
note: This term is intended to be used with non-literal values as defined in the DCMI Abstract Model (http://dublincore.org/documents/abstract-model/).  As of December 2007, the DCMI Usage Board is seeking a way to express this intention with a formal range declaration.

	 */
	public static final IRI relation = new IRI("http://purl.org/dc/terms/relation");

	/**
	 * comment: A related resource that is supplanted, displaced, or superseded by the described resource.
note: This term is intended to be used with non-literal values as defined in the DCMI Abstract Model (http://dublincore.org/documents/abstract-model/).  As of December 2007, the DCMI Usage Board is seeking a way to express this intention with a formal range declaration.

	 */
	public static final IRI replaces = new IRI("http://purl.org/dc/terms/replaces");

	/**
	 * comment: A related resource that is required by the described resource to support its function, delivery, or coherence.
note: This term is intended to be used with non-literal values as defined in the DCMI Abstract Model (http://dublincore.org/documents/abstract-model/).  As of December 2007, the DCMI Usage Board is seeking a way to express this intention with a formal range declaration.

	 */
	public static final IRI requires = new IRI("http://purl.org/dc/terms/requires");

	/**
	 * description: Typically, rights information includes a statement about various property rights associated with the resource, including intellectual property rights.
comment: Information about rights held in and over the resource.

	 */
	public static final IRI rights = new IRI("http://purl.org/dc/terms/rights");

	/**
	 * comment: A person or organization owning or managing rights over the resource.

	 */
	public static final IRI rightsHolder = new IRI("http://purl.org/dc/terms/rightsHolder");

	/**
	 * description: The described resource may be derived from the related resource in whole or in part. Recommended best practice is to identify the related resource by means of a string conforming to a formal identification system.
comment: A related resource from which the described resource is derived.
note: This term is intended to be used with non-literal values as defined in the DCMI Abstract Model (http://dublincore.org/documents/abstract-model/).  As of December 2007, the DCMI Usage Board is seeking a way to express this intention with a formal range declaration.

	 */
	public static final IRI source = new IRI("http://purl.org/dc/terms/source");

	/**
	 * comment: Spatial characteristics of the resource.

	 */
	public static final IRI spatial = new IRI("http://purl.org/dc/terms/spatial");

	/**
	 * description: Typically, the subject will be represented using keywords, key phrases, or classification codes. Recommended best practice is to use a controlled vocabulary. To describe the spatial or temporal topic of the resource, use the Coverage element.
comment: The topic of the resource.
note: This term is intended to be used with non-literal values as defined in the DCMI Abstract Model (http://dublincore.org/documents/abstract-model/).  As of December 2007, the DCMI Usage Board is seeking a way to express this intention with a formal range declaration.

	 */
	public static final IRI subject = new IRI("http://purl.org/dc/terms/subject");

	/**
	 * comment: A list of subunits of the resource.

	 */
	public static final IRI tableOfContents = new IRI("http://purl.org/dc/terms/tableOfContents");

	/**
	 * comment: Temporal characteristics of the resource.

	 */
	public static final IRI temporal = new IRI("http://purl.org/dc/terms/temporal");

	/**
	 * description: A name given to the resource.
note: In current practice, this term is used primarily with literal values; however, there are important uses with non-literal values as well.  As of December 2007, the DCMI Usage Board is leaving this range unspecified pending an investigation of options.

	 */
	public static final IRI title = new IRI("http://purl.org/dc/terms/title");

	/**
	 * description: Recommended best practice is to use a controlled vocabulary such as the DCMI Type Vocabulary [DCMITYPE]. To describe the file format, physical medium, or dimensions of the resource, use the Format element.
comment: The nature or genre of the resource.

	 */
	public static final IRI type = new IRI("http://purl.org/dc/terms/type");

	/**
	 * comment: Date (often a range) of validity of a resource.

	 */
	public static final IRI valid = new IRI("http://purl.org/dc/terms/valid");

	// Properties

	/**
	 * comment: The set of conceptual resources specified by the Dewey Decimal Classification.

	 */
	public static final IRI DDC = new IRI("http://purl.org/dc/terms/DDC");

	/**
	 * comment: The set of classes specified by the DCMI Type Vocabulary, used to categorize the nature or genre of the resource.

	 */
	public static final IRI DCMIType = new IRI("http://purl.org/dc/terms/DCMIType");

	/**
	 * comment: The set of media types specified by the Internet Assigned Numbers Authority.

	 */
	public static final IRI IMT = new IRI("http://purl.org/dc/terms/IMT");

	/**
	 * comment: The set of labeled concepts specified by the Library of Congress Subject Headings.

	 */
	public static final IRI LCSH = new IRI("http://purl.org/dc/terms/LCSH");

	/**
	 * comment: The set of labeled concepts specified by the Medical Subject Headings.

	 */
	public static final IRI MESH = new IRI("http://purl.org/dc/terms/MESH");

	/**
	 * comment: The set of conceptual resources specified by the Universal Decimal Classification.

	 */
	public static final IRI UDC = new IRI("http://purl.org/dc/terms/UDC");

	/**
	 * comment: The set of places specified by the Getty Thesaurus of Geographic Names.

	 */
	public static final IRI TGN = new IRI("http://purl.org/dc/terms/TGN");

	/**
	 * comment: The set of conceptual resources specified by the National Library of Medicine Classification.

	 */
	public static final IRI NLM = new IRI("http://purl.org/dc/terms/NLM");

	/**
	 * comment: The set of conceptual resources specified by the Library of Congress Classification.

	 */
	public static final IRI LCC = new IRI("http://purl.org/dc/terms/LCC");
}
