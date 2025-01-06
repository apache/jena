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

package org.apache.jena.sparql.algebra;

import java.util.List ;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.algebra.table.Table1 ;
import org.apache.jena.sparql.algebra.table.TableBuilder;
import org.apache.jena.sparql.algebra.table.TableEmpty ;
import org.apache.jena.sparql.algebra.table.TableN ;
import org.apache.jena.sparql.algebra.table.TableUnit ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.exec.RowSet ;

public class TableFactory
{
    public static Table createUnit()
    { return new TableUnit() ; }

    public static Table createEmpty()
    { return new TableEmpty() ; }

    public static Table create()
    { return new TableN() ; }

    public static Table create(List<Var> vars)
    { return new TableN(vars) ; }

    public static Table create(QueryIterator queryIterator)
    {
        if ( queryIterator.isJoinIdentity() ) {
            queryIterator.close();
            return createUnit() ;
        }

        return builder().consumeRowsAndVars(queryIterator).build();
    }

    public static Table create(Var var, Node value)
    { return new Table1(var, value) ; }

    /** Creates a table from the detached bindings of the row set. */
    public static Table create(RowSet rs)
    {
        TableBuilder builder = builder();
        builder.addVars(rs.getResultVars());
        rs.forEach(row -> {
            Binding b = row.detach();
            builder.addRow(b);
        });
        return builder.build();
    }

    public static TableBuilder builder() {
        return new TableBuilder();
    }
}
