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
package org.apache.clerezza.platform.dashboard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.platform.typerendering.UserContextProvider;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.platform.dashboard.ontologies.*;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.DCTERMS;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.rdf.utils.RdfList;

/**
 * Attaches the global menu to the context node.
 * 
 * @author mir
 */
@Component(enabled=true, immediate=true)
@Service(UserContextProvider.class)
@Reference(name="globalMenuItemsProvider",
		cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
		policy=ReferencePolicy.DYNAMIC,
		referenceInterface=GlobalMenuItemsProvider.class)
		
public class ContextualMenuGenerator implements UserContextProvider {

	Set<GlobalMenuItemsProvider> globalMenuItemsProviders = 
			new HashSet<GlobalMenuItemsProvider>();

	private static class Menu implements Comparable<Menu> {
		GlobalMenuItem root;
		SortedSet<GlobalMenuItem> children = new TreeSet<GlobalMenuItem>();

		private Menu(GlobalMenuItem root) {
			this.root = root;
		}

		private Menu(String identifier) {
			this.root = new GlobalMenuItem(null,identifier ,
					identifier.replace('_', ' '), 0, null);
		}

		@Override
		public int compareTo(Menu o) {
			return root.compareTo(o.root);
		}
	}

	@Override
	public GraphNode addUserContext(GraphNode node) {
		node.addProperty(GLOBALMENU.globalMenu, 
				asRdfList(createMenus(), node.getGraph()));
		return node;
	}

	protected void bindGlobalMenuItemsProvider(GlobalMenuItemsProvider provider) {
		globalMenuItemsProviders.add(provider);
	}

	protected void unbindGlobalMenuItemsProvider(GlobalMenuItemsProvider provider) {
		globalMenuItemsProviders.remove(provider);
	}

	private SortedSet<Menu> createMenus() {
		Set<GlobalMenuItem> allItems = new HashSet<GlobalMenuItem>();
		for (GlobalMenuItemsProvider provider : globalMenuItemsProviders) {
			Set<GlobalMenuItem> items = provider.getMenuItems();
			for (GlobalMenuItem item: items) {
				allItems.add(item);
			}
		}

		SortedSet<Menu> menus = new TreeSet<Menu>();
		Map<String, Menu> idMenuMap = new HashMap<String, Menu>();
		Iterator<GlobalMenuItem> globalMenuItems = allItems.iterator();
		while (globalMenuItems.hasNext()) {
			GlobalMenuItem globalMenuItem = globalMenuItems.next();
			if (globalMenuItem.getGroupIdentifier() == null) {
				final Menu menu = new Menu(globalMenuItem);
				idMenuMap.put(menu.root.getIdentifier(), menu);
				menus.add(menu);
				globalMenuItems.remove();
			}
		}
		globalMenuItems = allItems.iterator();
		while (globalMenuItems.hasNext()) {
			GlobalMenuItem globalMenuItem = globalMenuItems.next();
			Menu menu = idMenuMap.get(globalMenuItem.getGroupIdentifier());
			if (menu != null) {
				menu.children.add(globalMenuItem);
				globalMenuItems.remove();
			}
		}
		Set<String> implicitGroups = new HashSet<String>();
		for (GlobalMenuItem globalMenuItem : allItems) {
			final String groupIdentifier = globalMenuItem.getGroupIdentifier();
			implicitGroups.add(groupIdentifier);
		}
		for (String implicitGroupId : implicitGroups) {
			Menu menu = new Menu(implicitGroupId);
			idMenuMap.put(menu.root.getIdentifier(), menu);
			menus.add(menu);
		}

		for (GlobalMenuItem item : allItems) {
			Menu menu = idMenuMap.get(item.getGroupIdentifier());
			if (menu != null) {
				menu.children.add(item);
			}
		}
		return menus;
	}
	
	private NonLiteral asRdfList(SortedSet<Menu> menus, TripleCollection mGraph) {
		NonLiteral result = new BNode();
		RdfList list = new RdfList(result, mGraph);
		for (Menu menu : menus) {
			BNode node = new BNode();
			final String label = menu.root.getLabel();
			Literal labelLiteral = new PlainLiteralImpl(label);
			mGraph.add(new TripleImpl(node, RDFS.label,labelLiteral));
			final String description = menu.root.getDescription();
			if (description != null) {
				Literal descLiteral = new PlainLiteralImpl(description);
				mGraph.add(new TripleImpl(node, DCTERMS.description, descLiteral));
			}
			final String path = menu.root.getPath();
			if (path != null) {
				Literal pathLiteral = LiteralFactory.getInstance().
						createTypedLiteral(path);
				mGraph.add(new TripleImpl(node, GLOBALMENU.path, pathLiteral));
			}
			if (menu.children.size() > 0) {
				mGraph.add(new TripleImpl(node, GLOBALMENU.children, 
						itemsAsRdfList(menu.children, mGraph)));
				mGraph.add(new TripleImpl(node, RDF.type, GLOBALMENU.Menu));
			} else {
				mGraph.add(new TripleImpl(node, RDF.type, GLOBALMENU.MenuItem));
			}
			list.add(node);
			
		}
		return result;
	}

	private NonLiteral itemsAsRdfList(SortedSet<GlobalMenuItem> menus, TripleCollection mGraph) {
		NonLiteral result = new BNode();
		RdfList list = new RdfList(result, mGraph);
		for (GlobalMenuItem item : menus) {
			BNode node = new BNode();
			final String label = item.getLabel();
			Literal labelLiteral = new PlainLiteralImpl(label);
			mGraph.add(new TripleImpl(node, RDFS.label,labelLiteral));
			final String description = item.getDescription();
			if (description != null) {
				Literal descLiteral = new PlainLiteralImpl(description);
				mGraph.add(new TripleImpl(node, DCTERMS.description, descLiteral));
			}
			final String path = item.getPath();
			if (path != null) {
				Literal pathLiteral = LiteralFactory.getInstance().
						createTypedLiteral(path);
				mGraph.add(new TripleImpl(node, GLOBALMENU.path, pathLiteral));
			}
			mGraph.add(new TripleImpl(node, RDF.type, GLOBALMENU.MenuItem));
			list.add(node);
		}
		return result;
	}
}