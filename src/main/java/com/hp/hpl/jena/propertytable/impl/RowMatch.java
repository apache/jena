package com.hp.hpl.jena.propertytable.impl;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.BasicPattern;

public class RowMatch {
	
	private BasicPattern pattern;
	
	public RowMatch( BasicPattern pattern ){		
		this.pattern=pattern;
	}

	public Node getMatchSubject(){
		return pattern.get(0).getMatchSubject();
	}
	
	public Node getSubject(){
		return pattern.get(0).getSubject();
	}
	
	public BasicPattern getBasicPattern(){
		return pattern;
	}

}
