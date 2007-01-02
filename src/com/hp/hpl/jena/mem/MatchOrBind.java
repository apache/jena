/*
    (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
    All rights reserved - see end of file.
    $Id: MatchOrBind.java,v 1.3 2007-01-02 11:52:20 andy_seaborne Exp $
*/
package com.hp.hpl.jena.mem;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.QueryNode;
import com.hp.hpl.jena.mem.faster.ProcessedTriple;

public abstract class MatchOrBind
    {
    public static MatchOrBind createSP( final ProcessedTriple Q )
        {
        return new MatchOrBind()
            {
            protected Domain d;
            protected final QueryNode S = Q.S;
            protected final QueryNode P = Q.P;
            
            public MatchOrBind reset( Domain d )
                { this.d = d; return this; }
            
            public boolean matches( Triple t )
                {
                return 
                    S.matchOrBind( d, t.getSubject() )
                    && P.matchOrBind( d, t.getPredicate() )
                    ;
                }
            };
        }
    
    public static MatchOrBind createPO( final ProcessedTriple Q )
        {
        return new MatchOrBind()
            {
            protected Domain d;
            protected final QueryNode P = Q.P;
            protected final QueryNode O = Q.O;
            
            public MatchOrBind reset( Domain d )
                { this.d = d; return this; }
            
            public boolean matches( Triple t )
                {
                return 
                    P.matchOrBind( d, t.getPredicate() )
                    && O.matchOrBind( d, t.getObject() )
                    ;
                }
            };
        }   
    public abstract boolean matches( Triple t );
    
    public abstract MatchOrBind reset( Domain d );
    }

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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