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

package org.apache.jena.sparql.resultset;

import java.io.InputStream ;

import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.rowset.rw.RowSetReaderCSV;
import org.apache.jena.sparql.exec.RowSet;

/** Convenient comma separated values - see also TSV (tab separated values)
 *  which outputs full RDF terms (in Turtle-style).
 *
 *  The CSV format supported is:
 *  <ul>
 *  <li>First row is variable names without '?'</li>
 *  <li>Strings, quoted if necessary and numbers output only.
 *  No language tags, or datatypes.
 *  URIs are send without $lt;&gt;
 *  </li>
 *  CSV is RFC 4180, but there are many variations.
 *  </ul>
 *  This code reads the file and treats everything as strings.
 *  <p>
 *  The code also allows for parsing boolean results where we expect the header to be a single string
 *  from the set: true yes false no
 *  </p>
 *  <p>
 *  Any other value is considered an error for parsing a boolean results and anything past the first line is ignored
 *  </p>
 *  @deprecated To be removed
 */
@Deprecated
public class CSVInput
{
    // This code exists to support the SPARQL WG tests.
    public static RowSet fromCSV(InputStream in) {
        return RowSetReaderCSV.factory.create(ResultSetLang.RS_CSV).read(in, null);
    }
}
