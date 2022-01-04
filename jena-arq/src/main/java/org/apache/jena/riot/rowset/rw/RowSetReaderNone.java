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

import org.apache.jena.atlas.io.IO;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.rowset.RowSetReader;
import org.apache.jena.riot.rowset.RowSetReaderFactory;
import org.apache.jena.sparql.exec.QueryExecResult;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.util.Context;

public class RowSetReaderNone implements RowSetReader {

    public static RowSetReaderFactory factory = (Lang lang)->{
        if ( !Objects.equals(lang, ResultSetLang.RS_None) )
            throw new ResultSetException("Results reader None asked for a " + lang);
        return new RowSetReaderNone();
    };

    private RowSetReaderNone() {}

    @Override public RowSet read(InputStream in, Context context)       { IO.skipToEnd(in) ; return null; }

    @Override public RowSet read(Reader in, Context context)            { IO.skipToEnd(in); return null; }

    @Override public QueryExecResult readAny(InputStream in, Context context) { return null; }
}
