/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            July 19th 2003
 * Filename           $RCSfile: DIGQueryParentsTranslator.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2005-03-16 18:52:28 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * ****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;


// Imports
///////////////
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.util.iterator.Filter;



/**
 * <p>
 * Translator that generates DIG parents/childre queries in response to a find queries:
 * <pre>
 * :X direct-subClassOf *
 * *  direct-subClassOf :X
 * </pre>
 * or similar.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: DIGQueryParentsTranslator.java,v 1.5 2005-03-16 18:52:28 ian_dickinson Exp $)
 */
public class DIGQueryParentsTranslator
    extends DIGQueryAncestorsTranslator
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
     * <p>Construct a translator for the DIG query 'parents'.</p>
     * @param predicate The predicate URI to trigger on
     * @param parents If true, we are searching for parents of the class; if false, the children
     */
    public DIGQueryParentsTranslator( String predicate, boolean parents ) {
        super( predicate, parents );
    }


    /**
     * <p>Construct a translator for the DIG query 'parents', with explicit
     * subject and object values.</p>
     * @param subject The subject URI to trigger on
     * @param predicate The predicate URI to trigger on
     * @param object The object URI to trigger on
     * @param parents If true, we are searching for parents of the class; if false, the children
     */
    public DIGQueryParentsTranslator( String subject, String predicate, String object, boolean parents ) {
        super( subject, predicate, object, parents );
    }


    // External signature methods
    //////////////////////////////////


    /**
     * <p>Answer a query that will generate the direct class hierarchy (one level up or down) for a node</p>
     */
    public Document translatePattern( TriplePattern pattern, DIGAdapter da ) {
        DIGConnection dc = da.getConnection();
        Document query = dc.createDigVerb( DIGProfile.ASKS, da.getProfile() );

        if (m_ancestors) {
            Element parents = da.createQueryElement( query, DIGProfile.PARENTS );
            da.addClassDescription( parents, pattern.getSubject() );
        }
        else {
            Element descendants = da.createQueryElement( query, DIGProfile.CHILDREN );
            da.addClassDescription( descendants, pattern.getObject() );
        }

        return query;
    }

    /**
     * <p>For direct sub-class, we must sometimes ask a more general query
     * and filter the returned results against the original query.</p>
     * @return An optional filter on the results of a DIG query
     */
    protected Filter getResultsTripleFilter( TriplePattern query ) {
        return new FilterSubjectAndObject( query.getSubject(), query.getObject() );
    }


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

    private class FilterSubjectAndObject
        implements Filter
    {
        private Node m_subj;
        private Node m_obj;

        private FilterSubjectAndObject( Node subj, Node obj ) {
            m_subj = subj;
            m_obj = obj;
        }

        public boolean accept( Object o ) {
            Triple t = (Triple) o;
            return ((m_subj == null) ||
                    (m_subj == Node_RuleVariable.WILD) ||
                    (m_subj == Node.ANY) ||
                    (t.getSubject().equals( m_subj ))) &&
                   ((m_obj == null) ||
                    (m_obj == Node_RuleVariable.WILD) ||
                    (m_obj == Node.ANY) ||
                    (t.getObject().equals( m_obj )));
        }

    }
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
