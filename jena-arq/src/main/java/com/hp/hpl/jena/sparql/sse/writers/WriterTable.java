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

package com.hp.hpl.jena.sparql.sse.writers;

import java.util.Iterator ;

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

public class WriterTable
{
    public static void output(IndentedWriter out, Table table, SerializationContext sCxt)
    {
        WriterLib.start(out, Tags.tagTable, WriterLib.NoNL) ;
        WriterNode.outputVars(out, table.getVars(), sCxt) ;
        out.println() ;
        outputPlain(out, table, sCxt) ;
        WriterLib.finish(out, Tags.tagTable) ;
    }
    
    public static void outputPlain(IndentedWriter out, Table table, SerializationContext sCxt)
    {
        QueryIterator qIter = table.iterator(null) ; 
        for ( ; qIter.hasNext(); )
        {
            Binding b = qIter.nextBinding() ;
            output(out, b, sCxt) ;
            out.println() ;
        }
        qIter.close() ;
    }
    
    public static void output(IndentedWriter out, Binding binding, SerializationContext sCxt )
    {
        WriterLib.start(out, Tags.tagRow, WriterLib.NoSP) ;
        for ( Iterator<Var> iter = binding.vars() ; iter.hasNext() ; )
        {
            Var v = iter.next() ;
            Node n = binding.get(v) ;
            out.print(" ") ;
            WriterLib.start2(out) ;
            out.print(FmtUtils.stringForNode(v, sCxt)) ;
            out.print(" ") ;
            out.print(FmtUtils.stringForNode(n, sCxt)) ;
            WriterLib.finish2(out) ;
        }
        WriterLib.finish(out, Tags.tagRow) ;
    }
}
