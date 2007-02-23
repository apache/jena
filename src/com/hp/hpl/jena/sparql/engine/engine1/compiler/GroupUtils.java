/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.engine1.compiler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import com.hp.hpl.jena.query.engine1.PlanElement;
//import com.hp.hpl.jena.query.engine1.plan.PlanBlockTriples;
//import com.hp.hpl.jena.query.engine1.plan.PlanFilter;
//import com.hp.hpl.jena.query.util.Context;


public class GroupUtils
{
    static Log log = LogFactory.getLog(GroupUtils.class) ;

//    // Return true if have handled the plan element.
//    static boolean optimizableConstraint(PlanBlockTriples basePatterns, PlanElement e, Context context)
//    {
//        if ( ! context.isTrue(ARQ.enableRegexConstraintsOpt) )
//            return false ;
//        if ( ! ( e instanceof PlanFilter ) )
//            return false ;
//        log.warn("Attempt to optimize a constraint - currently disabled") ;
//        // because awaiting ExprMatcher.
//        return false ;
//    }
    
    
//        PlanFilter f = (PlanFilter)e ;
//        Constraint c = f.getConstraint() ;
//        if ( ! ( c instanceof E_Regex ) )
//            return false ;
//        // It's a single regex
//        E_Regex regex = (E_Regex)c ;
//        if ( ! regex.getExpr().isVariable() )
//            return false ;
//        String var = regex.getExpr().getVarName() ;
//
//        if ( ! regex.getPattern().isConstant() )
//            return false ;
//        if ( regex.getFlags() != null )
//            if ( ! regex.getFlags().isConstant() )
//                return false ;
//        
//        NodeValue patternNV = regex.getPattern().getConstant() ;
//        if ( ! patternNV.isString() )
//            return false ;
//        NodeValue flagsNV = null ;
//        if ( regex.getFlags() != null ) 
//            flagsNV = regex.getFlags().getConstant() ;
//        
//        if ( flagsNV != null && ! flagsNV.isString() )
//            return false ;
//
//        String pattern = patternNV.getString() ;
//        String flags = (flagsNV==null)?null:flagsNV.getString() ;
//        
//        if ( log.isDebugEnabled() )
//            log.debug("REGEX: "+var+" '"+pattern+"' '"+flags+"'") ;
//        Expression jenaExpr = new JenaRegex(regex, var, pattern, flags) ;
//        basePatterns.addConstraint(jenaExpr) ;
//        return true ;
//    }
}




/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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