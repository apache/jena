/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: RDFVisitor.java,v 1.2 2003-08-27 13:05:52 andy_seaborne Exp $
*/

package com.hp.hpl.jena.rdf.model;

/**
 	@author kers
    The interface for visiting (ie type-dispatching) an RDF Node.
*/
public interface RDFVisitor
    {
    /**
        Method to call when visiting a blank node r with identifier id.
        @param r the blank RDF node being visited
        @param id the identifier of that node
        @return value to be returned from the visit
    */
    Object visitBlank( Resource r, AnonId id );
    
    /**
        Method to call when visiting a URI node r with the given uri.
        @param r the URI node being visited
        @param uri the URI string of that node
        @return value to be returned from the visit
    */
    Object visitURI( Resource r, String uri );
    
    /**
        Method to call when visiting a literal RDF node l.
        @param l the RDF Literal node
        @return a value to be returned from the visit
    */
    Object visitLiteral( Literal l );
    }


/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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