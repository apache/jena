package com.hp.hpl.jena.db.impl;

import java.util.List;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.Bound;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.Element;
import com.hp.hpl.jena.graph.query.Fixed;
import com.hp.hpl.jena.graph.query.Mapping;
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
    /** no state, so the constructor is boring.
    */
    public DBQueryStageCompiler()
        {}
      
    /**
        to compile an array of triples, compile each triple and form the corresponding
        array of Patterns. *preserve the order*. 
    */  
    public static DBQuery compile( DBQueryStageCompiler compiler, SpecializedGraph sg,
    		List resVar, List dbPat, Mapping varMap, boolean qryFullReif )
        {
        DBQuery query = new DBQuery(sg,resVar,varMap,qryFullReif);
        int j;
        for (int i = 0; i < dbPat.size(); i += 1) {
			compilePattern (compiler, query, (DBPattern) dbPat.get(i));
        }
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

        	if ( query.isReifier ) {
        		boolean newAlias = true;
        		int alias = query.aliasCnt;
        		if ( !(pred instanceof Fixed) )
        			throw new JenaException("Reifier predicate not bound");
        		Node p = ((Fixed)pred).asNodeMatch((Domain)null);
        		char reifProp;    		
				if ( pred.equals(RDF.Nodes.subject) ) reifProp = 'S';
				else if ( pred.equals(RDF.Nodes.predicate) ) reifProp = 'P';
				else if ( pred.equals(RDF.Nodes.object) ) reifProp = 'O';
				else if ( pred.equals(RDF.Nodes.type) ) {
					reifProp = 'T';
					// need to check here the value of object and adjust appropriately
				} 
				else throw new JenaException("Unexpected reifier predicate");
				if ( !subj.equals(Element.ANY) ) {
					// optionally do join optimization for reification.
					// if the subject is joined with another pattern and
					// that subject is already bound, skip the join.
					if ( query.queryFullReifStmt && (subj instanceof Free) && 
						query.isBound(((Free)subj).getIndex()) ) {
							alias = (query.getBinding(((Free)subj).getIndex())).alias;
							newAlias = false;
						} else 
							query.stmt = query.stmt + query.ga.gen(getQual(query,alias,'N',subj));
				}			
				query.stmt = query.stmt + query.ga.gen(getQual(query,alias,reifProp,obj));
				if ( newAlias ) query.newAlias();
        		
        	} else {
        		// query over triple table
				query.stmt = query.stmt + query.ga.gen(getQual(query,query.aliasCnt,'S',subj));
				query.stmt = query.stmt + query.ga.gen(getQual(query,query.aliasCnt,'P',pred));
				query.stmt = query.stmt + query.ga.gen(getQual(query,query.aliasCnt,'O',obj));
				query.newAlias();
        	}
        }
        
		private static String getQual( DBQuery query, int alias, char pred, Element spo) {
			String qual;
			if (spo instanceof Fixed)
				qual = query.driver.genSQLQualConst(alias, pred,
								((Fixed)spo).asNodeMatch((Domain)null));
			else if (spo instanceof Bound) {
				query.argCnt++;
				query.argType += pred;
				query.argIndex.add(new Integer(((Bound)spo).getIndex()));
				qual = query.driver.genSQLQualParam(alias, pred);
			} else if (spo instanceof Free) {
				DBQuery.Var bind = query.getBinding(((Free) spo).getIndex());
				if (bind.isBound)
					qual = query.driver.genSQLJoin(bind.alias, bind.column,
						alias, pred);
				else {
					query.bindVar(bind, alias, pred);
					qual = "";
				}
			} else
				throw new JenaException("Invalid Element in qualifier");
			return qual;
	}

        
	/**
		compile the final form of the query statement.
	*/
	private static void compileQuery( DBQueryStageCompiler compiler, DBQuery query )
		{
			query.stmt = query.driver.genSQLSelectStmt(
				query.driver.genSQLResList(query.binding),
				query.driver.genSQLFromList(query.aliasCnt,query.table),
				query.stmt);
		}
        
}