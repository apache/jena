/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            02-Apr-2003
 * Filename           $RCSfile: AbstractProfile.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-24 15:28:05 $
 *               by   $Author: chris-dollin $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.util.*;
import com.hp.hpl.jena.ontology.*;

import java.util.*;



/**
 * <p>
 * Abstract base class to provide shared implementation for ontology language profiles. 
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: AbstractProfile.java,v 1.3 2003-06-24 15:28:05 chris-dollin Exp $
 */
public abstract class AbstractProfile
    implements Profile 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    /** Map of aliases for resources */
    protected OneToManyMap m_aliasesMap;
    
    
    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////
    
    /**
     * <p>
     * Answer true if the given resource has an alias in this profile.
     * </p>
     * 
     * @param res A resource (including properties) to test for an alias
     * @return True if there is an alias for <code>res</code>
     */
    public boolean hasAliasFor( Resource res ) {
        return aliasMap().containsKey( res );
    }
    
    /**
     * <p>
     * Answer an alias for the given resource.  If there is more than
     * one such alias, a choice is made non-deterministically between the
     * alternatives.
     * </p>
     * 
     * @param res A resource (including properties) to test for an alias
     * @return The alias for <code>res</code>, or one of the aliases for <code>res</code> if more
     * than one is defined, or null if no alias is defined for <code>res</code>.
     * 
     */
    public Resource getAliasFor( Resource res ) {
        return (Resource) aliasMap().get( res );
    }
    
    /**
     * <p>
     * Answer an iterator over the defined aliases for a resource.
     * </p>
     * 
     * @param res A resource (including properties)
     * @return An iterator over the aliases for <code>res</code>. If there are
     * no aliases, the empty iterator is returned.
     */
    public Iterator listAliasesFor( Resource res ) {
        return aliasMap().getAll( res );
    }

    /**
        Utility method: answer true iff the enhanced graph contains some triple which
        has n as subject, p.asNode() as predicate, and any object.
        
         @param an enhanced graph to search for triples
         @param n some node
         @param p a property containing a predicate node
         @return true iff the graph contains (n, p, X) for some X 
    */
    public static boolean containsSome( EnhGraph g, Node n, Property p )  { 
        return g.asGraph().contains( n, p.asNode(), Node.ANY ); 
    }

    // Internal implementation methods
    //////////////////////////////////

    /**
     * Answer a table of binary mappings denoting that one resource is the
     * alias for another (for example daml:Class and rdfs:Class).
     */
    protected abstract Resource[][] aliasTable();
    
    
    /**
     * <p>
     * Prepare the local alias map by reading the alias table from the concrete sub-class.
     * </p>
     */
    protected OneToManyMap aliasMap() {
        if (m_aliasesMap == null) {
            // aliases map not prepared yet, so initialise using the data from
            // the concrete profile class
            m_aliasesMap = new OneToManyMap();
            Resource[][] aliases = aliasTable();
            
            for (int i = 0;  i < aliases.length;  i++) {
                // since alias relationship is symmetric, we record both directions
                m_aliasesMap.put( aliases[i][0], aliases[i][1] );
                m_aliasesMap.put( aliases[i][1], aliases[i][0] );
            }
        }
        
        return m_aliasesMap;
    }


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

