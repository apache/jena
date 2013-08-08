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

package com.hp.hpl.jena.sparql.algebra.table ;

import java.util.Iterator ;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

public class TableWriter {
    public static String asSSE(Table table) {
        IndentedLineBuffer out = new IndentedLineBuffer() ;
        TableWriter.output(table, out) ;
        return out.asString() ;
    }

    public static void output(Table table, IndentedWriter out) {
        output(table, out, null) ;
    }

    public static void output(Table table, IndentedWriter out, SerializationContext sCxt) {
        if ( sCxt != null ) {} // Prefix. But then qnames are wrong.
        out.print("(table") ;
        out.incIndent() ;
        QueryIterator qIter = table.iterator(null) ;
        for (; qIter.hasNext();) {
            out.println() ;
            Binding binding = qIter.nextBinding() ;
            output(binding, out, sCxt) ;
        }
        out.decIndent() ;

        out.print(")") ;
    }

    private static void output(Binding binding, IndentedWriter out, SerializationContext sCxt) {
        out.print("(row") ;
        for (Iterator<Var> iter = binding.vars(); iter.hasNext();) {
            Var v = iter.next() ;
            Node n = binding.get(v) ;
            out.print(" ") ;
            out.print(Plan.startMarker2) ;
            out.print(FmtUtils.stringForNode(v)) ;
            out.print(" ") ;
            out.print(FmtUtils.stringForNode(n)) ;
            out.print(Plan.finishMarker2) ;
        }
        out.print(")") ;
    }
}
