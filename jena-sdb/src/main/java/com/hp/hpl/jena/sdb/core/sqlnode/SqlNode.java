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

package com.hp.hpl.jena.sdb.core.sqlnode;

import java.util.Set ;

import org.apache.jena.atlas.io.Printable ;

import com.hp.hpl.jena.sdb.core.Annotations ;
import com.hp.hpl.jena.sdb.core.Scope ;

public interface SqlNode extends Printable, Annotations
{
    public String               getAliasName() ;
    
    public boolean              isJoin() ;
    public boolean              isInnerJoin() ;
    public boolean              isLeftJoin() ;
//    public boolean              isRightJoin() ;
//    public boolean              isOuterJoin() ;
    
    public SqlJoin              asJoin() ;
    public SqlJoinLeftOuter     asLeftJoin() ;
    public SqlJoinInner         asInnerJoin() ;

    public boolean              isCoalesce() ;
    public SqlCoalesce          asCoalesce() ;
    
    public boolean              isRestrict() ;                // isSelect is confusing
    public SqlRestrict          asRestrict() ;
    
    public boolean              isProject() ;
    public SqlProject           asProject() ;
    
    public boolean              isDistinct() ;
    public SqlDistinct          asDistinct() ;
    
    public boolean              isTable() ;
    public SqlTable             asTable() ;

    public boolean              isSelectBlock() ;
    public SqlSelectBlock       asSelectBlock() ;

    public Scope getIdScope() ;
    public Scope getNodeScope() ;
    
    public Set<SqlTable> tablesInvolved() ;
    
    public void visit(SqlNodeVisitor visitor) ;
}
