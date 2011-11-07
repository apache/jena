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

package com.hp.hpl.jena.update;

import com.hp.hpl.jena.query.QuerySolution ;

/** An instance of a execution of an UpdateRequest */ 
public interface UpdateProcessor
{
//    /** Set the FileManger that might be used to load files.
//     *  May not be supported by all implementations.  
//     */
//    public void setFileManager(FileManager fm) ;
    
    /** Set the initial association of variables and values.
     * May not be supported by all UpdateProcessor implementations.
     * @param binding
     */
    public void setInitialBinding(QuerySolution binding) ;

    /**
     * The dataset against which the query will execute.
     * May be null, implying it is expected that the query itself
     * has a dataset description. 
     */
    public GraphStore getGraphStore() ;
    
    /** Execute */
    public void execute() ;
}
