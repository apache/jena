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

package com.hp.hpl.jena.tdb.setup;

import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;

public class TDBBuilder
{
    // TODO Testing (inc variations)
    // TODO Directory properties initial config.
    // TODO Separate out static for general creation purposes (a super factory)
    // TODO setting union graph?  Careful - updates!
    // TODO Set direct/mapped modes 
    
    private static DatasetBuilderStd singleton ;
    static 
    {   
        singleton = new DatasetBuilderStd() ;
        singleton.setStd() ;
    }
    
    public static DatasetGraphTDB build(Location location)
    {
        return singleton.build(location, null) ;  
    }
}
