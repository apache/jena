/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.expr.Expr;
import com.hp.hpl.jena.query.util.ExprUtils;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.exprmatch.Action;
import com.hp.hpl.jena.sdb.exprmatch.ExprMatcher;
import com.hp.hpl.jena.sdb.exprmatch.MapAction;
import com.hp.hpl.jena.sdb.exprmatch.MapResult;

public class ExprPattern
{
    Expr pattern ;
    MapAction mapAction;
    
    
    public ExprPattern(String pattern ,
                       Var[] vars,
                       Action[] actions)
    {
        this.pattern = ExprUtils.parse(pattern) ;
        if ( vars.length != actions.length )
            throw new SDBException("Variable and action arrays are different lengths") ;  
        mapAction = new MapAction() ;
        for ( int i = 0 ; i < vars.length ; i++ )
        {
            Var var = vars[i] ;
            Action a = actions[i] ;
            mapAction.put(var, a) ;
        }
    }
    
    public MapResult match(Expr expression)
    {
        return ExprMatcher.match(expression, pattern, mapAction) ;
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