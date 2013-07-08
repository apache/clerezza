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

package org.apache.clerezza.rdf.cris;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.clerezza.rdf.core.*;
import org.apache.clerezza.rdf.core.impl.*;
import org.apache.clerezza.rdf.cris.ontologies.CRIS;
import org.apache.clerezza.rdf.ontologies.FOAF;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.rdf.utils.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.*;
import org.wymiwyg.commons.util.Util;

/**
 *
 * @author rbn, tio, daniel
 */
public class GraphIndexerTest {

    MGraph definitions = new SimpleMGraph();
    MGraph dataGraph = new SimpleMGraph();
    GraphIndexer service = null;
    UriRef ownsPetProperty = new UriRef("http://example.org/pet#owns");

    private void createPerson(String firstName, String lastName) {
        GraphNode node = new GraphNode(new UriRef(Util.createURN5()), dataGraph);
        node.addProperty(RDF.type, FOAF.Person);
        node.addPropertyValue(FOAF.firstName, firstName);
        node.addPropertyValue(FOAF.lastName, lastName);
    }

    private void createDefinition(UriRef rdfType, List<UriRef> properties) {
        GraphNode node = new GraphNode(new BNode(), definitions);
        node.addProperty(RDF.type, CRIS.IndexDefinition);
        node.addProperty(CRIS.indexedType, rdfType);
        for (UriRef p : properties) {
            node.addProperty(CRIS.indexedProperty, p);
        }
    }

    @Before
    public void setUp() {

        definitions.clear();
        dataGraph.clear();
        
        
        List<UriRef> list = new ArrayList<UriRef>();
        list.add(FOAF.firstName);
        list.add(FOAF.lastName);
        list.add(FOAF.homepage);
        createDefinition(FOAF.Person, list);
        service = new GraphIndexer(definitions, dataGraph);
        
        GraphNode nodeB = new GraphNode(new UriRef(Util.createURN5()), dataGraph);
        nodeB.addProperty(RDF.type, FOAF.Person);
        nodeB.addProperty(FOAF.homepage, new UriRef("http://myhomepage/foo?query=bla&bla=test"));

        createPerson("John", "Doe");
        createPerson("Jane", "Doe");
        createPerson("Frank", "Capra");
        createPerson("Joe", "Bloggs");
        createPerson("Jane", "Bloggs");
        createPerson("Harry", "Wotsit");
        createPerson("Harry Joe", "Wotsit-Bloggs");
        //a person with two first-names
        GraphNode node = new GraphNode(new UriRef(Util.createURN5()), dataGraph);
        node.addProperty(RDF.type, FOAF.Person);
        node.addPropertyValue(FOAF.firstName, "John");
        node.addPropertyValue(FOAF.firstName, "William");
        node.addPropertyValue(FOAF.lastName, "Smith");
        node.addPropertyValue(RDFS.comment, "A person with two names");
        //and a pet
        BNode pet = new BNode();
        node.addProperty(ownsPetProperty, pet);

        GraphNode petNode = new GraphNode(pet, dataGraph);
        petNode.addPropertyValue(FOAF.name, "Silvio");

        
    }

    @Test
    public void findResourcesViaUriRef() throws ParseException, InterruptedException {
        List<NonLiteral> results;
        Thread.sleep(1000);
        
        results = service.findResources(FOAF.homepage, "*p://myhomepage/foo?query=bla&bla=te*");
        Assert.assertEquals(1, results.size());
        
        results = service.findResources(FOAF.homepage, "http://myhomepage/foo?query=bla&bla=test");
        Assert.assertEquals(1, results.size());
        
        
    }

    @Test
    public void findResources() throws ParseException, InterruptedException {
        List<NonLiteral> results;
        Thread.sleep(1000);
        results = service.findResources(FOAF.firstName, "*Joe*");

        
        Assert.assertEquals(2, results.size());
    }

    @Test
    public void findMultiProperties() throws InterruptedException, ParseException {
            List<Condition> conditions = new ArrayList<Condition>();
            conditions.add(new WildcardCondition(FOAF.firstName, "*Joe*"));
            conditions.add(new WildcardCondition(FOAF.lastName, "*Wotsit*"));
            Thread.sleep(1000);
            List<NonLiteral> results = service.findResources(conditions);
            Assert.assertEquals(1, results.size());
    }

    @Test
    public void findMultiProperties2() throws InterruptedException {
        try {
            List<Condition> conditions = new ArrayList<Condition>();
            conditions.add(new WildcardCondition(FOAF.firstName, "*Jo*"));
            conditions.add(new WildcardCondition(FOAF.firstName, "*Wil*"));
            Thread.sleep(1000);
            List<NonLiteral> results = service.findResources(conditions);

            Assert.assertEquals(1, results.size());
        } catch (ParseException ex) {
        }
    }

    @Test
    public void lateAddition() {
        try {
            createPerson("Another Joe", "Simpsons");
            Thread.sleep(1000);
            List<NonLiteral> results = service.findResources(FOAF.firstName, "*Joe*");
            Assert.assertEquals(3, results.size());
        } catch (InterruptedException ex) {
        } catch (ParseException ex) {
        }
    }

    @Test
    public void lateAdditionInverse() throws InterruptedException, ParseException {

            GraphNode node = new GraphNode(new UriRef(Util.createURN5()), dataGraph);
            node.addPropertyValue(FOAF.firstName, "Another Jane");
            node.addPropertyValue(FOAF.lastName, "Samsing");
            Thread.sleep(1000);
            List<NonLiteral> results = service.findResources(FOAF.firstName, "*Jane*");
            Assert.assertEquals(2, results.size());
            node.addProperty(RDF.type, FOAF.Person);
            Thread.sleep(1000);
            List<NonLiteral> results2 = service.findResources(FOAF.firstName, "*Jane*");
            Assert.assertEquals(3, results2.size());

    }

    @Test
    public void removeTypeTriple() {
        try {
            GraphNode node = new GraphNode(new UriRef(Util.createURN5()), dataGraph);
            node.addPropertyValue(FOAF.firstName, "Another Jane");
            node.addPropertyValue(FOAF.lastName, "Samsing");
            node.addProperty(RDF.type, FOAF.Person);
            Thread.sleep(1000);
            List<NonLiteral> results = service.findResources(FOAF.firstName, "*Jane*");
            Assert.assertEquals(3, results.size());
            node.deleteProperties(RDF.type);
            Thread.sleep(1000);
            List<NonLiteral> results2 = service.findResources(FOAF.firstName, "*Jane*");
            Assert.assertEquals(2, results2.size());
        } catch (ParseException ex) {
        } catch (InterruptedException ex) {
        }
    }

    @Test
    public void removeProperty() {
        try {
            GraphNode node = new GraphNode(new UriRef(Util.createURN5()), dataGraph);
            node.addPropertyValue(FOAF.firstName, "Another Jane");
            node.addPropertyValue(FOAF.lastName, "Samsing");
            node.addProperty(RDF.type, FOAF.Person);
            Thread.sleep(1000);
            List<NonLiteral> results = service.findResources(FOAF.firstName, "*Jane*");
            Assert.assertEquals(3, results.size());
            node.deleteProperties(FOAF.firstName);
            Thread.sleep(1000);
            List<NonLiteral> results2 = service.findResources(FOAF.firstName, "*Jane*");
            Assert.assertEquals(2, results2.size());
        } catch (ParseException ex) {
        } catch (InterruptedException ex) {
        }
    }
    
    @Test
    public void clearGraph() {
        try {
            GraphNode node = new GraphNode(new UriRef(Util.createURN5()), dataGraph);
            node.addPropertyValue(FOAF.firstName, "Another Jane");
            node.addPropertyValue(FOAF.lastName, "Samsing");
            node.addProperty(RDF.type, FOAF.Person);
            Thread.sleep(1000);
            List<NonLiteral> results = service.findResources(FOAF.firstName, "*Jane*");
            Assert.assertEquals(3, results.size());
            dataGraph.clear();
            Thread.sleep(1000);
            List<NonLiteral> results2 = service.findResources(FOAF.firstName, "*Jane*");
            Assert.assertEquals(0, results2.size());
        } catch (ParseException ex) {
        } catch (InterruptedException ex) {
        }
    }

    @Test
    public void reIndexWithoutActualChange() throws InterruptedException {
        try {
            service.reCreateIndex();
            //the old data still available
            Thread.sleep(1000);
            List<NonLiteral> results = service.findResources(FOAF.firstName, "*Joe*");
            Assert.assertEquals(2, results.size());
        } catch (ParseException ex) {
        }

    }

    @Test
    public void reIndexTest() throws InterruptedException {
        IndexDefinitionManager indexDefinitionManager = new IndexDefinitionManager(definitions);

        List<UriRef> predicates = new ArrayList<UriRef>();
        predicates.add(FOAF.firstName);
        predicates.add(FOAF.lastName);
        predicates.add(RDFS.comment);
        indexDefinitionManager.addDefinition(FOAF.Person, predicates);
        service.reCreateIndex();
        Thread.sleep(1000);
        {
            try {
                //the old data still available
                List<NonLiteral> results = service.findResources(FOAF.firstName, "*Joe*");
                Assert.assertEquals(2, results.size());
            } catch (ParseException ex) {
            }
        }
        {
            try {
                //the newly indexed property
                List<NonLiteral> results = service.findResources(RDFS.comment, "*two*");
                Assert.assertEquals(1, results.size());
            } catch (ParseException ex) {
            }
        }
    }

    @Test
    public void joinProperty() {
        try {
            //import VirtualProperties._
            List<VirtualProperty> joinProperties = new ArrayList<VirtualProperty>();
            joinProperties.add(new PropertyHolder(FOAF.firstName));
            joinProperties.add(new PropertyHolder(FOAF.lastName));
            JoinVirtualProperty joinProperty = new JoinVirtualProperty(joinProperties);
            IndexDefinitionManager indexDefinitionManager = new IndexDefinitionManager(definitions);
            List<VirtualProperty> list = new ArrayList<VirtualProperty>();
            list.add(joinProperty);
            indexDefinitionManager.addDefinitionVirtual(FOAF.Person, list);
            service.reCreateIndex();
            Thread.sleep(2000);
            {

                List<NonLiteral> results = service.findResources(joinProperty, "*John*");
                Assert.assertEquals(2, results.size());

            }
            {

                List<NonLiteral> results = service.findResources(joinProperty, "John Doe");
                Assert.assertEquals(1, results.size());

            }
            //late addition
            {
                //check before
                List<NonLiteral> results = service.findResources(joinProperty, "*Joe*");
                Assert.assertEquals(2, results.size());
            }
            createPerson("Another Joe", "Simpsons");
            Thread.sleep(2000);
            {

                List<NonLiteral> results = service.findResources(joinProperty, "*Joe*");
                Assert.assertEquals(3, results.size());

            }
        } catch (ParseException ex) {
        } catch (InterruptedException ex) {
        }
    }

    @Test
    public void pathVirtualProperty() {
        try {
            List<UriRef> pathProperties = new java.util.ArrayList<UriRef>();
            pathProperties.add(ownsPetProperty);
            pathProperties.add(FOAF.name);
            PathVirtualProperty pathProperty = new PathVirtualProperty(pathProperties);

            IndexDefinitionManager indexDefinitionManager = new IndexDefinitionManager(definitions);
            List<VirtualProperty> properties = new ArrayList<VirtualProperty>();
            properties.add(new PropertyHolder(FOAF.firstName));
            properties.add(new PropertyHolder(FOAF.lastName));
            properties.add(pathProperty);
            List<VirtualProperty> joinProperties = new ArrayList<VirtualProperty>();
            joinProperties.add(pathProperty);
            joinProperties.add(new PropertyHolder(ownsPetProperty));
            JoinVirtualProperty joinVirtualProperty = new JoinVirtualProperty(joinProperties);
            properties.add(joinVirtualProperty);
            properties.add(new PropertyHolder(ownsPetProperty));
            indexDefinitionManager.addDefinitionVirtual(FOAF.Person, properties);
            service.reCreateIndex();
            Thread.sleep(1000);
            {
                List<NonLiteral> results = service.findResources(pathProperty, "Silvio");
                Assert.assertEquals(1, results.size());
            }
            //and a late addtition, lets give Frank a pet
            GraphNode frank = new GraphNode(service.findResources(FOAF.firstName, "Frank").get(0), dataGraph);
            GraphNode pet = new GraphNode(new UriRef("http://example.org/pet"), dataGraph);
            //GraphNode pet = new GraphNode(new BNode(), dataGraph);
            frank.addProperty(ownsPetProperty, pet.getNode());
            //Silvio has become a quite popular pet-name
            pet.addPropertyValue(FOAF.name, "Silvio");
            Thread.sleep(1000);
            {
                List<NonLiteral> results = service.findResources(pathProperty, "Silvio");
                Assert.assertEquals(2, results.size());
            }
            //lets give that pet an additional name            
            pet.addPropertyValue(FOAF.name, "Fifi");
            Thread.sleep(1000);
            {
                List<NonLiteral> results = service.findResources(pathProperty, "Fifi");
                Assert.assertEquals(1, results.size());
            }
            //count occurence of distinct firstnames
            CountFacetCollector facetCollector = new CountFacetCollector();
            facetCollector.addFacetProperty(new PropertyHolder(ownsPetProperty));

            Thread.sleep(1000);
            {
                List<Condition> conditions = new ArrayList<Condition>();
                conditions.add(new WildcardCondition(pathProperty, "*i*"));
                //conditions.add(new WildcardCondition(ownsPetProperty, "*"));
                List<NonLiteral> results = service.findResources(conditions, facetCollector);
                Assert.assertTrue(results.size() > 0);
                final Set<Entry<String, Integer>> facets = facetCollector.getFacets(new PropertyHolder(ownsPetProperty));

                //there are 7 distinct first names
                
                Assert.assertEquals(1, facets.size());
                /*//the firstname "Frank" appears once
                Assert.assertEquals(new Integer(1), facetCollector.getFacetValue(firstName, "Frank"));
                //the firstname "frank" never appears
                Assert.assertNull(facetCollector.getFacetValue(firstName, "frank"));
                //the firstname "Jane" appears twice
                Assert.assertEquals(new Integer(2), facetCollector.getFacetValue(firstName, "Jane"));
                //the firstname "Harry" appears once
                Assert.assertEquals(new Integer(1), facetCollector.getFacetValue(firstName, "Harry"));
                //the firstname "Harry Joe" appears once
                Assert.assertEquals(new Integer(1), facetCollector.getFacetValue(firstName, "Harry Joe"));
                //the firstname "William" appears once
                Assert.assertEquals(new Integer(1), facetCollector.getFacetValue(firstName, "William"));
                //the firstname "John" appears twice
                Assert.assertEquals(new Integer(2), facetCollector.getFacetValue(firstName, "John")); */
            }
        } catch (ParseException ex) {
        } catch (InterruptedException ex) {
        }
    }
    
    @Test
    public void facetCollectorTest() throws InterruptedException, ParseException {
        IndexDefinitionManager indexDefinitionManager = new IndexDefinitionManager(definitions);
        final PropertyHolder firstName = new PropertyHolder(FOAF.firstName);
        List<VirtualProperty> properties = new ArrayList<VirtualProperty>();
        properties.add(firstName);
        properties.add(new PropertyHolder(FOAF.lastName));
        indexDefinitionManager.addDefinitionVirtual(FOAF.Person, properties);
        service.reCreateIndex();
        
        //count occurence of distinct firstnames
        CountFacetCollector facetCollector = new CountFacetCollector();
        facetCollector.addFacetProperty(firstName);
        
        Thread.sleep(1000);
        {
            List<NonLiteral> results = service.findResources(firstName, "*", false, facetCollector);
            Assert.assertTrue(results.size() > 0);
            
            //there are 7 distinct first names
            Assert.assertEquals(7, facetCollector.getFacets(firstName).size());
            //the firstname "Frank" appears once
            Assert.assertEquals(new Integer(1), facetCollector.getFacetValue(firstName, "Frank"));
            //the firstname "frank" never appears
            Assert.assertNull(facetCollector.getFacetValue(firstName, "frank"));
            //the firstname "Jane" appears twice
            Assert.assertEquals(new Integer(2), facetCollector.getFacetValue(firstName, "Jane"));
            //the firstname "Harry" appears once
            Assert.assertEquals(new Integer(1), facetCollector.getFacetValue(firstName, "Harry"));
            //the firstname "Harry Joe" appears once
            Assert.assertEquals(new Integer(1), facetCollector.getFacetValue(firstName, "Harry Joe"));
            //the firstname "William" appears once
            Assert.assertEquals(new Integer(1), facetCollector.getFacetValue(firstName, "William"));
            //the firstname "John" appears twice
            Assert.assertEquals(new Integer(2), facetCollector.getFacetValue(firstName, "John"));
        }
    }
    
    @Test
    public void sortedfacetCollectorTest() throws InterruptedException, ParseException {
        IndexDefinitionManager indexDefinitionManager = new IndexDefinitionManager(definitions);
        final PropertyHolder firstName = new PropertyHolder(FOAF.firstName);
        List<VirtualProperty> properties = new ArrayList<VirtualProperty>();
        properties.add(firstName);
        properties.add(new PropertyHolder(FOAF.lastName));
        indexDefinitionManager.addDefinitionVirtual(FOAF.Person, properties);
        
        createPerson("Aaron", "Ignore");
        createPerson("Aaron", "IgnoreMore");
        createPerson("Alpha", "Ignore");
        createPerson("Alpha", "IgnoreMore");
        createPerson("Beta", "Ignore");
        
        service.reCreateIndex();
        
        CountFacetCollector facetCollector = new SortedCountFacetCollector(false, true);
        facetCollector.addFacetProperty(firstName);
        
        Thread.sleep(1000);
        {
            List<NonLiteral> results = service.findResources(firstName, "*", false, facetCollector);
            Assert.assertTrue(results.size() > 0);
            
            Set<Entry<String, Integer>> facets = facetCollector.getFacets(firstName);
            Integer old = Integer.MAX_VALUE;
            String oldKey = null;
            for(Entry<String, Integer> facet : facets) {
                if(old == facet.getValue() && 
                        oldKey != null && 
                        (facet.getKey().compareTo(oldKey) < 0)) {
                    
                    Assert.fail("Facet keys are not ordered in ascending order.");
                }
                if(facet.getValue() > old) {
                    Assert.fail("Facet values are not ordered in descending order.");
                }
                old = facet.getValue();
                oldKey = facet.getKey();
            }
        }
        
        facetCollector = new SortedCountFacetCollector(false, false);
        facetCollector.addFacetProperty(firstName);

        Thread.sleep(1000);
        {
            List<NonLiteral> results = service.findResources(firstName, "*", false, facetCollector);
            Assert.assertTrue(results.size() > 0);

            Set<Entry<String, Integer>> facets = facetCollector.getFacets(firstName);
            Integer old = -1;
            String oldKey = null;
            for (Entry<String, Integer> facet : facets) {
                if (old == facet.getValue()
                        && oldKey != null
                        && (facet.getKey().compareTo(oldKey) < 0)) {
                    Assert.fail("Facet keys are not ordered in ascending order.");
                }
                if (facet.getValue() < old) {
                    Assert.fail("Facet values are not ordered in ascending order.");
                }
                old = facet.getValue();
                oldKey = facet.getKey();
            }
        }

        facetCollector = new SortedCountFacetCollector(true, false);
        facetCollector.addFacetProperty(firstName);

        Thread.sleep(1000);
        {
            List<NonLiteral> results = service.findResources(firstName, "*", false, facetCollector);
            Assert.assertTrue(results.size() > 0);

            Set<Entry<String, Integer>> facets = facetCollector.getFacets(firstName);
            Integer old = -1;
            String oldKey = null;
            for (Entry<String, Integer> facet : facets) {
                if (old == facet.getValue()
                        && oldKey != null
                        && (facet.getKey().compareTo(oldKey) > 0)) {
                    Assert.fail("Facet keys are not ordered in descending order.");
                }
                if (facet.getValue() < old) {
                    Assert.fail("Facet values are not ordered in ascending order.");
                }
                old = facet.getValue();
                oldKey = facet.getKey();
            }
        }

        facetCollector = new SortedCountFacetCollector(true, true);
        facetCollector.addFacetProperty(firstName);

        Thread.sleep(1000);
        {
            List<NonLiteral> results = service.findResources(firstName, "*", false, facetCollector);
            Assert.assertTrue(results.size() > 0);

            Set<Entry<String, Integer>> facets = facetCollector.getFacets(firstName);
            Integer old = Integer.MAX_VALUE;
            String oldKey = null;
            for (Entry<String, Integer> facet : facets) {
                if (old == facet.getValue()
                        && oldKey != null
                        && (facet.getKey().compareTo(oldKey) > 0)) {
                    Assert.fail("Facet keys are not ordered in descending order.");
                }
                if (facet.getValue() > old) {
                    Assert.fail("Facet values are not ordered in descending order.");
                }
                old = facet.getValue();
                oldKey = facet.getKey();
            }
        }
    }
    
    @Test
    public void sortResultsTest() throws InterruptedException, ParseException {
        IndexDefinitionManager indexDefinitionManager = new IndexDefinitionManager(definitions);
        final PropertyHolder firstName = new PropertyHolder(FOAF.firstName);
        List<VirtualProperty> properties = new ArrayList<VirtualProperty>();
        properties.add(firstName);
        indexDefinitionManager.addDefinitionVirtual(FOAF.Person, properties);
        service.reCreateIndex();
        
        SortSpecification sortSpecification = new SortSpecification();
        sortSpecification.add(firstName, SortSpecification.Type.STRING_VAL);
        sortSpecification.add(SortSpecification.INDEX_ORDER);
        
        Thread.sleep(1000);
        {
            List<NonLiteral> results = service.findResources(firstName, "*", false, sortSpecification);
            Assert.assertTrue(results.size() > 0);
            
            List<String> expected = new ArrayList<String>(7);
            expected.add("Frank");
            expected.add("Harry");
            expected.add("Harry Joe");
            expected.add("Jane");
            expected.add("Jane");
            expected.add("Joe");
            expected.add("John");
            
            
            
            List<String> actual = new ArrayList<String>(results.size());
            for(NonLiteral result : results) {
                GraphNode node = new GraphNode(result, dataGraph);
                Iterator<Literal> it = node.getLiterals(FOAF.firstName);
                while(it.hasNext()) {
                    actual.add(it.next().getLexicalForm());
                }
            }
            
            //ignore "John William" because we can not make assumptions about 
            //the order of properties
            Assert.assertArrayEquals(expected.toArray(), 
                    Arrays.copyOfRange(actual.toArray(), 0, 7));
        }
    }
    
    @Test
    public void paginationTest() throws InterruptedException, ParseException {
        IndexDefinitionManager indexDefinitionManager = new IndexDefinitionManager(definitions);
        final PropertyHolder firstName = new PropertyHolder(FOAF.firstName);
        List<VirtualProperty> properties = new ArrayList<VirtualProperty>();
        properties.add(firstName);
        indexDefinitionManager.addDefinitionVirtual(FOAF.Person, properties);
        service.reCreateIndex();
        
        SortSpecification sortSpecification = new SortSpecification();
        sortSpecification.add(firstName, SortSpecification.Type.STRING_VAL);
        sortSpecification.add(SortSpecification.INDEX_ORDER);
        
        Thread.sleep(1000);
        {
            List<Condition> fl = new ArrayList<Condition>();
            fl.add(new WildcardCondition(firstName, "*"));
            List<NonLiteral> results = service.findResources(fl, sortSpecification, 
                    Collections.EMPTY_LIST, 0, 2);
            Assert.assertTrue(results.size() == 2);
            
            List<String> expected = new ArrayList<String>(7);
            expected.add("Frank");
            expected.add("Harry");
            
            List<String> actual = new ArrayList<String>(results.size());
            for(NonLiteral result : results) {
                GraphNode node = new GraphNode(result, dataGraph);
                Iterator<Literal> it = node.getLiterals(FOAF.firstName);
                while(it.hasNext()) {
                    actual.add(it.next().getLexicalForm());
                }
            }
            
            Assert.assertArrayEquals(expected.toArray(), actual.toArray());
            
            results = service.findResources(fl, sortSpecification, 
                    Collections.EMPTY_LIST, 2, 5);
            Assert.assertTrue(results.size() == 3);
            
            expected = new ArrayList<String>(7);
            expected.add("Harry Joe");
            expected.add("Jane");
            expected.add("Jane");
            
            actual = new ArrayList<String>(results.size());
            for(NonLiteral result : results) {
                GraphNode node = new GraphNode(result, dataGraph);
                Iterator<Literal> it = node.getLiterals(FOAF.firstName);
                while(it.hasNext()) {
                    actual.add(it.next().getLexicalForm());
                }
            }
            
            Assert.assertArrayEquals(expected.toArray(), actual.toArray());
            
            results = service.findResources(fl, sortSpecification, 
                    Collections.EMPTY_LIST, 2, 100000);
            Assert.assertTrue(results.size() == 6);
        }
    }
    
    @Test
    public void genericConditionTest() throws InterruptedException, ParseException {
        IndexDefinitionManager indexDefinitionManager = new IndexDefinitionManager(definitions);
        final PropertyHolder firstName = new PropertyHolder(FOAF.firstName);
        List<VirtualProperty> properties = new ArrayList<VirtualProperty>();
        properties.add(firstName);
        indexDefinitionManager.addDefinitionVirtual(FOAF.Person, properties);
        service.reCreateIndex();
        
        
        Thread.sleep(1000);
        {
            List<Condition> conditions = new ArrayList<Condition>(1);
            //exact match --> exact expression match on untokenized
            conditions.add(new GenericCondition(properties, "\"Harry Joe\""));
            List<NonLiteral> results = service.findResources(conditions);
            Assert.assertEquals(1, results.size());
            conditions.clear();
            //case-insensitive exact match
            conditions.add(new GenericCondition(properties, "\"harry joe\""));
            results = service.findResources(conditions);
            Assert.assertEquals(1, results.size());
            conditions.clear();
            //tokenized term match (Harry or Joe)
            conditions.add(new GenericCondition(properties, "Harry Joe"));
            results = service.findResources(conditions);
            Assert.assertEquals(3, results.size());
            conditions.clear();
            //case-insensitive tokenized term
            conditions.add(new GenericCondition(properties, "harry joe"));
            results = service.findResources(conditions);
            Assert.assertEquals(3, results.size());
            conditions.clear();
            //tokenized term match (Harry and Joe)
            conditions.add(new GenericCondition(properties, "+Harry +Joe"));
            results = service.findResources(conditions);
            Assert.assertEquals(1, results.size());
            conditions.clear();
            //tokenized term match (Joe and and not Harry)
            conditions.add(new GenericCondition(properties, "-harry +joe"));
            results = service.findResources(conditions);
            Assert.assertEquals(1, results.size());
            conditions.clear();
            //wildcard match
            conditions.add(new GenericCondition(properties, "harry*"));
            results = service.findResources(conditions);
            Assert.assertEquals(2, results.size());
            conditions.clear();
            //leading wildcard match
            conditions.add(new GenericCondition(properties, "*joe"));
            results = service.findResources(conditions);
            Assert.assertEquals(2, results.size());
            conditions.clear();
        }
        
    }
}
