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
package org.apache.clerezza.platform.content.hierarchy;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.platform.config.PlatformConfig;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.HIERARCHY;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;


/**
 * @author mir
 */
public class HierarchyTest{

	private static UriRef root = new UriRef("http://localhost:8282/");
	private UriRef foo = new UriRef("http://localhost:8282/foo/");
	private UriRef fooResource = new UriRef("http://localhost:8282/foo/resource");
	private UriRef fooResource2 = new UriRef("http://localhost:8282/foo/resource2");
	private UriRef fooResource3 = new UriRef("http://localhost:8282/foo/resource3");
	private UriRef fooTest = new UriRef("http://localhost:8282/foo/test/");
	private UriRef fooTestResource4 = new UriRef("http://localhost:8282/foo/test/resource4");
	private UriRef fooFolder1 = new UriRef("http://localhost:8282/foo/folder1/");
	private UriRef bar = new UriRef("http://localhost:8282/bar/");
	private UriRef barResource = new UriRef("http://localhost:8282/bar/resource");
	private UriRef barResource2 = new UriRef("http://localhost:8282/bar/resource2");
	private UriRef barFoo = new UriRef("http://localhost:8282/bar/foo/");
	private UriRef barFooResource = new UriRef("http://localhost:8282/bar/foo/resource");
	private UriRef barFooTest = new UriRef("http://localhost:8282/bar/foo/test/");
	private UriRef newRoot = new UriRef("http://newRoot/");
	private UriRef newRootTest = new UriRef("http://newRoot/test/");
	private UriRef newRoot2Resource = new UriRef("http://newRoot2/resource");
	private UriRef newRoot2 = new UriRef("http://newRoot2/");
	private UriRef unencodedResource = new UriRef("http://localhost:8282/t +");
	private UriRef encodedResource = new UriRef("http://localhost:8282/t%20%2B");
	private UriRef unencodedCollection = new UriRef("http://localhost:8282/t +/");
	private UriRef encodedCollection = new UriRef("http://localhost:8282/t%20%2B/");

        

	@Test
	public void listPositionTest() throws Exception{
		HierarchyService hierarchyService = getHierarchyService();
		HierarchyNode res1Node = hierarchyService.createNonCollectionNode(fooResource);
		HierarchyNode res3Node = hierarchyService.createNonCollectionNode(fooResource3);
		HierarchyNode res2Node = hierarchyService.createNonCollectionNode(fooResource2, 1);
		CollectionNode fooNode = res1Node.getParent();
		Iterator<HierarchyNode> fooMembers = fooNode.getMembers().iterator();

		Assert.assertEquals(res1Node, fooMembers.next());
		Assert.assertEquals(res2Node, fooMembers.next());
		Assert.assertEquals(res3Node, fooMembers.next());
	}

	@Test
	public void collectionNodeCreationAndDeletionTest() throws Exception{
		HierarchyService hierarchyService = getHierarchyService();
		CollectionNode fooFolder1Node = hierarchyService.createCollectionNode(fooFolder1, 0);
		boolean exceptionThrown = false;
		try {
			hierarchyService.createCollectionNode(foo);
		} catch(NodeAlreadyExistsException e) {
			exceptionThrown = true;
		}
		Assert.assertTrue(exceptionThrown);
		try {
			hierarchyService.createCollectionNode(fooResource);
		} catch(IllegalArgumentException e) {
			exceptionThrown = true;
		}

		Assert.assertTrue(exceptionThrown);
		CollectionNode fooNode = (CollectionNode)hierarchyService.getHierarchyNode(foo);
		List<HierarchyNode> fooMembers = fooNode.getMembers();
		Assert.assertEquals(1, fooMembers.size());	
		Assert.assertEquals(fooFolder1Node, fooMembers.get(0));
		CollectionNode rootNode = (CollectionNode)hierarchyService.getHierarchyNode(root);
		List<HierarchyNode> rootList = rootNode.getMembers();
		Assert.assertEquals(1, rootList.size());
		Assert.assertEquals(fooNode, rootList.get(0));
	
		fooNode.delete();

		exceptionThrown = false;
		try {
			fooFolder1Node = (CollectionNode) hierarchyService.getHierarchyNode(fooFolder1);
		} catch(NodeDoesNotExistException e) {
			exceptionThrown = true;
		}
		Assert.assertTrue(exceptionThrown);

		exceptionThrown = false;
		try {
			fooNode = (CollectionNode) hierarchyService.getHierarchyNode(foo);
		} catch(NodeDoesNotExistException e) {
			exceptionThrown = true;
		}
		Assert.assertTrue(exceptionThrown);
	}

    @Test
	public void nonCollectionNodeCreation() throws Exception{
		HierarchyService hierarchyService = getHierarchyService();
		HierarchyNode fooTestResource4Node = hierarchyService.
		createNonCollectionNode(fooTestResource4, 0);
		CollectionNode fooTestNode = fooTestResource4Node.getParent();
		Assert.assertEquals(fooTest, fooTestNode.getNode());
		CollectionNode fooNode = fooTestNode.getParent();
		Assert.assertEquals(foo, fooNode.getNode());
	}

	@Test
	public void nonCollectionNodeCreationWithEncodedCharacters() throws Exception{
		HierarchyService hierarchyService = getHierarchyService();
		hierarchyService.createNonCollectionNode(unencodedResource, 0);
        HierarchyNode encodedNode = hierarchyService.getHierarchyNode(unencodedResource);
		Assert.assertEquals(encodedResource, encodedNode.getNode());
	}

	@Test
	public void collectionNodeCreationWithEncodedCharacters() throws Exception{
		HierarchyService hierarchyService = getHierarchyService();
		hierarchyService.createCollectionNode(unencodedCollection, 0);
        CollectionNode encodedNode = hierarchyService.getCollectionNode(unencodedCollection);
		Assert.assertEquals(encodedCollection, encodedNode.getNode());
	}

	@Test
	public void nonCollectionMoveTest() throws Exception{
		HierarchyService hierarchyService = getHierarchyService();		
		HierarchyNode resourceNode = hierarchyService.createNonCollectionNode(fooResource);
		CollectionNode barNode = hierarchyService.createCollectionNode(bar);

		CollectionNode fooNode = (CollectionNode)hierarchyService.getHierarchyNode(foo);
		List<HierarchyNode> fooList = fooNode.getMembers();
		Assert.assertEquals(1, fooList.size());
		Assert.assertEquals(resourceNode, fooList.get(0));
		resourceNode.move(barNode, 0);
		List<HierarchyNode> barList = barNode.getMembers();
		fooList = fooNode.getMembers();
		Assert.assertEquals(0, fooList.size());
		Assert.assertEquals(1, barList.size());
		HierarchyNode movedResourceNode = hierarchyService.getHierarchyNode(barResource);
		Assert.assertEquals(movedResourceNode, barList.get(0));
		hierarchyService.createNonCollectionNode(barResource2, 1);
		barList = barNode.getMembers();
		HierarchyNode barResource2Node = hierarchyService.getHierarchyNode(barResource2);
		Assert.assertEquals(barResource2Node, barList.get(1));
		movedResourceNode.move(barNode, 2);
		barList = barNode.getMembers();
		Assert.assertEquals(barResource2Node, barList.get(0));
		Assert.assertEquals(movedResourceNode, barList.get(1));
		movedResourceNode.move(barNode, 0);
		barList = barNode.getMembers();
		Assert.assertEquals(movedResourceNode, barList.get(0));
		Assert.assertEquals(barResource2Node, barList.get(1));
	}

	@Test
	public void nonCollectionMoveTest2() throws Exception{
		HierarchyService hierarchyService = getHierarchyService();
		hierarchyService.createNonCollectionNode(fooResource);
		CollectionNode barNode = hierarchyService.createCollectionNode(bar);

		CollectionNode fooNode = (CollectionNode)hierarchyService.getHierarchyNode(foo);
		fooNode.move(barNode, 0);
		List<HierarchyNode> barList = barNode.getMembers();
		CollectionNode barFooNode = hierarchyService.getCollectionNode(barFoo);
		Assert.assertEquals(1, barList.size());
		Assert.assertEquals(barFooNode, barList.get(0));
		List<HierarchyNode> barFooList = barFooNode.getMembers();
		Assert.assertEquals(1, barFooList.size());
		System.out.println(barFooList.get(0).toString());
		HierarchyNode barFooResourceNode = hierarchyService.getHierarchyNode(barFooResource);
		Assert.assertEquals(barFooResourceNode, barFooList.get(0));
	}
	
	@Test
	public void collectionMoveTest() throws Exception{
		HierarchyService hierarchyService = getHierarchyService();
		hierarchyService.createCollectionNode(fooFolder1);
		CollectionNode barNode = hierarchyService.createCollectionNode(bar);
		CollectionNode rootNode = (CollectionNode) hierarchyService.getHierarchyNode(root);
		CollectionNode fooNode = (CollectionNode) hierarchyService.getHierarchyNode(foo);
		Assert.assertEquals(0, barNode.getMembers().size());
		Assert.assertTrue(rootNode.getMembers().contains(fooNode));
		fooNode.move(barNode, 0);
		HierarchyNode barFooNode = hierarchyService.getHierarchyNode(barFoo);
		Assert.assertTrue(barNode.getMembers().contains(barFooNode));
		Assert.assertFalse(rootNode.getMembers().contains(fooNode));
	}

	@Test
	public void collectionMoveTest2() throws Exception{
		HierarchyService hierarchyService = getHierarchyService();
		hierarchyService.createCollectionNode(fooTest);
		CollectionNode barNode = hierarchyService.createCollectionNode(bar);

		CollectionNode fooNode = (CollectionNode)hierarchyService.getHierarchyNode(foo);
		fooNode.move(barNode, 0);
		List<HierarchyNode> barList = barNode.getMembers();
		CollectionNode barFooNode = hierarchyService.getCollectionNode(barFoo);
		Assert.assertEquals(1, barList.size());
		Assert.assertEquals(barFooNode, barList.get(0));
		List<HierarchyNode> barFooList = barFooNode.getMembers();
		Assert.assertEquals(1, barFooList.size());
		HierarchyNode barFooTestNode = hierarchyService.getHierarchyNode(barFooTest);
		Assert.assertEquals(barFooTestNode, barFooList.get(0));
	}

	@Test
	public void collectionMoveIntoItselfTest() throws Exception {
		HierarchyService hierarchyService = getHierarchyService();
		CollectionNode barNode = hierarchyService.createCollectionNode(bar);
		try {
			barNode.move(barNode, 0);
			Assert.assertTrue(false);
		} catch (IllegalMoveException ex) {
			Assert.assertTrue(true);
		}
	}

	@Test
	public void renamingTest() throws Exception {
		HierarchyService hierarchyService = getHierarchyService();
		CollectionNode barNode = hierarchyService.createCollectionNode(bar);
		barNode.move(barNode.getParent(), "foo", 0);
		try {
			barNode = hierarchyService.getCollectionNode(bar);
			Assert.assertTrue(false);
		} catch (NodeDoesNotExistException e) {}
		try {
			hierarchyService.getCollectionNode(foo);
		} catch (NodeDoesNotExistException e) {
			Assert.assertTrue(false);
		}
		HierarchyNode resource = hierarchyService.createNonCollectionNode(fooResource);
		resource.move(resource.getParent(), "resource2", 0);
		try {
			resource = hierarchyService.getHierarchyNode(fooResource);
			Assert.assertTrue(false);
		} catch (NodeDoesNotExistException e) {}
		try {
			hierarchyService.getHierarchyNode(fooResource2);
		} catch (NodeDoesNotExistException e) {
			Assert.assertTrue(false);
		}
		HierarchyNode resource3 = hierarchyService.createNonCollectionNode(fooResource3);
		try {
			resource.move(resource3.getParent(), "resource2", 0);
			Assert.assertTrue(false);
		} catch (NodeAlreadyExistsException ex) {}
	}

	@Test(expected=UnknownRootExcetpion.class)
	public void missingRootTest() throws Exception{
		HierarchyService hierarchyService = getHierarchyService();
		hierarchyService.getHierarchyNode(newRootTest);
	}

	@Test
	public void rootAutoCreationTest() throws Exception{
		HierarchyService hierarchyService = getHierarchyService();
		hierarchyService.createCollectionNode(newRootTest);
		CollectionNode newRootNode = hierarchyService.getCollectionNode(newRoot);
		Assert.assertTrue(hierarchyService.getRoots().contains(newRootNode));

		hierarchyService.createNonCollectionNode(newRoot2Resource);
		CollectionNode newRoot2Node = hierarchyService.getCollectionNode(newRoot2);
		Assert.assertTrue(hierarchyService.getRoots().contains(newRoot2Node));

		boolean exceptionThrown = false;
		try {
			hierarchyService.createCollectionNode(
					new UriRef("http:///test2/"));
		} catch(IllegalArgumentException e) {
			exceptionThrown = true;
		}
		Assert.assertTrue(exceptionThrown);
	}
	
	private static class MyContentGraphProvider extends ContentGraphProvider {
		private MGraph graph = new SimpleMGraph();
		@Override
		public MGraph getContentGraph() {
			return graph;
		} 
	}

	private static class MyPlatformConfig extends PlatformConfig {

		MyPlatformConfig() {
			final SimpleMGraph systemGraph = new SimpleMGraph();
			systemGraph.add(new TripleImpl(new BNode(),
					RDF.type, PLATFORM.Instance));
			bindSystemGraph(systemGraph);
		}
		@Override
		public Set<UriRef> getBaseUris() {
			return Collections.singleton(root);
		}
	}

	private HierarchyService getHierarchyService() {
		HierarchyService hierarchyService = new TestHierarchyService();
		ContentGraphProvider myCgProvider = new MyContentGraphProvider();
		final SimpleMGraph systemGraph = new SimpleMGraph();
		PlatformConfig myPlatConf = new MyPlatformConfig();
		hierarchyService.cgProvider = myCgProvider;
		hierarchyService.config = myPlatConf;
		
		hierarchyService.systemGraph = systemGraph;
		Triple rootTriple = new TripleImpl(root,
			RDF.type, HIERARCHY.Collection);
		myCgProvider.getContentGraph().add(rootTriple);
		hierarchyService.activate(null);
		return hierarchyService;
	}

	private class TestHierarchyService extends HierarchyService {
		@Override
		protected GraphNode getCreator() {
			GraphNode node = new GraphNode(new BNode(), new SimpleMGraph());
			node.addProperty(PLATFORM.userName, new PlainLiteralImpl("userName"));
			return node;
		}
	}
}
