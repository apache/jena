/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr.aggregate;

import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;

import org.openjena.atlas.lib.NotImplemented ;
import org.openjena.atlas.logging.Log ;

public class AggregatorFactory
{
    public static Aggregator createCount(boolean distinct)
    { 
        return distinct ? new AggCountDistinct() : new AggCount() ;
    }

    public static Aggregator createCountExpr(boolean distinct, Expr expr)
    { 
        return distinct ? new AggCountVarDistinct(expr) : new AggCountVar(expr) ;
    }
    
    public static Aggregator createSum(boolean distinct, Expr expr)
    { 
        return distinct ? new AggSumDistinct(expr) : new AggSum(expr) ;
    }
    
    public static Aggregator createMin(boolean distinct, Expr expr)
    { 
        // Only remember it's DISTINCT for getting the printing right.
        return distinct ? new AggMinDistinct(expr) : new AggMin(expr) ;
    }
    
    public static Aggregator createMax(boolean distinct, Expr expr)
    { 
        return distinct ? new AggMaxDistinct(expr)  : new AggMax(expr) ;
    }
    
    public static Aggregator createAvg(boolean distinct, Expr expr)
    { 
        return distinct ? new AggAvgDistinct(expr) : new AggAvg(expr) ;
    }
        
    public static Aggregator createSample(boolean distinct, Expr expr)
    { 
        return distinct ? new AggSampleDistinct(expr) : new AggSample(expr) ;
    }
    
    public static Aggregator createGroupConcat(boolean distinct, Expr expr, String separator, ExprList orderedBy)
    { 
        if ( orderedBy != null && ! orderedBy.isEmpty())
            throw new NotImplemented("GROUP_CONCAT / ORDER BY not implemented yet") ;        
        return distinct ? new AggGroupConcatDistinct(expr, separator) : new AggGroupConcat(expr, separator) ;
    }
    
    private static Aggregator err(String label)
    {
        Log.fatal(AggregatorFactory.class, "Not implemented: "+label) ;
        return null ;
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
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