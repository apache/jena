/*
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.rdql.test;

import java.io.* ;
import java.util.* ;
import com.hp.hpl.jena.rdql.*;
import com.hp.hpl.jena.rdf.model.* ;

/** Test the Jena-Sesame RDQL query engine.
 * @author		Andy Seaborne
 * @version 	$Id: SesameQueryMain.java,v 1.2 2003-02-20 16:46:31 andy_seaborne Exp $
 */
public class SesameQueryMain
{
	static final String serverURLStr = "http://localhost:3030/sesame/" ;
	static final String repository = "sesame-db" ;
		

	public static void main(String[] args)
	{
		try {
			PrintWriter pw = new PrintWriter(System.out) ;
			
			String queryString = "SELECT * WHERE (<http://foo/bar#C>, ?y, ?z)" ;
			Query q = new Query(queryString) ;
			
			QueryExecution qe = new QueryEngineSesame(q, serverURLStr, repository) ;
			QueryResultsMem qrm = new QueryResultsMem(qe.exec()) ;
			QueryResultsFormatter fmt = new QueryResultsFormatter(qrm) ;
			fmt.printAll(pw) ;
			qrm.reset() ;
			pw.flush() ;
			
			// Now check that producing triples works.
			System.out.println() ;
			for ( Iterator iter = q.getTriplePatterns().iterator() ; iter.hasNext() ; )
			{
				TriplePattern tp =(TriplePattern)iter.next() ;
				System.out.println(tp) ;
			}
			
			System.out.println() ;
			for ( Iterator iter = qrm ; iter.hasNext() ; )
			{
				ResultBinding rb = (ResultBinding)iter.next() ;
				System.out.println(rb) ;
				for ( Iterator iter2 = rb.getTriples().iterator() ; iter2.hasNext() ; )
				{
					Statement s = (Statement)iter2.next() ;
					System.out.println("    "+s) ;
				}
			}
		
			return ;
		
		} catch (Exception ex)
		{
			System.err.println("Unhandled exception: "+ex) ;
			ex.printStackTrace(System.err) ;
			return ;
		}
	}
}


/*
 *  (c) Copyright Hewlett-Packard Company 2001-2003
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
 *
 * This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/).
 *
 */
