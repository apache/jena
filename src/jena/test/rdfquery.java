/*
(c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
[See end of file]
$Id: rdfquery.java,v 1.1 2005-04-06 08:41:36 chris-dollin Exp $
*/

package jena.test;

import junit.framework.TestSuite;

import com.hp.hpl.jena.rdql.test.QueryTestProgrammatic;
import com.hp.hpl.jena.rdql.test.QueryTestScripts;
import com.hp.hpl.jena.rdql.test.TestExpressions;
import com.hp.hpl.jena.shared.Command;

public class rdfquery implements Command
    {
    protected String testFile;
    
    public rdfquery( String testFile )
        { this.testFile = testFile; }
    
    public Object execute()
        { 
        if (testFile.equals( "-all" ))
            { TestSuite ts = new TestSuite("RDQL") ;
            ts.addTest(TestExpressions.suite()) ;
            //ts.addTest(QueryTestScripts.suite()) ;
            ts.addTest(QueryTestProgrammatic.suite()) ;
            junit.textui.TestRunner.run(ts) ; }
        else
            { TestSuite suite = new QueryTestScripts( testFile ) ;
            junit.textui.TestRunner.run(suite) ; }
        return null;
        }
    }

/*
(c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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

