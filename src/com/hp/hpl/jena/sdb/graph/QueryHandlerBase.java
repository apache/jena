/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.graph;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.BufferPipe;
import com.hp.hpl.jena.graph.query.ExpressionSet;
import com.hp.hpl.jena.graph.query.Mapping;
import com.hp.hpl.jena.graph.query.Pipe;
import com.hp.hpl.jena.graph.query.SimpleQueryHandler;
import com.hp.hpl.jena.graph.query.Stage;
import com.hp.hpl.jena.graph.query.TreeQueryPlan;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.shared.SDBNotImplemented;

public abstract class QueryHandlerBase extends SimpleQueryHandler
{
    /** Called just before the worker thread is created and started.
     *  This is called on the application thread
     */
    abstract protected void initialize(Mapping map, Triple[] pattern) ;
    
    /** Called on separate thread to perform the pattern matching */
    abstract protected void execute(Mapping map, Triple [] pattern, Pipe inputPipe, Pipe outputPipe) ;
    
    public QueryHandlerBase(Graph graph)
    {
        super(graph) ;
    }

    @Override
    final public TreeQueryPlan prepareTree( Graph pattern )
    {
        throw new SDBNotImplemented("prepareTree - Chris says this will not be called") ;
    }

    @Override
    final public Stage patternStage(final Mapping map, ExpressionSet constraints, final Triple [] pattern )
    {
        if ( constraints != null && constraints.iterator().hasNext() )
            throw new SDBException("Constraints not supported") ;
        
        initialize(map, pattern) ;
        
        Stage stage = new Stage() {
            @Override
            public Pipe deliver(Pipe pipe)
            {
                Pipe pipe2 = new BufferPipe() ;
                // Previous's output is pipe2
                previous.deliver(pipe2) ;
                Thread t = new QueryThread(QueryHandlerBase.this, map, pattern, pipe2, pipe) ;
                t.setDaemon(true) ;
                t.start();
                return pipe ;
            }} ;
        return stage ;
    }

    
    static class QueryThread extends Thread
    {
        private Mapping mapping ;
        private Triple[] pattern ;
        private Pipe inputPipe ;
        private Pipe outputPipe ;
        private QueryHandlerBase queryHandler ;
        
        QueryThread(QueryHandlerBase queryHandler, Mapping map,
                    Triple[] pattern, Pipe inputPipe, Pipe outputPipe)
        { 
            this.mapping = map ;
            this.pattern = pattern ;
            this.inputPipe = inputPipe ;
            this.outputPipe = outputPipe ;
            this.queryHandler = queryHandler ;
        }
            
        @Override
        public void run()
        {
            queryHandler.execute(mapping, pattern, inputPipe, outputPipe) ;
        }
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */