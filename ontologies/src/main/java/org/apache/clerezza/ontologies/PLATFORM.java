package org.apache.clerezza.ontologies;

import org.apache.clerezza.api.IRI;

public class PLATFORM {
	// Classes

	/**
	 * definition: A web page typically rendered with a header.

	 */
	public static final IRI HeadedPage = new IRI("http://clerezza.org/2009/08/platform#HeadedPage");

	/**
	 * definition: 
		An instance of a Clerezza platform.
	

	 */
	public static final IRI Instance = new IRI("http://clerezza.org/2009/08/platform#Instance");

	// Properties

	/**
	 * definition: Points to a base URI of the Clerezza
		platform instance. A base Uri is the shortest URI of a URI-Hierarhy the
		platform handles.
	

	 */
	public static final IRI baseUri = new IRI("http://clerezza.org/2009/08/platform#baseUri");

	/**
	 * definition: Points to the default base URI of the Clerezza
		platform instance.
	

	 */
	public static final IRI defaultBaseUri = new IRI("http://clerezza.org/2009/08/platform#defaultBaseUri");

	/**
	 * definition: Points to a platform instance.
	

	 */
	public static final IRI instance = new IRI("http://clerezza.org/2009/08/platform#instance");

	/**
	 * definition: Points to a rdf list containing the languages
		supported by the platform instance. The first langague in the list is
		the default language.
	

	 */
	public static final IRI languages = new IRI("http://clerezza.org/2009/08/platform#languages");

	/**
	 * definition: Points to the last login time stamp of the user.
	

	 */
	public static final IRI lastLogin = new IRI("http://clerezza.org/2009/08/platform#lastLogin");

	/**
	 * definition: Points to a literal which represents the ISO code of
	a language.
	

	 */
	public static final IRI preferredLangInISOCode = new IRI("http://clerezza.org/2009/08/platform#preferredLangInISOCode");

	/**
	 * definition: Points to a platform user.
	

	 */
	public static final IRI user = new IRI("http://clerezza.org/2009/08/platform#user");

	/**
	 * definition: Points to a  unique name which is used as
identifier for an online account of a platform. This is an inverse functional
property.
	

	 */
	public static final IRI userName = new IRI("http://clerezza.org/2009/08/platform#userName");

	// Properties

	/**
	 * 
	 */
	public static final IRI THIS_ONTOLOGY = new IRI("http://clerezza.org/2009/08/platform#");
}
