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

package org.apache.jena.riot.rowset.rw;

import java.io.InputStream;
import java.io.Reader;
import java.util.Objects;

import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.rowset.RowSetReader;
import org.apache.jena.riot.rowset.RowSetReaderFactory;
import org.apache.jena.riot.thrift.ThriftRDF;
import org.apache.jena.sparql.exec.QueryExecResult;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.util.Context;

public class RowSetReaderThrift implements RowSetReader {

    public static RowSetReaderFactory factory = lang->{
        if (!Objects.equals(lang, ResultSetLang.RS_Thrift ) )
            throw new ResultSetException("RowSetReaderThrift for Thrift asked for a "+lang);
        return new RowSetReaderThrift();
    };

    private RowSetReaderThrift() {}

    @Override
    public RowSet read(InputStream in, Context context) {
        return ThriftRDF.readRowSet(in);
    }

    @Override
    public RowSet read(Reader in, Context context) {
        throw new NotImplemented("Reading binary data from a java.io.Reader is not possible");
    }

    @Override
    public QueryExecResult readAny(InputStream in, Context context) {
        return new QueryExecResult(read(in, context));
    }
}
