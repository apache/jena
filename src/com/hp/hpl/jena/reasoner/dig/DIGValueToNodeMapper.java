/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            04-Dec-2003
 * Filename           $RCSfile: DIGValueToNodeMapper.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 12:16:24 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;



// Imports
///////////////
import org.w3c.dom.Element;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.util.iterator.Map1;


/**
 * <p>
 * Mapper to map DIG identifier names and concrete value elements to Jena graph nodes.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: DIGValueToNodeMapper.java,v 1.3 2005-02-21 12:16:24 andy_seaborne Exp $
 */
public class DIGValueToNodeMapper 
    implements Map1
{
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
     * <p>Return the node corresponding to the given element; either a literal
     * node for ival and sval values, or a URI node for named elements.</p>
     * @param o An object, expected to be an XML element
     */
    public Object map1( Object o ) {
        if (o instanceof Element) {
            // we know that this mapper is applied to lists of Elements
            Element elem = (Element) o;
            
            if (elem.getNodeName().equals( DIGProfile.IVAL )) {
                // this is an integer element
                return Node.createLiteral( elem.getNodeValue(), null, XSDDatatype.XSDint );
            }
            else if (elem.getNodeName().equals( DIGProfile.SVAL )) {
                // this is an integer element
                return Node.createLiteral( elem.getNodeValue(), null, XSDDatatype.XSDstring );
            }
            else if (elem.hasAttribute( DIGProfile.NAME )) {
                return convertNameToNode( elem.getAttribute( DIGProfile.NAME ) );
            }
        }
        else if (o instanceof String) {
            return convertNameToNode( (String) o );
        }

        throw new IllegalArgumentException( "Cannot map value " + o + " to an RDF node because it is not a recognised type" );
    }


    // Internal implementation methods
    //////////////////////////////////

    /** Answer the node with the given name. It may be the node ID of a bNode */
    private Object convertNameToNode( String name ) {
        if (name.startsWith( DIGAdapter.ANON_MARKER )) {
            String anonID = name.substring( DIGAdapter.ANON_MARKER.length() );
            return Node.createAnon( new AnonId( anonID ) );
        }
        else {
            return Node.createURI( name );
        }
    }


    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

