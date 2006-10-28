/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

import java.util.Set;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.sdb.core.Block;
import com.hp.hpl.jena.sdb.core.CompileContext;
import com.hp.hpl.jena.sdb.core.compiler.BlockCompiler;
import com.hp.hpl.jena.sdb.core.compiler.BlockCompilerFactory;
import com.hp.hpl.jena.sdb.core.compiler.QueryCompilerMain;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.store.ConditionCompiler;
import com.hp.hpl.jena.sdb.store.SQLBridge;

public class QueryCompiler2 extends QueryCompilerMain
{
    private ConditionCompiler conditionCompiler ;
    private BlockCompilerFactory blockCompilerFactory ;
    
    public QueryCompiler2()
    {
        this(null, null) ;
    }

    public QueryCompiler2(BlockCompilerFactory blockCompilerFactory)
    {
        this(blockCompilerFactory, null) ;
    }
    
    private QueryCompiler2(BlockCompilerFactory blockCompilerFactory, ConditionCompiler conditionCompiler)
    {
        this.conditionCompiler = conditionCompiler ;
        if ( blockCompilerFactory == null )
            blockCompilerFactory = new BlockCompiler2Factory() ;
            
        this.blockCompilerFactory = blockCompilerFactory ;
    }
    
    @Override
    protected BlockCompiler  createBlockCompiler()     { return blockCompilerFactory.createBlockCompiler() ; }
    @Override
    protected SQLBridge createSQLBridge(Set<Var> projectVars)   { return new SQLBridge2(projectVars) ; }

    public ConditionCompiler getConditionCompiler()    { return conditionCompiler ; }
    
    @Override
    protected void startCompile(CompileContext context, Block block)
    { return ; }

    @Override
    protected SqlNode finishCompile(CompileContext context, Block block, SqlNode sqlNode, Set<Var> projectVars)
    { return sqlNode ; } 
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */