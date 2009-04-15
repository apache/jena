/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.iri.impl;

import java.util.Iterator;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.Violation;


/** Driver for the build process */
public class AFS_Build
{
    static public void main(String args[]) throws Exception
    {
        checkOne("http://123.18.56/foo") ;
        checkOne("http://123.18/foo") ;
        checkOne("http://123/foo") ;
        checkOne("http://123.18.56.19/foo") ;
        System.exit(0) ;

        // violation.xml ==> ViolationCodes
        BuildViolationCodes.main(args) ;
        
        // host.jflex
        PatternCompiler.main(args) ;
        // Other jflex files
        AbsLexer.main(args) ;
        
        // Now refresh and rebuild.
        // Need to edit result to remove "private" from yytext in each subparser
    }
    
    static void checkOne(String s)
    {
        IRI iri = IRIFactory.iriImplementation().create(s) ;
        System.out.println(">> "+iri) ;
        for ( Iterator<Violation> iter = iri.violations(true) ; iter.hasNext() ; )
        {
            Violation v = iter.next();
            System.out.println(v.getShortMessage()) ;
        }
        System.out.println("<< "+iri) ;
        System.out.println() ;
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
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