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
import java.util.regex.Pattern ;

import org.apache.jena.query.ResultSet ;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.rowset.rw.RowSetReaderTSV;
import org.apache.jena.sparql.exec.RowSet;

/**
 * Input reader for Tab Separated Values format.
 * @deprecated To be removed
 */
@Deprecated
public class TSVInput {
    private static Pattern pattern = Pattern.compile("\t");
	/**
	 * Reads SPARQL Results from TSV format into a {@link ResultSet} instance
	 * @param in Input Stream
	 */
    public static RowSet fromTSV(InputStream in) {
        return RowSetReaderTSV.factory.create(ResultSetLang.RS_CSV).read(in, null);
    }

    /**
     * Reads SPARQL Boolean result from TSV
     * @param in Input Stream
     * @return boolean
     */
    public static boolean booleanFromTSV(InputStream in) {
        return RowSetReaderTSV.factory.create(ResultSetLang.RS_CSV).readAny(in, null).booleanResult();
    }
 }
