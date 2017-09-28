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

package org.seaborne.tdb2.store.tupletable;

import org.apache.jena.atlas.lib.tuple.TupleMap ;
import org.seaborne.dboe.base.file.FileSet ;
import org.seaborne.dboe.base.record.RecordFactory ;
import org.seaborne.dboe.index.IndexParams ;
import org.seaborne.dboe.index.RangeIndex ;
import org.seaborne.tdb2.junit.BuildTestLib ;
import org.seaborne.tdb2.setup.StoreParams ;
import org.seaborne.tdb2.sys.SystemTDB ;

public class TestTupleIndexRecord extends AbstractTestTupleIndex
{
    static RecordFactory factory = new RecordFactory(3*SystemTDB.SizeOfNodeId, 0) ;
    
    @Override
    protected TupleIndexRecord create(String description)
    {
        IndexParams indexParams = StoreParams.getDftStoreParams() ; 
        RangeIndex rIdx = BuildTestLib.buildRangeIndex(FileSet.mem(), factory, indexParams) ;
        TupleMap tmap = TupleMap.create("SPO", description) ;
        TupleIndexRecord index = new TupleIndexRecord(3, tmap, description, factory, rIdx) ;
        return index ;
    }
}
