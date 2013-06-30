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

package com.hp.hpl.jena.tdb.graph;

import org.apache.jena.web.DatasetGraphAccessor ;
import org.apache.jena.web.AbstractTestDatasetGraphAccessor ;

import com.hp.hpl.jena.query.DatasetAccessorFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.tdb.TDBFactory ;

public class TestDatasetGraphAccessorTDB extends AbstractTestDatasetGraphAccessor
{
    @Override
    protected DatasetGraphAccessor getDatasetUpdater()
    {
        DatasetGraph dsg = TDBFactory.createDatasetGraph() ;
        return DatasetAccessorFactory.make(dsg) ;
    }
}
