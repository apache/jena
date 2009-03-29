/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.main;

import com.hp.hpl.jena.db.impl.DBQueryHandler;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.mem.GraphMemBaseQueryHandler;

import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterBlockTriples;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterBlockTriplesQH;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.Symbol;
import com.hp.hpl.jena.sparql.util.Utils;

/** StageGenerator for plain basic graph patterns.  Chooses the best way
 * to solve the pattern based on whether it is a databse or a memory graph. 
 */

// NO LONGER USED.
class StageGenBasicPattern implements StageGenerator
{
    public static Symbol altMatcher = ARQConstants.allocSymbol("altmatcher") ;
    
    private StageGenBasicPattern() {}
    
    public QueryIterator execute(BasicPattern pattern, 
                                 QueryIterator input,
                                 ExecutionContext execCxt)
    {
        if ( input == null )
                ALog.fatal(this, "Null input to "+Utils.classShortName(this.getClass())) ;
        QueryIterator cIter = createMatcher(input, pattern, execCxt) ;
        return cIter ;
    }
    
    // Decision process for which matcher to use.
    // The built in one (in-memory cases)
    // or the graphs (for databases)
    private static QueryIterator createMatcher(QueryIterator input,
                                               BasicPattern pattern , 
                                               ExecutionContext cxt)
    {
        QueryHandler qh = cxt.getActiveGraph().queryHandler() ;
        
        // Always use the pass-through triple matcher for databases
        if ( qh instanceof DBQueryHandler )
            return QueryIterBlockTriplesQH.create(input, pattern, cxt) ;
        
        // If in-memory and allowing alt matching ...
        // Was SimpleQueryHandler.
        if ( qh instanceof GraphMemBaseQueryHandler &&
             cxt.getContext().isTrueOrUndef(altMatcher) )
        {
            // The alt matcher avoids thread creation - makes a difference when called very heavily.
            return QueryIterBlockTriples.create(input, pattern, cxt) ;
        }
        
        // When in doubt ... use the general pass-through to graph query handler matcher.
        return QueryIterBlockTriplesQH.create(input, pattern, cxt) ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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