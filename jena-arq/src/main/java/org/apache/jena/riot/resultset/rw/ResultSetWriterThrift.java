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
import java.io.Writer;
import java.util.Objects;

import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.ResultSetWriter;
import org.apache.jena.riot.resultset.ResultSetWriterFactory;
import org.apache.jena.riot.thrift.ThriftRDF;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.util.Context;

public class ResultSetWriterThrift implements ResultSetWriter {

    public static ResultSetWriterFactory factory = lang -> {
        if (!Objects.equals(lang, ResultSetLang.RS_Thrift ) )
            throw new ResultSetException("ResultSetWriter for RDF/Thift asked for a "+lang);
        return new ResultSetWriterThrift();
    };

    @Override
    public void write(OutputStream out, ResultSet resultSet, Context context)
    { ThriftRDF.writeRowSet(out, RowSet.adapt(resultSet)) ; }

    @Override
    public void write(Writer out, ResultSet resultSet, Context context) {
        throw new NotImplemented("Writing binary data to a java.io.Writer is not possible") ;
    }

    @Override
    public void write(OutputStream out, boolean result, Context context)
    { throw new NotImplemented("No Thrift RDF encoding defined for boolean results"); }
}
