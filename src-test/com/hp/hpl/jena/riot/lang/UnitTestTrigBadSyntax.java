/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.lang;

import java.io.InputStream ;

import junit.framework.TestCase ;
import atlas.io.IO ;
import atlas.lib.SinkNull ;

import com.hp.hpl.jena.riot.Lang ;
import com.hp.hpl.jena.riot.RiotException ;
import com.hp.hpl.jena.sparql.core.Quad ;


public class UnitTestTrigBadSyntax extends TestCase
{
    String uri ;
    public UnitTestTrigBadSyntax(String name, String uri) { super(name) ; this.uri = uri ; }
    
    @Override
    public void runTest()
    {
        InputStream in = IO.openFile(uri) ;
        assertNotNull(in) ;
        LangRIOT parser = Lang.createParserTriG(uri, in, new SinkNull<Quad>()) ;
        try {
            parser.parse() ;
        } catch (RiotException ex) { return ; }
        fail("Bad syntax trig test succeed in parsing the file") ;
    }
}

/*
 * (c) Copyright 2010 Talis Information Ltd.
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