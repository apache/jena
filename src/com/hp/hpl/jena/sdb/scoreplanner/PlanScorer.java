/**
 * 
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
import com.hp.hpl.jena.query.engine1.PlanElement;
import com.hp.hpl.jena.query.engine1.plan.PlanBlockTriples;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

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
			if (elt instanceof PlanBlockTriples)
				subscores[i] = calcScores((PlanBlockTriples) elt);
			else
				subscores[i] = calcScores(elt);
		}
		int score = score(subscores, planElt);
		scores.put(planElt, score);
		return score;
	}
	
	public int calcScores(PlanBlockTriples planElt)
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