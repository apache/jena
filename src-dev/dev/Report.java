package dev ;

import java.net.MalformedURLException;

import junit.framework.JUnit4TestAdapter;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

public class Report
{
    static public junit.framework.Test suite()
    {
        return new JUnit4TestAdapter(Report.class) ;
    }
    
    @Test public void testcase() throws MalformedURLException
    {
       IRIFactory f = IRIFactory.iriImplementation() ;
       
       // See ResolvedRelativeIRI.mergePathsRemoveDots
       /*
        * if (relPath.startsWith("./")) {
                //return basePath + relPath.substring(2);
                relPath = relPath.substring(2); //fix by marcus
            }
        */
       
       IRI iri = f.construct("http://a/b/c/dddd;pppp?qqqqq") ;
       IRI iri2 = iri.resolve("./") ;
       test(iri2, "http://a/b/c/") ;
    }
    
    
    private static void test(IRI iri, String iriStr) throws MalformedURLException
    {
        Assert.assertEquals(iriStr, iri.toASCIIString()) ;
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