/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout1;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.core.Binding;
import com.hp.hpl.jena.query.core.BindingMap;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;
import com.hp.hpl.jena.query.engine1.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sdb.store.ResultsBuilder;

public class ResultsBuilder1 implements ResultsBuilder
{
    private EncoderDecoder codec ;
    ResultsBuilder1(EncoderDecoder codec) { this.codec = codec ; }
    
    public QueryIterator assembleResults(java.sql.ResultSet rs, Binding binding,
                                         Set<Var> vars, ExecutionContext execCxt)
        throws SQLException
    {
        List<Binding> results = new ArrayList<Binding>() ;
        
        while(rs.next())
        {
            Binding b = new BindingMap(binding) ;
            for ( Var v : vars )
            {
                try {
                    String s = rs.getString(v.getName()) ;
                    // Same as rs.wasNull() for things that can return Java nulls.
                    if ( s == null )
                        continue ;
                    Node n = codec.decode(s) ;
                    b.add(v.getName(), n) ;
                    // Ignore any access error (variable requested not in results)
                } catch (SQLException ex) {}
            }
            results.add(b) ;
        }
        return new QueryIterPlainWrapper(results.iterator(), execCxt) ;
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