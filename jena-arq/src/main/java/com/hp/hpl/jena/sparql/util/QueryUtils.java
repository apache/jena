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

package com.hp.hpl.jena.sparql.util;

import org.apache.jena.atlas.io.IndentedLineBuffer ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryException ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.QueryCheckException ;
import com.hp.hpl.jena.sparql.lang.SPARQLParserRegistry ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.SSEParseException ;
import com.hp.hpl.jena.sparql.sse.WriterSSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuildException ;

public class QueryUtils
{
    public static void checkQuery(Query query, boolean optimizeAlgebra)
    {
        checkParse(query) ;
        checkOp(query, optimizeAlgebra) ;
    }

    public static void checkOp(Query query, boolean optimizeAlgebra)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        Op op = Algebra.compile(query) ;
        if ( optimizeAlgebra )
            op = Algebra.optimize(op) ;
        WriterSSE.out(buff, op, query) ;
        String str = buff.toString() ;
        
        try {
            Op op2 = SSE.parseOp(str) ;
            if ( op.hashCode() != op2.hashCode() )
            {
                op.hashCode() ;
                op2.hashCode() ;
                dump(op, op2) ;
                throw new QueryCheckException("reparsed algebra expression hashCode does not equal algebra from query") ;
            }
            if ( ! op.equals(op2) )
            {
                dump(op, op2) ;
                throw new QueryCheckException("reparsed algebra expression does not equal query algebra") ;
            }
        } catch (SSEParseException | BuildException ex)
        { 
            System.err.println(str);
            throw ex ; 
        }      // Breakpoint

    }
    
    private static void dump(Op op, Op op2)
    {
        System.err.println("***********") ;
        System.err.println(op) ;
        System.err.println(op2) ;
        System.err.println("***********") ;   
    }
    
    public static void checkParse(Query query)
    {
        if ( ! SPARQLParserRegistry.get().containsFactory(query.getSyntax()) )
            return ;
        
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        query.serialize(buff, query.getSyntax()) ;
        
        String tmp = buff.toString() ;
        
        Query query2 = null ;
        try {
            String baseURI = null ;
            if ( ! query.explicitlySetBaseURI() )
                // Not in query - use the same one (e.g. file read from) .  
                baseURI = query.getBaseURI() ;
            
            query2 = QueryFactory.create(tmp, baseURI, query.getSyntax()) ;
            
            if ( query2 == null )
                return ;
        } catch (UnsupportedOperationException ex)
        {
            // No parser after all.
            return ;
        }
        catch (QueryException ex)
        {
            System.err.println(tmp) ;
            throw new QueryCheckException("could not parse output query", ex) ;
        }
        
        if ( query.hashCode() != query2.hashCode() )
            throw new QueryCheckException("reparsed query hashCode does not equal parsed input query \nQuery (hashCode: " + query.hashCode() + ")=\n" + query + "\n\nQuery2 (hashCode: " + query2.hashCode() + ")=\n" + query2) ;
        
        if ( ! query.equals(query2) ) 
            throw new QueryCheckException("reparsed output does not equal parsed input") ;
    }
}
