/*
 * (c) Copyright 2003 Hewlett-Packard Development Company, LP  
 * [see end of file]
 */

package com.hp.hpl.jena.db.impl;

import java.util.List;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.Element;
import com.hp.hpl.jena.graph.query.Fixed;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
	@author kers
<br>
    PatternStageCompiler serves two purposes: it contains the standard algorithm
    for compiling patterns-as-triples to patterns-as-Pattern(s), and it has the
    standard implementation of PatternCompiler in terms of ordinary Elements.
*/
public final class DBQueryStageCompiler
    {
	
    public DBQueryStageCompiler()
        {}
      
    /**
        to compile an array of triples, compile each triple and form the corresponding
        array of Patterns. *preserve the order*. 
    */  
    public static DBQuery compile( DBQueryStageCompiler compiler, DBQueryHandler qh, SpecializedGraph sg,
    		List varList, List dbPat, Graph constraints )
        {
        DBQuery query = new DBQuery(sg,varList,qh.queryOnlyStmt,
        		qh.queryOnlyReif,qh.queryFullReif);
        int j;
        for (int i = 0; i < dbPat.size(); i += 1) {
			compilePattern (compiler, query, (DBPattern) dbPat.get(i));
        }
		compileConstraints (compiler, query, constraints);
		compileQuery (compiler, query);
        return query;
    }
       
    
	/**
		compile a triple pattern.
	*/

    private static void compilePattern( DBQueryStageCompiler compiler, DBQuery query,
    		DBPattern dbpat )
        {
			Element subj = dbpat.S;
			Element obj = dbpat.O;
			Element pred = dbpat.P;
			String qual = null;
			int alias = query.aliasCnt;;

        	if ( query.isReifier ) {
        		boolean newAlias = true;
        		if ( !(pred instanceof Fixed) )
        			throw new JenaException("Reifier predicate not bound");
        		Node p = ((Fixed)pred).asNodeMatch((Domain)null);
        		char reifProp;    		
				if ( p.equals(RDF.Nodes.subject) ) reifProp = 'S';
				else if ( p.equals(RDF.Nodes.predicate) ) reifProp = 'P';
				else if ( p.equals(RDF.Nodes.object) ) reifProp = 'O';
				else if ( p.equals(RDF.Nodes.type) ) {
					reifProp = 'T';
					// need to check here the value of object and adjust appropriately
				} 
				else throw new JenaException("Unexpected reifier predicate");
				if ( !subj.equals(Element.ANY) ) {
					// optionally do join optimization for reification.
					// if the subject is joined with another pattern and
					// that subject is already bound, skip the join.
					if ( query.qryFullReif && (subj instanceof Free) && 
						query.getBinding(((Free)subj).getListing()).isBoundToCol() ) {
							alias = (query.getBinding(((Free)subj).getListing())).alias;
							newAlias = false;
						} else 
							qual = query.ga.gen(getQual(query,alias,'N',subj));
				}			
				qual += query.ga.gen(getQual(query,alias,reifProp,obj));
				qual += query.ga.gen(query.driver.genSQLQualGraphId(alias,query.graphId));
				if ( newAlias ) query.newAlias();
        		
        	} else {
        		// query over triple table
				qual = query.ga.gen(getQual(query,alias,'S',subj));
				qual += query.ga.gen(getQual(query,alias,'P',pred));
				qual += query.ga.gen(getQual(query,alias,'O',obj));
				qual += query.ga.gen(query.driver.genSQLQualGraphId(alias,query.graphId));
				query.newAlias();
        	}
		query.stmt += qual;
        }
        
		private static String getQual(DBQuery query,int alias,char pred, Element spo) {
			String qual = "";
			if (spo instanceof Fixed) {
				qual = query.driver.genSQLQualConst(alias,pred,
						((Fixed) spo).asNodeMatch((Domain) null));
			} else if (spo instanceof Free){
				Free v = (Free) spo;
				VarIndex bind = query.getBinding(v.getListing());
				// only bind to argument value for first use of variable.
				// subsequent references should join to first binding.
				if ( v.isArg() && !bind.isBoundToCol ) {
					query.argCnt++;
					query.argType += pred;
					query.argIndex.add(new Integer(v.getMapping()));
					qual = query.driver.genSQLQualParam(alias, pred);
					bind.bindToCol(alias, pred);
				} else {
					if (bind.isBoundToCol()) {
						qual =
							query.driver.genSQLJoin( bind.alias, bind.column,
								alias, pred);
					} else {
						// nothing to compare. just binding the var to the column
						bind.bindToCol(alias, pred);
						qual = "";
					}
				}
			} else if ( spo != Element.ANY )
				throw new JenaException("Invalid Element in qualifier");
			return qual;
		}

		/**
			compile the constraints.
		*/
		private static void compileConstraints( DBQueryStageCompiler compiler, DBQuery query,
			Graph constraints )
			{
				// for each variable bound by this query, compile any equal
				// or not equal constraints
			/* disable for now
				int varCnt = query.binding.length;
				for(int i = 0; (i<varCnt) && (constraints.size() > 0); i++) {
					VarIndex vx = query.binding[i];
					if ( vx.isArgVar() )
						continue;
					// look for variable on both left and right side of operator
					compConstraints(query, vx.var, constraints);
					compConstraints(query, vx.var, constraints);
				}
			*/	
			}
			
		private static void compConstraints( DBQuery query, Node_Variable var, Graph constraints ) {
			Triple[] done = new Triple[constraints.size()];
			int doneCnt = 0;
			// check for var on left side of operator
			ExtendedIterator it = constraints.find(var, Node.ANY, Node.ANY);
			while ( it.hasNext() ) {
				Triple t = (Triple) it.next();
				if ( genConstraints(query,(Node_Variable)t.getSubject(),t.getPredicate(),t.getObject()) )
					done[doneCnt++] = t;
			}
			for(doneCnt--;doneCnt>0;doneCnt--) constraints.delete(done[doneCnt]);
			
			// check for var on right side of operator
			it = constraints.find( Node.ANY, Node.ANY, var);
			while ( it.hasNext() ) {
				Triple t = (Triple) it.next();
				if ( genConstraints(query,(Node_Variable)t.getObject(),t.getPredicate(),t.getSubject()) )
					done[doneCnt++] = t;
			}
			for(doneCnt--;doneCnt>0;doneCnt--) constraints.delete(done[doneCnt]);
		}
		
		private static boolean genConstraints( DBQuery query, Node_Variable var, Node pred, Node obj ) {
			boolean res = false;
			// not sure what to check for here
			return res;
		}
		

	
			

	/**
		compile the final form of the query statement.
	*/
	private static void compileQuery( DBQueryStageCompiler compiler, DBQuery query )
		{
			// create result list
			int resCnt = query.binding.length - query.argCnt;
			query.resList = new int[resCnt];
			query.stmt = query.driver.genSQLSelectStmt(
				query.driver.genSQLResList(query.resList,query.binding),
				query.driver.genSQLFromList(query.aliasCnt,query.table),
				query.stmt);
		}
        
}

/*
 * (c) Copyright 2003 Hewlett-Packard Development Company, LP
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
