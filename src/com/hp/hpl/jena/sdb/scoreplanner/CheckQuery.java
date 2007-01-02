/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.scoreplanner;

//import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
//import com.hp.hpl.jena.query.engine1.PlanElement;
//import com.hp.hpl.jena.query.engine1.QueryEngine;
//import com.hp.hpl.jena.query.engine1.plan.Transformer;
import com.hp.hpl.jena.query.engine2.QueryEngineQuad;
import com.hp.hpl.jena.query.engine2.op.Op;
//import com.hp.hpl.jena.rdf.model.Model;
//import com.hp.hpl.jena.rdf.model.ModelFactory;
//import com.hp.hpl.jena.sdb.SDBException;

public class CheckQuery {
	
	static Log log = LogFactory.getLog(CheckQuery.class);
	
	public static void main(String... args)
	{
        Query query = QueryFactory.create("SELECT * WHERE {{ ?a <b> ?c , <d> .  FILTER (?a < 10) .} UNION { <a> <b> <e> }}");
        QueryEngineQuad qe = new QueryEngineQuad(query);
        Op op = qe.getOp() ;
        
        PlanScorer pv = new PlanScorer();
        pv.addScorer(new TripleScorer(TripleScorer.UNBOUND, TripleScorer.UNBOUND, TripleScorer.UNBOUND, 1000));
        pv.addScorer(new TripleScorer(null, null, TripleScorer.UNBOUND, 100));
        pv.addScorer(new TripleScorer(null, TripleScorer.UNBOUND, null, 100));
        pv.addScorer(new TripleScorer(TripleScorer.UNBOUND, null, null, 100));
        //pv.score(qe.getPlan());
        log.info("Score is: " + pv.getScore(qe.getPlan()));
        
        
        
        
//		Query query = QueryFactory.create("SELECT * WHERE {{ ?a <b> ?c , <d> .  FILTER (?a < 10) .} UNION { <a> <b> <e> }}");
//		QueryEngine qe = new QueryEngine(query);
//		PlanScorer pv = new PlanScorer();
//		pv.addScorer(new TripleScorer(TripleScorer.UNBOUND, TripleScorer.UNBOUND, TripleScorer.UNBOUND, 1000));
//		pv.addScorer(new TripleScorer(null, null, TripleScorer.UNBOUND, 100));
//		pv.addScorer(new TripleScorer(null, TripleScorer.UNBOUND, null, 100));
//		pv.addScorer(new TripleScorer(TripleScorer.UNBOUND, null, null, 100));
//		pv.score(qe.getPlan());
//		log.info("Score is: " + pv.getScore(qe.getPlan()));
//		PlanElement newPlan = Transformer.transform(new TransformerScore(pv), qe.getPlan());
//		log.info(qe.getPlan().toString());
//		log.info(newPlan.toString());
//		
//		String config = "@prefix : <http://jena.hpl.hp.com/schemas/sdbscore#> . " +
//			"[ :s :unbound ; :o :unbound ; :score 1000 ] .";
//		
//		Model cModel = ModelFactory.createDefaultModel();
//		cModel.read(new StringReader(config), "", "N3");
//		pv = new PlanScorer(cModel);
//		pv.score(qe.getPlan());
//		log.info("Score is: " + pv.getScore(qe.getPlan()));
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

