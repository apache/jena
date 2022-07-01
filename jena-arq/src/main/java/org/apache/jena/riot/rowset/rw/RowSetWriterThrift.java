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

import java.io.OutputStream;
import java.io.Writer;
import java.util.Objects;

import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.rowset.RowSetWriter;
import org.apache.jena.riot.rowset.RowSetWriterFactory;
import org.apache.jena.riot.thrift.ThriftRDF;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.util.Context;

public class RowSetWriterThrift implements RowSetWriter {

    public static RowSetWriterFactory factory = lang -> {
        if (!Objects.equals(lang, ResultSetLang.RS_Thrift ) )
            throw new ResultSetException("RowSetWriter for RDF/Thrift asked for a "+lang);
        return new RowSetWriterThrift();
    };

    @Override
    public void write(OutputStream out, RowSet rowSet, Context context)
    { ThriftRDF.writeRowSet(out, rowSet) ; }

    @Override
    public void write(Writer out, RowSet resultSet, Context context) {
        throw new NotImplemented("Writing binary data to a java.io.Writer is not possible") ;
    }

    @Override
    public void write(OutputStream out, boolean result, Context context)
    { throw new NotImplemented("No Thrift RDF encoding defined for boolean results"); }
}
