/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            July 19th 2003
 * Filename           $RCSfile: DIGQueryAncestorsTranslator.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-12-09 13:02:30 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * ****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;


// Imports
///////////////
import org.w3c.dom.*;

import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.util.xml.SimpleXMLPath;



/**
 * <p>
 * Translator that generates DIG ancestors/desendants queries in response to a find queries:
 * <pre>
 * :X rdf:subClassOf *
 * *  rdf:subClassOf :X
 * </pre>
 * or similar.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: DIGQueryAncestorsTranslator.java,v 1.1 2003-12-09 13:02:30 ian_dickinson Exp $)
 */
public class DIGQueryAncestorsTranslator 
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
     * <p>Construct a translator for the DIG query 'parents'.</p>
     * @param predicate The predicate URI to trigger on
     * @param ancestors If true, we are searching for parents of the class; if false, the descendants
     */
    public DIGQueryAncestorsTranslator( String predicate, boolean ancestors ) {
        super( (ancestors ? null : ALL), predicate, (ancestors ? ALL : null) );
        m_ancestors = ancestors;
    }
    

    // External signature methods
    //////////////////////////////////


    /**
     * <p>Answer a query that will test subsumption between two classes</p>
     */
    public Document translatePattern( TriplePattern pattern, DIGAdapter da ) {
        DIGConnection dc = da.getConnection();
        Document query = dc.createDigVerb( DIGProfile.ASKS, da.getProfile() );
        
        if (m_ancestors) {
            Element parents = da.addElement( query.getDocumentElement(), DIGProfile.ANCESTORS );
            da.addClassDescription( parents, pattern.getSubject() );
        }
        else {
            Element descendants = da.addElement( query.getDocumentElement(), DIGProfile.DESCENDANTS );
            da.addClassDescription( descendants, pattern.getObject() );
        }
        
        return query;
    }


    /**
     * <p>Answer an iterator of triples that match the original find query.</p>
     */
    public ExtendedIterator translateResponse( Document response, TriplePattern query, DIGAdapter da ) {
        // evaluate a path through the return value to give us an iterator over catom names
        ExtendedIterator catomNames = new SimpleXMLPath( true )
                                          .appendElementPath( DIGProfile.CONCEPT_SET )
                                          .appendElementPath( DIGProfile.SYNONYMS )
                                          .appendElementPath( DIGProfile.CATOM )
                                          .appendAttrPath( DIGProfile.NAME )
                                          .getAll( response );
        
        ExtendedIterator catomNodes = catomNames.mapWith( new NameToNodeMapper() );
        
        // to match OWL semantics, we must include this node itself
        catomNodes = catomNodes.andThen( new SingletonIterator( m_ancestors ? query.getSubject() : query.getObject() ) );
        
        // return the results as triples
        if (m_ancestors) {
            return catomNodes.mapWith( new TripleObjectFiller( query.getSubject(), query.getPredicate() ) );
        }
        else {
            return catomNodes.mapWith( new TripleSubjectFiller( query.getPredicate(), query.getObject() ) );
        }
    }
    
    public boolean checkSubject( com.hp.hpl.jena.graph.Node subject ) {
        return !m_ancestors || subject.isConcrete();
    }
    
    public boolean checkObject( com.hp.hpl.jena.graph.Node object ) {
        return m_ancestors || object.isConcrete();
    }


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
 *  (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
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
