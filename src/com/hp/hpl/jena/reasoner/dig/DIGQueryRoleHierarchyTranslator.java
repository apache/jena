/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            July 19th 2003
 * Filename           $RCSfile: DIGQueryRoleHierarchyTranslator.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 12:16:24 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * ****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;


// Imports
///////////////
import java.util.Iterator;


import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.RDF;



/**
 * <p>
 * Translator that generates DIG queries in response to find queries that search the entire class
 * hierarchy:
 * <pre>
 * * rdf:subClassOf *
 * </pre>
 * or similar.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: DIGQueryRoleHierarchyTranslator.java,v 1.4 2005-02-21 12:16:24 andy_seaborne Exp $)
 */
public class DIGQueryRoleHierarchyTranslator 
    extends DIGIteratedQueryTranslator
{

    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>Construct a translator for the DIG class hierarchy queries.</p>
     * @param predicate The predicate URI to trigger on
     */
    public DIGQueryRoleHierarchyTranslator( String predicate ) {
        super( ALL, predicate, ALL );
    }


    // External signature methods
    //////////////////////////////////

    /**
     * <p>To query the entire role hierarchy, we need to know every super-class of every class.</p>
     * @param pattern The incoming pattern
     * @param da The current DIG adapter
     */
    protected Iterator expandQuery( TriplePattern pattern, DIGAdapter da ) {
        final Node pred = pattern.getPredicate();

        // find all concepts, and use them to replace the lhs nodes in the pattern
        return new DIGQueryAllRolesTranslator( null, null )
               .find( new TriplePattern( null, RDF.type.asNode(), null ), da )
               .mapWith( new Map1() {
                   public Object map1( Object x ) {
                       Triple triple = (Triple) x;
                       return new TriplePattern( triple.getSubject(), pred, Node_RuleVariable.WILD );
                   }
               } );
    }



    // Internal implementation methods
    //////////////////////////////////

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
