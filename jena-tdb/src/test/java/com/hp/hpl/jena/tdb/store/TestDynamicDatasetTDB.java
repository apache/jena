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

package com.hp.hpl.jena.tdb.store;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sparql.core.AbstractTestDynamicDataset ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;

public class TestDynamicDatasetTDB extends AbstractTestDynamicDataset
{
    @Override
    protected Dataset createDataset()
    {
        return TDBFactory.createDataset() ;
    }
    
    protected void startDynamicAndUnionTest()    { TDB.getContext().setTrue(TDB.symUnionDefaultGraph) ; }

    protected void finishDynamicAndUnionTest()   { TDB.getContext().unset(TDB.symUnionDefaultGraph) ; }
}

