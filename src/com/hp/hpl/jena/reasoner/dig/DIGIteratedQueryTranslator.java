/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            09-Dec-2003
 * Filename           $RCSfile: DIGIteratedQueryTranslator.java,v $
 * Revision           $Revision: 1.6 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2005-03-16 18:52:28 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;



// Imports
///////////////
import java.util.Iterator;

import org.w3c.dom.Document;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.util.iterator.*;


/**
 * <p>
 * A specialisation of DIG query translator that aggregates iterated queries
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: DIGIteratedQueryTranslator.java,v 1.6 2005-03-16 18:52:28 ian_dickinson Exp $
 */
public abstract class DIGIteratedQueryTranslator 
    extends DIGQueryTranslator
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
     * <p>Construct a query translator for the given query parameters.</p>
     * @param subject Represents the incoming subject to trigger against
     * @param predicate Represents the incoming predicate to trigger against
     * @param object Represents the incoming object to trigger against
     */
    public DIGIteratedQueryTranslator( String subject, String predicate, String object ) {
        super( subject, predicate, object );
    }



    // External signature methods
    //////////////////////////////////

    /**
     * <p>Takes the incoming query pattern and expands it out to a series of subsidary
     * triple patterns that will be taken as queries in their own right.</p> 
     * @param pattern The incomimg query pattern
     * @param da The DIG adapter currently being used to communicate with the DIG reasoner
     * @return An iterator over a series of {@link TriplePattern}'s that represent
     * the expanded query
     */
    protected abstract Iterator expandQuery( TriplePattern pattern, DIGAdapter da );
    
    
    /**
     * <p>Expand the given pattern to a series of more grounded patterns, and collate
     * the results of querying with each of these expanded patterns. This is used in
     * cases where the incoming query is too ungrounded to pass to DIG in one go, e.g. 
     * <code>*&nbsp;rdfs:subClassOf&nbsp;*</code>. The strategy is to expand one of 
     * the ungrounded terms to form a series of queries, then solve each of these
     * queries separately.</p>
     * @param pattern The pattern to translate to a DIG query
     * @param da The DIG adapter through which we communicate with a DIG reasoner
     */
    public ExtendedIterator find( TriplePattern pattern, DIGAdapter da ) {
        ExtendedIterator all = null;
        
        for (Iterator i = expandQuery( pattern, da );  i.hasNext(); ) {
            ExtendedIterator results = da.find( (TriplePattern) i.next() );
            all = (all == null) ? results : all.andThen( results );
        }
        
        return UniqueExtendedIterator.create( all );
    }
    
    
    /**
     * Not needed in this class - delegated to the specific query handlers
     */
    public Document translatePattern( TriplePattern query, DIGAdapter da ) {
        return null;
    }

    public Document translatePattern( TriplePattern pattern, DIGAdapter da, Model premises ) {
        // not used
        return null;
    }

    /**
     * Not needed in this class - delegated to the specific query handlers
     */
    public ExtendedIterator translateResponseHook(Document Response, TriplePattern query, DIGAdapter da) {
        return null;
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
