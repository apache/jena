/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.compiler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sparql.core.Quad;

public abstract
class QuadBlockCompilerBase implements QuadBlockCompiler
{
    private static Log log = LogFactory.getLog(QuadBlockCompilerBase.class) ;
    protected SDBRequest request ;
    protected SlotCompiler slotCompiler ;

    public QuadBlockCompilerBase(SDBRequest request, SlotCompiler slotCompiler)
    { 
        this.request = request ; 
        //prefixMapping = request.getPrefixMapping() ;
        this.slotCompiler = slotCompiler ;
    }

    final
    public SqlNode compile(QuadBlock quads)
    {
        // See QuadBlockStageBuilder for the replacement
        
        SqlNode sqlNode = slotCompiler.start(quads);
        
        for ( Quad quad : quads )
        {
            SqlNode sNode = compile(quad) ;
            if ( sNode != null )
                sqlNode = QC.innerJoin(request, sqlNode, sNode) ;
        }
        sqlNode = slotCompiler.finish(sqlNode, quads);
        return sqlNode ;
    }
        
    protected abstract SqlNode compile(Quad quad) ;

//    @Override
//    protected final SqlNode start(QuadBlock quads)
//    { return slotCompiler.start(quads) ; }
//    
//    @Override
//    protected final SqlNode finish(SqlNode sqlNode, QuadBlock quads)
//    { return slotCompiler.finish(sqlNode, quads) ; }
//    
//
//    
//    protected abstract SqlNode start(QuadBlock quads) ;
//    protected abstract SqlNode finish(SqlNode sqlNode, QuadBlock quads) ;
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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