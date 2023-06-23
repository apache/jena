/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.assembler.test;

import java.util.List;

import org.apache.jena.assembler.* ;
import org.apache.jena.assembler.assemblers.ModelAssembler ;
import org.apache.jena.graph.GraphMemFactory ;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.rdf.model.impl.ModelCom ;
import org.apache.jena.rdf.model.test.ModelTestBase ;
import org.junit.Assert;

/**
    A model assembler that creates a model with controllable supporting of
    transactions and aborting on adding statements; the model logs transactions
    and adding-of-models. For testing only.
*/
final class MockTransactionModel extends ModelAssembler
    {
    private final List<String> history;
    private final Model expected;
    private final boolean supportsTransactions;
    private final boolean abortsOnAdd;

    protected MockTransactionModel
        ( List<String> history, Model expected, boolean supportsTransactions, boolean abortsOnAdd )
        {
        super();
        this.history = history;
        this.expected = expected;
        this.supportsTransactions = supportsTransactions;
        this.abortsOnAdd = abortsOnAdd;
        }

    @Override
    protected Model openEmptyModel( Assembler a, Resource root, Mode irrelevant )
        {
        return new ModelCom( GraphMemFactory.createDefaultGraph() ) 
            {
            @Override
            public Model begin()
                {
                history.add( "begin" );
                Assert.assertTrue( isEmpty() );
                return this;
                }

            @Override
            public Model add( Model other )
                {
                history.add( "add" );
                if (abortsOnAdd) throw new RuntimeException( "model aborts on add of " + other );
                super.add( other );
                return this;
                }

            @Override
            public Model abort()
                {
                history.add( "abort" );
                return this;
                }

            @Override
            public Model commit()
                {
                ModelTestBase.assertIsoModels( expected, this );
                history.add( "commit" );
                return this;
                }

            @Override
            public boolean supportsTransactions()
                {
                history.add( "supports[" + supportsTransactions + "]" );
                return supportsTransactions;
                }
            };
        }
    }
