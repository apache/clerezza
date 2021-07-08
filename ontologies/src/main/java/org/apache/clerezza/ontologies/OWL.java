package org.apache.clerezza.ontologies;

import org.apache.clerezza.IRI;

public class OWL {
	// Classes

	/**
	 * 
	 */
	public static final IRI AllDifferent = new IRI("http://www.w3.org/2002/07/owl#AllDifferent");

	/**
	 * 
	 */
	public static final IRI AnnotationProperty = new IRI("http://www.w3.org/2002/07/owl#AnnotationProperty");

	/**
	 * 
	 */
	public static final IRI Class = new IRI("http://www.w3.org/2002/07/owl#Class");

	/**
	 * 
	 */
	public static final IRI DataRange = new IRI("http://www.w3.org/2002/07/owl#DataRange");

	/**
	 * 
	 */
	public static final IRI DatatypeProperty = new IRI("http://www.w3.org/2002/07/owl#DatatypeProperty");

	/**
	 * 
	 */
	public static final IRI DeprecatedClass = new IRI("http://www.w3.org/2002/07/owl#DeprecatedClass");

	/**
	 * 
	 */
	public static final IRI DeprecatedProperty = new IRI("http://www.w3.org/2002/07/owl#DeprecatedProperty");

	/**
	 * 
	 */
	public static final IRI FunctionalProperty = new IRI("http://www.w3.org/2002/07/owl#FunctionalProperty");

	/**
	 * 
	 */
	public static final IRI InverseFunctionalProperty = new IRI("http://www.w3.org/2002/07/owl#InverseFunctionalProperty");

	/**
	 * 
	 */
	public static final IRI Nothing = new IRI("http://www.w3.org/2002/07/owl#Nothing");

	/**
	 * 
	 */
	public static final IRI ObjectProperty = new IRI("http://www.w3.org/2002/07/owl#ObjectProperty");

	/**
	 * 
	 */
	public static final IRI Ontology = new IRI("http://www.w3.org/2002/07/owl#Ontology");

	/**
	 * 
	 */
	public static final IRI OntologyProperty = new IRI("http://www.w3.org/2002/07/owl#OntologyProperty");

	/**
	 * 
	 */
	public static final IRI Restriction = new IRI("http://www.w3.org/2002/07/owl#Restriction");

	/**
	 * 
	 */
	public static final IRI SymmetricProperty = new IRI("http://www.w3.org/2002/07/owl#SymmetricProperty");

	/**
	 * 
	 */
	public static final IRI Thing = new IRI("http://www.w3.org/2002/07/owl#Thing");

	/**
	 * 
	 */
	public static final IRI TransitiveProperty = new IRI("http://www.w3.org/2002/07/owl#TransitiveProperty");

	// Properties

	/**
	 * 
	 */
	public static final IRI allValuesFrom = new IRI("http://www.w3.org/2002/07/owl#allValuesFrom");

	/**
	 * 
	 */
	public static final IRI backwardCompatibleWith = new IRI("http://www.w3.org/2002/07/owl#backwardCompatibleWith");

	/**
	 * 
	 */
	public static final IRI cardinality = new IRI("http://www.w3.org/2002/07/owl#cardinality");

	/**
	 * 
	 */
	public static final IRI complementOf = new IRI("http://www.w3.org/2002/07/owl#complementOf");

	/**
	 * 
	 */
	public static final IRI differentFrom = new IRI("http://www.w3.org/2002/07/owl#differentFrom");

	/**
	 * 
	 */
	public static final IRI disjointWith = new IRI("http://www.w3.org/2002/07/owl#disjointWith");

	/**
	 * 
	 */
	public static final IRI distinctMembers = new IRI("http://www.w3.org/2002/07/owl#distinctMembers");

	/**
	 * 
	 */
	public static final IRI equivalentClass = new IRI("http://www.w3.org/2002/07/owl#equivalentClass");

	/**
	 * 
	 */
	public static final IRI equivalentProperty = new IRI("http://www.w3.org/2002/07/owl#equivalentProperty");

	/**
	 * 
	 */
	public static final IRI hasValue = new IRI("http://www.w3.org/2002/07/owl#hasValue");

	/**
	 * 
	 */
	public static final IRI imports = new IRI("http://www.w3.org/2002/07/owl#imports");

	/**
	 * 
	 */
	public static final IRI incompatibleWith = new IRI("http://www.w3.org/2002/07/owl#incompatibleWith");

	/**
	 * 
	 */
	public static final IRI intersectionOf = new IRI("http://www.w3.org/2002/07/owl#intersectionOf");

	/**
	 * 
	 */
	public static final IRI inverseOf = new IRI("http://www.w3.org/2002/07/owl#inverseOf");

	/**
	 * 
	 */
	public static final IRI maxCardinality = new IRI("http://www.w3.org/2002/07/owl#maxCardinality");

	/**
	 * 
	 */
	public static final IRI minCardinality = new IRI("http://www.w3.org/2002/07/owl#minCardinality");

	/**
	 * 
	 */
	public static final IRI onProperty = new IRI("http://www.w3.org/2002/07/owl#onProperty");

	/**
	 * 
	 */
	public static final IRI oneOf = new IRI("http://www.w3.org/2002/07/owl#oneOf");

	/**
	 * 
	 */
	public static final IRI priorVersion = new IRI("http://www.w3.org/2002/07/owl#priorVersion");

	/**
	 * 
	 */
	public static final IRI sameAs = new IRI("http://www.w3.org/2002/07/owl#sameAs");

	/**
	 * 
	 */
	public static final IRI someValuesFrom = new IRI("http://www.w3.org/2002/07/owl#someValuesFrom");

	/**
	 * 
	 */
	public static final IRI unionOf = new IRI("http://www.w3.org/2002/07/owl#unionOf");

	/**
	 * 
	 */
	public static final IRI versionInfo = new IRI("http://www.w3.org/2002/07/owl#versionInfo");

	// Properties

	/**
	 * comment: This file specifies in RDF Schema format the
    built-in classes and properties that together form the basis of
    the RDF/XML syntax of OWL Full, OWL DL and OWL Lite.
    We do not expect people to import this file
    explicitly into their ontology. People that do import this file
    should expect their ontology to be an OWL Full ontology. 
  

	 */
	public static final IRI owl = new IRI("http://www.w3.org/2002/07/owl");
}
