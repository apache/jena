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

package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.graph.Node;

class ReificationCache
    {
    protected Node stmtURI;
    protected ReificationStatementMask mask;
    protected int tripleCount;

    ReificationCache( Node s, ReificationStatementMask m, int cnt )
        {
        stmtURI = s;
        mask = m;
        tripleCount = cnt;
        }

    public ReificationStatementMask getStmtMask()
        { return mask; }

    public int getCnt()
        { return tripleCount; }

    public Node getStmtURI()
        { return stmtURI; }

    public void setMask( ReificationStatementMask m )
        { mask = m; }

    public void setCount( int count )
        { tripleCount = count; }

    public void incCount( int count )
        { tripleCount += 1; }

    public void decCount( int count )
        { tripleCount -= 1; }

    public boolean canMerge( ReificationStatementMask fragMask )
        {  return !mask.hasIntersect( fragMask ); }

    public boolean canUpdate( ReificationStatementMask fragMask )
        { return (canMerge( fragMask ) && (tripleCount == 1)); }

    public void update( ReificationStatementMask fragMask )
        {
        mask.setMerge( fragMask );
        if (isStmt()) mask.setIsStmt();
        }

    private boolean isStmt()
        { return mask.hasSPOT() && tripleCount == 1; }
    }
