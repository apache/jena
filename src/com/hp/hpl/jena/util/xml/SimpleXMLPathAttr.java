/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            04-Dec-2003
 * Filename           $RCSfile: SimpleXMLPathAttr.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-12-06 13:50:26 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.util.xml;


// Imports
///////////////
import java.util.*;

import org.w3c.dom.*;


/**
 * <p>
 * An implementation of a simple XML path component that handles named attributes
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: SimpleXMLPathAttr.java,v 1.3 2004-12-06 13:50:26 andy_seaborne Exp $
 */
public class SimpleXMLPathAttr 
    implements SimpleXMLPathComponent
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /** The element name we are evaluating */
    protected String m_attrName;
    
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>Construct a simple XML path component that selects the value
     * of a named attribute.</p>
     * @param attrName The name of the attribute to select
     */
    public SimpleXMLPathAttr( String attrName ) {
        m_attrName = attrName;
    }


    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer an iterator over all of the values of this path component when
     * evaluated with respect to the given node.</p>
     * @param node The parent node to evaluate against
     * @return An iterator over all of the objects that correspond to evaluating
     * this path against the given node.
     */
    public Iterator getAll( Node node ) {
        // elements should occur within elements
        if (!(node instanceof Element)) {
            throw new IllegalArgumentException( "Tried to get attribute " + m_attrName + " from a parent node of type " + node.getClass().getName() );
        } 
        
        List attr = new ArrayList();
        Element e = (Element) node;
        if (e.hasAttribute( m_attrName )) {
            attr.add( e.getAttribute( m_attrName ) );
        }
        
        return attr.iterator();
    }
    
    /**
     * <p>Answer the first value for this path expression against the given node.</p>
     * @param node The parent node to evalauate against
     * @return The first object that corresponds to evaluating
     * this path against the given node, or null if there is no such value
     */
    public Object getFirst( Node node ) {
        return ((Element) node).getAttribute( m_attrName );
    } 
    
    

    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
 *  (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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
