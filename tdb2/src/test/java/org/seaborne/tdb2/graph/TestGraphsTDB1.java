/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.tdb2.graph;

import org.apache.jena.query.Dataset ;
import org.apache.jena.query.ReadWrite ;
import org.junit.After ;
import org.junit.Before ;
import org.seaborne.tdb2.junit.TL ;

public class TestGraphsTDB1 extends AbstractTestGraphsTDB
{
    Dataset ds = TL.createTestDatasetMem();
    @Before public void before() {
        ds.begin(ReadWrite.WRITE);
    }
    
    @After public void after() {
        ds.abort();
        ds.end();
        TL.expel(ds);
    }
    @Override
    protected Dataset createDataset() {
        return ds ;
    }
}
