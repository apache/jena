/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    protected OneToManyMap<Resource, Resource> m_aliasesMap;
    
    
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
    @Override
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
    @Override
    public Resource getAliasFor( Resource res ) {
        return aliasMap().get( res );
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
    @Override
    public Iterator<Resource> listAliasesFor( Resource res ) {
        return aliasMap().getAll( res );
    }

    /**
        Utility method: answer true iff the enhanced graph contains some triple which
        has n as subject, p.asNode() as predicate, and any object.
        
         @param g an enhanced graph to search for triples
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
     * alias for another (for example rdfs:Class).
     */
    protected abstract Resource[][] aliasTable();
    
    
    /**
     * <p>
     * Prepare the local alias map by reading the alias table from the concrete sub-class.
     * </p>
     */
    protected OneToManyMap<Resource, Resource> aliasMap() {
        if (m_aliasesMap == null) {
            // aliases map not prepared yet, so initialise using the data from
            // the concrete profile class
            m_aliasesMap = new OneToManyMap<>();
            Resource[][] aliases = aliasTable();
            for ( Resource[] aliase : aliases )
            {
                // since alias relationship is symmetric, we record both directions
                m_aliasesMap.put( aliase[0], aliase[1] );
                m_aliasesMap.put( aliase[1], aliase[0] );
            }
        }     
        return m_aliasesMap;
    }


    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
