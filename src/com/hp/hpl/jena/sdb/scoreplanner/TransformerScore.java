/**
 * 
 */
package com.hp.hpl.jena.sdb.scoreplanner;

import java.util.Collections;
import java.util.Comparator;

import com.hp.hpl.jena.query.engine1.PlanElement;
import com.hp.hpl.jena.query.engine1.plan.PlanBlockTriples;
import com.hp.hpl.jena.query.engine1.plan.TransformCopy;

class TransformerScore extends TransformCopy implements Comparator
{
	private PlanScorer pv;
	
	public TransformerScore(PlanScorer pv)
	{
		this.pv = pv;
	}
	
	@Override
    @SuppressWarnings("unchecked")
	public PlanElement transform(PlanBlockTriples planElt)
	{
		planElt = (PlanBlockTriples) planElt.copy();
		Collections.sort(planElt.getPattern(), this);
		return planElt;
	}

	public int compare(Object o1, Object o2)
	{
		return  pv.getScore(o1).compareTo(pv.getScore(o2));
	}
}