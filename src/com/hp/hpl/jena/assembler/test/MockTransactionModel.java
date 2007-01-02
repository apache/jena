/*
 (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 All rights reserved - see end of file.
 $Id: MockTransactionModel.java,v 1.5 2007-01-02 11:52:50 andy_seaborne Exp $
 */

package com.hp.hpl.jena.assembler.test;

import java.util.List;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.ModelAssembler;
import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;

final class MockTransactionModel extends ModelAssembler
    {
    private final List history;
    private final Model expected;
    private final boolean supports;
    private final boolean aborts;

    protected MockTransactionModel
        ( List history, Model expected, boolean supports, boolean aborts )
        {
        super();
        this.history = history;
        this.expected = expected;
        this.supports = supports;
        this.aborts = aborts;
        }

    protected Model openModel( Assembler a, Resource root, Mode irrelevant )
        {
        return new ModelCom( Factory.createDefaultGraph() ) 
            {
            public Model begin()
                {
                history.add( "begin" );
                TestModelContent.assertTrue( isEmpty() );
                return this;
                }

            public Model add( Model other )
                {
                history.add( "add" );
                if (aborts) throw new RuntimeException( "model aborts on add" );
                super.add( other );
                return this;
                }

            public Model abort()
                {
                history.add( "abort" );
                return this;
                }

            public Model commit()
                {
                TestModelContent.assertIsoModels( expected, this );
                history.add( "commit" );
                return this;
                }

            public boolean supportsTransactions()
                {
                history.add( "supports[" + supports + "]" );
                return supports;
                }
            };
        }
    }

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */