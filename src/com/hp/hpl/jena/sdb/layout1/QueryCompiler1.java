/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout1;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.sdb.core.Block;
import com.hp.hpl.jena.sdb.core.CompileContext;
import com.hp.hpl.jena.sdb.core.compiler.BlockCompiler;
import com.hp.hpl.jena.sdb.core.compiler.ConditionCompilerNone;
import com.hp.hpl.jena.sdb.core.compiler.QueryCompilerMain;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlProject;
import com.hp.hpl.jena.sdb.store.ConditionCompiler;
import com.hp.hpl.jena.sdb.store.ResultsBuilder;
import com.hp.hpl.jena.sdb.util.Pair;

public class QueryCompiler1 extends QueryCompilerMain
{
    private static Log log = LogFactory.getLog(QueryCompiler1.class) ;
    
    private TripleTableDesc tripleTableDesc ;
    private EncoderDecoder codec ;

    public QueryCompiler1(EncoderDecoder codec, TripleTableDesc tripleTableDesc)
    {
        if ( tripleTableDesc == null )
            tripleTableDesc = new TripleTableDescSPO() ;
        this.codec = codec ;
        this.tripleTableDesc = tripleTableDesc ;
    }
    

    public QueryCompiler1(EncoderDecoder codec)    { this(codec, null) ; }

    @Override
    protected BlockCompiler  createBlockCompiler()    { return new BlockCompiler1(codec, tripleTableDesc) ; }
    
    @Override
    protected ResultsBuilder createResultsBuilder()   { return new ResultsBuilder1(codec) ; }


    public ConditionCompiler getConditionCompiler()
    {
        return ConditionCompilerNone.get() ;
    }

    @Override
    protected void startCompile(CompileContext context, Block block) { return ; }

    @Override
    protected SqlNode finishCompile(CompileContext context, Block block, SqlNode sqlNode, Set<Var> projectVars)
    {
        sqlNode = makeProject(sqlNode, projectVars) ;
        return sqlNode ;
    }

    private SqlNode makeProject(SqlNode sqlNode, Collection<Var> projectVars)
    {
        for ( Var v : projectVars )
        {
            if ( ! v.isNamedVar() )
                continue ;
            // Value scope == IdScope for layout1
            // CHECK
            SqlColumn c = sqlNode.getIdScope().getColumnForVar(v) ;
            if ( c != null )
                sqlNode = SqlProject.project(sqlNode, new Pair<Var, SqlColumn>(v,c)) ;
//            else
//                log.warn("Can't find column for var: "+v) ;
                
        }
        return sqlNode ;
    }
    
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