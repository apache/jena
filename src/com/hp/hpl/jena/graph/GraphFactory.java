/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            06-Mar-2003
 * Filename           $RCSfile: GraphFactory.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-05-03 11:40:31 $
 *               by   $Author: chris-dollin $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.graph;


// Imports
///////////////

/**
 * <p>
 * A factory for providing instances of graphs with appropriate storage models. 
 * It is <b>not</b> part of the contract for this factory that each instance
 * is unique; some applications may legitimately want to write triples into
 * a single graph (such as a database). 
 * </p>
 * 
 * Updated by kers; added methods that create or locate named graphs.
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: GraphFactory.java,v 1.3 2003-05-03 11:40:31 chris-dollin Exp $
 */
public interface GraphFactory 
{

    /**
     * <p>
     * Answer an instance of a graph
     * </p>
     * 
     * @return A new or existing graph.
     */
    public Graph getGraph();
    
    /**
        Create a new graph associated with the given name. If this factory
        already knows about a graph with this name, throw an AlreadyExistsException.
        Otherwise create and return the new graph.
        
        @param name the name to give to the new graph
        @exception AlreadyExistsException if that name is already bound.
    */
    public Graph createGraph( String name );
    
    /**
        Find an existing graph that this factory knows about under the given
        name. If no such graph exists, throw a DoesNotExistException.
        
        @param name the name of the graph to find and return
        @exception DoesNotExistException if there's no such named graph
    */
    public Graph openGraph( String name );
    
    /**
        Remove the association between the name and the graph. create
        will now be able to create a graph with that name, and open will no
        longer be able to find it. Throws an exception if there's no such graph.
        The graph itself is not touched.
        
        @param name the name to disassociate
        @exception DoesNotExistException if the name is unbound
    */
    public void removeGraph( String name );
    
    /**
        Close the factory - no more requests need be honoured, and any clean-up
        can be done.
    */
    public void close();
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
