/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: RDFS.java,v 1.1 2003-02-21 15:45:00 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.compose;

/**
	@author kers
*/
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.mem.*;

import java.util.*;

public class RDFS extends Dyadic implements Vocabulary
    {
    private Graph derived;
    
    public RDFS( Graph base )
        {
        super( base, GraphTestBase.graphWith( "" ) );
        Union basis = new Union( L, rdfsAxioms );
        derived = basis.union( new Properties( basis ) ).union( new Resources( basis ) );
        }
        
    public static class Resources extends Dyadic
        {
        public Resources( Graph basis )
            { super( basis, new GraphMem() ); }

        public void delete( Triple t )
            { die( "Resources::delete" ); }
            
        public void add( Triple t )
            {  die( "Resources::add" ); }
            
        public Graph extractResources( Graph x )
            {
            Graph result = new GraphMem();
            ClosableIterator it = x.find( null, null, null );
            while (it.hasNext()) addResources( result, (Triple) it.next() );
            return result;
            }
            
        private void addResources( Graph result, Triple t )
            {
            addResource( result, t.getSubject() );
            addResource( result, t.getPredicate() );
            addResource( result, t.getObject() );
            }
            
        private void addResource( Graph result, Node n )
            {
            if (n.isURI()) result.add( new Triple( n, rdfType, rdfsResource ) );
            }
            
        public ExtendedIterator find( TripleMatch tm )
            {
            return extractResources( L ).find( tm ); 
            }
        }
        
    public static class Properties extends Dyadic
        {
        public Properties( Graph basis )
            { super( basis, extractProperties( basis ) ); }

        private static Graph extractProperties( Graph x )
            {
            Graph result = new GraphMem();
            Query q = new Query();
            // Variable P = q.variable( "p" );
            Node P = Node.createVariable( "?p" ); 
            ClosableIterator it = q.addMatch( Query.ANY, P, Query.ANY ).executeBindings( x, new Node [] {P} );
            while (it.hasNext())
                {
                Domain d = (Domain) it.next();
                Node n = (Node) d.get( 0 );
                result.add( new Triple( n, rdfType, rdfProperty ) );
                }
            return result;
            }
            
        public void delete( Triple t )
            { die( "Properties::delete" ); }
            
        public void add( Triple t )
            {  die( "Properties::add" ); }
            
        public ExtendedIterator find( TripleMatch tm )
            {
            return extractProperties( L ).find( tm ); 
            }
        }

    public void add( Triple t )
        {
        derived.add( t );
        }
        
    public void delete( Triple t )
        {
        throw new UnsupportedOperationException( "RDFS::delete" );
        }
        
    public ExtendedIterator find( TripleMatch tm )
        {
        return derived.find( tm );
        }
    }

/*
    (c) Copyright Hewlett-Packard Company 2002
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
