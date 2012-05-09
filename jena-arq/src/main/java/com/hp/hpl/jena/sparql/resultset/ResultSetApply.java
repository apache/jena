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

package com.hp.hpl.jena.sparql.resultset;

import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.RDFNode ;

/** A class to walk a result set. */

public class ResultSetApply
{
    ResultSetProcessor proc = null ;
    ResultSet rs = null ;
    
    public ResultSetApply(ResultSet rs, ResultSetProcessor proc)
    {
        this.proc = proc ;
        this.rs = rs ;
    }
    
    public void apply()
    {
        proc.start(rs) ;
        for ( ; rs.hasNext() ; )
        {
            QuerySolution qs = rs.next() ;
            proc.start(qs) ;
            for ( String varName : rs.getResultVars()  )
            {
                RDFNode node = qs.get(varName) ;
                // node may be null
                proc.binding(varName, node) ;
            }
            proc.finish(qs) ;
        }
        proc.finish(rs) ;
    }
    
    public static void apply(ResultSet rs, ResultSetProcessor proc)
    {
        ResultSetApply rsa = new ResultSetApply(rs, proc) ;
        rsa.apply() ;
    }
}
