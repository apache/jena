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

import java.io.InputStream ;
import java.io.Reader ;

import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.sparql.util.Context ;

public interface ResultSetReader {
    
    /**
     * Read from an {@code InputStream} and produce a {@linkplain ResultSet}.
     * Note that return result may stream and so the input stream be read
     * while the ResultSet is used.
     * See {@linkplain ResultSetFactory#copyResults(ResultSet)} for a ResultSet that is detached from the {@code InputStream}.
     * @param in InputStream to read from.
     * @param context
     * @return ResultSet
     */
    public ResultSet read(InputStream in, Context context) ;
    
    /**
     * Using {@link #read(InputStream, Context)} is preferred.
     * Read from an {@code Reader} and produce a {@linkplain ResultSet}.
     * Note that return result may stream and so the reader be read
     * while the ResultSet is used.
     * See {@linkplain ResultSetFactory#copyResults(ResultSet)} for a ResultSet that is detached from the {@code InputStream}.
     * @param in Reader
     * @param context
     * @return ResultSet
     */
    public ResultSet read(Reader in, Context context) ;
}

