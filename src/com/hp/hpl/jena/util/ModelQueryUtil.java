/*
  (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: ModelQueryUtil.java,v 1.6 2004-12-06 13:50:24 andy_seaborne Exp $
*/

package com.hp.hpl.jena.util;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;

/**
    A utility for using the graph query interface from a Model. Queries may be represented
    as models, where each statement in the model corresponds to a search for matching
    statements in the model being queried. Variables are represented as resources
    with URIs using the ficticious "jqv" protocol.
<p>    
    See also <code>QueryMapper</code>.
    
 	@author kers
*/
public class ModelQueryUtil
    {
    private ModelQueryUtil()
        {}
    
    public static ExtendedIterator queryBindingsWith
        ( final Model model, Model query, Resource [] variables )
        {
        Map1 mm = new Map1()
            { public Object map1( Object x ) { return mappy( model, x ); } };
        QueryMapper qm = new QueryMapper( query, variables );
        return
            qm.getQuery().executeBindings( model.getGraph(), qm.getVariables() )
            .mapWith( mm )
            ;
        }

    public static RDFNode asRDF( Model m, Node n )
        { return m.asRDFNode( n ); }
        
    public static List mappy( Model m, Object x )
        {
        List L = (List) x;
        ArrayList result = new ArrayList( L.size() );
        for (int i = 0; i < L.size(); i += 1) result.add( asRDF( m, (Node) L.get( i ) ) );
        return result;
        }

    }


/*
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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