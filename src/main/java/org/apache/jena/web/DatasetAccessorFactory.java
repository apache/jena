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

package org.apache.jena.web;

import org.apache.jena.web.impl.DatasetAdapter ;
import org.apache.jena.web.impl.DatasetGraphAccessor ;
import org.apache.jena.web.impl.DatasetGraphAccessorBasic ;
import org.apache.jena.web.impl.DatasetGraphAccessorHTTP ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;

public class DatasetAccessorFactory
{
    public static DatasetAccessor createHTTP(String serviceURI)
    {
        return adapt(new DatasetGraphAccessorHTTP(serviceURI)) ;
    }

    public static DatasetAccessor create(DatasetGraph dataset)
    {
        return adapt(new DatasetGraphAccessorBasic(dataset)) ;
    }
    
    public static DatasetAccessor create(Dataset dataset)
    {
        return adapt(new DatasetGraphAccessorBasic(dataset.asDatasetGraph())) ;
    }

    public static DatasetGraphAccessor make(DatasetGraph dataset)
    {
        return new DatasetGraphAccessorBasic(dataset) ;
    }
    
    private static DatasetAccessor adapt(DatasetGraphAccessor dgu)
    {
        return new DatasetAdapter(dgu) ;
    }
    
}
