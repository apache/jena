/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            04-Dec-2003
 * Filename           $RCSfile: TripleSubjectFiller.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-12-07 09:56:37 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;



// Imports
///////////////
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.Map1;



/**
 * <p>
 * Mapper to create triples from a given predicate and object
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TripleSubjectFiller.java,v 1.2 2004-12-07 09:56:37 andy_seaborne Exp $
 */
public class TripleSubjectFiller 
    implements Map1
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    private Node m_predicate;
    private Node m_object;
    
    // Constructors
    //////////////////////////////////

    /**
     * Construct a mapper to create triples from the given predicate and object,
     * with a subject supplied by the iterator being mapped.
     */
    public TripleSubjectFiller( Resource predicate, Resource object ) {
        this( predicate.asNode(), object.asNode() );
    }
    

    /**
     * Construct a mapper to create triples from the given predicate and object,
     * with a subject supplied by the iterator being mapped.
     */
    public TripleSubjectFiller( Node predicate, Node object ) {
        m_predicate = predicate;
        m_object = object;
    }
    
    
    // External signature methods
    //////////////////////////////////

    public Object map1( Object x ) {
        return new Triple( (Node) x, m_predicate, m_object );
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
