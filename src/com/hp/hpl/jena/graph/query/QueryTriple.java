/*
    (c) Copyright 2005 Hewlett-Packard Development Company, LP
    All rights reserved - see end of file.
    $Id: QueryTriple.java,v 1.3 2005-07-24 21:13:04 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph.query;

import java.util.HashSet;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.shared.BrokenException;

public class QueryTriple
    {
    public final QueryNode S;
    public final QueryNode P;
    public final QueryNode O;
    
    public QueryTriple( QueryNode S, QueryNode P, QueryNode O )
        { this.S = S; this.P = P; this.O = O; }   
    
    public String toString()
        { return "<qt " + S.toString() + " " + P.toString() + " " + O.toString() + ">"; }
    
    public static QueryTriple [] classify( Mapping m, Triple [] t )
        {
        QueryTriple [] result = new QueryTriple [t.length];
        for (int i = 0; i < t.length; i += 1) result[i] = classify( m, t[i] );
        return result;
        }
    
    public static QueryTriple classify( Mapping m, Triple t )
        { 
        HashSet fresh = new HashSet();
        return new QueryTriple
            ( QueryNode.classify( m, fresh, t.getSubject() ), 
            QueryNode.classify( m, fresh, t.getPredicate() ),
            QueryNode.classify( m, fresh, t.getObject() ) );
        }
    
    public Matcher getMatcher()
        {
        final int SMATCH = 4, PMATCH = 2, OMATCH = 1, NOMATCH = 0;
        int bits = 
            (S.mustMatch() ? SMATCH : 0) 
            + (P.mustMatch() ? PMATCH : 0)
            + (O.mustMatch() ? OMATCH : 0)
            ;
        switch (bits)
            {
            case SMATCH + PMATCH + OMATCH:
                return new Matcher()
                    {
                    public boolean match( Domain d, Triple t )
                        { return S.match( d, t.getSubject() )
                            && P.match( d, t.getPredicate() )
                            && O.match( d, t.getObject() ); }
                    };
                    
            case SMATCH + OMATCH:
                return new Matcher() 
                    {
                    public boolean match( Domain d, Triple t )
                        { 
                        return S.match( d, t.getSubject() ) 
                        && O.match( d, t.getObject() ); }
                    };
                    
            case SMATCH + PMATCH:  
                return new Matcher() 
                    {
                    public boolean match( Domain d, Triple t )
                        { 
                        return S.match( d, t.getSubject() ) 
                        && P.match( d, t.getPredicate() ); 
                        }
                    };
                    
            case PMATCH + OMATCH:
                return new Matcher()
                    {
                    public boolean match( Domain d, Triple t )
                        {
                        return P.match( d, t.getPredicate() )
                        && O.match( d, t.getObject() );
                        }
                    };
    
            case SMATCH:                
                return new Matcher() 
                    {
                    public boolean match( Domain d, Triple t )
                        { return S.match( d, t.getSubject() ); }
                    };
    
            case PMATCH:
                return new Matcher()
                    {
                    public boolean match( Domain d, Triple t )
                        { return P.match( d, t.getPredicate() ); }
                    };
                    
            case OMATCH:
                return new Matcher()
                    {
                    public boolean match( Domain d, Triple t )
                        { return O.match( d, t.getObject() ); }
                    };
    
            case NOMATCH:
                return Matcher.always;
                    
            }
        throw new BrokenException( "uncatered-for case in optimisation" );
        }
    }
/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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