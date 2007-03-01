/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.compiler;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.layout1.CodecSimple;
import com.hp.hpl.jena.sdb.layout1.QuadBlockCompiler1;
import com.hp.hpl.jena.sdb.layout1.TripleTableDescSPO;

/** Highly experimental - will become an interface
 * For now, its layout specific.
 *
 */

public class QuadPatternCompiler
{
    private static Log log = LogFactory.getLog(QuadPatternCompiler.class) ;
    
    static Map<SDBRequest,QuadBlockCompiler> gen = new HashMap<SDBRequest, QuadBlockCompiler>() ;
    
    public static SqlNode compile(SDBRequest request, QuadBlock quads)
    {
        // TODO Make part of QueryCompiler.
        // Then QuadBlockCompiler2 
        QuadBlockCompiler qbc = get(request) ;
        return qbc.compile(quads) ;
    }
    
    private static QuadBlockCompiler get(SDBRequest request)
    {
        QuadBlockCompiler qbc = gen.get(request) ;
        if ( qbc == null )
            // From store ....
            gen.put(request, new QuadBlockCompiler1(request, 
                                                    new CodecSimple(),
                                                    new TripleTableDescSPO()
                                                    ) ) ;
        return gen.get(request) ;
    }
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