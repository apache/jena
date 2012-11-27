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

package com.hp.hpl.jena.sparql.sse;

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.writers.WriterExpr ;
import com.hp.hpl.jena.sparql.sse.writers.WriterGraph ;
import com.hp.hpl.jena.sparql.sse.writers.WriterNode ;
import com.hp.hpl.jena.sparql.sse.writers.WriterOp ;

public class WriterSSE
{
    // No need for SerializationContext forms because these are the external intefraces
    // PrintStream [, Base] [, PrefixMap]
    
    public static void out(IndentedWriter out, Node node, Prologue prologue)
    { WriterNode.output(out, node, sCxt(prologue)) ; }
    
    public static void out(IndentedWriter out, Triple triple, Prologue prologue)
    { WriterNode.output(out, triple, sCxt(prologue)) ; }

    public static void out(IndentedWriter out, Expr expr, Prologue prologue)
    { WriterExpr.output(out, expr, sCxt(prologue)) ; }

    public static void out(IndentedWriter out, Op op, Prologue prologue)
    { WriterOp.output(out, op, sCxt(prologue)) ; }
    
    public static void out(IndentedWriter out, Graph g, Prologue prologue)
    { WriterGraph.output(out, g, sCxt(prologue)) ; }
    
    public static void out(IndentedWriter out, DatasetGraph dsg, Prologue prologue)
    { WriterGraph.output(out, dsg, sCxt(prologue)) ; }

    private static SerializationContext sCxt(Prologue prologue)
    { 
//        return new SerializationContext(prologue) ;
        // Pragmatic.
        if ( false && prologue.explicitlySetBaseURI() )
            return new SerializationContext(prologue) ;
        else
            return new SerializationContext(prologue.getPrefixMapping()) ;
    }
    
}
