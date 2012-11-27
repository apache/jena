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

import java.util.List ;

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

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
    
    public static void outputPlain(IndentedWriter out, Triple triple, SerializationContext naming)
    {
        // No tag
        output(out, triple.getSubject(), naming) ;
        out.print(" ") ;
        output(out, triple.getPredicate(), naming) ;
        out.print(" ") ;
        output(out, triple.getObject(), naming) ;
    }
    
    public static void output(IndentedWriter out, Quad qp, SerializationContext naming)
    {
        WriterLib.startOneLine(out, Tags.tagQuad) ;
        outputPlain(out, qp, naming) ;
        WriterLib.finishOneLine(out, Tags.tagQuad) ;
    }
    
    public static void outputPlain(IndentedWriter out, Quad qp, SerializationContext naming)
    {
        output(out, qp.getGraph(), naming) ;
        out.print(" ") ;
        output(out, qp.getSubject(), naming) ;
        out.print(" ") ;
        output(out, qp.getPredicate(), naming) ;
        out.print(" ") ;
        output(out, qp.getObject(), naming) ;
    }
    
    public static void output(IndentedWriter out, Node node, SerializationContext naming)
    {
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
            out.print(FmtUtils.stringForNode(node, naming)) ;
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
