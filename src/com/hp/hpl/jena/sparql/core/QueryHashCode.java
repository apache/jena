/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core ;


import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryVisitor;

//Calculate hashcode (inline with QueryCompare) 

public class QueryHashCode
{
    int x = 0 ; 

    public static int calc(Query query)
    {
        QueryHashCodeWorker visitor = new QueryHashCodeWorker() ;
        query.visit(visitor) ;
        return visitor.calculatedHashCode() ;
    }

    private static class  QueryHashCodeWorker implements QueryVisitor
    {
        int x = 0 ; 
        public QueryHashCodeWorker()
        {}

        public void startVisit(Query query)
        { } 

        public void visitResultForm(Query query)
        { }

        public void visitPrologue(Prologue query)
        {
            if ( query.explicitlySetBaseURI() )
                x ^= query.getBaseURI().hashCode() ;
            x ^= query.getPrefixMapping().getNsPrefixMap().hashCode() ;
        }

        public void visitSelectResultForm(Query query)
        { 
            //query.setResultVars() ;
            if ( ! query.isQueryResultStar() )
                x^= query.getProject().hashCode() ;
        }

        public void visitConstructResultForm(Query query)
        {
            x ^= query.getConstructTemplate().hashCode() ;
        }

        public void visitDescribeResultForm(Query query)
        {
            x ^= query.getResultVars().hashCode() ;
            x ^= query.getResultURIs().hashCode() ;
        }

        public void visitAskResultForm(Query query)
        { }

        public void visitDatasetDecl(Query query)
        {
            x ^= query.getNamedGraphURIs().hashCode() ; 
        }

        public void visitQueryPattern(Query query)
        {
            if ( query.getQueryPattern() != null )
                x ^= query.getQueryPattern().hashCode() ;
        }

        public void visitGroupBy(Query query)
        {
            if ( query.hasGroupBy() )
                x ^= query.getGroupBy().hashCode() ;
        }
        
        public void visitHaving(Query query) 
        {
            if ( query.hasHaving() )
                x ^= query.getHavingExprs().hashCode() ;
        }
        
        public void visitOrderBy(Query query)
        {
            if ( query.getOrderBy() != null )
                x ^= query.getOrderBy().hashCode() ;
        }

        public void visitLimit(Query query)
        {
            x ^= query.getLimit() ;
        }

        public void visitOffset(Query query)
        {
            x ^= query.getOffset() ;
        }

        public void finishVisit(Query query)
        {}

        public int  calculatedHashCode() { return x ; }
    }
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