This JAX-RS resource can be used to retrieve a resource and its associated concepts. 
Go to http://[your.server]/concepts/generic-resource?uri=[resource] where [resource] 
specifies the UriRef of the resource. Furthermore there is a method for associating 
concepts with resources. POST to http://[your.server]/concepts/tagger/set a form 
with form parameter uri which specifies the UriRef of the resource and a form parameter 
concepts which contains the UriRefs of the concepts.

