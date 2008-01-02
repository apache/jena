/**
 * 
 */
package com.hp.hpl.jena.sdb.scoreplanner;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;

class TripleScorer
{
	public static final Node BOUND = NodeCreateUtils.create("BOUND");
	public static final Node UNBOUND = NodeCreateUtils.create("UNBOUND");
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


/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
