package org.apache.clerezza.ontologies;

import org.apache.clerezza.api.IRI;

public class SKOS08 {
	// Classes

	/**
	 * definition: A meaningful collection of concepts.
comment: Labelled collections can be used with collectable semantic relation properties e.g. skos:narrower, where you would like a set of concepts to be displayed under a 'node label' in the hierarchy.

	 */
	public static final IRI Collection = new IRI("http://www.w3.org/2008/05/skos#Collection");

	/**
	 * definition: An abstract idea or notion; a unit of thought.

	 */
	public static final IRI Concept = new IRI("http://www.w3.org/2008/05/skos#Concept");

	/**
	 * definition: A set of concepts, optionally including statements about semantic relationships between those concepts.
comment: A concept scheme may be defined to include concepts from different sources.
comment: Thesauri, classification schemes, subject heading lists, taxonomies, 'folksonomies', and other types of controlled vocabulary are all examples of concept schemes.  Concept schemes are also embedded in glossaries and terminologies.

	 */
	public static final IRI ConceptScheme = new IRI("http://www.w3.org/2008/05/skos#ConceptScheme");

	/**
	 * definition: An ordered collection of concepts, where both the grouping and the ordering are meaningful.
comment: Ordered collections can be used with collectable semantic relation properties, where you would like a set of concepts to be displayed in a specific order, and optionally under a 'node label'.

	 */
	public static final IRI OrderedCollection = new IRI("http://www.w3.org/2008/05/skos#OrderedCollection");

	// Properties

	/**
	 * definition: An alternative lexical label for a resource.
comment: skos:prefLabel, skos:altLabel and skos:hiddenLabel are pairwise disjoint properties.
comment: The range of skos:altLabel is the class of RDF plain literals.
comment: Acronyms, abbreviations, spelling variants, and irregular plural/singular forms may be included among the alternative labels for a concept.  Mis-spelled terms are normally included as hidden labels (see skos:hiddenLabel).

	 */
	public static final IRI altLabel = new IRI("http://www.w3.org/2008/05/skos#altLabel");

	/**
	 * definition: skos:broadMatch is used to state a hierarchical mapping link between two conceptual resources in different concept schemes.

	 */
	public static final IRI broadMatch = new IRI("http://www.w3.org/2008/05/skos#broadMatch");

	/**
	 * definition: A concept that is more general in meaning.
comment: By convention, skos:broader is only used to assert an immediate (i.e. direct) hierarchical link between two conceptual resources.
comment: Broader concepts are typically rendered as parents in a concept hierarchy (tree).

	 */
	public static final IRI broader = new IRI("http://www.w3.org/2008/05/skos#broader");

	/**
	 * definition: skos:broaderTransitive is a transitive superproperty of skos:broader.
comment: By convention, skos:broaderTransitive is not used to make assertions. Rather, the properties can be used to draw inferences about the transitive closure of the hierarchical relation, which is useful e.g. when implementing a simple query expansion algorithm in a search application.

	 */
	public static final IRI broaderTransitive = new IRI("http://www.w3.org/2008/05/skos#broaderTransitive");

	/**
	 * definition: A note about a modification to a concept.

	 */
	public static final IRI changeNote = new IRI("http://www.w3.org/2008/05/skos#changeNote");

	/**
	 * definition: skos:closeMatch is used to link two concepts that are sufficiently similar that they can be used interchangeably in some information retrieval applications. In order to avoid the possibility of "compound errors" when combining mappings across more than two concept schemes, skos:closeMatch is not declared to be a transitive property.

	 */
	public static final IRI closeMatch = new IRI("http://www.w3.org/2008/05/skos#closeMatch");

	/**
	 * definition: A statement or formal explanation of the meaning of a concept.

	 */
	public static final IRI definition = new IRI("http://www.w3.org/2008/05/skos#definition");

	/**
	 * definition: A note for an editor, translator or maintainer of the vocabulary.

	 */
	public static final IRI editorialNote = new IRI("http://www.w3.org/2008/05/skos#editorialNote");

	/**
	 * definition: skos:exactMatch is used to link two concepts, indicating a high degree of confidence that the concepts can be used interchangeably across a wide range of information retrieval applications. skos:exactMatch is a transitive property, and is a sub-property of skos:closeMatch.
comment: skos:exactMatch is disjoint with each of the properties skos:broadMatch and skos:relatedMatch.

	 */
	public static final IRI exactMatch = new IRI("http://www.w3.org/2008/05/skos#exactMatch");

	/**
	 * definition: An example of the use of a concept.

	 */
	public static final IRI example = new IRI("http://www.w3.org/2008/05/skos#example");

	/**
	 * definition: A top level concept in the concept scheme.

	 */
	public static final IRI hasTopConcept = new IRI("http://www.w3.org/2008/05/skos#hasTopConcept");

	/**
	 * definition: A lexical label for a resource that should be hidden when generating visual displays of the resource, but should still be accessible to free text search operations.
comment: The range of skos:hiddenLabel is the class of RDF plain literals.
comment: skos:prefLabel, skos:altLabel and skos:hiddenLabel are pairwise disjoint properties.

	 */
	public static final IRI hiddenLabel = new IRI("http://www.w3.org/2008/05/skos#hiddenLabel");

	/**
	 * definition: A note about the past state/use/meaning of a concept.

	 */
	public static final IRI historyNote = new IRI("http://www.w3.org/2008/05/skos#historyNote");

	/**
	 * definition: A concept scheme in which the concept is included.
comment: A concept may be a member of more than one concept scheme.

	 */
	public static final IRI inScheme = new IRI("http://www.w3.org/2008/05/skos#inScheme");

	/**
	 * definition: Definition
comment: These concept mapping relations mirror semantic relations, and the data model defined below is similar (with the exception of skos:exactMatch) to the data model defined for semantic relations. A distinct vocabulary is provided for concept mapping relations, to provide a convenient way to differentiate links within a concept scheme from links between concept schemes. However, this pattern of usage is not a formal requirement of the SKOS data model, and relies on informal definitions of best practice.

	 */
	public static final IRI mappingRelation = new IRI("http://www.w3.org/2008/05/skos#mappingRelation");

	/**
	 * definition: A member of a collection.

	 */
	public static final IRI member = new IRI("http://www.w3.org/2008/05/skos#member");

	/**
	 * definition: An RDF list containing the members of an ordered collection.
comment: For any resource, every item in the list given as the value of the skos:memberList property is also a value of the skos:member property.

	 */
	public static final IRI memberList = new IRI("http://www.w3.org/2008/05/skos#memberList");

	/**
	 * definition: skos:narrowMatch is used to state a hierarchical mapping link between two conceptual resources in different concept schemes.

	 */
	public static final IRI narrowMatch = new IRI("http://www.w3.org/2008/05/skos#narrowMatch");

	/**
	 * definition: A concept that is more specific in meaning.
comment: By convention, skos:broader is only used to assert an immediate (i.e. direct) hierarchical link between two conceptual resources.
comment: Narrower concepts are typically rendered as children in a concept hierarchy (tree).

	 */
	public static final IRI narrower = new IRI("http://www.w3.org/2008/05/skos#narrower");

	/**
	 * definition: skos:narrowerTransitive is a transitive superproperty of skos:broader. By convention, skos:narrowerTransitive is not intended to be used in assertions, but provides a mechanism whereby the transitive closure of skos:narrower can be queried.
comment: By convention, skos:narrowerTransitive is not used to make assertions. Rather, the properties can be used to draw inferences about the transitive closure of the hierarchical relation, which is useful e.g. when implementing a simple query expansion algorithm in a search application.

	 */
	public static final IRI narrowerTransitive = new IRI("http://www.w3.org/2008/05/skos#narrowerTransitive");

	/**
	 * definition: A notation, also known as classification code, is a string of characters such as "T58.5" or "303.4833" used to uniquely identify a concept within the scope of a given concept scheme.
comment: By convention, skos:notation is used with a typed literal in the object position of the triple.

	 */
	public static final IRI notation = new IRI("http://www.w3.org/2008/05/skos#notation");

	/**
	 * definition: A general note, for any purpose.
comment: This property may be used directly, or as a super-property for more specific note types.

	 */
	public static final IRI note = new IRI("http://www.w3.org/2008/05/skos#note");

	/**
	 * definition: The preferred lexical label for a resource, in a given language.
comment: The range of skos:prefLabel is the class of RDF plain literals.
comment: skos:prefLabel, skos:altLabel and skos:hiddenLabel are pairwise disjoint properties.
comment: No two concepts in the same concept scheme may have the same value for skos:prefLabel in a given language.

	 */
	public static final IRI prefLabel = new IRI("http://www.w3.org/2008/05/skos#prefLabel");

	/**
	 * definition: A concept with which there is an associative semantic relationship.
comment: skos:related is disjoint with skos:broaderTransitive

	 */
	public static final IRI related = new IRI("http://www.w3.org/2008/05/skos#related");

	/**
	 * definition: skos:relatedMatch is used to state an associative mapping link between two conceptual resources in different concept schemes.

	 */
	public static final IRI relatedMatch = new IRI("http://www.w3.org/2008/05/skos#relatedMatch");

	/**
	 * definition: A note that helps to clarify the meaning of a concept.

	 */
	public static final IRI scopeNote = new IRI("http://www.w3.org/2008/05/skos#scopeNote");

	/**
	 * definition: A concept related by meaning.
comment: This property should not be used directly, but as a super-property for all properties denoting a relationship of meaning between concepts.

	 */
	public static final IRI semanticRelation = new IRI("http://www.w3.org/2008/05/skos#semanticRelation");

	/**
	 * definition: Relates a concept to the concept scheme that it is a top level concept of.

	 */
	public static final IRI topConceptOf = new IRI("http://www.w3.org/2008/05/skos#topConceptOf");

	// Properties

	/**
	 * 
	 */
	public static final IRI skos = new IRI("http://www.w3.org/2008/05/skos");
}
