package org.apache.clerezza.ontologies;

import org.apache.clerezza.IRI;

public class RDFS {
	// Classes

	/**
	 * comment: The class of classes.

	 */
	public static final IRI Class = new IRI("http://www.w3.org/2000/01/rdf-schema#Class");

	/**
	 * comment: The class of RDF containers.

	 */
	public static final IRI Container = new IRI("http://www.w3.org/2000/01/rdf-schema#Container");

	/**
	 * comment: The class of container membership properties, rdf:_1, rdf:_2, ...,
                    all of which are sub-properties of 'member'.

	 */
	public static final IRI ContainerMembershipProperty = new IRI("http://www.w3.org/2000/01/rdf-schema#ContainerMembershipProperty");

	/**
	 * comment: The class of RDF datatypes.

	 */
	public static final IRI Datatype = new IRI("http://www.w3.org/2000/01/rdf-schema#Datatype");

	/**
	 * comment: The class of literal values, eg. textual strings and integers.

	 */
	public static final IRI Literal = new IRI("http://www.w3.org/2000/01/rdf-schema#Literal");

	/**
	 * comment: The class resource, everything.

	 */
	public static final IRI Resource = new IRI("http://www.w3.org/2000/01/rdf-schema#Resource");

	// Properties

	/**
	 * comment: A description of the subject resource.

	 */
	public static final IRI comment = new IRI("http://www.w3.org/2000/01/rdf-schema#comment");

	/**
	 * comment: A domain of the subject property.

	 */
	public static final IRI domain = new IRI("http://www.w3.org/2000/01/rdf-schema#domain");

	/**
	 * comment: The defininition of the subject resource.

	 */
	public static final IRI isDefinedBy = new IRI("http://www.w3.org/2000/01/rdf-schema#isDefinedBy");

	/**
	 * comment: A human-readable name for the subject.

	 */
	public static final IRI label = new IRI("http://www.w3.org/2000/01/rdf-schema#label");

	/**
	 * comment: A member of the subject resource.

	 */
	public static final IRI member = new IRI("http://www.w3.org/2000/01/rdf-schema#member");

	/**
	 * comment: A range of the subject property.

	 */
	public static final IRI range = new IRI("http://www.w3.org/2000/01/rdf-schema#range");

	/**
	 * comment: Further information about the subject resource.

	 */
	public static final IRI seeAlso = new IRI("http://www.w3.org/2000/01/rdf-schema#seeAlso");

	/**
	 * comment: The subject is a subclass of a class.

	 */
	public static final IRI subClassOf = new IRI("http://www.w3.org/2000/01/rdf-schema#subClassOf");

	/**
	 * comment: The subject is a subproperty of a property.

	 */
	public static final IRI subPropertyOf = new IRI("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");

	// Properties

	/**
	 * 
	 */
	public static final IRI THIS_ONTOLOGY = new IRI("http://www.w3.org/2000/01/rdf-schema#");
}
