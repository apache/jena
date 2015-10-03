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

package org.apache.jena.sdb.store;

import org.apache.jena.sdb.core.sqlnode.SqlNode ;
import org.apache.jena.sdb.sql.ResultSetJDBC ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;

/** Convert from whatever results a particular layout returns into
 *  an ARQ QueryIterator of Bindings.  An SQLBridge object
 *  is allocated for each SQL query execution. 
 */  

public interface SQLBridge
{
    /** Actually build the bridge */
    public void build() ;
    
    /** Get the (possibly altered) SqlNode */
    public SqlNode getSqlNode() ;
    
    /** Process a JDBC result set */
    public QueryIterator assembleResults(ResultSetJDBC jdbcResultSet, 
                                         Binding binding,
                                         ExecutionContext execCtl) ;
}
