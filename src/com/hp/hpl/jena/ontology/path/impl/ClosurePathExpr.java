/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            20-Mar-2003
 * Filename           $RCSfile: ClosurePathExpr.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-08-27 13:04:44 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.path.impl;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.ontology.path.*;

import java.util.*;



/**
 * <p>
 * Implementation of path expressions that trasitively close over a 
 * given property, optionally with an occurs check.
 * </p>
 * <p>
 * TO DO: This needs to be extended to consider whether the underlying
 * graph is an inferencing graph that is already able to do closure
 * on the given predicate, and if so, there's no need for the bookkeeping
 * here.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: ClosurePathExpr.java,v 1.4 2003-08-27 13:04:44 andy_seaborne Exp $
 */
public class ClosurePathExpr
    extends AbstractPathExpr 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    /** The predicate that we are closing over */
    protected Property m_pred;
    
    /** Flag for including the occurs check or not */
    protected boolean m_occurs = true;
    
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a path expression for the transitive closure of the 
     * given property, optionally with an occurs check to prevent
     * loops (and hence an unbounded set of paths).
     * </p>
     * 
     * @param pred The predicate to close over
     * @param occurs Flag to indicate whether the occurs check should be
     * applied (<code>true</code>) or not (<code>false</code>).
     */
    public ClosurePathExpr( Property pred, boolean occurs ) {
        m_pred = pred;
        m_occurs = occurs;
    }
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Evaluate the path expression against the given root, which will result in a set of paths
     * that can be iterated through
     * </p>
     * 
     * @param root The root resource to evaluate the path from
     * @return A path iterator
     */
    public PathIterator evaluate( Resource root ) {
        return new ComposePathIterator( root );
    }
    
    
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================
    /**
     * <p>A path iterator for composition paths represented by the given statement iterator</p>
     */
    protected class ComposePathIterator
        extends PathIteratorImpl
    {
        protected StmtIterator m_si;
        protected Path m_head = null;
        protected List m_queue = new ArrayList();
        protected Statement m_stmt = null;
        
        protected ComposePathIterator( Resource root ) {
            m_si = root.listProperties( m_pred );
        }
        
        public boolean hasNext() { 
            checkNext();
            return m_stmt != null;
        }
        
        public Object next() { 
            checkNext();
            
            // add the cached statement to the path so far
            Path p = (m_head == null) ? new PathImpl( m_stmt) : new PathImpl( m_head, m_stmt );
            
            // if this path ends with a resource, add it to the queue
            if (m_stmt.getObject() instanceof Resource) {
                m_queue.add( p ); 
            }
            
            m_stmt = null;
            return p;
        }
        
        /**
         * <p>
         * Attempt to find the next statement on the closure, and cache it in m_stmt.
         * If no next statement can be found (respecting the occurs check), set m_stmt
         * to null.
         * </p>
         */
        protected void checkNext() {
            while (m_stmt == null  &&  ((m_si != null  &&  m_si.hasNext()) || !m_queue.isEmpty())) {
                // no cached statement yet
                if (m_si.hasNext()) {
                    m_stmt = m_si.nextStatement();
                    
                    if (occursCheck()) {
                        // occurs check failed - no good
                        m_stmt = null;
                    }
                }
                else {
                    if (!m_queue.isEmpty()) {
                        // no more at this level, so pop the next off the queue
                        m_head = (Path) m_queue.remove( 0 );
                        m_si = ((Resource) m_head.getValue()).listProperties( m_pred );
                    }
                }
            }
        }
        
        /**
         * <p>
         * Perform the occurs check by testing whether the object of the newly 
         * generated statement in m_stmt is a resource pointing back into the
         * path up to this point.
         * </p>
         * 
         * @return True if occurs check fails (ie there is a loop).
         */
        protected boolean occursCheck() {
            if (m_occurs  &&  (m_stmt.getObject() instanceof Resource)) {
                return m_head != null  &&  m_head.containsSubject( m_stmt.getResource() );
            }
            else {
                // no occurs check failure
                return false;
            }
        }
    }

}


/*
    (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
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

