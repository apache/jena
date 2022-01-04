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

import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.ResultSetReader;
import org.apache.jena.riot.resultset.ResultSetReaderFactory;
import org.apache.jena.riot.rowset.rw.RowSetReaderXML;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.resultset.SPARQLResult;
import org.apache.jena.sparql.util.Context;

/** @deprecated Use {@link RowSetReaderXML} */
@Deprecated
public class ResultSetReaderXML implements ResultSetReader {

    public static final ResultSetReaderFactory factory = lang -> {
        if (!Objects.equals(lang, ResultSetLang.RS_XML ) )
            throw new ResultSetException("ResultSet for XML asked for a "+lang);
        return new ResultSetReaderXML();
    };

    private ResultSetReaderXML() {}

    @Override
    public SPARQLResult readAny(InputStream in, Context context) {
        SPARQLResult result = ResultsStAX.read(in, null, context);
        return result;
    }

    @Override
    public ResultSet read(Reader in, Context context) {
        SPARQLResult result = ResultsStAX.read(in, null, context);
        return result.getResultSet();
    }
}
