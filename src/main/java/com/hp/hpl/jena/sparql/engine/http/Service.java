/**
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

package com.hp.hpl.jena.sparql.engine.http;

import java.io.InputStream ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecException ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery ;
import com.hp.hpl.jena.sparql.algebra.op.OpService ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.Rename ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorResultSet ;
import com.hp.hpl.jena.sparql.mgt.Explain ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Execution of OpService */

public class Service
{
    public static QueryIterator exec(OpService op, Context context)
    {
        if ( ! op.getService().isURI() )
            throw new QueryExecException("Service URI not bound: "+op.getService()) ; 
        
        
        // This relies on the observation that the query was originally correct,
        // so reversing the scope renaming is safe (it merely restores the algebra expression).
        // Any variables that reappear should be internal ones that were hidden by renaming
        // in the first place.
        // Any substitution is also safe because it replaced variables by values. 
        Op opRemote = Rename.reverseVarRename(op.getSubOp(), true) ;

        //Explain.explain("HTTP", opRemote, context) ;
        
        Query query ;
//        if ( op.getServiceElement() != null )
//        {
// does not cope with substitution?
//            query = QueryFactory.make() ;
//            query.setQueryPattern(op.getServiceElement().getElement()) ;
//            query.setQuerySelectType() ;
//            query.setQueryResultStar(true) ;
//        }
//        else
            query = OpAsQuery.asQuery(opRemote) ;
            
        Explain.explain("HTTP", query, context) ;            
        HttpQuery httpQuery = new HttpQuery(op.getService().getURI()) ;
        httpQuery.addParam(HttpParams.pQuery, query.toString() );
        httpQuery.setAccept(HttpParams.contentTypeResultsXML) ;
        InputStream in = httpQuery.exec() ;
        ResultSet rs = ResultSetFactory.fromXML(in) ;
        return new QueryIteratorResultSet(rs) ; 
    }
}
