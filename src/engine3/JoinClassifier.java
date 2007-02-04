/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package engine3;

import java.util.Set;

import com.hp.hpl.jena.query.core.ARQInternalErrorException;
import com.hp.hpl.jena.query.engine2.OpVars;
import com.hp.hpl.jena.query.engine2.op.Op;
import com.hp.hpl.jena.query.engine2.op.OpFilter;
import com.hp.hpl.jena.query.engine2.op.OpJoin;
import com.hp.hpl.jena.query.engine2.op.OpLeftJoin;
import com.hp.hpl.jena.query.expr.Expr;
import com.hp.hpl.jena.query.util.SetUtils;

public class JoinClassifier
{
    static public boolean isLinear(OpJoin join)
    {
        Op left = join.getLeft() ;
        Op right = join.getRight() ;
        
        
        
        return check(left, right) && check(right, left) ;
    }
    
    static final boolean print = false ; 
    
    static private boolean check(Op op, Op other)
    {
        Expr expr = null ;
        Set fixedFilterScope = null ;   // Vars in scope to the filter - fixed
        Set optFilterScope = null ;     // Vars in scope to the filter - optional
        
        if ( op instanceof OpFilter )
        {
            OpFilter f = (OpFilter)op ;    
            op = f.getSubOp() ;
            expr = f.getExpr() ;
            VarFinder vf = new VarFinder(op) ;
            fixedFilterScope = vf.getFixed() ;
            optFilterScope = vf.getOpt() ;
        }
        
        if ( op instanceof OpLeftJoin )
        {
            OpLeftJoin j = (OpLeftJoin)op ;
            // Leave op
            expr = j.getExpr() ;
            VarFinder vf1 = new VarFinder(j.getLeft()) ;
            VarFinder vf2 = new VarFinder(j.getRight()) ;
            // Both sides of the LeftJoin are in-scope to the filter. 
            fixedFilterScope = SetUtils.union(vf1.getFixed(), vf2.getFixed()) ;
            optFilterScope = SetUtils.union(vf1.getOpt(), vf2.getOpt()) ;
        }
        
        if ( expr == null )
            return true ;
        
        if ( fixedFilterScope == null || optFilterScope == null )
            throw new ARQInternalErrorException("JoinClassifier: Failed to set up variable sets correctly") ;
        
        Set exprVars = expr.getVarsMentioned() ;
        
        // remove variables that are safe:
        //   fixed mentioned here
        //   or optional and not mentioned in the other branch
        
        // **** XXX If expr from a left join, then safe if filter var in LHS or RHS of LJ.  
        
        if ( print ) System.out.println("Expr vars:     "+exprVars) ;
        if ( print ) System.out.println("Fixed, here:   "+fixedFilterScope) ;
        if ( print ) System.out.println("Opt, here:     "+optFilterScope) ;
        
        Set allVarsOther = OpVars.patternVars(other) ;      // The other side.
        if ( print ) System.out.println("allVarsOther:  "+allVarsOther) ;
        
        // Remove variables in filter that are always set below it (hence safe) 
        exprVars.removeAll(fixedFilterScope) ;
        if ( print ) System.out.println("Expr vars:(\\F) "+exprVars) ;
        if ( exprVars.size() == 0 ) return true ;

        // Ditto safe are any optionals vars IFF they not in other side 
        // (where they may have a value when they don't in the op below the filter)
        Set s = SetUtils.difference(optFilterScope, allVarsOther) ;
        // s is the set of optionals only on this side. 
        exprVars.removeAll(s) ;
        if ( exprVars.size() == 0 ) return true ;
        
        // At this point, an expr var is "dangerous" if it is mentioned in the other side.
        if ( print ) System.out.println("Diff           "+s) ;
        if ( print ) System.out.println("Expr vars:(\\s) "+exprVars) ;
        
        // Keep those in the other side : thien, if not empty, it's a dangerous variable.
        exprVars.retainAll(allVarsOther) ;
        if ( print ) System.out.println("Expr vars:(!!) "+exprVars) ;
        
        // Safe if nothing in the intersection of remaining exprVars and  
        return exprVars.size() == 0 ;
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