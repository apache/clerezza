JAX-RS resource called GenericResourcePage can be used to retrieve a resource and its associated concepts. 
Go to http://[your.server]/concepts/generic-resource?uri=[resource] where [resource] 
specifies the UriRef of the resource. Furthermore there is a JAX-RS resource called ResourceTagger
 for associating concepts with resources. POST to http://[your.server]/concepts/tagger/set a form 
with form parameter uri which specifies the UriRef of the resource and a form parameter 
concepts which contains the UriRefs of the concepts.

JAX-RS resource called ConceptManipulator can be used to add concept into the content graph
The resource path is "/concepts/manipulator/add-concept" and the form parameters are
pref-label, lang, and comment.

On the client side there is a ConceptManipulator widget to add free concepts to the content graph
and there is also a ConceptFinder widget to find exisiting concepts to be suggested for a specified search term.
 
In ConceptManipulator widget and ConceptFinder widget a developer can register a callback function
which is called when a concept is added to the list of selected concept.


