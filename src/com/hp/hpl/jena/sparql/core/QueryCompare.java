/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core ;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryVisitor;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;
import com.hp.hpl.jena.sparql.util.Utils;

// Two queries comparison 

public class QueryCompare implements QueryVisitor
{
    private Query   query2 ;
    private boolean result = true ;
    static public boolean PrintMessages = false ;

    public static boolean equals(Query query1, Query query2)
    {
        if ( query1 == query2 ) return true ;
        
        query1.setResultVars() ;
        query2.setResultVars() ;
        QueryCompare visitor = new QueryCompare(query1) ;
        try {
            query2.visit(visitor) ;
        } catch ( ComparisonException ex)
        {
            return false ;
        }
        return visitor.isTheSame() ;
    }
    
    public QueryCompare(Query query2)
    {
        this.query2 = query2 ;
        
    }

    public void startVisit(Query query1)
    {  }  

    public void visitResultForm(Query query1)
    { check("Query result form", query1.getQueryType() == query2.getQueryType()) ; }

    public void visitPrologue(Prologue query1)
    {
        // This is after parsing so all IRIs in the query have been made absolute.
        // For two queries to be equal, their explicitly set base URIs must be the same. 
        
        String b1 = query1.explicitlySetBaseURI() ? query1.getBaseURI() : null ;
        String b2 = query2.explicitlySetBaseURI() ? query2.getBaseURI() : null ;        
        check("Base URIs", b1, b2) ;

        if ( query1.getPrefixMapping() == null &&
            query2.getPrefixMapping() == null )
            return ;
        check("Prefixes", query1.getPrefixMapping().samePrefixMappingAs(query2.getPrefixMapping())) ;
    }

    public void visitSelectResultForm(Query query1)
    { 
        check("Not both SELECT queries", query2.isSelectType()) ;
        check("DISTINCT modifier",
              query1.isDistinct() == query2.isDistinct()) ;
        check("SELECT *", query1.isQueryResultStar() == query2.isQueryResultStar()) ;
        check("Result variables",   query1.getProject(), query2.getProject() ) ;
    }

    public void visitConstructResultForm(Query query1)
    {
        check("Not both CONSTRUCT queries", query2.isConstructType()) ;
        check("CONSTRUCT templates", 
              query1.getConstructTemplate().equalIso(query2.getConstructTemplate(), new NodeIsomorphismMap()) ) ;
    }

    public void visitDescribeResultForm(Query query1)
    {
        check("Not both DESCRIBE queries", query2.isDescribeType()) ;
        check("Result variables", 
              query1.getResultVars(), query2.getResultVars() ) ;
        check("Result URIs", 
              query1.getResultURIs(), query2.getResultURIs() ) ;
        
    }

    public void visitAskResultForm(Query query1)
    {
        check("Not both ASK queries", query2.isAskType()) ;
    }

    public void visitDatasetDecl(Query query1)
    {
        boolean b1 = Utils.equalsListAsSet(query1.getGraphURIs(), query2.getGraphURIs()) ;
        check("Default graph URIs", b1 ) ;
        boolean b2 = Utils.equalsListAsSet(query1.getNamedGraphURIs(), query2.getNamedGraphURIs()) ; 
        check("Named graph URIs", b2 ) ;
    }

    public void visitQueryPattern(Query query1)
    {
        if ( query1.getQueryPattern() == null &&
             query2.getQueryPattern() == null )
            return ;
        
        if ( query1.getQueryPattern() == null ) throw new ComparisonException("Missing pattern") ;
        if ( query2.getQueryPattern() == null ) throw new ComparisonException("Missing pattern") ;
        
        // The checking for patterns (elements) involves a potential
        // remapping of system-allocated variable names.
        // Assumes blank node variables only appear in patterns.
        check("Pattern", query1.getQueryPattern().equalTo(query2.getQueryPattern(), new NodeIsomorphismMap())) ;
    }

    public void visitGroupBy(Query query1)
    {
        check("GROUP BY", query1.getGroupBy(), query2.getGroupBy()) ;
    }
    
    public void visitHaving(Query query1) 
    {
        check("HAVING", query1.getHavingExprs(), query2.getHavingExprs()) ;
    }
    
    public void visitLimit(Query query1)
    {
        check("LIMIT", query1.getLimit() == query2.getLimit() ) ;
    }

     public void visitOrderBy(Query query1)
     {
         check("ORDER BY", query1.getOrderBy(), query2.getOrderBy() ) ;
     }

     public void visitOffset(Query query1)
    {
        check("OFFSET", query1.getOffset() == query2.getOffset() ) ;
    }

    public void finishVisit(Query query1)
    {}
    
    private void check(String msg, Object obj1, Object obj2)
    {
        check(msg, Utils.equal(obj1,obj2)) ;
    }
    
    private void check(String msg, boolean b)
    {
        if ( !b )
        {
            if ( PrintMessages && msg != null )
                System.out.println("Different: "+msg) ;
            result = false ;
            throw new ComparisonException(msg) ;
        }
    }

    public boolean isTheSame() { return result ; }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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