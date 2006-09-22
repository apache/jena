/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.inf;

import java.util.Collection;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.util.FmtUtils;
import com.hp.hpl.jena.sdb.core.CompileContext;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlRestrict;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.layout2.BlockCompiler2;
import com.hp.hpl.jena.vocabulary.RDFS;

public class BlockCompilerSubProperty extends BlockCompiler2
{
    @Override
    protected void addMoreConstants(Collection<Node> constants)
    {
        super.addMoreConstants(constants) ;
        constants.add(RDFS.subPropertyOf.asNode()) ;
    }
    
    @Override
    public SqlNode compile(Triple triple, CompileContext context)
    {
        if ( ! triple.getPredicate().equals(RDFS.subPropertyOf.asNode()) )
            return super.compile(triple, context) ;
        
        // rdfs:subCPropertyOf - different table.
        
        String alias = context.allocAlias("SC") ;
        SqlExprList conditions = new SqlExprList() ;
        
        SqlTable subPropertyTriple = new SubPropertyTable(alias) ;
        
        subPropertyTriple.addNote("Special: "+FmtUtils.stringForTriple(triple, context.getQuery().getPrefixMapping())) ;
        
        processSlot(context, subPropertyTriple, conditions, triple.getSubject(),   SubPropertyTable.colSubProperty) ; 
        processSlot(context, subPropertyTriple, conditions, triple.getObject(), SubPropertyTable.colSuperProperty) ;
        
        if ( conditions.size() == 0 )
            return subPropertyTriple ;
        
        return SqlRestrict.restrict(subPropertyTriple, conditions) ;
        
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