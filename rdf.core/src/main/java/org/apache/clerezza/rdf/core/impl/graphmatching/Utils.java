package org.apache.clerezza.rdf.core.impl.graphmatching;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Triple;

public class Utils {

	static Set<BNode> getBNodes(Collection<Triple> s) {
		Set<BNode> result = new HashSet<BNode>();
		for (Triple triple : s) {
			if (triple.getSubject() instanceof BNode) {
				result.add((BNode) triple.getSubject());
			}
			if (triple.getObject() instanceof BNode) {
				result.add((BNode) triple.getObject());
			}
		}
		return result;
	}

	/**
	 * removes the common grounded triples from s1 and s2. returns false if
	 * a grounded triple is not in both sets, true otherwise
	 */
	static boolean removeGrounded(Collection<Triple> s1, Collection<Triple> s2) {
		Iterator<Triple> triplesIter = s1.iterator();
		while (triplesIter.hasNext()) {
			Triple triple = triplesIter.next();
			if (!isGrounded(triple)) {
				continue;
			}
			if (!s2.remove(triple)) {
				return false;
			}
			triplesIter.remove();
		}
		//for efficiency we might skip this (redefine method)
		for (Triple triple : s2) {
			if (isGrounded(triple)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isGrounded(Triple triple) {
		if (triple.getSubject() instanceof BNode) {
			return false;
		}
		if (triple.getObject() instanceof BNode) {
			return false;
		}
		return true;
	}

}
