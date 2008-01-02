/*
 	(c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: AmbiguousSpecificTypeException.java,v 1.2 2008-01-02 12:07:39 andy_seaborne Exp $
*/

package com.hp.hpl.jena.assembler.exceptions;

import java.util.*;

import com.hp.hpl.jena.rdf.model.Resource;

/**
    Exception to throw when an AssemblerGroup has a choice of types
    from which to try and find an implementation.
    
 	@author kers
*/
public class AmbiguousSpecificTypeException extends AssemblerException
    {
    protected final List types;
    
    public AmbiguousSpecificTypeException( Resource root, ArrayList types )
        {
        super( root, makeMessage( root, types ) );
        this.types = types;
        }

    private static String makeMessage( Resource root, List types )
        { return 
            "cannot find a most specific type for " + nice( root )
            + ", which has as possibilities:" + nice( types )
            + "."; 
        }

    private static String nice( List types )
        {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < types.size(); i += 1)
            result.append( " " ).append( nice( (Resource) types.get(i) ) );
        return result.toString();
        }

    public List getTypes()
        { return types; }
    }


/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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