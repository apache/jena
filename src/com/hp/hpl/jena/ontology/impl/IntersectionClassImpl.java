/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            28-Apr-2003
 * Filename           $RCSfile: IntersectionClassImpl.java,v $
 * Revision           $Revision: 1.8 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 12:06:23 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;


// Imports
///////////////
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.Property;


/**
 * <p>
 * Implementation of a node representing an intersection class description.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: IntersectionClassImpl.java,v 1.8 2005-02-21 12:06:23 andy_seaborne Exp $
 */
public class IntersectionClassImpl 
    extends BooleanClassDescriptionImpl
    implements IntersectionClass
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating IntersectionClass facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use 
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                return new IntersectionClassImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n + " to IntersectionClass");
            } 
        }
            
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an IntersectionClass facet if it has rdf:type owl:Class and an owl:intersectionOf statement (or equivalents) 
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&  
                   profile.isSupported( node, eg, OntClass.class )  &&
                   AbstractProfile.containsSome( eg, node, profile.INTERSECTION_OF() ); 
        }
    };


    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct an intersection class node represented by the given node in the given graph.
     * </p>
     * 
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public IntersectionClassImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////

	public Property operator() {return getProfile().INTERSECTION_OF();}
	public String getOperatorName() {return "INTERSECTION_OF";}
    

    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
    (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
