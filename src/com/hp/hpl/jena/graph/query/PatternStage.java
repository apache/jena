/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: PatternStage.java,v 1.5 2003-08-04 13:28:27 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;

/**
    A PatternStage is a Stage that handles some bunch of related patterns; those patterns
    are encoed as Triples.
    
    @author hedgehog
*/

public class PatternStage extends Stage
    {
    protected Graph graph;
    protected Pattern [] compiled;
    
    public PatternStage( Graph graph, Mapping map, Triple [] triples )
        {
        this.graph = graph;
        this.compiled = compile( map, triples );
        }
        
    protected Pattern [] compile( Mapping map, Triple [] triples )
        { return compile( compiler, map, triples ); }
        
    protected Pattern [] compile( PatternCompiler pc, Mapping map, Triple [] source )
        {
        return PatternStageCompiler.compile( pc, map, source );
        }
        
    private static final PatternCompiler compiler = new PatternStageCompiler();
        
    protected void run( Pipe source, Pipe sink )
    	{
        while (stillOpen && source.hasNext())
            {
            Domain current = source.get();
            Domain useme = current.extend();           
            ClosableIterator it = graph.find( compiled[0].asTripleMatch( current ) );
            while (stillOpen && it.hasNext())
                {
                Triple t = (Triple) it.next();
                if (compiled[0].matches( useme, t ))
                    {
                    sink.put( compiled[0].matched( useme, t ) );
                    useme = current.extend();
                    }
                }
            }
        sink.close();
    	}

    public Pipe deliver( final Pipe result )
        {
        final Pipe stream = previous.deliver( new BufferPipe() );
		new Thread() { public void run() { PatternStage.this.run( stream, result ); } } .start();
        return result;
        }
    }

/*
    (c) Copyright Hewlett-Packard Company 2002
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
