package org.apache.clerezza.ontologies;

import org.apache.clerezza.IRI;

public class RDF {
	// Classes

	/**
	 * comment: The class of containers of alternatives.

	 */
	public static final IRI Alt = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt");

	/**
	 * comment: The class of unordered containers.

	 */
	public static final IRI Bag = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag");

	/**
	 * comment: The class of RDF Lists.

	 */
	public static final IRI List = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#List");

	/**
	 * comment: The class of RDF properties.

	 */
	public static final IRI Property = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property");

	/**
	 * comment: The class of ordered containers.

	 */
	public static final IRI Seq = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq");

	/**
	 * comment: The class of RDF statements.

	 */
	public static final IRI Statement = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement");

	/**
	 * comment: The class of XML literal values.

	 */
	public static final IRI XMLLiteral = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral");

	// Properties

	/**
	 * comment: The first item in the subject RDF list.

	 */
	public static final IRI first = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#first");

	/**
	 * comment: The object of the subject RDF statement.

	 */
	public static final IRI object = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#object");

	/**
	 * comment: The predicate of the subject RDF statement.

	 */
	public static final IRI predicate = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate");

	/**
	 * comment: The rest of the subject RDF list after the first item.

	 */
	public static final IRI rest = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest");

	/**
	 * comment: The subject of the subject RDF statement.

	 */
	public static final IRI subject = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject");

	/**
	 * comment: The subject is an instance of a class.

	 */
	public static final IRI type = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

	/**
	 * comment: Idiomatic property used for structured values.

	 */
	public static final IRI value = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#value");

	// Properties

	/**
	 * 
	 */
	public static final IRI THIS_ONTOLOGY = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#");

	/**
	 * comment: The empty list, with no items in it. If the rest of a list is nil then the list has no more items in it.

	 */
	public static final IRI nil = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil");
}
