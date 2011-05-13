/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import org.openjena.atlas.io.IndentedLineBuffer;
import org.openjena.atlas.io.IndentedWriter;

public class GenerateSQLOracle extends GenerateSQL
{
    @Override
    protected SqlNodeVisitor makeVisitor(IndentedLineBuffer buff)
    {
        return new GeneratorVisitorOracle(buff) ;
    }
}

class GeneratorVisitorOracle extends GenerateSQLVisitor
{
    public GeneratorVisitorOracle(IndentedWriter out)
    { super(out) ; }

    // No "AS" in Oracle
    @Override
    protected String aliasToken()
    {
        return " " ;
    }
    
    // No "true" in Oracle
    @Override
    protected String leftJoinNoConditionsString()
    { return "1=1" ; }

    // TODO
    // It looks like the LIMIT only case can be done with SELECT * from T WHERE ROWNUM <= 10
    // but OFFSET/LIMIT needs SELECT * FROM (SELECT ... FROM ...) WHERE ROWNUM BETWEEN 3 AND 200
    
//    @Override
//    protected void genLimitOffset(SqlSelectBlock sqlSelectBlock)
//    { super.genLimitOffset(sqlSelectBlock) ; }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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