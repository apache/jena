/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: BadObjectException.java,v 1.2 2006-01-13 08:37:28 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler;

import com.hp.hpl.jena.assembler.exceptions.AssemblerException;
import com.hp.hpl.jena.rdf.model.*;

/**
    Exception used to report that the object of a statement is not a Resource.
    The subject of the exception is treated as the root object. The nature of the
    unsuitability is (currently) not described.
    
    @author kers
*/
public class BadObjectException extends AssemblerException
    {
    protected final RDFNode object;
    
    public BadObjectException( Statement s )
        { 
        super( s.getSubject(), makeMessage( s ) ); 
        this.object = s.getObject();
        }

    private static String makeMessage( Statement s )
        { 
        RDFNode subject = s.getObject();
        return 
            "the " + typeOf( subject ) + " " + nice( subject ) 
            + " is unsuitable as the object of a " + nice( s.getPredicate() ) 
            + " statement";
        }

    private static String typeOf( RDFNode x )
        { return x.isAnon() ? "bnode" : x.isLiteral() ? "literal" : "resource"; }

    public RDFNode getObject()
        { return object; }
    }


/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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