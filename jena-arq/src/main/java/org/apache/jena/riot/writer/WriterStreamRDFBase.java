/**
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

package org.apache.jena.riot.writer;

import java.io.BufferedWriter ;
import java.io.OutputStream ;
import java.io.Writer ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.out.NodeFormatterTTL ;
import org.apache.jena.riot.out.NodeToLabel ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;
import org.apache.jena.riot.system.RiotLib ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.util.Context;

/** Core engine for output of triples / quads that is streaming.
 *  Handles prefixes and base, together with the environment for processing.
 *  If fed quads, the output is valid TriG.
 *  If fed only triples, the output is valid Turtle.
 *  Not for N-Quads and N-triples.
 */

public abstract class WriterStreamRDFBase implements StreamRDF
{
    // What did we do last?
    protected boolean activeTripleData  = false ;
    protected boolean activeQuadData    = false ;
    protected boolean lastWasDirective  = false ;

    protected final PrefixMap pMap ;
    protected String baseURI = null ;
    protected final NodeToLabel nodeToLabel ;

    protected NodeFormatterTTL fmt ;
    protected final IndentedWriter out ;
    protected final DirectiveStyle prefixStyle;

    public WriterStreamRDFBase(OutputStream output, Context context)
    {
        this(new IndentedWriter(output), context) ;
    }

    public WriterStreamRDFBase(Writer output, Context context)
    { this(wrap(output), context) ; }


    public WriterStreamRDFBase(IndentedWriter output, Context context)
    {
        out = output ;
        baseURI = null ;
        pMap = PrefixMapFactory.create() ;
        nodeToLabel = NodeToLabel.createScopeByDocument() ;
        prefixStyle = WriterLib.directiveStyle(context);
        setFormatter() ;
    }

    private void setFormatter()
    {
        fmt = new NodeFormatterTTL(baseURI, pMap, nodeToLabel) ;
    }

    private static IndentedWriter wrap(Writer output)
    {
        if ( ! ( output instanceof BufferedWriter ) )
            output = new BufferedWriter(output, 32*1024) ;
        return RiotLib.create(output) ;
    }

    private void reset$()
    {
        activeTripleData = false ;
        activeQuadData = false ;
        lastWasDirective = false ;
    }

    @Override
    public final void start()
    {
        reset$() ;
        startData() ;
    }

    @Override
    public final void finish()
    {
        endData() ;
        out.flush() ;
    }

    @Override
    public final void triple(Triple triple)
    {
        print(triple) ;
        activeTripleData = true ;
    }

    @Override
    public final void quad(Quad quad)
    {
        print(quad) ;
        activeQuadData = true ;
    }

    @Override
    public final void base(String base)
    {
        baseURI = base ;
        lastWasDirective = true ;
        setFormatter() ;
        RiotLib.writeBase(out, base, prefixStyle==DirectiveStyle.SPARQL) ;
    }

    @Override
    public final void prefix(String prefix, String iri)
    {
        endData() ;
        lastWasDirective = true ;
        pMap.add(prefix, iri) ;
        RiotLib.writePrefix(out, prefix, iri, prefixStyle==DirectiveStyle.SPARQL);
    }

    protected void outputNode(Node n)
    {
        fmt.format(out, n) ;
    }

    // Subclass contract

    protected abstract void startData() ;

    protected abstract void endData() ;

    protected abstract void print(Quad quad) ;

    protected abstract void print(Triple triple) ;

    protected abstract void reset() ;

    protected void DEBUG(String fmt, Object...args)
    {
        int loc = out.getCol() ;            // Absolute
        int off = out.getAbsoluteIndent() ;
        out.ensureStartOfLine();
        out.setAbsoluteIndent(0) ;
        out.println(String.format(fmt, args)) ;
        out.setAbsoluteIndent(off) ;
        out.ensureStartOfLine();
        out.pad(loc, true) ;
    }
}
