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

package com.hp.hpl.jena.sparql.core ;


import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryVisitor ;

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

        @Override
        public void startVisit(Query query)
        { } 

        @Override
        public void visitResultForm(Query query)
        { }

        @Override
        public void visitPrologue(Prologue query)
        {
            x ^= Prologue.hash(query) ;
        }

        @Override
        public void visitSelectResultForm(Query query)
        { 
            //query.setResultVars() ;
            if ( ! query.isQueryResultStar() )
                x^= query.getProject().hashCode() ;
        }

        @Override
        public void visitConstructResultForm(Query query)
        {
            x ^= query.getConstructTemplate().hashCode() ;
        }

        @Override
        public void visitDescribeResultForm(Query query)
        {
            x ^= query.getResultVars().hashCode() ;
            x ^= query.getResultURIs().hashCode() ;
        }

        @Override
        public void visitAskResultForm(Query query)
        { }

        @Override
        public void visitDatasetDecl(Query query)
        {
            x ^= query.getNamedGraphURIs().hashCode() ; 
        }

        @Override
        public void visitQueryPattern(Query query)
        {
            if ( query.getQueryPattern() != null )
                x ^= query.getQueryPattern().hashCode() ;
        }

        @Override
        public void visitGroupBy(Query query)
        {
            if ( query.hasGroupBy() )
                x ^= query.getGroupBy().hashCode() ;
        }
        
        @Override
        public void visitHaving(Query query) 
        {
            if ( query.hasHaving() )
                x ^= query.getHavingExprs().hashCode() ;
        }
        
        @Override
        public void visitOrderBy(Query query)
        {
            if ( query.getOrderBy() != null )
                x ^= query.getOrderBy().hashCode() ;
        }

        @Override
        public void visitLimit(Query query)
        {
            x ^= query.getLimit() ;
        }

        @Override
        public void visitOffset(Query query)
        {
            x ^= query.getOffset() ;
        }

        @Override
        public void visitValues(Query query)
        {
            if ( query.hasValues() )
            {
                x ^= query.getValuesVariables().hashCode() ;
                x ^= query.getValuesData().hashCode() ;
            }
        }

        @Override
        public void finishVisit(Query query)
        {}

        public int  calculatedHashCode() { return x ; }
    }
}
