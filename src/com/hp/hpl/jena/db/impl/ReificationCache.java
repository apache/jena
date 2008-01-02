/*
 (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 All rights reserved.
 $Id: ReificationCache.java,v 1.3 2008-01-02 12:08:24 andy_seaborne Exp $
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

/*
 * (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development
 * Company, LP All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */