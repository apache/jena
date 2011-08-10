/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;
import org.openjena.riot.lang.TestIRI ;
import org.openjena.riot.lang.TestLang ;
import org.openjena.riot.lang.TestLangNQuads ;
import org.openjena.riot.lang.TestLangNTriples ;
import org.openjena.riot.lang.TestLangTrig ;
import org.openjena.riot.lang.TestLangTurtle ;
import org.openjena.riot.lang.TestNodeAllocator ;
import org.openjena.riot.lang.TestParserFactory ;
import org.openjena.riot.lang.TestSuiteTrig ;
import org.openjena.riot.lang.TestSuiteTurtle ;
import org.openjena.riot.lang.TestTurtleTerms ;
import org.openjena.riot.out.TestNodeFmtLib ;
import org.openjena.riot.out.TestOutput ;
import org.openjena.riot.out.TestOutputNTriples ;
import org.openjena.riot.pipeline.TestNormalization ;
import org.openjena.riot.tokens.TestTokenForNode ;
import org.openjena.riot.tokens.TestTokenizer ;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
      TestTokenizer.class
    , TestTokenForNode.class
    , TestPrefixMap.class
    , TestIRI.class
    , TestChecker.class
    , TestLang.class
    , TestNodeAllocator.class
    
    , TestTurtleTerms.class

    , TestLangNTriples.class
    , TestLangNQuads.class

    , TestLangTurtle.class
    , TestSuiteTurtle.class

    , TestLangTrig.class
    , TestSuiteTrig.class

    , TestParserFactory.class

    , TestNodeFmtLib.class
    , TestOutput.class
    , TestOutputNTriples.class
    , TestLangTag.class
    , TestNormalization.class
})

public class TS_Riot
{
    @BeforeClass public static void beforeClass()
    { 
        ErrorHandlerFactory.setTestLogging(false) ;
    }

    @AfterClass public static void afterClass()
    { 
        ErrorHandlerFactory.setTestLogging(true) ;
    }

}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
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