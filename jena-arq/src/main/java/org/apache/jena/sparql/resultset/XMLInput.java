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

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.resultset.rw.ResultsStAX;
import org.apache.jena.sparql.SystemARQ;

/**
 * Code that reads an XML Result Set and builds the ARQ structure for the same.
 * 
 * @deprecated Use {@code ResultSetMgr.read}
 */
@Deprecated
public class XMLInput {
    public static ResultSet fromXML(InputStream in) {
        return fromXML(in, null);
    }

    public static ResultSet fromXML(InputStream in, Model model) {
        return make(in, model).getResultSet();
    }

    public static ResultSet fromXML(Reader in) {
        return fromXML(in, null);
    }

    public static ResultSet fromXML(Reader in, Model model) {
        return make(in, model).getResultSet();
    }

    public static ResultSet fromXML(String str) {
        return fromXML(str, null);
    }

    public static ResultSet fromXML(String str, Model model) {
        return make(str, model).getResultSet();
    }

    public static boolean booleanFromXML(InputStream in) {
        return make(in, null).getBooleanResult();
    }

    public static boolean booleanFromXML(String str) {
        return make(str, null).getBooleanResult();
    }

    // Low level operations

    public static SPARQLResult make(InputStream in) {
        return make(in, null);
    }

    public static SPARQLResult make(InputStream in, Model model) {
        if ( SystemARQ.UseSAX )
            return new XMLInputSAX(in, model);
        return ResultsStAX.read(in, model, null);
    }

    public static SPARQLResult make(Reader in) {
        return make(in, null);
    }

    public static SPARQLResult make(Reader in, Model model) {
        if ( SystemARQ.UseSAX )
            return new XMLInputSAX(in, model);
        return ResultsStAX.read(in, model, null);
    }

    public static SPARQLResult make(String str) {
        return make(str, null);
    }

    public static SPARQLResult make(String str, Model model) {
        if ( SystemARQ.UseSAX )
            return new XMLInputSAX(str, model);
        return ResultsStAX.read(new StringReader(str), model, null);
    }

}
