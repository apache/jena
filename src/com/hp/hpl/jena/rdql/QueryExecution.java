/*
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdql;

/** Interface for query execution engines.
 *  The normal sequence is:
 *  <code>
 *    QueryExecution qe = new .... ;
 *    qe.init() ;
 *    QueryResults qres = qe.exec() ;
 *    ....
 *    qres.close() ;
 *    qe.end() ;
 * </code>
 *
 * @see QueryEngine
 *
 * @author   Andy Seaborne
 * @version  $Id: QueryExecution.java,v 1.4 2003-08-27 12:25:58 andy_seaborne Exp $
 */


public interface QueryExecution
{
	/** Initialise a query execution.  Should be called before exec. */
	public void init();

	/** Do it! */
	public QueryResults exec();

	/** Do a query, given an initail starting set of bindings*/
	public QueryResults exec(ResultBinding startBinding) ;

	/** Stop in mid execution.
	 *  No guarantee that the concrete implementation actual will stop or
	 *  that it will do so immediately.
	 */

	public void abort();

	/** Normal end of use of this execution */
	public void close();
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
