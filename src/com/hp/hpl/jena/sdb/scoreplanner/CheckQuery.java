package com.hp.hpl.jena.sdb.scoreplanner;


import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.engine1.PlanElement;
import com.hp.hpl.jena.query.engine1.QueryEngine;
import com.hp.hpl.jena.query.engine1.plan.Transformer;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class CheckQuery {
	
	static Log log = LogFactory.getLog(CheckQuery.class);
	
	public static void main(String... args)
	{
		Query query = QueryFactory.create("SELECT * WHERE {{ ?a <b> ?c , <d> .  FILTER (?a < 10) .} UNION { <a> <b> <e> }}");
		QueryEngine qe = new QueryEngine(query);
		PlanScorer pv = new PlanScorer();
		pv.addScorer(new TripleScorer(TripleScorer.UNBOUND, TripleScorer.UNBOUND, TripleScorer.UNBOUND, 1000));
		pv.addScorer(new TripleScorer(null, null, TripleScorer.UNBOUND, 100));
		pv.addScorer(new TripleScorer(null, TripleScorer.UNBOUND, null, 100));
		pv.addScorer(new TripleScorer(TripleScorer.UNBOUND, null, null, 100));
		pv.score(qe.getPlan());
		log.info("Score is: " + pv.getScore(qe.getPlan()));
		PlanElement newPlan = Transformer.transform(new TransformerScore(pv), qe.getPlan());
		log.info(qe.getPlan().toString());
		log.info(newPlan.toString());
		
		String config = "@prefix : <http://jena.hpl.hp.com/schemas/sdbscore#> . " +
			"[ :s :unbound ; :o :unbound ; :score 1000 ] .";
		
		Model cModel = ModelFactory.createDefaultModel();
		cModel.read(new StringReader(config), "", "N3");
		pv = new PlanScorer(cModel);
		pv.score(qe.getPlan());
		log.info("Score is: " + pv.getScore(qe.getPlan()));
	}
}
