/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            14-Mar-2003
 * Filename           $RCSfile: PathImpl.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-03-27 16:28:45 $
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

import java.util.*;


/**
 * <p>
 * Default implementation of the {@link Path} interface for encoding a given path 
 * through the graph.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: PathImpl.java,v 1.2 2003-03-27 16:28:45 ian_dickinson Exp $
 */
public class PathImpl
    implements Path 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    /** The list of statements forming this path */
    protected List m_statements = new ArrayList();
    
    /** Optional prefix path to this path */
    protected Path m_prePath = null;
    
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>Construct an empty path</p>
     */
    public PathImpl() {
    }
    
    /**
     * <p>Construct a path with the given statements as the path entries</p>
     * @param path A sequence of statements
     */
    public PathImpl( List path ) {
        m_statements.addAll( path );
    }
    
    /**
     * <p>Construct a path with the given statements as the path entries</p>
     * @param i A sequence of statements
     */
    public PathImpl( Iterator i ) {
        for (; i.hasNext(); add( (Statement) i.next() ));
    }
    
    /**
     * <p>Construct the unit path from the given statement</p>
     * @param stmt A statement that is the initial path
     */
    public PathImpl( Statement stmt ) {
        m_statements.add( stmt );
    }
    
    /**
     * <p>Construct a path from the given statement, with the given
     * path prepended</p>
     * @param path A pre-path
     * @param stmt A statement that is the initial path
     */
    public PathImpl( Path path, Statement stmt ) {
        m_prePath = path;
        m_statements.add( stmt );
    }
    
    /**
     * <p>Construct the path that is the concatenation of the given paths
     * @param path0 A path
     * @param path1 A path
     */
    public PathImpl( Path path0, Path path1 ) {
        m_prePath = path0;
        m_statements.addAll( path1.asList() );
    }
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Answer the value of the path, interpreted as the object of the last
     * statement.
     * </p>
     * 
     * @return The object of the last statement in the path
     */
    public RDFNode getValue() {
        return getLastStatement().getObject();
    }
    
    
    /**
     * <p>
     * Answer an iterator over the statements in the path, starting with the 
     * root node.
     * </p>
     * 
     * @return A statement iterator over the statements in the path, in order
     */
    public StmtIterator iterator() {
        return new PathStmtIterator();
    }
    
    
    /**
     * <p>
     * Answer the length of the path.
     * </p>
     * 
     * @return The number of statements in the path
     */
    public int length() {
        return (m_prePath == null ? 0 : m_prePath.length()) + m_statements.size();
    }
    
    
    /**
     * <p>
     * Answer true if the path is empty (has length zero).
     * </p>
     * 
     * @return True if this path is empty
     */
    public boolean isEmpty() {
        return m_prePath == null ? m_statements.isEmpty() : (m_prePath.isEmpty() && m_statements.isEmpty());
    }


    /**
     * <p>
     * Add the given statement to the end of the path
     * </p>
     * 
     * @param stmt A statement that will extend this path. Note that the subject
     * of this statement should equal the object of the last statement in the
     * path (if there is one), but this is not checked. 
     */
    public void add( Statement stmt ) {
        m_statements.add( stmt ); 
    }
    
    
    // Internal implementation methods
    //////////////////////////////////

    /**
     * <p>Answer the last statement in the path</p>
     * 
     * @return The statement at the end of the path
     * @exception PathException if the path has no last element.
     */
    protected Statement getLastStatement() {
        try {
            return (Statement) m_statements.get( m_statements.size() - 1 );
        }
        catch (IndexOutOfBoundsException e) {
            throw new PathException( "Cannot get the last statement from an empty path" );
        }
    }

    
    /**
     * <p>
     * Answer the path as a list of statements
     * </p>
     * 
     * @return A List whose values are the statements in the path
     */
    public List asList() {
        List s = new ArrayList();
        for (Iterator i = new PathStmtIterator();  i.hasNext();  s.add( i.next() ));
        
        return s;
    }
    
    
    /**
     * <p>
     * Answer true if any of the statements on the path have <code>s</code>
     * as a subject.
     * </p>
     * 
     * @param s A resource
     * @return True if s is an existing subject on the path.
     */
    public boolean containsSubject( Resource s ) {
        for (Iterator i = m_statements.iterator();  i.hasNext(); ) {
            Statement stmt = (Statement) i.next();
            
            if (stmt.getSubject().equals( s )) {
                return true;
            }
        }
        
        return m_prePath == null ? false : m_prePath.containsSubject( s );
    }
    
    
    /**
     * <p>
     * Answer the i'th statement from the path, where the first statement in
     * the path (closest to the root node) has index 0.
     * </p>
     * 
     * @param i An integer index into the statements on the path
     * @return The i'th statement along the path, from zero.
     * @exception IndexOutOfBoundsException if i is less than zero, or there are not
     * <code>i - 1</code> statements in the path.
     */
    public Statement getStatement( int i ) {
        return (Statement) m_statements.get( i );
    }



    //==============================================================================
    // Inner class definitions
    //==============================================================================

    /**
     * <p>Implementation of statement iterator for path objects</p>
     */
    protected class PathStmtIterator
        implements StmtIterator
    {
        private int i = 0;
        private StmtIterator si;
        private Statement s;
        
        protected PathStmtIterator() {
            if (m_prePath != null) {
                si = m_prePath.iterator();
            }
        }
        
        public boolean hasNext() { 
            return (si != null && si.hasNext())  ||  i < m_statements.size(); 
        }
        
        public Object next() { 
            // lookup the next statement 
            return (si != null  && si.hasNext()) ? si.next() : m_statements.get( i++ );
        }
        
        public Statement nextStatement() { 
            return (Statement) next(); 
        }
        
        public void remove() {
            if (si != null && si.hasNext()) {
                si.remove();
            }
            else {
                ((Statement) m_statements.get( i - 1 )).remove();
            }
        }
        
        public void close() {}
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


