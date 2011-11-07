/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.db.impl;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.Element;
import com.hp.hpl.jena.graph.query.Expression;
import com.hp.hpl.jena.graph.query.ExpressionSet;
import com.hp.hpl.jena.graph.query.Fixed;
import com.hp.hpl.jena.shared.JenaException;
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
    protected static Logger logger = LoggerFactory.getLogger( DBQueryStageCompiler.class );
    
    public DBQueryStageCompiler()
        {}
      
    /**
        to compile an array of triples, compile each triple and form the corresponding
        array of Patterns. *preserve the order*. 
    */  
    public static DBQuery compile
        ( DBQueryStageCompiler compiler, DBQueryHandler qh, SpecializedGraph sg,
    		List<VarDesc> varList, List<DBPattern> dbPat, ExpressionSet constraints )
        {
        DBQuery query = new DBQuery( sg,varList,qh.queryOnlyStmt, qh.queryOnlyReif,qh.queryFullReif );
        if ( qh.getQueryOnlyReified() && !qh.getQueryFullReified() )
        	throw new JenaException("Fastpath currently requires QueryFullReified to be true if QueryOnlyReified is also true");
        if (!query.isEmpty) 
            {
        	for (int i = 0; i < dbPat.size(); i += 1) compilePattern ( compiler, query, dbPat.get(i) );
            compileConstraints( compiler, query, constraints );
			compileQuery( compiler, query );
            }
        if (logger.isDebugEnabled()) logger.debug( "generated SQL: " + query.stmt );
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
			String qual = "";
			int alias = query.aliasCnt;

			if ( query.isReifier ) {
        		boolean newAlias = true;
        		if ( !(pred instanceof Fixed) ) throw new JenaException("Reifier predicate not bound");
        		Node p = ((Fixed) pred).asNodeMatch( (Domain) null );
        		char reifProp = getReificationChar( p );
				if ( !subj.equals(Element.ANY) ) {
					// optionally do join optimization for reification.
					// if the subject is joined with another pattern and
					// that subject is already bound, skip the join.
					if ( query.qryFullReif && (subj instanceof Free) && 
						query.getBinding(((Free)subj).getListing()).isBoundToCol() ) {
							alias = (query.getBinding(((Free)subj).getListing())).alias;
							newAlias = false;
						} else 
							qual = query.sqlAnd.gen(getQual(query,alias,'N',subj));
				}			
				qual += query.sqlAnd.gen(getQual(query,alias,reifProp,obj));
				qual += query.sqlAnd.gen(query.driver.genSQLQualGraphId(alias,query.graphId));
				if ( newAlias ) query.newAlias();
        		
        	} else {
        		// query over triple table
				qual = query.sqlAnd.gen(getQual(query,alias,'S',subj));
				qual += query.sqlAnd.gen(getQual(query,alias,'P',pred));
				qual += query.sqlAnd.gen(getQual(query,alias,'O',obj));
				qual += query.sqlAnd.gen(query.driver.genSQLQualGraphId(alias,query.graphId));
				query.newAlias();
        	}
		query.stmt += qual;
        }

    /**
     	Answer the character S, P, O, or T which describes this predicate node.
        Throw an exception if it's not one of rdf: subject/predicate/object/type. 
        @param p the predicate node to be tested
     	@return the (manky) character code
    */
    private static char getReificationChar( Node p )
        {
        if (p.equals( RDF.Nodes.subject) ) return 'S';
        else if ( p.equals( RDF.Nodes.predicate ) ) return 'P';
        else if ( p.equals( RDF.Nodes.object ) ) return 'O';
        else if ( p.equals( RDF.Nodes.type ) ) return 'T';
        else throw new JenaException( "Unexpected reifier predicate: " + p );
        }
        
	private static String getQual(DBQuery query,int alias,char pred, Element spo) {
		String qual = "";
		if (spo instanceof Fixed) {
			Node obj = ((Fixed) spo).asNodeMatch((Domain) null);
			if ( query.isReifier )
				qual = query.driver.genSQLReifQualConst(alias,pred,obj);
			else
				qual = query.driver.genSQLQualConst(alias,pred,obj);
		} else if (spo instanceof Free){
			Free v = (Free) spo;
			VarDesc bind = query.getBinding(v.getListing());
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
	private static void compileConstraints ( DBQueryStageCompiler compiler, DBQuery query, ExpressionSet constraints ) 
	{
	    Iterator<Expression> it = constraints.iterator();
	    while (it.hasNext()) 
	    {
	        Expression e = it.next();
	        VarDesc bind = query.findBinding( e.getArg(0).getName() );
	        if ( bind == null ) throw new JenaException( "Unbound variable in constraint" );
	        String strMat = ((Expression.Fixed) e.getArg(1)).toString();
	        query.stmt += query.sqlAnd.gen
	        ( query.driver.genSQLStringMatch( bind.alias, bind.column, e.getFun(), strMat ) );
	    }		
	}
			
	/**
		compile the final form of the query statement.
	*/
	private static void compileQuery( DBQueryStageCompiler compiler, DBQuery query )
		{
			// create result list
			int resCnt = query.vars.length - query.argCnt;
			query.resList = new int[resCnt];
			query.stmt = query.driver.genSQLSelectStmt(
				query.driver.genSQLResList(query.resList,query.vars),
				query.driver.genSQLFromList(query.aliasCnt,query.table),
				query.stmt);
		}
        
}
