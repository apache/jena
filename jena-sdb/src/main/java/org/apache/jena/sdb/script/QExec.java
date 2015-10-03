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

package org.apache.jena.sdb.script;

import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryExecution ;
import org.apache.jena.sparql.resultset.ResultsFormat ;
import org.apache.jena.sparql.util.QueryExecUtils ;

public class QExec
{
    private Query query ;
    private QueryExecution exec ;
    private ResultsFormat format ;

    public QExec(Query query, QueryExecution exec, ResultsFormat format)
    {
        this.query = query ;
        this.exec = exec ;
        this.format = format ;
    }
    
    public void exec()
    {
        QueryExecUtils.executeQuery(query, exec, format) ;
    }

}
