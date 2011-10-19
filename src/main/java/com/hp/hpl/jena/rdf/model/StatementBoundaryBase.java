/*
  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: StatementBoundaryBase.java,v 1.1 2009-06-29 08:55:38 castagna Exp $
*/
package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleBoundary;

/**
    StatementBoundaryBase - a base class for StatementBoundarys, with
    built-in converstion to triples and a continueWith as well as a stopAt.
    
    @author kers
*/
public abstract class StatementBoundaryBase implements StatementBoundary
    {
    /**
         Method to over-ride to define what stops the boundary search; default
         definition is !continueWith(s). <i>exactly one</code> of these two methods
         must be defined.
    */
    @Override
    public boolean stopAt( Statement s ) 
        { return !continueWith( s ); }

    /**
         Method to over-ride to define what continues the boundary search; default
         definition is !stopAt(s). <i>exactly one</code> of these two methods
         must be defined.
    */
    public boolean continueWith( Statement s ) 
        { return !stopAt( s ); }
    
    /**
         Expresses this StatementBoundary as a TripleBoundary.
    */
    @Override
    public final TripleBoundary asTripleBoundary( Model m ) 
        { return convert( m, this ); }

    /**
         Answer a TripleBoundary that is implemented in terms of a StatementBoundary. 
    */
    public static TripleBoundary convert( final Model s, final StatementBoundary b )
        {
        return new TripleBoundary()
            { @Override
            public boolean stopAt( Triple t ) { return b.stopAt( s.asStatement( t ) ); } };
        }
    }

/*
    (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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