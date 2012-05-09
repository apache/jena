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

package com.hp.hpl.jena.sparql.mgt;

/** Overall statistics from a query engine - one such per type of engine. */
public interface QueryEngineInfoMBean
{
    /** Number of queries executed */
    long getQueryCount() ; 
    
    /** Last query seen, as a string */
    String getLastQueryString() ;
    
    /** Last algebra expression seen, as a string */
    String getLastAlgebra() ;

    /** Point in time when last query seen */
    String getLastQueryExecAt() ;

//    /** Length of elapsed time (in microseconds) for the last query : -1 for unknown */  
//    long getLastQueryExecTime() ;
}
