/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestModelRDB.java,v 1.1 2004-06-18 10:20:45 chris-dollin Exp $
*/
package com.hp.hpl.jena.db.test;

import junit.framework.TestSuite;

import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.*;

/**
 TestModelRDB
 @author kers
 */
public class TestModelRDB extends AbstractTestModel
    {
    public TestModelRDB( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestModelRDB.class ); }

    private IDBConnection con;
    private int count = 0;
    
    public void setUp()
        { con = TestConnection.makeAndCleanTestConnection();
        super.setUp(); }
    
    public Model getModel()
        {
        return ModelRDB.createModel( con, "db-test-model-" + ++count );
        }
    }


/*
(c) Copyright 2004, Hewlett-Packard Development Company, LP
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