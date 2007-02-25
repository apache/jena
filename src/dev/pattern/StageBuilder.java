/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.pattern;

import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.engine.compiler.QC;
import com.hp.hpl.jena.sdb.engine.compiler.QuadBlock;
import com.hp.hpl.jena.sdb.engine.compiler.QuadBlockCompiler;

public class StageBuilder
{
    //?? How to interface into varibable handling.
    
    QuadBlockCompiler baseCompiler ;
    public SqlNode compile(SDBRequest request, QuadBlockCompiler compiler, QuadBlock quads)
    {
        baseCompiler = compiler ; 
        //return baseCompiler.compile(quads) ;
        // Find any interesting bits.
        
        StageList sList = split(quads) ;
        
        SqlNode sqlNode = null ;
        // See QuadCompilerBase.compile
        for ( Stage s : sList )
        {
            SqlNode sNode = s.build() ;
            if ( sNode != null )
                sqlNode = QC.innerJoin(request, sqlNode, sNode) ;
        }
        
        return sqlNode ;
    }
    
    private StageList split(QuadBlock quads)
    {
        StageList sList = new StageList() ;
        sList.add(new StagePlain(baseCompiler, quads)) ;
        return sList ;
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