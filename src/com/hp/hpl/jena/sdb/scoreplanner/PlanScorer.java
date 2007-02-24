/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.scoreplanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.engine.engine1.PlanElement;
import com.hp.hpl.jena.sparql.engine.engine1.plan.PlanTriples;
import com.hp.hpl.jena.rdf.model.*;

class PlanScorer
{
	final static Log log = LogFactory.getLog(PlanScorer.class);
	
	final static String ns = "http://jena.hpl.hp.com/schemas/sdbscore#";
	final static Property s = ResourceFactory.createProperty(ns + "s");
	final static Property p = ResourceFactory.createProperty(ns + "p");
	final static Property o = ResourceFactory.createProperty(ns + "o");
	final static Property score = ResourceFactory.createProperty(ns + "score");
	final static Resource bound = ResourceFactory.createResource(ns + "bound");
	final static Resource unbound = ResourceFactory.createResource(ns + "unbound");
	
	protected Map <Object, Integer> scores;
	protected List<TripleScorer> scorers;
	
	public PlanScorer()
	{
		scores = new HashMap<Object, Integer>();
		this.scorers = new ArrayList<TripleScorer>();
	}
	
	public PlanScorer(Model config)
	{
		this();
		ResIterator ri = config.listSubjects();
		while (ri.hasNext())
			makeTripleScorer(ri.nextResource());
		ri.close();
	}
	
	private void makeTripleScorer(Resource resource)
	{
		Node subject = null, predicate = null, object = null;
		Integer scoreVal = null;
		if (resource.hasProperty(s))
			subject = getNodeTest(resource.getProperty(s));
		if (resource.hasProperty(p))
			predicate = getNodeTest(resource.getProperty(p));
		if (resource.hasProperty(o))
			object = getNodeTest(resource.getProperty(o));
		if (resource.hasProperty(score))
		{
			RDFNode val = resource.getProperty(score).getObject();
			if (val instanceof Literal)
				scoreVal = ((Literal) val).getInt();
			else
				log.warn("Score is not a literal");
		}
		
		if (scoreVal != null)
			addScorer(new TripleScorer(subject, predicate, object, scoreVal));
	}

	private Node getNodeTest(Statement stmt)
	{
		RDFNode val = stmt.getObject();
		if (val instanceof Literal) return val.asNode();
		if (bound.equals(val)) return TripleScorer.BOUND;
		if (unbound.equals(val)) return TripleScorer.UNBOUND;
		return val.asNode();
	}

	public void addScorer(TripleScorer scorer)
	{
		scorers.add(scorer);
	}
	
	public void score(PlanElement plan)
	{
		// recurse plan to determine scores
		calcScores(plan);
	}
	
	public Integer getScore(Object planElt)
	{
		return scores.get(planElt);
	}
	
	public int calcScores(PlanElement planElt)
	{
		int[] subscores = new int[planElt.numSubElements()];
		for (int i = 0; i < planElt.numSubElements(); i++)
		{
			PlanElement elt = planElt.getSubElement(i);
			if (elt instanceof PlanTriples)
				subscores[i] = calcScores((PlanTriples) elt);
			else
				subscores[i] = calcScores(elt);
		}
		int score = score(subscores, planElt);
		scores.put(planElt, score);
		return score;
	}
	
	public int calcScores(PlanTriples planElt)
	{
		int[] subscores = new int[planElt.getPattern().size()];
		for (int i = 0; i < planElt.getPattern().size(); i++)
		{
			int tripScore = score((Triple) planElt.getPattern().get(i));
			scores.put(planElt.getPattern().get(i), tripScore);
			subscores[i] = tripScore;
		}
		int score = score(subscores, planElt);
		scores.put(planElt, score);
		return score;
	}
	
	protected int score(Triple t)
	{
		int score = 0;
		for (TripleScorer ts: scorers)
			score += ts.score(t);
		return score;
	}
	
	protected int score(int[] scores, PlanElement planElt)
	{
		int score = 0;
		for (int i: scores) score += i;
		return score;
	}
}


/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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

