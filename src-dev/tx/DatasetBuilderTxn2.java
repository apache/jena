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

package tx;

import java.util.Properties ;

import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;

import setup.DatasetBuilder ;

/** Yet another dataset builder.
 *  This one builds the DatasetGraph and the transactional DatasetGraph toegther.
 *  It heavily assumes that there is one active transaction
 *  It does currently assume that a transaction   
 */
public class DatasetBuilderTxn2 implements DatasetBuilder
{
    private static Object builderLock = new Object() ;
    
    @Override
    public DatasetGraphTDB build(Location location, Properties config)
    {
        synchronized(builderLock)
        {
            return _build(location, config) ;
        }
    }

    private DatasetGraphTDB _build(Location location, Properties config)
    {
        return null ;
    }

}

