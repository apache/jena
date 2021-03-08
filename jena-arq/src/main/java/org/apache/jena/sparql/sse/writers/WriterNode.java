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

package org.apache.jena.sparql.sse.writers;

import java.util.List ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.serializer.SerializationContext ;
import org.apache.jena.sparql.sse.Tags ;
import org.apache.jena.sparql.util.FmtUtils ;

public class WriterNode
{
    private static final int NL = WriterLib.NL ;
    private static final int NoNL = WriterLib.NoNL ;
    private static final int NoSP = WriterLib.NoSP ;
    
    public static void output(IndentedWriter out, Triple triple, SerializationContext naming)
    {
        WriterLib.startOneLine(out, Tags.tagTriple) ;
        outputPlain(out, triple, naming) ;
        WriterLib.finishOneLine(out, Tags.tagTriple) ;
    }
    
    public static void outputNoTag(IndentedWriter out, Triple triple, SerializationContext naming)
    {
        // No tag, with ()
        out.print("(") ;
        outputPlain(out, triple, naming) ;
        out.print(")") ;
    }
    
    public static void outputPlain(IndentedWriter out, Triple triple, SerializationContext naming)
    {
        // No tag, no ()
        output(out, triple.getSubject(), naming) ;
        out.print(" ") ;
        output(out, triple.getPredicate(), naming) ;
        out.print(" ") ;
        output(out, triple.getObject(), naming) ;
    }

    public static void output(IndentedWriter out, Quad quad, SerializationContext naming)
    {
        WriterLib.startOneLine(out, Tags.tagQuad) ;
        outputPlain(out, quad, naming) ;
        WriterLib.finishOneLine(out, Tags.tagQuad) ;
    }
    
    public static void outputNoTag(IndentedWriter out, Quad quad, SerializationContext naming)
    {
        // No tag, with ()
        out.print("(") ;
        outputPlain(out, quad, naming) ;
        out.print(")") ;
    }
    
    public static void outputPlain(IndentedWriter out, Quad quad, SerializationContext naming)
    {
        output(out, quad.getGraph(), naming) ;
        out.print(" ") ;
        output(out, quad.getSubject(), naming) ;
        out.print(" ") ;
        output(out, quad.getPredicate(), naming) ;
        out.print(" ") ;
        output(out, quad.getObject(), naming) ;
    }
    
    public static void output(IndentedWriter out, Node node, SerializationContext naming)
    {
        if ( node.isNodeTriple() ) {
            Triple t = node.getTriple();
            out.print("<< ");
            output(out, t.getSubject(), naming);
            out.print(" ");
            output(out, t.getPredicate(), naming);
            out.print(" ");
            output(out, t.getObject(), naming);
            out.print(" >>");
        } else
            out.print(FmtUtils.stringForNode(node, naming)) ;
    }
    
    public static void output(IndentedWriter out, List<Node> nodeList, SerializationContext naming)
    {
        out.print("(") ;
        boolean first = true ;
        for ( Node node : nodeList )
        {
            if ( ! first )
                out.print(" ") ;
            output(out, node, naming);
            first = false ;
        }
        out.print(")") ;
    }

    public static void outputVars(IndentedWriter out, List<Var> vars, SerializationContext sContext)
    {
        WriterLib.start(out, Tags.tagVars, WriterLib.NoSP) ;
        for ( Var v : vars )
        {
            out.print(" ?") ;
            out.print(v.getVarName()) ;
        }
        WriterLib.finish(out, Tags.tagVars) ;
    }

}
