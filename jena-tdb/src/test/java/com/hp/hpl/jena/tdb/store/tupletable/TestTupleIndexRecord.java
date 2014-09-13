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

package com.hp.hpl.jena.tdb.store.tupletable;


import org.apache.jena.atlas.lib.ColumnMap ;

import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.AbstractTestTupleIndex ;
import com.hp.hpl.jena.tdb.index.IndexFactory ;
import com.hp.hpl.jena.tdb.index.IndexParams ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.setup.StoreParams ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class TestTupleIndexRecord extends AbstractTestTupleIndex
{
    static RecordFactory factory = new RecordFactory(3*SystemTDB.SizeOfNodeId, 0) ;
    
    @Override
    protected TupleIndexRecord create(String description)
    {
        IndexParams indexParams = StoreParams.getDftStoreParams() ; 
        RangeIndex rIdx = IndexFactory.buildRangeIndex(FileSet.mem(), factory, indexParams) ;
        ColumnMap cmap = new ColumnMap("SPO", description) ;
        TupleIndexRecord index = new TupleIndexRecord(3, cmap, description, factory, rIdx) ;
        return index ;
    }
}
