/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestVocabDB.java,v 1.3 2003-08-27 13:08:11 andy_seaborne Exp $
*/

package com.hp.hpl.jena.vocabulary.test;

import com.hp.hpl.jena.vocabulary.*;
import junit.framework.*;

/**
 	@author kers
*/
public class TestVocabDB extends VocabTestBase
    {
    public TestVocabDB(String name)
        { super(name); }

    public static TestSuite suite()
        { return new TestSuite( TestVocabDB.class ); }

    public void testXX()
        {
		String ns = "http://jena.hpl.hp.com/2003/04/DB#";
    /* */
        assertResource( ns + "SystemGraph", DB.systemGraphName );
        assertProperty( ns + "EngineType", DB.engineType );
        assertProperty( ns + "DriverVersion", DB.driverVersion );
        assertProperty( ns + "FormatDate", DB.formatDate );
        assertProperty( ns + "Graph", DB.graph );
        // assertProperty( ns + "MaxLiteral", DB.maxLiteral );
    /* */
        assertProperty( ns + "GraphName", DB.graphName );
        assertProperty( ns + "GraphType", DB.graphType );
        assertProperty( ns + "GraphLSet", DB.graphLSet );
        assertProperty( ns + "GraphPrefix", DB.graphPrefix );
        assertProperty( ns + "GraphId", DB.graphId );
        assertProperty( ns + "GraphDBSchema", DB.graphDBSchema );
        assertProperty( ns + "StmtTable", DB.stmtTable );
        assertProperty( ns + "ReifTable", DB.reifTable );
    /* */
        assertProperty( ns + "PrefixValue", DB.prefixValue );
        assertProperty( ns + "PrefixURI", DB.prefixURI );
    /* */
        assertProperty( ns + "LSetName", DB.lSetName );
        assertProperty( ns + "LSetType", DB.lSetType );
        assertProperty( ns + "LSetPSet", DB.lSetPSet );
    /* */
        assertProperty( ns + "PSetName", DB.pSetName );
        assertProperty( ns + "PSetType", DB.pSetType );
        assertProperty( ns + "PSetTable", DB.pSetTable );
    /* */
        assertResource( ns + "undefined", DB.undefined );
        }
    }


/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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
