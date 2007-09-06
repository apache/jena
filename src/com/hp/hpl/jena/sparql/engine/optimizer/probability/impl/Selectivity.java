/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.probability.impl;

import java.util.Iterator;
import java.util.Random;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.optimizer.core.BasicPatternJoin;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

/**
 * The class allows selectivity calculation of patterns
 * by executing a corresponding SPARQL query.
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public class Selectivity 
{
	private Model model = null ;
	private static Log log = LogFactory.getLog(Selectivity.class) ;
	
	/**
	 * Constructor, set the ontology model
	 * 
	 * @param model
	 */
	public Selectivity(Model model)
	{
		this.model = model ;
	}

	/**
	 * Get the result set size of a triple pattern
	 * 
	 * @param triple
	 * @return long
	 */
	public long calculate(Triple triple)
	{
		BasicPattern bp = new BasicPattern() ;
		bp.add(normalize(triple)) ;
		
		return calculate(bp) ;
	}
	
	/**
	 * Get the result set size of joined triple patterns
	 * 
	 * @param triple1
	 * @param triple2
	 * @return long
	 */
	public long calculate(Triple triple1, Triple triple2)
	{
		BasicPattern bp = new BasicPattern() ;
		bp.add(normalize(triple1)) ;
		bp.add(normalize(triple2)) ;
		
		return calculate(bp) ;
	}
	
	/**
	 * Get the result set size of a pattern
	 * 
	 * @param pattern
	 * @return long
	 */
	public long calculate(Pattern pattern)
	{
		BasicPattern bp = new BasicPattern() ;
		
		bp.add(new Triple(Var.alloc("x"), pattern.getJoiningProperty().asNode(), Var.alloc("y"))) ;
		
		if (pattern.getJoinType().getURI().equals(BasicPatternJoin.SS))
			bp.add(new Triple(Var.alloc("x"), pattern.getJoinedProperty().asNode(), Var.alloc("z"))) ;
		else if (pattern.getJoinType().getURI().equals(BasicPatternJoin.SO))
			bp.add(new Triple(Var.alloc("z"), pattern.getJoinedProperty().asNode(), Var.alloc("x"))) ;
		else if (pattern.getJoinType().getURI().equals(BasicPatternJoin.OS))
			bp.add(new Triple(Var.alloc("y"), pattern.getJoinedProperty().asNode(), Var.alloc("z"))) ;
		else if (pattern.getJoinType().getURI().equals(BasicPatternJoin.OO))
			bp.add(new Triple(Var.alloc("z"), pattern.getJoinedProperty().asNode(), Var.alloc("y"))) ;
		
		return calculate(bp) ;
	}

	/**
	 * Get the result set size of a BGP
	 * 
	 * @param bp
	 * @return long
	 */
	public long calculate(BasicPattern bp)
	{
		if (model == null)
		{ log.debug("The graph model is required to calculate the selectivity") ; return -1L ; }
		
		Query q = QueryFactory.make() ;
		q.setQuerySelectType() ;
		q.setQueryResultStar(true) ;
		q.setQueryPattern(getQueryBasicPattern(bp)) ;
		QueryExecution qe = QueryExecutionFactory.create(q, model) ;
        ResultSet rs = qe.execSelect() ;
        ResultSetFormatter.consume(rs) ;
        qe.close() ;
       	
       	return rs.getRowNumber() ;
	}
	
	// Construct a SPARQL triples block from a basic pattern
	private ElementTriplesBlock getQueryBasicPattern(BasicPattern bp)
	{
		ElementTriplesBlock el = new ElementTriplesBlock() ;

		for (Iterator iter = bp.iterator(); iter.hasNext(); )
		{
			Triple triple = (Triple)iter.next() ;
		
			el.addTriple(triple) ;
		}
		
		return el ;
	}
	
	// Normalize patterns to a canonical form
	private Triple normalize(Triple triple)
	{
		Node subject = triple.getSubject() ;
		Node predicate = triple.getPredicate() ;
		Node object = triple.getObject() ;
		
		Random generator = new Random();
		int randInt = Math.abs(generator.nextInt()) ;
		
		if (subject.isVariable())
			subject = Var.alloc(subject) ;
		else if (! subject.isConcrete())
			subject = Var.alloc("x" + randInt) ;

		if (predicate.isVariable())
			predicate = Var.alloc(predicate) ;
		else if (! predicate.isConcrete())
			predicate = Var.alloc("y" + randInt) ;
		
		if (object.isVariable())
			object = Var.alloc(object) ;
		else if (! object.isConcrete())
			object = Var.alloc("z" + randInt) ;

		return new Triple(subject, predicate, object) ;
	}
}


/*
 *  (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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