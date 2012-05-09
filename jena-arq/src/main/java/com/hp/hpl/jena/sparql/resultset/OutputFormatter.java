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

package com.hp.hpl.jena.sparql.resultset;

import java.io.OutputStream ;

import com.hp.hpl.jena.query.ResultSet ;

/** 
 * Interface for all formatters of result sets. */

public interface OutputFormatter
{
    /** Format a result set - output on the given stream
     * @param out
     * @param resultSet
     */
    
    public void format(OutputStream out, ResultSet resultSet) ;

    /** Format a boolean result - output on the given stream
     * @param out
     * @param booleanResult
     */
    
    public void format(OutputStream out, boolean booleanResult) ;
    
    /** Turn into a string */
    public String asString(ResultSet resultSet) ;
}
