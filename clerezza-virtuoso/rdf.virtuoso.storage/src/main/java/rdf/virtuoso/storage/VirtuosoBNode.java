package rdf.virtuoso.storage;

import org.apache.clerezza.rdf.core.BNode;

public class VirtuosoBNode extends BNode {
	private String skolemId;
	public VirtuosoBNode(String skolemId) {
		this.skolemId = skolemId;
	}
	
	public String getSkolemId(){
		return skolemId;
	}
	
	public String asSkolemIri(){
		return new StringBuilder().append('<').append(skolemId).append('>').toString();
	}
	
	public String toString(){
		return skolemId;
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof VirtuosoBNode) && (obj.toString().equals(toString()));
	}
}
