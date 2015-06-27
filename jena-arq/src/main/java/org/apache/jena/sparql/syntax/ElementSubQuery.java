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

package org.apache.jena.sparql.syntax;

import org.apache.jena.query.Query ;
import org.apache.jena.sparql.util.NodeIsomorphismMap ;

public class ElementSubQuery extends Element
{
    Query query ;
    
    public ElementSubQuery(Query query)
    {
        this.query = query ;
    }
    
    public Query getQuery() { return query ; } 
    
    @Override
    public boolean equalTo(Element other, NodeIsomorphismMap isoMap)
    {
        if ( ! ( other instanceof ElementSubQuery) )
            return false ;
        ElementSubQuery el = (ElementSubQuery)other ;
        return query.equals(el.query) ;
    }

    @Override
    public int hashCode()
    {
        return query.hashCode() ;
    }

    @Override
    public void visit(ElementVisitor v)
    { v.visit(this) ; }
}
