/**
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

package org.apache.jena.sparql.transaction;

import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.core.DatasetGraphWithLock ;
import org.junit.Ignore ;
import org.junit.Test ;

public class TestTransactionMem extends AbstractTestTransaction
{
    @Override
    protected Dataset create()
    { 
        DatasetGraph dsg = DatasetGraphFactory.createMem() ;
        DatasetGraphWithLock dsgl = new  DatasetGraphWithLock(dsg) ;
        return DatasetFactory.create(dsgl) ;
    }
    
    // Tests that don't make sense because they abort a write transaction.
    // (Using org.junit.Assume would be better?)
    @Test @Override @Ignore public void transaction_err_10() {} 
    @Test @Override @Ignore public void transaction_err_12() {} 
    @Test @Override @Ignore public void transaction_03() {} 
    @Test @Override @Ignore public void transaction_05() {} 
    @Test @Override @Ignore public void transaction_06() {} 
    
 }

