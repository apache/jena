/**
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

package org.seaborne.tdb2.junit;

import org.apache.jena.atlas.lib.NotImplemented ;
import org.seaborne.dboe.base.file.FileSet ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.base.record.RecordFactory ;
import org.seaborne.dboe.index.IndexParams ;
import org.seaborne.dboe.index.RangeIndex ;
import org.seaborne.tdb2.store.DatasetPrefixesTDB ;
import org.seaborne.tdb2.store.nodetable.NodeTable ;
import org.seaborne.tdb2.sys.DatasetControl ;

/** Build things for non-transactional tests */
public class BuildTestLib {

    public static RangeIndex buildRangeIndex(FileSet mem, RecordFactory factory, IndexParams indexParams) {
        throw new NotImplemented() ;
    }

    public static NodeTable makeNodeTable(Location mem, String baseName, 
                                          int cacheNodeId2NodeSize, 
                                          int cacheNode2NodeIdSize,
                                          int cacheMissSize) {
        throw new NotImplemented() ;
    }

    public static NodeTable makeNodeTable(Location mem) {
        throw new NotImplemented() ;
    }

    public static DatasetPrefixesTDB makePrefixes(Location location, DatasetControl policy) {
        throw new NotImplemented() ;
    }

}

