/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            14-Mar-2003
 * Filename           $RCSfile: ComposePathExpr.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-03-28 22:30:25 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.path.impl;


// Imports
///////////////
import com.hp.hpl.jena.ontology.path.*;
import com.hp.hpl.jena.rdf.model.*;



/**
 * <p>
 * A path that is created from the composition of two successive paths
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: ComposePathExpr.java,v 1.2 2003-03-28 22:30:25 ian_dickinson Exp $
 */
public class ComposePathExpr
    extends AbstractPathExpr 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    /** The left hand path expression, that will be evaluated first */
    protected PathExpr m_left;
    
    /** The right hand path expression, that will be evaluated second */
    protected PathExpr m_right;
    
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a path expression that presents the composition of two
     * path expressions
     * </p>
     * 
     * @param left The lhs path expression, to be evaluated first
     * @param right The rhs path expression, to be evaluated second
     */
    public ComposePathExpr( PathExpr left, PathExpr right ) {
        m_left = left; 
        m_right = right;
    }
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Evaluate the path expression against the given root, which will result in a set of paths
     * that can be iterated through
     * </p>
     * 
     * @param root
     * @return StmtIterator
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
        protected PathIterator m_leftIter;
        protected PathIterator m_rightIter = null;
        protected Path m_head;
        
        protected ComposePathIterator( Resource root ) {
            m_leftIter = m_left.evaluate( root );
        }
        
        public boolean hasNext() { 
            checkRightIter();
            return m_rightIter != null  &&  m_rightIter.hasNext();
        }
        
        public Object next() { 
            checkRightIter();
            return new PathImpl( m_head, m_rightIter.nextPath() ); 
        }
        
        /* Ensure that both left and right iterators are ready for next() or hasNext() */
        protected void checkRightIter() {
            if (m_rightIter == null  ||  !m_rightIter.hasNext()) {
                if (m_leftIter.hasNext()) {
                    m_head = m_leftIter.nextPath();
                    
                    // look at the last node in the path so far
                    RDFNode t = m_head.getValue();
                    
                    if (t instanceof Resource) {
                        // we can only chain from resources, not literals
                        m_rightIter = m_right.evaluate( (Resource) t );
                    }
                    else {
                        // skip the literal and try again
                        checkRightIter();
                    }
                }
                else {                
                    // signal no more options by setting the right iterator to null
                    m_rightIter = null;
                }
            }
        }
    }

}


/*
    (c) Copyright Hewlett-Packard Company 2002-2003
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


