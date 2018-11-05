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

package org.apache.jena.sparql.core;

import java.util.Arrays ;
import java.util.Collection ;

import org.apache.jena.atlas.lib.Creator ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;

/** <b>This class is not in the test suite (it tests for currently unavailable features)</b>.
 * TDB does support this feature and uses AbstractTestDynamicDatabase
 */
@RunWith(Parameterized.class)
public class TestDynamicDatasetMem extends AbstractTestDynamicDataset
{
    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        Creator<Dataset> datasetGeneralMaker = ()-> DatasetFactory.createGeneral() ; 
        Creator<Dataset> datasetTxnMemMaker = ()-> DatasetFactory.createTxnMem() ;
        return Arrays.asList(new Object[][] {
            { "General",  datasetGeneralMaker },
            { "TxnMem",   datasetTxnMemMaker} });
    }   

    private final Creator<Dataset> maker;

    public TestDynamicDatasetMem(String name, Creator<Dataset> maker) {
        this.maker = maker ;
    }
    
    @Override
    protected Dataset createDataset()
    {
        return maker.create() ;
    }
    
    @Override
    protected void releaseDataset(Dataset ds) {}

}

