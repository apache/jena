/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            05-Jun-2003
 * Filename           $RCSfile: ResourceUtils.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-08 21:29:58 $
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
 * @version CVS $Id: ResourceUtils.java,v 1.3 2003-06-08 21:29:58 ian_dickinson Exp $
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
     * ordering <code>rel</code>. See {@link #maximalLowerElements( Iterator, Property, boolean )}
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


    /**
     * <p>Answer a new resource that occupies the same position in the graph as the current
     * resource <code>old</code>, but that has the given URI.  In the process, the existing
     * statements referring to <code>old</code> are removed.  Since Jena does not allow the
     * identity of a resource to change, this is the closest approximation to a rename operation
     * that works.
     * </p>
     * <p><strong>Notes:</strong> This method does minimal checking, so renaming a resource
     * to its own URI is unpredictable.  Furthermore, it is a general and simple approach, and
     * in given applications it may be possible to do this operation more efficiently. Finally,
     * if <code>res</code> is a property, existing statements that use the property will not
     * be renamed, nor will occurrences of <code>res</code> in other models.
     * </p>
     * @param old An existing resource in a given model
     * @param uri A new URI for resource old, or null to rename old to a bNode
     * @return A new resource that occupies the same position in the graph as old, but which
     * has the new given URI.
     */
    public static Resource renameResource( Resource old, String uri ) {
        Model m = old.getModel();
        List subjectRefs = new ArrayList();
        List objectRefs = new ArrayList();
        
        // list the statements that mention old as a subject
        for (Iterator i = old.listProperties();  i.hasNext(); subjectRefs.add( i.next() ) );
        
        // list the statements that mention old an an object
        for (Iterator i = m.listStatements( null, null, old );  i.hasNext();  objectRefs.add( i.next() ) );
        
        // create a new resource to replace old
        Resource res = (uri == null) ? m.createResource() : m.createResource( uri );
        
        // now move the statements to refer to res instead of old
        for (Iterator i = subjectRefs.iterator(); i.hasNext(); ) {
            Statement s = (Statement) i.next();
            
            res.addProperty( s.getPredicate(), s.getObject() );
            s.remove();
        }
        
        for (Iterator i = objectRefs.iterator(); i.hasNext(); ) {
            Statement s = (Statement) i.next();
            
            s.getSubject().addProperty( s.getPredicate(), res );
            s.remove();
        }
        
        return res;
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
