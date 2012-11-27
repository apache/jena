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

package com.hp.hpl.jena.sdb.util;

import com.hp.hpl.jena.shared.PrefixMapping;

import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpPrefixesUsed;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.sparql.algebra.op.OpExt;
import org.apache.jena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.query.Query;

import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.compiler.OpSQL;
import com.hp.hpl.jena.sdb.engine.QueryEngineSDB;



/** Print utilities */

public class PrintSDB
{
    public static String divider = "----------------" ;
    
    public static void print(Store store, Query query, QueryEngineSDB queryEngine)
    {
        if ( queryEngine == null )
            queryEngine = new QueryEngineSDB(store, query) ;
        Op op = queryEngine.getPlan().getOp() ;
        System.out.println(op.toString(query.getPrefixMapping())) ;
    }

    public static void print(Op op)
    { print(op, null) ; }

    
    public static void print(Op op, PrefixMapping pmap)
    {
        if ( pmap == null )
            pmap = OpPrefixesUsed.used(op, ARQConstants.getGlobalPrefixMap()) ;
        System.out.print(op.toString(pmap)) ;
    }
    
    public static void printSQL(Op op)
    {
        OpWalker.walk(op, new PrintSQL()) ;
    }
    
    public static void printSqlNodes(Op op)
    {
        OpWalker.walk(op, new PrintSqlNodes()) ;
    }

    static class PrintSqlNodes extends OpVisitorBase
    {
        boolean first = true ;
        private IndentedWriter out ;
        PrintSqlNodes(IndentedWriter out)
        { this.out = out ; }

        PrintSqlNodes()
        { this.out = new IndentedWriter(System.out) ; }
        
        @Override
        public void visit(OpExt op)
        {
            if ( ! ( op instanceof OpSQL ) )
            {
                super.visit(op) ;
                return ;
            }
            OpSQL opSQL = (OpSQL)op ;
            if ( ! first )
                out.println(divider) ;
            opSQL.output(out) ;
            out.ensureStartOfLine() ;
            out.flush();
            first = false ;
        }
    }

    
    static class PrintSQL extends OpVisitorBase
    {
        boolean first = true ;
        private IndentedWriter out ;
        PrintSQL(IndentedWriter out)
        { this.out = out ; }

        PrintSQL()
        { this.out = new IndentedWriter(System.out) ; }
        
        @Override
        public void visit(OpExt op)
        {
            if ( ! ( op instanceof OpSQL ) )
            {
                super.visit(op) ;
                return ;
            }
            OpSQL opSQL = (OpSQL)op ;
            
            
            if ( ! first )
                out.println(divider) ;
            out.print(opSQL.toSQL()) ;
            out.ensureStartOfLine() ;
            out.flush();

            first = false ;
        }
    }
    
}
