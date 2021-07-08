package org.apache.clerezza.ontologies;

import org.apache.clerezza.IRI;

public class SKOS04 {
	// Classes

	/**
	 * 
	 */
	public static final IRI Collection = new IRI("http://www.w3.org/2004/02/skos/core#Collection");

	/**
	 * 
	 */
	public static final IRI Concept = new IRI("http://www.w3.org/2004/02/skos/core#Concept");

	/**
	 * example: Thesauri, classification schemes, subject heading lists, taxonomies, 'folksonomies', and other types of controlled vocabulary are all examples of concept schemes. Concept schemes are also embedded in glossaries and terminologies.

	 */
	public static final IRI ConceptScheme = new IRI("http://www.w3.org/2004/02/skos/core#ConceptScheme");

	/**
	 * 
	 */
	public static final IRI OrderedCollection = new IRI("http://www.w3.org/2004/02/skos/core#OrderedCollection");

	// Properties

	/**
	 * comment: The range of skos:altLabel is the class of RDF plain literals.
comment: skos:prefLabel, skos:altLabel and skos:hiddenLabel are pairwise disjoint properties.
example: Acronyms, abbreviations, spelling variants, and irregular plural/singular forms may be included among the alternative labels for a concept. Mis-spelled terms are normally included as hidden labels (see skos:hiddenLabel).

	 */
	public static final IRI altLabel = new IRI("http://www.w3.org/2004/02/skos/core#altLabel");

	/**
	 * 
	 */
	public static final IRI broadMatch = new IRI("http://www.w3.org/2004/02/skos/core#broadMatch");

	/**
	 * comment: Broader concepts are typically rendered as parents in a concept hierarchy (tree).

	 */
	public static final IRI broader = new IRI("http://www.w3.org/2004/02/skos/core#broader");

	/**
	 * 
	 */
	public static final IRI broaderTransitive = new IRI("http://www.w3.org/2004/02/skos/core#broaderTransitive");

	/**
	 * 
	 */
	public static final IRI changeNote = new IRI("http://www.w3.org/2004/02/skos/core#changeNote");

	/**
	 * 
	 */
	public static final IRI closeMatch = new IRI("http://www.w3.org/2004/02/skos/core#closeMatch");

	/**
	 * 
	 */
	public static final IRI definition = new IRI("http://www.w3.org/2004/02/skos/core#definition");

	/**
	 * 
	 */
	public static final IRI editorialNote = new IRI("http://www.w3.org/2004/02/skos/core#editorialNote");

	/**
	 * comment: skos:exactMatch is disjoint with each of the properties skos:broadMatch and skos:relatedMatch.

	 */
	public static final IRI exactMatch = new IRI("http://www.w3.org/2004/02/skos/core#exactMatch");

	/**
	 * 
	 */
	public static final IRI example = new IRI("http://www.w3.org/2004/02/skos/core#example");

	/**
	 * 
	 */
	public static final IRI hasTopConcept = new IRI("http://www.w3.org/2004/02/skos/core#hasTopConcept");

	/**
	 * comment: skos:prefLabel, skos:altLabel and skos:hiddenLabel are pairwise disjoint properties.
comment: The range of skos:hiddenLabel is the class of RDF plain literals.

	 */
	public static final IRI hiddenLabel = new IRI("http://www.w3.org/2004/02/skos/core#hiddenLabel");

	/**
	 * 
	 */
	public static final IRI historyNote = new IRI("http://www.w3.org/2004/02/skos/core#historyNote");

	/**
	 * 
	 */
	public static final IRI inScheme = new IRI("http://www.w3.org/2004/02/skos/core#inScheme");

	/**
	 * comment: These concept mapping relations mirror semantic relations, and the data model defined below is similar (with the exception of skos:exactMatch) to the data model defined for semantic relations. A distinct vocabulary is provided for concept mapping relations, to provide a convenient way to differentiate links within a concept scheme from links between concept schemes. However, this pattern of usage is not a formal requirement of the SKOS data model, and relies on informal definitions of best practice.

	 */
	public static final IRI mappingRelation = new IRI("http://www.w3.org/2004/02/skos/core#mappingRelation");

	/**
	 * 
	 */
	public static final IRI member = new IRI("http://www.w3.org/2004/02/skos/core#member");

	/**
	 * comment: For any resource, every item in the list given as the value of the
      skos:memberList property is also a value of the skos:member property.

	 */
	public static final IRI memberList = new IRI("http://www.w3.org/2004/02/skos/core#memberList");

	/**
	 * 
	 */
	public static final IRI narrowMatch = new IRI("http://www.w3.org/2004/02/skos/core#narrowMatch");

	/**
	 * comment: Narrower concepts are typically rendered as children in a concept hierarchy (tree).

	 */
	public static final IRI narrower = new IRI("http://www.w3.org/2004/02/skos/core#narrower");

	/**
	 * 
	 */
	public static final IRI narrowerTransitive = new IRI("http://www.w3.org/2004/02/skos/core#narrowerTransitive");

	/**
	 * 
	 */
	public static final IRI notation = new IRI("http://www.w3.org/2004/02/skos/core#notation");

	/**
	 * 
	 */
	public static final IRI note = new IRI("http://www.w3.org/2004/02/skos/core#note");

	/**
	 * comment: A resource has no more than one value of skos:prefLabel per language tag, and no more than one value of skos:prefLabel without language tag.
comment: skos:prefLabel, skos:altLabel and skos:hiddenLabel are pairwise
      disjoint properties.
comment: The range of skos:prefLabel is the class of RDF plain literals.

	 */
	public static final IRI prefLabel = new IRI("http://www.w3.org/2004/02/skos/core#prefLabel");

	/**
	 * comment: skos:related is disjoint with skos:broaderTransitive

	 */
	public static final IRI related = new IRI("http://www.w3.org/2004/02/skos/core#related");

	/**
	 * 
	 */
	public static final IRI relatedMatch = new IRI("http://www.w3.org/2004/02/skos/core#relatedMatch");

	/**
	 * 
	 */
	public static final IRI scopeNote = new IRI("http://www.w3.org/2004/02/skos/core#scopeNote");

	/**
	 * 
	 */
	public static final IRI semanticRelation = new IRI("http://www.w3.org/2004/02/skos/core#semanticRelation");

	/**
	 * 
	 */
	public static final IRI topConceptOf = new IRI("http://www.w3.org/2004/02/skos/core#topConceptOf");

	// Properties

	/**
	 * title: SKOS Vocabulary
description: An RDF vocabulary for describing the basic structure and content of concept schemes such as thesauri, classification schemes, subject heading lists, taxonomies, 'folksonomies', other types of controlled vocabulary, and also concept schemes embedded in glossaries and terminologies.

	 */
	public static final IRI core = new IRI("http://www.w3.org/2004/02/skos/core");
}
