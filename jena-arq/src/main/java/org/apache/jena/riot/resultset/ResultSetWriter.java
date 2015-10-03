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

package org.apache.jena.riot.resultset;

import java.io.OutputStream ;
import java.io.Writer ;

import org.apache.jena.query.ResultSet ;
import org.apache.jena.sparql.util.Context ;

public interface ResultSetWriter {
    /** Write the ResultSet to the OutputStream */
    public void write(OutputStream out, ResultSet resultSet, Context context) ;
    
    /**
     * Using {@link #write(OutputStream, ResultSet, Context)} is preferred. 
     * Write the ResultSet to the Writer
     */
    public void write(Writer out, ResultSet resultSet, Context context) ;
    
    /**
     * Write a boolean result to the output stream
     */
    public void write(OutputStream out, boolean result, Context context);
}

