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

package com.hp.hpl.jena.query;

import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.web.DatasetAdapter ;
import org.apache.jena.web.DatasetGraphAccessor ;
import org.apache.jena.web.DatasetGraphAccessorBasic ;
import org.apache.jena.web.DatasetGraphAccessorHTTP ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;

/**
 * Factory which produces dataset accessors
 *
 */
public class DatasetAccessorFactory
{
    /**
     * Create an accessor for a remote HTTP service
     * @param serviceURI Service URI
     * @return Accessor
     */
    public static DatasetAccessor createHTTP(String serviceURI)
    {
        return adapt(new DatasetGraphAccessorHTTP(serviceURI)) ;
    }
    
    /**
     * Create an accessor for a remote HTTP service that requires authentication
     * @param serviceURI Service URI
     * @param authenticator HTTP authenticator
     * @return Accessor
     */
    public static DatasetAccessor createHTTP(String serviceURI, HttpAuthenticator authenticator)
    {
        return adapt(new DatasetGraphAccessorHTTP(serviceURI, authenticator));
    }

    /**
     * Create an accessor for a local dataset
     * @param dataset Dataset
     * @return Accessor
     */
    public static DatasetAccessor create(DatasetGraph dataset)
    {
        return adapt(new DatasetGraphAccessorBasic(dataset)) ;
    }
    
    /**
     * Create an accessor for a local dataset
     * @param dataset Dataset
     * @return Accessor
     */
    public static DatasetAccessor create(Dataset dataset)
    {
        return adapt(new DatasetGraphAccessorBasic(dataset.asDatasetGraph())) ;
    }

    /**
     * Makes an graph level accessor over a local dataset
     * @param dataset Dataset
     * @return Accessor
     */
    public static DatasetGraphAccessor make(DatasetGraph dataset)
    {
        return new DatasetGraphAccessorBasic(dataset) ;
    }
    
    private static DatasetAccessor adapt(DatasetGraphAccessor dgu)
    {
        return new DatasetAdapter(dgu) ;
    }
    
}
