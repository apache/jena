/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            10-Dec-2003
 * Filename           $RCSfile: DIGQueryEquivalentsTranslator.java,v $
 * Revision           $Revision: 1.9 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-12-07 09:56:35 $
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
 * <p>
 * Translator to map owl:equivalentClass to the DIG &lt;equivalents&gt; query.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: DIGQueryEquivalentsTranslator.java,v 1.9 2004-12-07 09:56:35 andy_seaborne Exp $
 */
public class DIGQueryEquivalentsTranslator 
    extends DIGQueryTranslator
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    /** Flag for whether the free variable is on the lhs or the rhs */
    protected boolean m_subjectFree;
    
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>Construct a translator for the DIG query 'equivalents'.</p>
     * @param predicate The predicate URI to trigger on
     * @param subjectFree If true, the free variable is the subject of the triple
     */
    public DIGQueryEquivalentsTranslator( String predicate, boolean subjectFree ) {
        super( null, predicate, null );
        m_subjectFree = subjectFree;
    }
    

    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer a query that will generate the class hierachy for a concept</p>
     */
    public Document translatePattern( TriplePattern pattern, DIGAdapter da ) {
        DIGConnection dc = da.getConnection();
        Document query = dc.createDigVerb( DIGProfile.ASKS, da.getProfile() );
        
        Element equivalents = da.createQueryElement( query, DIGProfile.EQUIVALENTS );
        da.addClassDescription( equivalents, m_subjectFree ? pattern.getObject() : pattern.getSubject() );
        
        return query;
    }


    /**
     * <p>Answer an iterator of triples that match the original find query.</p>
     */
    public ExtendedIterator translateResponse( Document response, TriplePattern query, DIGAdapter da ) {
        return translateConceptSetResponse( response, query, !m_subjectFree );
    }
    
    public Document translatePattern( TriplePattern pattern, DIGAdapter da, Model premises ) {
        // not used
        return null;
    }

    
    public boolean checkSubject( com.hp.hpl.jena.graph.Node subject, DIGAdapter da, Model premises ) {
        return (m_subjectFree && !subject.isConcrete()) || da.isConcept( subject, premises );
    }
    
    public boolean checkObject( com.hp.hpl.jena.graph.Node object, DIGAdapter da, Model premises ) {
        return (!m_subjectFree && !object.isConcrete()) || da.isConcept( object, premises );
    }

    public boolean checkTriple( TriplePattern pattern, DIGAdapter da, Model premises ) {
        return super.checkTriple( pattern, da, premises ) &&
               (!pattern.getSubject().isConcrete() || !pattern.getObject().isConcrete());

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
