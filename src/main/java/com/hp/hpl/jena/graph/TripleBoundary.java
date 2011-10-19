/*
      (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP, all rights reserved.
      [See end of file]
      $Id: TripleBoundary.java,v 1.1 2009-06-29 08:55:45 castagna Exp $
*/

package com.hp.hpl.jena.graph;

/**
     An interface for expressing a stopping condition on triples, such as in 
     sub-graph extraction.
      
 	@author hedgehog
*/
public interface TripleBoundary
    {
    /**
         Answer true if this triple is a stopping triple, and whatever search is using
         this interface should proceed no further.
    */
    boolean stopAt( Triple t );
    
    /**
         A TripleBoundary without limits - stopAt always returns false.
    */
    public static final TripleBoundary stopNowhere = new TripleBoundary()
        { @Override
        public boolean stopAt( Triple t ) { return false; } };
    
    /**
        A TripleBoundary that stops at triples with anonymous objects.
    */
    public static final TripleBoundary stopAtAnonObject = new TripleBoundary()
        { @Override
        public boolean stopAt( Triple t ) { return t.getObject().isBlank(); } };

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