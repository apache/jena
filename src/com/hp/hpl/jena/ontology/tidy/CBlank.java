package com.hp.hpl.jena.ontology.tidy;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;

/**
 * @author jjc
 *
 */
class CBlank extends CGeneral {
    
	/**
	 * Constructor for CBlank.
	 * @param n
	 * @param eg
	 */
	public CBlank(Node n, EnhGraph eg) {
		super(n, eg);
	}
    public void incrObjectCount(int i){
    }

}
