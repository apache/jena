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

package com.hp.hpl.jena.update;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.io.PrintUtils ;

import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.modify.request.UpdateVisitor ;
import com.hp.hpl.jena.sparql.modify.request.UpdateWriter ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;
import com.hp.hpl.jena.sparql.util.PrintSerializable ;
import com.hp.hpl.jena.sparql.util.QueryOutputUtils ;

public abstract class Update implements PrintSerializable
{
    public abstract void visit(UpdateVisitor visitor) ; 
    
    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        UpdateWriter.output(this, out, sCxt) ;
    }

    @Override
    public void output(IndentedWriter out)
    {
        UpdateWriter.output(this, out, null) ;
    }

    @Override
    public String toString(PrefixMapping pmap)
    { return QueryOutputUtils.toString(this, pmap) ; } 
    
    @Override
    public String toString()
    { return PrintUtils.toString(this) ; }
    
    /** Compare by isomorphism - if the isomorphism map is null, compare nodes by .equals */ 
    public abstract boolean equalTo(Update other, NodeIsomorphismMap isoMap) ;
    
//    @Override
//    public boolean equals(Object other) {
//        if ( ! ( other instanceof Update ) )
//            return false ;
//        return equalTo((Update)other, null) ;
//    }
//    
//    @Override
//    public abstract int hashCode() ;
    
    protected static final int hashAdd          = 0x1000 ;
    protected static final int hashCopy         = 0x1001 ;
    protected static final int hashMove         = 0x1002 ;
    protected static final int hashCreate       = 0x1003 ;
    protected static final int hashInsertData   = 0x1004 ;
    protected static final int hashDeleteData   = 0x1005 ;
    protected static final int hashDeleteWhere  = 0x1006 ;

    protected static final int hashClear        = 0x1007 ;
    protected static final int hashDrop         = 0x1008 ;

    protected static final int hashLoad         = 0x1009 ;
    protected static final int hashDeleteInsert = 0x100A ;
    
}
