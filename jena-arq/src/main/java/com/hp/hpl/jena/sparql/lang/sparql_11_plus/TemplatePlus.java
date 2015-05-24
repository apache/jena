package com.hp.hpl.jena.sparql.lang.sparql_11_plus;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.Template;

public class TemplatePlus extends Template {
	public TemplatePlus(BasicPattern bgp) {
		super(bgp);
	}

	private Node graph =null ;

	public Node getGraph() {
		return graph;
	}

	public void setGraph(Node graph) {
		this.graph = graph;
	}
	
	

	

}
