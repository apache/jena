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

package org.apache.jena.fuseki.servlets;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.core.DatasetDescription ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DynamicDatasets ;

public class SPARQL_QueryDataset extends SPARQL_Query
{
    public SPARQL_QueryDataset(boolean verbose)     { super() ; }

    public SPARQL_QueryDataset()
    { this(false) ; }
    
    @Override
    protected void validateRequest(HttpAction action) 
    { }

    @Override
    protected void validateQuery(HttpAction action, Query query) 
    { }
   
    @Override
    protected Dataset decideDataset(HttpAction action, Query query, String queryStringLog) 
    { 
        DatasetGraph dsg = action.getActiveDSG() ;
        
        // query.getDatasetDescription() ;
        
        // Protocol.
        DatasetDescription dsDesc = getDatasetDescription(action) ;
        if (dsDesc != null )
        {
            //errorBadRequest("SPARQL Query: Dataset description in the protocol request") ;
            dsg = DynamicDatasets.dynamicDataset(dsDesc, dsg, false) ;
        }
        
        return DatasetFactory.create(dsg) ;
    }
}
