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

import java.io.InputStream;
import java.io.Reader;
import java.util.Objects;

import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.ResultSetReader;
import org.apache.jena.riot.resultset.ResultSetReaderFactory;
import org.apache.jena.riot.thrift.BinRDF;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.resultset.SPARQLResult;
import org.apache.jena.sparql.util.Context;

public class ResultSetReaderThrift implements ResultSetReader {
    
    public static ResultSetReaderFactory factory = lang->{
        if (!Objects.equals(lang, ResultSetLang.RS_Thrift ) )
            throw new ResultSetException("ResultSetReadernot  for Thrift asked for a "+lang); 
        return new ResultSetReaderThrift(); 
    };
    
    private ResultSetReaderThrift() {}
    
    @Override
    public ResultSet read(InputStream in, Context context) {
        return BinRDF.readResultSet(in);
    }

    @Override
    public ResultSet read(Reader in, Context context) {
        throw new NotImplemented("Reading binary data from a java.io.Reader is not possible");
    }

    @Override
    public SPARQLResult readAny(InputStream in, Context context) {
        return new SPARQLResult(read(in, context));
    }

}
