/**
 * 
 */
package com.hp.hpl.jena.graph.query;

import java.util.HashSet;

import com.hp.hpl.jena.graph.Triple;

public class QueryTriple
{
public final QueryNode S;
public final QueryNode P;
public final QueryNode O;

public QueryTriple( QueryNode S, QueryNode P, QueryNode O )
    { this.S = S; this.P = P; this.O = O; }   

public String toString()
    { return "<qt " + S.toString() + " " + P.toString() + " " + O.toString() + ">"; }

public static QueryTriple [] classify( Mapping m, Triple [] t )
    {
    QueryTriple [] result = new QueryTriple [t.length];
    for (int i = 0; i < t.length; i += 1) result[i] = classify( m, t[i] );
    return result;
    }

public static QueryTriple classify( Mapping m, Triple t )
    { 
    HashSet fresh = new HashSet();
    return new QueryTriple
        ( QueryNode.classify( m, fresh, t.getSubject() ), 
        QueryNode.classify( m, fresh, t.getPredicate() ),
        QueryNode.classify( m, fresh, t.getObject() ) );
    }
}