/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */


package com.hp.hpl.jena.rdql;

import java.util.*;

/** Results from a query in a "ResultSet"-like manner.
 *  Each row corresponds to a set of bindings which fulfil the conditions
 *  of the query.  Access to the results is by variable name.
 *
 * @see Query
 * @see QueryEngine
 * @see ResultBindingImpl
 * @see QueryResultsStream
 * 
 * @author   Andy Seaborne
 * @version  $Id: QueryResults.java,v 1.9 2005-02-21 12:15:25 andy_seaborne Exp $
 */

public interface QueryResults extends java.util.Iterator
{
    /**
     *  @throws UnsupportedOperationException Always thrown.
     */

    public void remove() throws java.lang.UnsupportedOperationException ;

    /**
     * Is there another possibility?
     */
    public boolean hasNext() ;

    /** Moves onto the next result possibility.
     *  The returned object should be of class ResultBindingImpl
     */
    
    public Object next() ;

    /** Close the results iterator and stop query evaluation as soon as convenient.
     *  It is important to close query result iterators inorder to release
     *  resources such as working memory and to stop the query execution.
     *  Some storage subsystems require explicit ends of operations and this
     *  operation will cause those to be called where necessary.
     */

    public void close() ;

	/** Return the "row" number for the current iterator item
	 */
    public int getRowNumber() ;
    
    /** Get the variable names for the projection
     */
    public List getResultVars() ;

    /** Convenience function to consume a query.
     *  Returns a list of {@link ResultBindingImpl}s.
     *
     *  @return List
     *  @deprecated Use {@link QueryResultsMem} to get all the results of a query.
     */

    public List getAll() ;

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

