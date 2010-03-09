/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver.reorder;

import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.tdb.solver.stats.StatsMatcher;

public class ReorderLib
{
    private static class ReorderProcIdentity implements ReorderProc
    {
        //@Override
        public BasicPattern reorder(BasicPattern pattern)
        {
            return pattern ;
        } 
        @Override
        public String toString()
        {
            return "identity reorder" ;
        }
    } ;
    private static ReorderProc _identityProc = new ReorderProcIdentity() ;

    private static class ReorderTransformationIdentity implements ReorderTransformation
    {
        //@Override
        public BasicPattern reorder(BasicPattern pattern)
        {
            return pattern ;
        }

        //@Override
        public ReorderProc reorderIndexes(BasicPattern pattern)
        {
            return _identityProc ;
        }
    } ;
    private static ReorderTransformation _identity = new ReorderTransformationIdentity() ;

    public static ReorderProc identityProc()
    { return _identityProc ; }

    public static ReorderTransformation identity()
    { return _identity ; }

    public static ReorderTransformation fixed()
    {
        return new ReorderFixed() ;
    }
    
    
    public static ReorderTransformation weighted(String filename)
    {
        StatsMatcher stats = new StatsMatcher(filename) ;
        return new ReorderWeighted(stats) ;
    }

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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