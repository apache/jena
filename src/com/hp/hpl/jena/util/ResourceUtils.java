/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            05-Jun-2003
 * Filename           $RCSfile: ResourceUtils.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-06 11:07:02 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.util;



// Imports
///////////////
import java.util.*;

import com.hp.hpl.jena.rdf.model.*;



/**
 * <p>
 * General utility methods that operate on RDF resources, but which are not specific
 * to a given model.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: ResourceUtils.java,v 1.1 2003-06-06 11:07:02 ian_dickinson Exp $
 */
public class ResourceUtils {
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Answer the maximal lower elements of the given collection, given the partial
     * ordering <code>rel</code>. See {@link #maximalLowerElements( Iterator, Property)}
     * for details.
     * </p>
     * 
     * @param resources A collection of resources
     * @param rel A property defining a partial-ordering on <code>resources</code>
     * @param inverse If true, we invert the given property (by reversing the order
     * of the arguments), which allows us to use eg subClassOf as a partial order 
     * operator for both sub-class and super-class relationships
     * @return The collection that contains only those <code>resources</code> are not
     * greater than another resource under the partial order.
     */
    public static Collection maximalLowerElements( Collection resources, Property rel, boolean inverse ) {
        return maximalLowerElements( resources.iterator(), rel, inverse );
    }
    
    /**
     * <p>
     * Given a collection of resources, and a relation defining a partial order over
     * those resources, answer the sub-collection that contains only those elements
     * that appear in the maximal generator of the relation.  Specifically, a resource
     * <code>x</code> is excluded from the return value if there is another resource
     * <code>y</code> in the input collection such that <code>y&nbsp;Rel&nbsp;x</code> holds.
     * </p>
     * 
     * @param resources An iterator over a collection of resources
     * @param rel A property defining a partial-ordering on <code>resources</code>
     * @param inverse If true, we invert the given property (by reversing the order
     * of the arguments), which allows us to use eg subClassOf as a partial order 
     * operator for both sub-class and super-class relationships
     * @return The collection that contains only those <code>resources</code> are not
     * greater than another resource under the partial order.
     */
    public static Collection maximalLowerElements( Iterator resources, Property rel, boolean inverse ) {
        List in = new ArrayList();
        List out = new ArrayList();
        
        while (resources.hasNext()) {
            in.add( resources.next() );
        }
        
        while (! in.isEmpty()) {
            boolean rCovered = false;
            Resource r = (Resource) in.remove( 0 ); 
            
            // check the remaining input list
            for (Iterator i = in.iterator();  !rCovered && i.hasNext(); ) {
                Resource next = (Resource) i.next(); 
                rCovered = inverse ? r.hasProperty( rel, next ) : next.hasProperty( rel, r );
            }
            
            // check the output list
            for (Iterator i = out.iterator();  !rCovered && i.hasNext(); ) {
                Resource next = (Resource) i.next(); 
                rCovered = inverse ? r.hasProperty( rel, next ) : next.hasProperty( rel, r );
            }
            
            // if r is not covered by another resource, we can add it to the output
            if (!rCovered) {
                out.add( r );
            } 
        }
        
        return out;
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
