/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            04-Dec-2003
 * Filename           $RCSfile: SimpleXMLPathIterator.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 12:20:41 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
 * An iterator over all of the results of evaluating a simple XML path.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: SimpleXMLPathIterator.java,v 1.5 2005-02-21 12:20:41 andy_seaborne Exp $
 */
public class SimpleXMLPathIterator 
    implements Iterator
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    /** The stack of iterators we use to search down the paths */
    protected List m_stack;
    
    /** The simple path we are evaluating */
    protected SimpleXMLPath m_path;
    
    /** Flag for whether the latest evaluation of the path has been prepared */
    protected boolean m_prepared = false;
    
    /** The value of the final iterator on the path */
    protected Object m_result;
    
    /** The length of the path */
    protected int m_len;
    
    // Constructors
    //////////////////////////////////

    public SimpleXMLPathIterator( SimpleXMLPath path, Node node ) {
        m_path = path;
        m_len = path.getPathComponents().size();
        m_stack = new ArrayList( m_len );
        
        // put the first stage on the stack
        m_stack.add( path.getPathComponent( 0 ).getAll( node ) );
        prepare();
    }
    
    
    // External signature methods
    //////////////////////////////////


    /**
     * Not supported.
     */
    public void remove() {
        throw new UnsupportedOperationException( "Cannot remove from SimpleXMLPathIterator" );
    }

    /**
     * Answer true if there is at least one more value
     */
    public boolean hasNext() {
        prepare();
        return m_result != null;
    }

    /**
     * Answer the next value in the iterator
     */
    public Object next() {
        prepare();
        
        if (m_result == null) {
            throw new NoSuchElementException( "No more values on this SimpleXMLPath" );
        }
        
        m_prepared = false;
        return m_result;
    }


    // Internal implementation methods
    //////////////////////////////////

    /** Prepare the next value in the sequence */
    protected void prepare() {
        if (!m_prepared) {
            evaluate();
            m_prepared = true;
        }
    }
    
    /**
     * <p>Evaluate the next traversal through the path, terminating either with a 
     * valid result in m_result, or a failure to find any (more) paths, and m_result
     * is null.</p>
     *
     */
    protected void evaluate() {
        // search for a route through to the end of the path
        int i = 0;
        m_result = null;
        
        // find the tidemark
        for (; i < min(m_len, m_stack.size()) && (m_stack.get(i) != null); i++);
        i--;
        
        while (i >= 0 && i < min(m_len, m_stack.size())) {
            Iterator j = (Iterator) m_stack.get( i );
            
            if (j == null) {
                // go back a stage
                i--;
            }
            else if (!j.hasNext()) {
                // finished iterator
                m_stack.add( i, null );
                m_result = null;
                i--;
            }
            else {
                // there is a valid stage here
                m_result = j.next();
                i++;
                
                if (i < m_len) {
                    // advance the stack along the path
                    m_stack.add( i, m_path.getPathComponent( i ).getAll( (Node) m_result ) );
                }
            }
        }
    }


    /** Answer the minimum of two ints */
    private int min( int x, int y ) {
        return (x < y) ? x : y;
    }
    
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
