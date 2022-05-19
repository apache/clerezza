/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor  license  agreements.  See the NOTICE file distributed
 * with this work  for  additional  information  regarding  copyright
 * ownership.  The ASF  licenses  this file to you under  the  Apache
 * License, Version 2.0 (the "License"); you may not  use  this  file
 * except in compliance with the License.  You may obtain  a copy  of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless  required  by  applicable law  or  agreed  to  in  writing,
 * software  distributed  under  the  License  is  distributed  on an
 * "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR  CONDITIONS  OF ANY KIND,
 * either  express  or implied.  See  the License  for  the  specific
 * language governing permissions and limitations under  the License.
 */
package org.apache.clerezza.schemagen;

import org.apache.clerezza.*;
import org.apache.clerezza.implementation.in_memory.SimpleGraph;
import org.apache.clerezza.representation.Parser;
import org.wymiwyg.commons.util.arguments.AnnotatedInterfaceArguments;
import org.wymiwyg.commons.util.arguments.ArgumentHandler;
import org.wymiwyg.commons.util.arguments.InvalidArgumentsException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Generates the source code of a java-class with constants for an ontology described in RDF.
 *
 * @author reto
 */
public class SchemaGen {

    private final Graph schemaGraph;
    private final String className;
    private final String namespace;

    public static void main(String... args) throws Exception {
        SchemaGenArguments arguments;
        try {
            arguments = new ArgumentHandler(args).getInstance(SchemaGenArguments.class);
        } catch (InvalidArgumentsException e) {
            System.out.println(e.getMessage());
            System.out.print("Usage: SchemaGen ");
            System.out.println(AnnotatedInterfaceArguments.getArgumentsSyntax(SchemaGenArguments.class));
            PrintWriter out = new PrintWriter(System.out, true);
            AnnotatedInterfaceArguments.printArgumentDescriptions(
                    SchemaGenArguments.class, out);
            out.flush();
            return;
        }

        SchemaGen schemaGen = new SchemaGen(arguments);
        PrintWriter out = new PrintWriter(System.out);
        schemaGen.writeClass(out);
        out.flush();
    }

    /**
     * Creates an instance doing the transformation as specified by the
     * arguments.
     *
     * @param arguments specification of the transformation
     * @throws IOException If an IO error occurs.
     */
    public SchemaGen(SchemaGenArguments arguments)
            throws IOException, URISyntaxException {
        Parser parser = Parser.getInstance();
        InputStream serializedGraph = arguments.getSchemaUrl().openStream();
        schemaGraph = parser.parse(serializedGraph,
                arguments.getFormatIdentifier());
        className = arguments.getClassName();

        if (arguments.getNamespace() == null) {
            namespace = getOntologyUri();
        } else {
            namespace = arguments.getNamespace();
        }
    }

    private String getOntologyUri() {
        Iterator<Triple> ontologyTriples = schemaGraph.filter(null, RDF.type, OWL.Ontology);
        String result;
        if (ontologyTriples.hasNext()) {
            result = ((IRI) ontologyTriples.next().getSubject()).getUnicodeString();
        } else {
            throw new RuntimeException("No OWL Ontology found!");
        }
        if (ontologyTriples.hasNext()) {
            throw new RuntimeException("More than one OWL Ontology found!");
        }
        return result;
    }

    /**
     * Writes the generated source code of a java class to the specified
     * print writer.
     *
     * @param out The print writer to write the transformation to.
     * @throws IllegalArgumentException If out is <code>null</code>.
     */
    public void writeClass(PrintWriter out) {
        if (out == null) {
            throw new IllegalArgumentException("Invalid out: out");
        }
        out.print("// Generated by ");
        out.println(getClass().getName());
        String packageName = getPackageName();
        if (packageName != null) {
            out.print("package ");
            out.print(packageName);
            out.println(';');
        }
        out.println();
        out.println("import org.apache.clerezza.IRI;");
        out.println();
        out.print("public class ");
        out.print(getSimpleName());
        out.println(" {");

        SortedSet<OntologyResource> ontClasses = new TreeSet<>();
        ontClasses.addAll(getResourcesOfType(RDFS.Class));
        ontClasses.addAll(getResourcesOfType(RDFS.Datatype));
        ontClasses.addAll(getResourcesOfType(OWL.Class));

        if (ontClasses.size() > 0) {
            out.println("\t// Classes");
            printResources(ontClasses.iterator(), out);
        }
        SortedSet<OntologyResource> ontProperties = new TreeSet<>();
        //some ontologies defining things that are both classes
        //and properties, like image in RSS 1.0 - so we remove those
        ontProperties.addAll(getResourcesOfType(RDF.Property, ontClasses));
        ontProperties.addAll(getResourcesOfType(OWL.ObjectProperty, ontClasses));
        ontProperties.addAll(getResourcesOfType(OWL.DatatypeProperty, ontClasses));

        if (ontProperties.size() > 0) {
            out.println();
            out.println("\t// Properties");
            printResources(ontProperties.iterator(), out);
        }

        //create a set of classes and properties. Everything else should be instances
        ontClasses.addAll(ontProperties);
        Collection<OntologyResource> instances = getResourcesOfType(null, ontClasses);

        if (instances.size() > 0) {
            out.println();
            out.println("\t// Properties");
            printResources(instances.iterator(), out);
        }

        out.println("}");
    }

    private void printResources(Iterator<OntologyResource> iterator,
                                PrintWriter out) {
        while (iterator.hasNext()) {
            OntologyResource ontologyResource = iterator.next();
            String description = ontologyResource.getDescription();
            if (description != null) {
                out.println();
                out.println("\t/**");
                out.print("\t * ");
                out.println(description);
                out.println("\t */");
            }
            out.print("\tpublic static final IRI ");
            out.print(ontologyResource.getLocalName());
            out.print(" = new IRI(\"");
            out.print(ontologyResource.getUriString());
            out.println("\");");
        }

    }

    private Collection<OntologyResource> getResourcesOfType(IRI type) {
        return getResourcesOfType(type, null);
    }

    /**
     * @param type   the type of the class, or null for all things that are declared to be of a type
     * @param ignore a set things to ignore
     * @return       the result set of things
     */
    private Collection<OntologyResource> getResourcesOfType(IRI type, Collection<OntologyResource> ignore) {
        Set<OntologyResource> result = new HashSet<>();
        Iterator<Triple> classStatements = schemaGraph.filter(null, RDF.type,
                type);
        while (classStatements.hasNext()) {
            Triple triple = classStatements.next();
            BlankNodeOrIRI classResource = triple.getSubject();
            if (classResource instanceof BlankNode) {
                if (type != null)
                    System.err.println("Ignoring anonymous resource of type " + type.getUnicodeString());
                else System.err.println("Ignoring anonymous resource");
                for (Triple contextTriple : getNodeContext(classResource, schemaGraph)) {
                    System.err.println(contextTriple);
                }
                continue;
            }

            // Test if the given resource belongs to the ontology
            final IRI classUri = (IRI) classResource;
            final String strClassUri = classUri.getUnicodeString();
            if (strClassUri.startsWith(namespace)) {
                // The remaining part of the class URI must not contain
                // a slash '/' or a hash '#' character. Otherwise we assume
                // that is belongs to another ontology.
                final int offset = namespace.length();
                int idxSlash = strClassUri.indexOf('/', offset);
                int idxHash = strClassUri.indexOf('#', offset);

                // Note that we generously ignore the first character of the
                // remaining part that may be a '/' or a '#' because the
                // namespace may not end with such a character.
                if (idxSlash <= offset && idxHash <= offset) {
                    OntologyResource ontologyResource =
                            new OntologyResource(classUri, schemaGraph);
                    if (ignore == null || !ignore.contains(ontologyResource))
                        result.add(ontologyResource);
                }
            }
        }
        return result;
    }

    private ImmutableGraph getNodeContext(BlankNodeOrIRI resource, Graph graph) {
        final HashSet<BlankNode> dontExpand = new HashSet<>();
        if (resource instanceof BlankNode) {
            dontExpand.add((BlankNode) resource);
        }
        return getContextOf(resource, dontExpand, graph).getImmutableGraph();

    }

    private Graph getContextOf(BlankNodeOrIRI node, Set<BlankNode> dontExpand,
                               Graph graph) {
        Graph result = new SimpleGraph();
        Iterator<Triple> forwardProperties = graph.filter(node, null, null);
        while (forwardProperties.hasNext()) {
            Triple triple = forwardProperties.next();
            result.add(triple);
            RDFTerm object = triple.getObject();
            if (object instanceof BlankNode) {
                BlankNode bNodeObject = (BlankNode) object;
                if (!dontExpand.contains(bNodeObject)) {
                    dontExpand.add(bNodeObject);
                    result.addAll(getContextOf(bNodeObject, dontExpand, graph));
                }
            }
        }
        Iterator<Triple> backwardProperties = graph.filter(null, null, node);
        while (backwardProperties.hasNext()) {
            Triple triple = backwardProperties.next();
            result.add(triple);
            BlankNodeOrIRI subject = triple.getSubject();
            if (subject instanceof BlankNode) {
                BlankNode bNodeSubject = (BlankNode) subject;
                if (!dontExpand.contains(bNodeSubject)) {
                    dontExpand.add(bNodeSubject);
                    result.addAll(getContextOf(bNodeSubject, dontExpand, graph));
                }
            }
        }
        return result;
    }

    private String getSimpleName() {
        int lastDotPos = className.lastIndexOf('.');
        if (lastDotPos == -1) {
            return className;
        }
        return className.substring(lastDotPos + 1);
    }

    private String getPackageName() {
        int lastDotPos = className.lastIndexOf('.');
        if (lastDotPos == -1) {
            return null;
        }
        return className.substring(0, lastDotPos);
    }

    private static class OntologyResource implements
            Comparable<OntologyResource> {

        static final List<String> reservedWords = Arrays.asList(
                "abstract", "assert", "boolean", "break", "byte", "case",
                "catch", "char", "class", "const", "continue", "default",
                "do", "double", "else", "enum", "extends", "false", "final",
                "finally", "float", "for", "goto", "if", "implements",
                "import", "instanceof", "int", "interface", "long", "native",
                "new", "null", "package", "private", "protected", "public",
                "return", "short", "static", "strictfp", "super", "switch",
                "synchronized", "this", "throw", "throws", "transient",
                "true", "try", "void", "volatile", "while");

        final Graph graph;
        final IRI uri;

        OntologyResource(IRI uri, Graph graph) {
            this.uri = uri;
            this.graph = graph;
        }

        String getLocalName() {
            String uriValue = uri.getUnicodeString();
            int hashPos = uriValue.lastIndexOf('#');
            int slashPos = uriValue.lastIndexOf('/');
            int delimiter = Math.max(hashPos, slashPos);
            String val = uriValue.substring(delimiter + 1);
            if (val.length() == 0) return "THIS_ONTOLOGY";
            //replace bad characters...
            val = val.replace('-', '_').replace('.', '_');
            return reservedWords.contains(val) ? val + "_" : val;
        }

        String getUriString() {
            return uri.getUnicodeString();
        }

        String getDescription() {
            StringBuilder result = new StringBuilder();
            Iterator<Triple> titleStatements = graph.filter(
                    uri, DCTERMS.title, null);
            while (titleStatements.hasNext()) {
                RDFTerm object = titleStatements.next().getObject();
                if (object instanceof Literal) {
                    result.append("title: ");
                    result.append(((Literal) object).getLexicalForm());
                    result.append("\n");
                }
            }
            Iterator<Triple> descriptionStatements = graph.filter(
                    uri, DCTERMS.description, null);
            while (descriptionStatements.hasNext()) {
                RDFTerm object = descriptionStatements.next().getObject();
                if (object instanceof Literal) {
                    result.append("{@literal description: ");
                    result.append(((Literal) object).getLexicalForm());
                    result.append("}\n");
                }
            }
            Iterator<Triple> skosDefStatements = graph.filter(
                    uri, SKOS.definition, null);
            while (skosDefStatements.hasNext()) {
                RDFTerm object = skosDefStatements.next().getObject();
                if (object instanceof Literal) {
                    result.append("{@literal definition: ");
                    result.append(((Literal) object).getLexicalForm());
                    result.append("}\n");
                }
            }
            Iterator<Triple> rdfsCommentStatements = graph.filter(
                    uri, RDFS.comment, null);
            while (rdfsCommentStatements.hasNext()) {
                RDFTerm object = rdfsCommentStatements.next().getObject();
                if (object instanceof Literal) {
                    // Use {@literal ...} to avoid javadoc complaining about "malformed HTML" in some texts
                    result.append("{@literal comment: ");
                    result.append(((Literal) object).getLexicalForm());
                    result.append("}\n");
                }
            }
            Iterator<Triple> skosNoteStatements = graph.filter(
                    uri, SKOS.note, null);
            while (skosNoteStatements.hasNext()) {
                RDFTerm object = skosNoteStatements.next().getObject();
                if (object instanceof Literal) {
                    result.append("{@literal note: ");
                    result.append(((Literal) object).getLexicalForm());
                    result.append("}\n");
                }
            }
            Iterator<Triple> skosExampleStatements = graph.filter(
                    uri, SKOS.example, null);
            while (skosExampleStatements.hasNext()) {
                RDFTerm object = skosExampleStatements.next().getObject();
                if (object instanceof Literal) {
                    result.append("{@literal example: ");
                    result.append(((Literal) object).getLexicalForm());
                    result.append("}\n");
                } else if (object instanceof IRI) {
                    result.append("see <a href=").append(((IRI) object).getUnicodeString()).append(">example</a>");
                    result.append("\n");
                }
            }
            return result.toString();
        }

        @Override
        public int hashCode() {
            return getUriString().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else if (obj == this) {
                return true;
            } else if (OntologyResource.class.equals(obj.getClass())) {
                final OntologyResource other = (OntologyResource) obj;
                return getUriString().equals(other.getUriString());
            }
            return false;
        }

        @Override
        public int compareTo(OntologyResource o) {
            return getUriString().compareTo(o.getUriString());
        }

    }

    /*
     * Ontology vocabs are re-defined here and not imported to avoid a maven dependency loop
     */

    /**
     * OWL Ontology.
     */
    private static class OWL {
        private static final String NS = "http://www.w3.org/2002/07/owl#";
        public static final RDFTerm Ontology = new IRI(NS + "Ontology");
        private static final IRI Class = new IRI(NS + "Class");
        private static final IRI DatatypeProperty = new IRI(NS + "DatatypeProperty");
        private static final IRI ObjectProperty = new IRI(NS + "ObjectProperty");
    }

    /**
     * RDFS Ontology.
     */
    private static class RDFS {
        private static final String NS = "http://www.w3.org/2000/01/rdf-schema#";
        private static final IRI Class = new IRI(NS + "Class");
        private static final IRI Datatype = new IRI(NS + "Datatype");
        private static final IRI comment = new IRI(NS + "comment");
    }

    /**
     * RDF Ontology.
     */
    private static class RDF {
        private static final String NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
        private static final IRI Property = new IRI(NS + "Property");
        private static final IRI type = new IRI(NS + "type");
    }

    private static class SKOS {
        static final IRI definition = new IRI("http://www.w3.org/2008/05/skos#definition");
        static final IRI note = new IRI("http://www.w3.org/2004/02/skos/core#note");
        static final IRI example = new IRI("http://www.w3.org/2004/02/skos/core#example");
    }

    private static class DCTERMS {
        public static final IRI title = new IRI("http://purl.org/dc/terms/title");
        public static final IRI description = new IRI("http://purl.org/dc/terms/description");
    }
}
