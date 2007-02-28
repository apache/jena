/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.pattern;

import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.engine.compiler.QuadBlock;
import com.hp.hpl.jena.sdb.engine.compiler.QuadBlockCompiler;

// Two phases : 
//    QuadBlock => QuadBlock rewrite
//    QuadBlock => QuadBlock+SQL 

public class SqlStageBuilder implements QuadBlockCompiler
{
    QuadBlockCompiler baseCompiler ;
    private SDBRequest request ;
    public SqlStageBuilder(SDBRequest request, QuadBlockCompiler baseCompiler)
    {
        this.request = request ;
        this.baseCompiler = baseCompiler ;
    }
    
    QuadBlockRewrite qbr1 = new QBR_SubType() ;
    QuadBlockRewrite qbr2 = new QBR_SubProperty() ;
    
    public SqlNode compile(QuadBlock quads)
    {
        // Phase 1.
        quads = qbr1.rewrite(request, quads) ;
        if ( false )
            // rewrites the rewrites done for rdf:type.
            // reverse the order and theer is a variable in the property slot for rdf:type.  
            quads = qbr2.rewrite(request, quads) ;
        
        // Phase 2.
        // Look for alternative tables.
        SqlStage stage = new SqlStagePlain(baseCompiler, quads) ;
        return stage.build(request) ;
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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