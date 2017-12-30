## Building

Build using Apache Maven with

    mvn install

## Documentation

You can build the documentation with:

    mvn site

## FAQ

Some more advanced questions focused on showing the differences to other proposed APIs.

### Can I add RdfTerms from one implementation to another?

Yes, any compliant implementation of BlankNode, Iri or Literal as well as Triple
can be added to any implemenation of Graph, as long the Graph supports adding triples. Implementation 
may not require the nodes to be of a particular implementation of having been 
created with a specific factory.

### How does it work?

Implementation might need to map instances of BlankNode to their internal 
implementation. This should be done in a way that when there is no more reference
to the BlankNode object (i.e. when the object can be garbage collected) the mapping
to the internal implementation is removed from memory to. This can be achieved 
by using a java.util.WeakHashMap

### Do I get back the same object that I added?

For instances of Iri or Literals you get back an object that result equal to the
originally added object, i.e. an object with the same HashCode and of which the
equals method return true when compared with the originally added object. Ther
is no guarantee that the same instance will be returned.
For instances of BlankNode the above in only guaranteed as long as the original 
object is referenced. When the original object becomes eligible for garbage 
collection the implementation may start returning a different (an not equal)
object. In practice this means BlankNode objects cannot safely be serialized 
(using Java serialization) or passed around via RMI.

### Can an implementation remove redundant information from a Graph?

Yes, as long as this doesn't affect any BlankNode instance that is currently 
reachable (i.e. the Java object is in memory and is not eligible for garbage
collection).

For  example given the non-lean graph:

    ex:a ex:p _:x .
    _:y ex:p _:x .

As long as there is no BlankNode instance referencing _:y the implementation can
reduce the graph to:

    ex:a ex:p _:x .

removing the redundancy. If however there is a reachable BlankNode instance for 
_:y the implementation must not remove the redundancy as the code which has
access to the object can go on adding a triple:

    _:y ex:p2 ex:b .

Thus creating a graph that doesn't contain any internal redundancy, namely:

    ex:a ex:p _:x .
    _:y ex:p _:x .
    _:y ex:p2 ex:b .


