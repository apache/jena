/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            July 19th 2003
 * Filename           $RCSfile: DIGQueryRoleAncestorsTranslator.java,v $
 * Revision           $Revision: 1.6 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-12-07 09:56:35 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * ****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;


// Imports
///////////////
import org.w3c.dom.*;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.util.iterator.*;



/**
 * <p>
 * Translator that generates DIG role ancestors/desendants queries in response to a find queries:
 * <pre>
 * :X rdf:subPropertyOf *
 * *  rdf:subPropertyOf :X
 * </pre>
 * or similar.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS $Id: DIGQueryRoleAncestorsTranslator.java,v 1.6 2004-12-07 09:56:35 andy_seaborne Exp $
 */
public class DIGQueryRoleAncestorsTranslator 
    extends DIGQueryTranslator
{

    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    /** Flag for querying for ancestors */
    protected boolean m_ancestors;
    
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>Construct a translator for the DIG query 'rparents'.</p>
     * @param predicate The predicate URI to trigger on
     * @param ancestors If true, we are searching for parents of the class; if false, the descendants
     */
    public DIGQueryRoleAncestorsTranslator( String predicate, boolean ancestors ) {
        super( (ancestors ? null : ALL), predicate, (ancestors ? ALL : null) );
        m_ancestors = ancestors;
    }
    

    // External signature methods
    //////////////////////////////////


    /**
     * <p>Answer a query that will generate the class hierachy for a concept</p>
     */
    public Document translatePattern( TriplePattern pattern, DIGAdapter da ) {
        DIGConnection dc = da.getConnection();
        Document query = dc.createDigVerb( DIGProfile.ASKS, da.getProfile() );
        
        if (m_ancestors) {
            Element parents = da.createQueryElement( query, DIGProfile.RANCESTORS );
            da.addClassDescription( parents, pattern.getSubject() );
        }
        else {
            Element descendants = da.createQueryElement( query, DIGProfile.RDESCENDANTS );
            da.addClassDescription( descendants, pattern.getObject() );
        }
        
        return query;
    }


    /**
     * <p>Answer an iterator of triples that match the original find query.</p>
     */
    public ExtendedIterator translateResponse( Document response, TriplePattern query, DIGAdapter da ) {
        // translate the concept set to triples, but then we must add :a rdfs:subPropertyOf :a to match owl semantics
        return translateRoleSetResponse( response, query, m_ancestors );
    }
    
    
    public Document translatePattern( TriplePattern pattern, DIGAdapter da, Model premises ) {
        // not used
        return null;
    }

    public boolean checkSubject( com.hp.hpl.jena.graph.Node subject, DIGAdapter da, Model premises ) {
        return !m_ancestors || da.isRole( subject, premises );
    }
    
    public boolean checkObject( com.hp.hpl.jena.graph.Node object, DIGAdapter da, Model premises ) {
        return m_ancestors || da.isRole( object, premises );
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
