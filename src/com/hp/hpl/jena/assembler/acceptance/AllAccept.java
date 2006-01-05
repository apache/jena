/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: AllAccept.java,v 1.1 2006-01-05 13:40:01 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.acceptance;

import java.io.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.test.AssemblerTestBase;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.util.FileUtils;

public class AllAccept extends AssemblerTestBase
    {
    public AllAccept( String name )
        { super( name ); }

    protected Class getAssemblerClass()
        { return null; }
    
    public void testUnadornedInferenceModel()
        {
        Resource root = resourceInModel( "x ja:reasoner R; R rdf:type ja:ReasonerFactory" );
        Model m = Assembler.general.createModel( root );
        assertInstanceOf( InfModel.class, m );
        InfModel inf = (InfModel) m;
        assertIsoModels( empty, inf.getRawModel() );
        assertInstanceOf( GenericRuleReasoner.class, inf.getReasoner() );
        }
    
    public void testWithContent() throws IOException
        {
        File f = FileUtils.tempFileName( "assembler-acceptance-", ".n3" );
        Model data = model( "a P b; b Q c" );
        FileOutputStream fs = new FileOutputStream( f );
        data.write( fs, "N3" );
        fs.close();
        Resource root = resourceInModel( "x rdf:type ja:MemoryModel; x ja:content y; y ja:externalContent file:" + f.getAbsolutePath() );
        Model m = Assembler.general.createModel( root );
        assertIsoModels( data, m );
        }

    }


/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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