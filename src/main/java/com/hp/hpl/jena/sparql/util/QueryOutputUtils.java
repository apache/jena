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
import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory ;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry ;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.WriterSSE ;

public class QueryOutputUtils
{
    // ---- PrintSerializable
    public static String toString(PrintSerializable item, PrefixMapping pmap)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        SerializationContext sCxt = new SerializationContext(pmap) ;
        item.output(buff, sCxt) ;
        return buff.toString() ;
    }

    public static String toString(PrintSerializable item)
    { return toString(item, null) ; }
    
    // ModQueryOut
        
    public static void printPlan(Query query, QueryExecution qe)
    {
        QueryEngineFactory f = QueryEngineRegistry.findFactory(query, qe.getDataset().asDatasetGraph(), ARQ.getContext()) ;
        if ( f == null )
            System.err.println("printPlan: Unknown engine type: "+Utils.className(qe)) ;
        
        Plan plan = f.create(query, qe.getDataset().asDatasetGraph(), BindingRoot.create(), ARQ.getContext()) ;
        SerializationContext sCxt = new SerializationContext(query) ;
        IndentedWriter out = IndentedWriter.stdout ;
    
        plan.output(out, sCxt) ;
        out.flush();
    }

    public static void printQuery(Query query)
    {
        IndentedWriter out = IndentedWriter.stdout ;
        printQuery(out, query) ;
    }

    public static void printQuery(IndentedWriter out, Query query)
    {
        printQuery(out, query, Syntax.defaultQuerySyntax) ;
    }

    public static void printQuery(IndentedWriter out, Query query, Syntax syntax)
    {
        query.serialize(out, syntax) ;
        out.flush() ;
    }

    public static void printOp(Query query, boolean optimize)
    {
        IndentedWriter out = IndentedWriter.stdout ;
        printOp(out, query, optimize) ; // Flush done
    }

    public static void printOp(IndentedWriter out, Query query, boolean printOptimized)
    {
        Op op = Algebra.compile(query) ;
        if ( printOptimized )
            op =  Algebra.optimize(op) ;
        WriterSSE.out(out, op, query) ;
        out.flush();
    }

    public static void printQuad(Query query, boolean printOptimized)
    {
        IndentedWriter out = IndentedWriter.stdout ;
        printQuad(out, query, printOptimized) ; // Flush done
    }

    public static void printQuad(IndentedWriter out, Query query, boolean printOptimized)
    {
        Op op = Algebra.compile(query) ;
        if ( printOptimized )
            op =  Algebra.optimize(op) ;
        op = Algebra.toQuadForm(op) ;
        WriterSSE.out(out, op, query) ;
//        SerializationContext sCxt = new SerializationContext(query) ;
//        op.output(out, sCxt) ;
        out.flush() ;
    }
}
