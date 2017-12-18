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

package org.apache.jena.riot.resultset.rw;

import java.io.OutputStream;

import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.resultset.SPARQLResult;

public class ResultsMgrX {
    // For org.apache.jena.riotResultSetMgr
    public static ResultSet readResults(String url) {
        ResultSet rs = read(url).getResultSet();
        if ( rs == null )
            throw new ResultSetException("Not a result set"); 
        return rs;
    }

    public static boolean readBoolean(String url) {
        Boolean b = read(url).getBooleanResult();
        if ( b == null )
            throw new ResultSetException("Not a boolean result"); 
        return b;
    }
    
    private static SPARQLResult read(String url) {
        return null;
//        ResultsReader.create()
//            //.forceLang(lang)
//            //.context(context)
//            .build()
//            .read(url);
    }
    
    public static void write(OutputStream output, ResultSet resultSet) {
        ResultsWriter.create()
            .build()
            .write(output, resultSet);
    }

    public static void write(OutputStream output, Boolean booleanResult) {
    }

    public static void write(String filename, ResultSet resultSet) {
        ResultsWriter.create()
            .build()
            .write(filename, resultSet);
    }

    public static void write(String filename, Boolean booleanResult) {
    }
}
