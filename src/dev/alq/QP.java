/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.alq;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.core.Element;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine2.AlgebraCompilerQuad;
import com.hp.hpl.jena.query.engine2.op.Op;
import com.hp.hpl.jena.query.util.Context;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.store.SQLBridge;
import com.hp.hpl.jena.sdb.store.Store;

public class QP 
{
    public static SqlNode toSqlTopNode(SqlNode sqlNode, List<Var> projectVars,
                                       SQLBridge bridge, Store store)
    {
        bridge.init(sqlNode, projectVars) ;
        sqlNode = bridge.buildProject() ;
        return sqlNode ;
    }
    
    public static List<Var> projectVars(Query query)
    {
        List<Var> vars = new ArrayList<Var>() ;
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>)query.getResultVars() ;
        if ( list.size() == 0 )
            LogFactory.getLog(QP.class).warn("No project variables") ;
        for ( String vn  : list )
            vars.add(Var.alloc(vn)) ;
        return vars ;
    }
    
    
    //  Temporary - access to a protected while we think about the correct overall
    //  design of the algebra compiler (which is just for SDB).   
    private static class SDBCompiler extends AlgebraCompilerQuad
    {
        static Op compile(Context context, Element queryPatternElement) 
        { return new SDBCompiler(context).compileFixedElement(queryPatternElement) ; }

        public SDBCompiler(Context context)
        {
            super(context) ;
        }
        public Op dwim(Element queryPatternElement)
        {
            return super.compileFixedElement(queryPatternElement) ;
        }
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