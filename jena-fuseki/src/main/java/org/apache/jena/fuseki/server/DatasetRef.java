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

package org.apache.jena.fuseki.server;

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;

public class DatasetRef
{
    public String name                          = null ;
    
    public List<String> queryEP                 = new ArrayList<String>() ;
    public List<String> updateEP                = new ArrayList<String>() ;
    public List<String> uploadEP                = new ArrayList<String>() ;
    public List<String> readGraphStoreEP        = new ArrayList<String>() ;
    public List<String> readWriteGraphStoreEP   = new ArrayList<String>() ;
    public DatasetGraph dataset                 = null ;

    //TODO Need to be able to set this from the config file.  
    public boolean allowDatasetUpdate              = false;
    
    public boolean allowTimeoutOverride         = false;
    public long maximumTimeoutOverride          = Long.MAX_VALUE;
    
    public boolean isReadOnly()
    {
        return updateEP.size() == 0 && 
               uploadEP.size() == 0 &&
               readWriteGraphStoreEP.size() == 0 &&
               !allowDatasetUpdate ;
    }
    
}
