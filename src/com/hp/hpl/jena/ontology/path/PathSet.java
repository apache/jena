/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            14-Mar-2003
 * Filename           $RCSfile: PathSet.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-03-25 10:11:40 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.path;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;



/**
 * <p>
 * Provides a set of convenience functions for accessing
 * the results of evaluating a {@link PathExpr} against a given ontology
 * resource.  This set of set of zero or more paths through the graph
 * can then be inspected in various ways, or, in some cases, added to.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: PathSet.java,v 1.1 2003-03-25 10:11:40 ian_dickinson Exp $
 */
public class PathSet {
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** The path expression this convenience class wraps */
    protected PathExpr m_expr;
    
    /** The root resource */
    protected Resource m_root;
    

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a path set object that represents the abstract set
     * of paths (defined by the given path expression) leading from the
     * given root resource. 
     * </p>
     * 
     * @param root The root resource
     * @param pathExpr The path expresion
     */
    public PathSet( Resource root, PathExpr pathExpr ) {
        m_expr = pathExpr;
        m_root = root;        
    }
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Add the given value as the value of a path expression from the ontology resource against
     * which this path was evaluated. This operation is not defined for all types
     * of path expression.  In particular, paths other than a unit path with a named
     * property may throw an exception.
     * </p>
     * 
     * @param value The value to add
     * @exception PathException if this path expression cannot be used to add a value
     */
    public void add( RDFNode value ) {
        m_expr.add( m_root, value );
    }
    
    /**
     * <p>
     * Remove all of statements on the paths through the graph that this 
     * <code>PathSet</code> corresponds to.
     * </p>
     */
    public void removeAll() {
        Set stmts = new HashSet();
        
        // collect the statements from all of the paths
        for (PathIterator i = paths();  i.hasNext(); ) {
            for (StmtIterator j = i.nextPath().iterator();  j.hasNext();  stmts.add( j.next() ));
        }
        
        // now remove them all
        for (Iterator i = stmts.iterator();  i.hasNext(); ) {
            ((Statement) i.next()).remove();
        }
    }
    
    /**
     * <p>
     * Answer the number of distinct paths in this set
     * </p>
     * 
     * @return The size of the set of paths
     */
    public int size() {
        int size = 0;
        
        for (Iterator i = paths();  i.hasNext();  i.next()) {
            size++;
        }
        
        return size;
    }
    
    /**
     * <p>
     * Answer an iterator over all the values of the paths in this set, where a value
     * is defined as the terminal node on each path.  Duplicate values may be returned
     * if more than one path arrives at the same terminal node.
     * </p>
     * 
     * @return An extended iterator of values
     */
    public ExtendedIterator iterator() {
        return paths().mapWith( new Map1() {
                                    public Object map1( Object x ) {
                                        return ((Path) x).getValue();
                                    }
                                } );
    }
    
    /**
     * <p>
     * Answer an iterator over the paths in the set, where each path is encoded as an
     * ordered Java list of statements.
     * </p>
     * 
     * @return An extended iterator, each value of which is a {@link Path}.
     */
    public PathIterator paths() {
        return m_expr.evaluate( m_root );
    }
    
    /**
     * <p>
     * Answer true if at least one path in this set has <code>value</code> as 
     * a terminal.
     * </p>
     * 
     * @param value A value to test for
     * @return True if value is a terminal node for any path in this set
     */
    public boolean hasValue( RDFNode value ) {
        boolean found = false;
        ExtendedIterator i = iterator();
        
        while (!found && i.hasNext()) {
            found = value.equals( i.next() );
        }
        
        i.close();
        return found;
    }
    
    /**
     * <p>
     * Answer true if there are no paths in this path set.
     * </p>
     * 
     * @return True if empty
     */
    public boolean isEmpty() {
        ExtendedIterator i = paths();
        
        boolean empty = !i.hasNext();
        i.close();
        
        return empty;
    }
    
    /**
     * <p>
     * Convenience function to get the value of the first path in this set. This is 
     * uniquely defined if there is only one path in the set.  If there is more than
     * one path, an arbitrary value is returned.  If the set is empty, an exception
     * is thrown.
     * </p>
     * 
     * @return The value of the first path in the path set
     * @exception PathException if there are no paths in the set.
     */
    public RDFNode getValue() {
        PathIterator i = paths();
        
        try {
            return i.nextPath().getValue();
        }
        catch (Exception e) {
            throw new PathException( "Tried to get the value from an empty path set" );
        }
        finally {
            i.close();
        }
    }
    
    
    // Internal implementation methods
    //////////////////////////////////

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
