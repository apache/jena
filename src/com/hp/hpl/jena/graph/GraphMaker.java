/*
  (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: GraphMaker.java,v 1.12 2004-12-06 13:50:14 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph;

import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;

/**
    A factory for providing instances of named graphs with appropriate storage models.
    It represents a directory, or a database, or a mapping: names map to graphs for the
    lifetime of the GraphMaker. Names can be "arbitrary" character sequences.
<p>
    A GraphMaker has a reification style which is shared by all the graphs it creates.
*/

public interface GraphMaker 
{
    /**
        Answer the reification style of all the graphs that this GraphMaker constructs.
        @return the reification style given to all created graphs
    */
    public ReificationStyle getReificationStyle();
    
    /**
        Answer the default graph of this ModelMaker. The same graph is returned on
        each call. It may only be constructed on the first call of getGraph(), or at any
        previous time.
        
        @return the same default graph each time
     */
    public Graph getGraph();
    
    /**
        Answer a graph who's name isn't interesting. Each call delivers a different graph.
        The GraphMaker may reserve a bunch of names for this purpose, of the form
        "anon_<digits>", if it cannot support truly anonymous graphs.
        
        @return a fresh anonymous graph
    */
    public Graph createGraph();
    
    /**
        Create a new graph associated with the given name. If there is no such
        association, create one and return it. If one exists but <code>strict</code>
        is false, return the associated graph. Otherwise throw an AlreadyExistsException.
        
        @param name the name to give to the new graph
        @param strict true to cause existing bindings to throw an exception
        @exception AlreadyExistsException if that name is already bound.
    */
    public Graph createGraph( String name, boolean strict );
    
    /**
        Create a graph that does not already exist - equivalent to
        <br><code>createGraph( name, false )</code>.
    */
    public Graph createGraph( String name );
    
    /**
        Find an existing graph that this factory knows about under the given
        name. If such a graph exists, return it. Otherwise, if <code>strict</code>
        is false, create a new graph, associate it with the name, and return it.
        Otherwise throw a DoesNotExistException. 
        
        @param name the name of the graph to find and return
        @param strict false to create a new one if one doesn't already exist
        @exception DoesNotExistException if there's no such named graph
    */
    public Graph openGraph( String name, boolean strict );
    
    /**
        Equivalent to <code>openGraph( name, false )</code> 
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
        return true iff the factory has a graph with the given name
        
        @param name the name of the graph to look for
        @return true iff there's a graph with that name
    */
    public boolean hasGraph( String name );
    
    /**
        Answer a Graph describing this GraphMaker using the vocabulary of
        JMS.
        
        @return a Graph describing this Maker.
    */
    public Graph getDescription();
    
    public Graph getDescription( Node root );
    
    /**
        Add the description of this GraphMaker to the description graph desc, under the
        name self.
        @param desc the graph to which to add the description
        @param self the root resource to use for the description
    */
    public Graph addDescription( Graph desc, Node self );
    
    /**
        Close the factory - no more requests need be honoured, and any clean-up
        can be done.
    */
    public void close();
    
    /**
        Answer an [extended] iterator where each element is the name of a graph in
        the maker, and the complete sequence exhausts the set of names. No particular
        order is expected from the list.
     	@return an extended iterator over the names of graphs known to this Maker.
     */
    ExtendedIterator listGraphs();
}

/* ****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            06-Mar-2003
 *
 * Last modified on   $Date: 2004-12-06 13:50:14 $
 *               by   $Author: andy_seaborne $

 *****************************************************************************/

/*
    (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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
