/**
 * 
 */
package com.hp.hpl.jena.sdb.scoreplanner;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

class TripleScorer
{
	public static final Node BOUND = Node.create("BOUND");
	public static final Node UNBOUND = Node.create("UNBOUND");
	private Node subject;
	private Node predicate;
	private Node object;
	private int score;

	public TripleScorer(Node subject, Node predicate, Node object, int score)
	{
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.score = score;
	}
	
	public int score(Triple triple)
	{
		if (match(triple.getSubject(), subject) &&
				match(triple.getPredicate(), predicate) &&
				match(triple.getObject(), object))
			return score;
		return 0;
	}

	private boolean match(Node node, Node match) {
		if (match == null) return true;
		if ((match == BOUND) && node.isConcrete()) return true;
		if ((match == UNBOUND) && !node.isConcrete()) return true;
		return node.equals(match);
	}
}