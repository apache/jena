/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;


import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.lang.sse.Item;
import com.hp.hpl.jena.sparql.lang.sse.SSE;
import com.hp.hpl.jena.sparql.lang.sse.builders.OpBuilder;
import com.hp.hpl.jena.sparql.lang.sse.builders.ResolveURI;
import com.hp.hpl.jena.sparql.util.PrefixMapping2;


public class Algebra
{
    static public Op read(String filename)
    {
        Item item = SSE.parseFile(filename) ;
        return parse(item) ;
    }
    
    static public Op parse(String string)
    {
        Item item = SSE.parseString(string) ;
        return parse(item) ;
    }
    
    static public Op parse(Item item)
    {
        // TODO - design AND write
        
        PrefixMapping pmap = new PrefixMappingImpl() ;
        PrefixMapping pmapSub = new PrefixMappingImpl() ;

        
        // Add any prefixes to pmapSub.
        //e.g.
        pmap.setNsPrefix("", "http://example/") ;

        PrefixMapping2 pmap2 = new PrefixMapping2(pmap, pmapSub) ;
        item = ResolveURI.resolve(item, pmap2) ;
        Op op = OpBuilder.build(item) ;
        return op ;
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