/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            4 Mar 2003
 * Filename           $RCSfile: MultiUnion.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-04-08 22:12:01 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.graph.compose;


// Imports
///////////////
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.RDFException;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;


/**
 * <p>
 * A graph implementation that presents the union of zero or more subgraphs,
 * one of which is distinguished as the updateable graph.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: MultiUnion.java,v 1.3 2003-04-08 22:12:01 ian_dickinson Exp $
 */
public class MultiUnion
    extends Polyadic
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////


    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a union of exactly no sub graphs.
     * </p>
     */
    public MultiUnion() {
        super();
    }

    
    /**
     * <p>
     * Construct a union of all of the given graphs
     * </p>
     * 
     * @param graphs An array of the sub-graphs of this union
     */
    public MultiUnion( Graph[] graphs) {
        super( graphs );
    }

    
    /**
     * <p>
     * Construct a union of all of the given graphs.
     * </p>
     * 
     * @param graphs An iterator of the sub-graphs of this union. If graphs is
     *               a closable iterator, it will be automatically closed.
     */
    public MultiUnion( Iterator graphs ) {
        super( graphs );
    }
    

    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Add the given triple to the union model; the actual component model to
     * be updated will be the designated (or default) {@linkplain #getBaseGraph updateable} graph. 
     * </p>
     * 
     * @param t A triple to add to the union graph
     * @exception RDFException if the union does not contain any sub-graphs yet
     */
    public void add( Triple t ) {
        try {
            getBaseGraph().add( t );
        }
        catch (NullPointerException e) {
            // TODO: this should be changed to JenaException when it gets done
            throw new RDFException( "Tried to add to a union graph that has no component graphs." );
        }
    }


    /**
     * <p>
     * Delete the given triple from the union model; the actual component model to
     * be updated will be the designated (or default) {@linkplain #getBaseGraph updateable} graph. 
     * </p>
     * 
     * @param t A triple to from the union graph
     * @exception RDFException if the union does not contain any sub-graphs yet
     */
    public void delete( Triple t ) {
        try {
            getBaseGraph().delete( t );
        }
        catch (NullPointerException e) {
            // TODO: this should be changed to JenaException when it gets done
            throw new RDFException( "Tried to delete from a union graph that has no component graphs." );
        }
    }


    /**
     * <p>
     * Answer true if at least one of the graphs in this union contain the given triple.
     * </p>
     * 
     * @param t A triple
     * @return True if any of the graphs in the union contain t
     */
    public boolean contains( Triple t ) {
        for (Iterator i = m_subGraphs.iterator();  i.hasNext();  ) {
            if (((Graph) i.next()).contains( t )) {
                return true;
            }
        }
        
        return false;
    }
    

    /**
     * <p>
     * Answer an iterator over the triples in the union of the graphs in this composition. <b>Note</b>
     * that the requirement to remove duplicates from the union means that this will be an
     * expensive operation for large (and especially for persistent) graphs.
     * </p>
     * 
     * @param t The matcher to match against
     * @return An iterator of all triples matching t in the union of the graphs.
     */
    public ExtendedIterator find( final TripleMatch t ) {
        // anything in this model?
        if (m_subGraphs.size() == 0) {
            // the default NiceIterator has no elements
            return new NiceIterator();
        }
        else {
            // start building the iterator chain
            Set seen = new HashSet();
            ExtendedIterator i = null;
            
            // now add the rest of the chain
            for (Iterator graphs = m_subGraphs.iterator(); graphs.hasNext(); ) {
                ExtendedIterator newTriples = recording( rejecting( ((Graph) graphs.next()).find( t ), seen ), seen );
                 
                i = (i == null) ? newTriples : i.andThen( newTriples );
            }
            
            return i;
        }
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

