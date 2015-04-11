/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.clerezza.templating.seedsnipe;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.commons.rdf.BlankNode;
import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.BlankNodeOrIRI;
import org.apache.clerezza.commons.rdf.RDFTerm;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.impl.utils.PlainLiteralImpl;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.UnsupportedFormatException;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.RdfList;
import org.apache.clerezza.templating.RenderingFunction;
import org.apache.clerezza.templating.RenderingFunctions;
import org.apache.clerezza.templating.seedsnipe.datastructure.DataFieldResolver;
import org.apache.clerezza.templating.seedsnipe.graphnodeadapter.GraphNodeDataFieldResolver;
import org.apache.clerezza.templating.seedsnipe.simpleparser.DefaultParser;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.Literal;

/**
 * Unit Test for RDF Templating.
 *
 * @author reto, daniel
 */
public class GraphNodeTest {

    private RenderingFunctions simpleFunctions = new RenderingFunctions() {

        @Override
        public RenderingFunction<Object, String> getDefaultFunction() {
            return new RenderingFunction<Object, String>() {

                @Override
                public String process(Object... values) {
                    return values[0].toString();
                }
            };
        }

        @Override
        public Map<String, RenderingFunction> getNamedFunctions() {
            Map<String, RenderingFunction> result = new HashMap<String, RenderingFunction>();
            result.put("noop", new RenderingFunction() {

                @Override
                public Object process(Object... value) {
                    return value[0];
                }
            });
            result.put("getAudio", new RenderingFunction() {

                @Override
                public Object process(Object... value) {
                    return "audio";
                }
            });
            result.put("uppercase", new RenderingFunction() {

                @Override
                public Object process(Object... value) {
                    return value[0].toString().toUpperCase();
                }
            });
            return result;
        }
    };

    @Test
    public void simple() throws IOException {
        Graph mGraph = new SimpleGraph();
        BlankNodeOrIRI resource = new BlankNode() {
        };
        mGraph.add(new TripleImpl(resource, RDFS.comment, new PlainLiteralImpl("a resource")));
        GraphNode node = new GraphNode(resource, mGraph);
        DataFieldResolver dataFieldResolver = new GraphNodeDataFieldResolver(node, simpleFunctions);

        StringReader reader = new StringReader("${ns:rdfs=http://www.w3.org/2000/01/rdf-schema#}${rdfs:comment}");
        StringWriter writer = new StringWriter();

        new DefaultParser(reader, writer).perform(dataFieldResolver);

        Assert.assertEquals("\"a resource\"", writer.toString());
    }

    @Test
    public void inverse() throws IOException {
        Graph mGraph = new SimpleGraph();
        BlankNodeOrIRI subject = new IRI("http://example.org/subject");
        IRI object = new IRI("http://example.org/object");
        mGraph.add(new TripleImpl(subject, RDFS.comment, object));
        GraphNode node = new GraphNode(object, mGraph);
        DataFieldResolver dataFieldResolver = new GraphNodeDataFieldResolver(node, simpleFunctions);

        StringReader reader = new StringReader("${ns:rdfs=http://www.w3.org/2000/01/rdf-schema#}${-rdfs:comment}");
        StringWriter writer = new StringWriter();

        new DefaultParser(reader, writer).perform(dataFieldResolver);

        Assert.assertEquals("<http://example.org/subject>", writer.toString());
    }

    @Test
    public void defaultFunction() throws IOException {
        Graph mGraph = new SimpleGraph();
        BlankNodeOrIRI resource = new BlankNode() {
        };
        mGraph.add(new TripleImpl(resource, RDFS.comment, new PlainLiteralImpl("a resource")));
        GraphNode node = new GraphNode(resource, mGraph);
        DataFieldResolver dataFieldResolver = new GraphNodeDataFieldResolver(node, new RenderingFunctions() {

            @Override
            public RenderingFunction<Object, String> getDefaultFunction() {
                return new RenderingFunction() {

                    @Override
                    public Object process(Object... value) {
                        return "VALUE:" + value[0].toString();
                    }
                };
            }

            @Override
            public Map<String, RenderingFunction> getNamedFunctions() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });

        StringReader reader = new StringReader("${ns:rdfs=http://www.w3.org/2000/01/rdf-schema#}${rdfs:comment}");
        StringWriter writer = new StringWriter();

        new DefaultParser(reader, writer).perform(dataFieldResolver);

        Assert.assertEquals("VALUE:\"a resource\"", writer.toString());
    }

    @Test
    public void simpleIriRoot() throws IOException {
        Graph mGraph = new SimpleGraph();
        BlankNodeOrIRI resource = new IRI("http://example.org/");
        mGraph.add(new TripleImpl(resource, RDFS.comment, new PlainLiteralImpl("a resource")));
        GraphNode node = new GraphNode(resource, mGraph);
        DataFieldResolver dataFieldResolver = new GraphNodeDataFieldResolver(node, simpleFunctions);

        StringReader reader = new StringReader("${ns:rdfs=http://www.w3.org/2000/01/rdf-schema#}${.}${rdfs:comment}");
        StringWriter writer = new StringWriter();

        new DefaultParser(reader, writer).perform(dataFieldResolver);

        Assert.assertEquals("<http://example.org/>\"a resource\"", writer.toString());
    }

    @Test
    public void simpleWithNoOp() throws IOException {
        Graph mGraph = new SimpleGraph();
        BlankNodeOrIRI resource = new BlankNode() {
        };
        mGraph.add(new TripleImpl(resource, RDFS.comment, new PlainLiteralImpl("a resource")));
        GraphNode node = new GraphNode(resource, mGraph);
        DataFieldResolver dataFieldResolver = new GraphNodeDataFieldResolver(node, simpleFunctions);

        StringReader reader = new StringReader("${ns:rdfs=http://www.w3.org/2000/01/rdf-schema#}${noop(rdfs:comment)}");
        StringWriter writer = new StringWriter();

        new DefaultParser(reader, writer).perform(dataFieldResolver);

        Assert.assertEquals("\"a resource\"", writer.toString());
    }

    @Test
    public void simpleWithUppercase() throws IOException {
        Graph mGraph = new SimpleGraph();
        BlankNodeOrIRI resource = new BlankNode() {
        };
        mGraph.add(new TripleImpl(resource, RDFS.comment, new PlainLiteralImpl("a resource")));
        GraphNode node = new GraphNode(resource, mGraph);
        DataFieldResolver dataFieldResolver = new GraphNodeDataFieldResolver(node, simpleFunctions);

        StringReader reader = new StringReader("${ns:rdfs=http://www.w3.org/2000/01/rdf-schema#}${uppercase(rdfs:comment)}");
        StringWriter writer = new StringWriter();

        new DefaultParser(reader, writer).perform(dataFieldResolver);

        Assert.assertEquals("\"A RESOURCE\"", writer.toString());
    }

    @Test
    public void simpleWithLiteralUppercase() throws IOException {
        Graph mGraph = new SimpleGraph();
        BlankNodeOrIRI resource = new BlankNode() {
        };
        mGraph.add(new TripleImpl(resource, RDFS.comment, new PlainLiteralImpl("a resource")));
        GraphNode node = new GraphNode(resource, mGraph);
        DataFieldResolver dataFieldResolver = new GraphNodeDataFieldResolver(node, simpleFunctions);

        StringReader reader = new StringReader("${uppercase(\"a string\")}");
        StringWriter writer = new StringWriter();

        new DefaultParser(reader, writer).perform(dataFieldResolver);

        Assert.assertEquals("A STRING", writer.toString());
    }

    @Test
    public void simpleWithCombinedFunctions() throws IOException {
        Graph mGraph = new SimpleGraph();
        BlankNodeOrIRI resource = new BlankNode() {
        };
        mGraph.add(new TripleImpl(resource, RDFS.comment, new PlainLiteralImpl("a resource")));
        GraphNode node = new GraphNode(resource, mGraph);
        DataFieldResolver dataFieldResolver = new GraphNodeDataFieldResolver(node, simpleFunctions);

        StringReader reader = new StringReader("${ns:rdfs=http://www.w3.org/2000/01/rdf-schema#}${noop(uppercase(noop(rdfs:comment)))}");
        StringWriter writer = new StringWriter();

        new DefaultParser(reader, writer).perform(dataFieldResolver);

        Assert.assertEquals("\"A RESOURCE\"", writer.toString());
    }

    @Test
    public void multiple() throws IOException {
        Graph mGraph = new SimpleGraph();
        BlankNodeOrIRI resource = new BlankNode() {
        };
        mGraph.add(new TripleImpl(resource, RDFS.comment, new PlainLiteralImpl("a resource")));
        mGraph.add(new TripleImpl(resource, RDFS.comment, new PlainLiteralImpl("another resource")));
        GraphNode node = new GraphNode(resource, mGraph);
        DataFieldResolver dataFieldResolver = new GraphNodeDataFieldResolver(node, simpleFunctions);

        StringReader reader = new StringReader("Multiple\n${ns:rdfs=http://www.w3.org/2000/01/rdf-schema#} ${loop}\t${rdfs:comment}\n${/loop}");
        StringWriter writer = new StringWriter();

        new DefaultParser(reader, writer).perform(dataFieldResolver);

        Assert.assertTrue(writer.toString().contains("\"a resource\"") && writer.toString().contains("\"another resource\""));
    }

    @Test
    public void fieldTest() throws IOException {
        Graph mGraph = new SimpleGraph();
        BlankNodeOrIRI resource = new BlankNode() {
        };
        mGraph.add(new TripleImpl(resource, RDFS.comment, new PlainLiteralImpl("a resource")));
        GraphNode node = new GraphNode(resource, mGraph);
        DataFieldResolver dataFieldResolver = new GraphNodeDataFieldResolver(node, simpleFunctions);

        StringReader reader = new StringReader("${ns:rdfs=http://www.w3.org/2000/01/rdf-schema#}${if rdfs:comment}yes${/if} and ${if rdfs:label}yes${else}no${/if}");
        StringWriter writer = new StringWriter();

        new DefaultParser(reader, writer).perform(dataFieldResolver);

        Assert.assertEquals("yes and no", writer.toString());
    }

    @Test
    public void fieldTest2() throws IOException {
        Graph mGraph = new SimpleGraph();
        BlankNodeOrIRI resource = new BlankNode() {
        };
        mGraph.add(new TripleImpl(resource, RDFS.comment, new PlainLiteralImpl("a resource")));
        GraphNode node = new GraphNode(resource, mGraph);
        DataFieldResolver dataFieldResolver = new GraphNodeDataFieldResolver(node, simpleFunctions);
        /* without the final access to field it does not work,
        field access in if-conditins does not count for loops*/
        StringReader reader = new StringReader("${ns:rdfs=http://www.w3.org/2000/01/rdf-schema#}${loop}${if rdfs:comment}${rdfs:comment}${/if} and ${if rdfs:label}yes${else}no${/if}${rdfs:comment}${/loop}");
        StringWriter writer = new StringWriter();

        new DefaultParser(reader, writer).perform(dataFieldResolver);

        Assert.assertEquals("\"a resource\" and no\"a resource\"", writer.toString());
    }

    @Test
    public void foaf() throws IOException {
        Graph mGraph = new SimpleGraph();
        final Parser parser = Parser.getInstance();

        try {
            ImmutableGraph deserializedGraph = parser.parse(getClass().getResourceAsStream("libby-foaf.rdf"), "application/rdf+xml");
            mGraph.addAll(deserializedGraph);
            IRI document = new IRI("http://swordfish.rdfweb.org/people/libby/rdfweb/webwho.xrdf");

            Assert.assertTrue((mGraph.size() > 0));

            GraphNode node = new GraphNode(document, mGraph);
            DataFieldResolver dataFieldResolver = new GraphNodeDataFieldResolver(node, simpleFunctions);

            String templateString =
                    "FOAF: ${ns:rdfs=http://www.w3.org/2000/01/rdf-schema#}" +
                    "${ns:foaf=http://xmlns.com/foaf/0.1/}" +
                    "${foaf:maker/foaf:name}" +
                    "${loop}" +
                    "${loop}" +
                    "${foaf:maker/foaf:knows/foaf:name}" +
                    "${/loop}" +
                    "${/loop}";
            StringReader reader = new StringReader(templateString);
            StringWriter writer = new StringWriter();

            new DefaultParser(reader, writer).perform(dataFieldResolver);

            Assert.assertTrue(writer.toString().contains("Martin Poulter") && writer.toString().contains("Kal Ahmed") && writer.toString().contains("Libby Miller"));

        } catch (UnsupportedFormatException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void foafSorted() throws IOException {
        Graph mGraph = new SimpleGraph();
        final Parser parser = Parser.getInstance();

        try {
            ImmutableGraph deserializedGraph = parser.parse(getClass().getResourceAsStream("libby-foaf.rdf"), "application/rdf+xml");
            mGraph.addAll(deserializedGraph);
            IRI document = new IRI("http://swordfish.rdfweb.org/people/libby/rdfweb/webwho.xrdf");

            Assert.assertTrue((mGraph.size() > 0));

            GraphNode node = new GraphNode(document, mGraph);
            DataFieldResolver dataFieldResolver = new GraphNodeDataFieldResolver(node, simpleFunctions);

            String templateString =
                    "FOAF: ${ns:rdfs=http://www.w3.org/2000/01/rdf-schema#}" +
                    "${ns:foaf=http://xmlns.com/foaf/0.1/}" +
                    "${loop}" +
                    "${loop sort asc foaf:maker/foaf:knows/foaf:name}" +
                    "${foaf:maker/foaf:knows/foaf:name}" +
                    "${/loop}" +
                    "${/loop}";
            StringReader reader = new StringReader(templateString);
            StringWriter writer = new StringWriter();

            new DefaultParser(reader, writer).perform(dataFieldResolver);


            final String templateResult = writer.toString();
            Assert.assertTrue(templateResult.contains("Martin Poulter"));
            Assert.assertTrue(templateResult.indexOf("Martin Poulter") < templateResult.indexOf("Sarah Miller"));
            Assert.assertTrue(templateResult.indexOf("Sarah Miller") < templateResult.indexOf("Wendy Chisholm"));


        } catch (UnsupportedFormatException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void rdfListAsPropertyValue() throws IOException {
        Graph mGraph = new SimpleGraph();
        BlankNodeOrIRI resource = new BlankNode() {
        };
        BlankNodeOrIRI listNode = new BlankNode() {
        };
        mGraph.add(new TripleImpl(resource, RDF.value, listNode));
        List<RDFTerm> list = new RdfList(listNode, mGraph);
        list.add(new PlainLiteralImpl("first"));
        list.add(new PlainLiteralImpl("second"));
        list.add(new PlainLiteralImpl("third"));
        GraphNode node = new GraphNode(resource, mGraph);
        DataFieldResolver dataFieldResolver = new GraphNodeDataFieldResolver(node, simpleFunctions);
        StringReader reader = new StringReader(
                "${ns:rdf=http://www.w3.org/1999/02/22-rdf-syntax-ns#}" +
                "${loop}${loop}${rdf:value/contains}${/loop}${/loop}");
        StringWriter writer = new StringWriter();

        new DefaultParser(reader, writer).perform(dataFieldResolver);

        Assert.assertEquals("\"first\"\"second\"\"third\"", writer.toString());
    }

    @Test
    public void rdfListAsRoot() throws IOException {
        Graph mGraph = new SimpleGraph();
        BlankNodeOrIRI resource = new BlankNode() {
        };
        List<RDFTerm> list = new RdfList(resource, mGraph);
        list.add(new PlainLiteralImpl("first"));
        list.add(new PlainLiteralImpl("second"));
        list.add(new PlainLiteralImpl("third"));
        GraphNode node = new GraphNode(resource, mGraph);
        DataFieldResolver dataFieldResolver = new GraphNodeDataFieldResolver(node, simpleFunctions);
        StringReader reader = new StringReader(
                "${loop}${./contains}${/loop}");
        StringWriter writer = new StringWriter();

        new DefaultParser(reader, writer).perform(dataFieldResolver);

        Assert.assertEquals("\"first\"\"second\"\"third\"", writer.toString());
    }

    @Test
    public void rdfListAsRootElementProperties() throws IOException {
        Graph mGraph = new SimpleGraph();
        BlankNodeOrIRI resource = new BlankNode() {
        };
        List<RDFTerm> list = new RdfList(resource, mGraph);
        list.add(createLabeledRes("first", mGraph));
        list.add(createLabeledRes("second", mGraph));
        list.add(createLabeledRes("third", mGraph));
        GraphNode node = new GraphNode(resource, mGraph);
        DataFieldResolver dataFieldResolver = new GraphNodeDataFieldResolver(node, simpleFunctions);
        StringReader reader = new StringReader(
                "${ns:rdfs=http://www.w3.org/2000/01/rdf-schema#}" +
                "${loop}${loop}${contains/rdfs:label}${/loop}${/loop}");
        StringWriter writer = new StringWriter();

        new DefaultParser(reader, writer).perform(dataFieldResolver);

        Assert.assertEquals("\"first\"\"second\"\"third\"", writer.toString());
    }

    @Test
    public void rdfListAsRootElementPropertiesWithPresenceTest() throws IOException {
        Graph mGraph = new SimpleGraph();
        BlankNodeOrIRI resource = new BlankNode() {
        };
        List<RDFTerm> list = new RdfList(resource, mGraph);
        list.add(createLabeledRes("first", mGraph));
        list.add(createLabeledRes("second", mGraph));
        list.add(createLabeledRes("third", mGraph));
        GraphNode node = new GraphNode(resource, mGraph);
        DataFieldResolver dataFieldResolver = new GraphNodeDataFieldResolver(node, simpleFunctions);
        StringReader reader = new StringReader(
                "${ns:rdfs=http://www.w3.org/2000/01/rdf-schema#}" +
                "${loop}${if contains/rdfs:label}${contains/rdfs:label}${/if}${/loop}");
        StringWriter writer = new StringWriter();

        new DefaultParser(reader, writer).perform(dataFieldResolver);

        Assert.assertEquals("\"first\"\"second\"\"third\"", writer.toString());
    }

    @Test
    public void compareFunctionResultInIf() throws IOException {
        Graph mGraph = new SimpleGraph();
        BlankNodeOrIRI resource = new BlankNode();
        mGraph.add(new TripleImpl(resource, RDFS.comment, new PlainLiteralImpl("a resource")));
        GraphNode node = new GraphNode(resource, mGraph);
        DataFieldResolver dataFieldResolver = new GraphNodeDataFieldResolver(node, simpleFunctions);

        StringReader reader = new StringReader("${ns:rdfs=http://www.w3.org/2000/01/rdf-schema#}" +
                "${if getAudio(\"foo/bar\") = \"audio\"}noproblem${/if}");
        StringWriter writer = new StringWriter();

        new DefaultParser(reader, writer).perform(dataFieldResolver);

        Assert.assertEquals("noproblem", writer.toString());
    }

    @Test
    public void loopWithEmptyList() throws IOException {
        Graph mGraph = new SimpleGraph();
        BlankNodeOrIRI resource = new BlankNode() {
        };

        BlankNodeOrIRI listNode1 = new BlankNode() {
        };
        mGraph.add(new TripleImpl(resource, RDF.value, listNode1));
        List<RDFTerm> list1 = new RdfList(listNode1, mGraph);
        list1.add(new PlainLiteralImpl("first"));
        list1.add(new PlainLiteralImpl("second"));
        list1.add(new PlainLiteralImpl("third"));

        BlankNodeOrIRI listNode2 = new BlankNode() {
        };
        mGraph.add(new TripleImpl(resource, RDF.value, listNode2));
        RdfList.createEmptyList(listNode2, mGraph);

        GraphNode node = new GraphNode(resource, mGraph);

        DataFieldResolver dataFieldResolver = new GraphNodeDataFieldResolver(node, simpleFunctions);
        StringReader reader = new StringReader(
                "${ns:rdf=http://www.w3.org/1999/02/22-rdf-syntax-ns#}" +
                "${loop}${loop}${rdf:value/contains}${/loop}${/loop}");
        StringWriter writer = new StringWriter();

        new DefaultParser(reader, writer).perform(dataFieldResolver);

        Assert.assertEquals("\"first\"\"second\"\"third\"", writer.toString());
    }

    private BlankNodeOrIRI createLabeledRes(String label, Graph mGraph) {
        BlankNode bNode = new BlankNode();
        Literal lit = new PlainLiteralImpl(label);
        mGraph.add(new TripleImpl(bNode, RDFS.label, lit));
        return bNode;
    }
}