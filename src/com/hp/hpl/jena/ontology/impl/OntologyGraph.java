/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            04-Apr-2003
 * Filename           $RCSfile: OntologyGraph.java,v $
 * Revision           $Revision: 1.6 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-04-22 13:56:24 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;



// Imports
///////////////
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.transitiveReasoner.TransitiveReasonerFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;



/**
 * <p>
 * Graph wrapper that allows inferencing to be turned on across the whole ontology model graph.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntologyGraph.java,v 1.6 2003-04-22 13:56:24 ian_dickinson Exp $
 */
public class OntologyGraph
    extends GraphBase 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    protected MultiUnion m_unionGraph;
    
    protected InfGraph m_inf;
    protected Reasoner m_reasoner;
    
    
    // Constructors
    //////////////////////////////////

    public OntologyGraph() {
        m_unionGraph = new MultiUnion();
        m_reasoner = TransitiveReasonerFactory.theInstance().create( null );
    }
    
    
    // External signature methods
    //////////////////////////////////

    /** Delegated to the union component */
    public boolean dependsOn( Graph other ) {
        return m_unionGraph.dependsOn( other );
    }
    
    /** Delegated to the inf graph, or the union graoh if null */
    public QueryHandler queryHandler() {
        return (m_inf != null) ? m_inf.queryHandler() : m_unionGraph.queryHandler();
    }
    
    /** Delegated to the union component */
    public Reifier getReifier() {
        return m_unionGraph.getReifier();
    }
    
    /** adds the triple t (if possible) to the set belong to the graph */
    public void add(Triple t) 
        throws UnsupportedOperationException, VirtualTripleException
    {
        m_unionGraph.add( t );
    }
    
    /** removes the triple t (if possible) from the set belonging to this graph */   
    public void delete(Triple t) 
        throws UnsupportedOperationException, NoSuchTripleException, VirtualTripleException
    {
        m_unionGraph.delete( t );
    }
      
    public ExtendedIterator find(TripleMatch m) {
        return (m_inf != null) ? m_inf.find( m ) : m_unionGraph.find( m );
    }
    
    public ExtendedIterator find(Node s,Node p,Node o) {
        return (m_inf != null) ? m_inf.find( s, p, o ) : m_unionGraph.find( s, p, o );
    }
    
    public boolean isIsomorphicWith(Graph g) {
        return (m_inf != null) ? m_inf.isIsomorphicWith( g ) : m_unionGraph.isIsomorphicWith( g );
    }
    
    public boolean contains( Node s, Node p, Node o ) {
        return (m_inf != null) ? m_inf.contains( s, p, o ) : m_unionGraph.contains( s, p, o );
    }
    
    public boolean contains( Triple t ) {
        return (m_inf != null) ? m_inf.contains( t ) : m_unionGraph.contains( t );
    }
    
    public void close() {
        m_unionGraph.close();
    }

    public int size() 
        throws UnsupportedOperationException
    {
         // TODO: should delegate to inf graph return (m_inf != null) ? m_inf.size() : m_unionGraph.size();
         return m_unionGraph.size();
    }
     
    public int capabilities() {
        return (m_inf != null) ? m_inf.capabilities() : m_unionGraph.capabilities();
    }

    public MultiUnion getUnion() {
        return m_unionGraph;
    }
    
    public InfGraph getInf() {
        return m_inf;
    }

    /**
     * This must be called once the graphs have been loaded.
     */
    public void bind() {    
        // wrap an transitive reasoner around the union graph
        m_inf = m_reasoner.bind( m_unionGraph );
    }
    
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
    (c) Copyright Hewlett-Packard Company 2002-2003
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
