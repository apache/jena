/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.graph;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.test.AbstractTestReifier;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.tdb.TDBFactory;

public class TestReifier2 extends AbstractTestReifier
{
    public TestReifier2()
    {
        super("Reifier2") ;
    }

    @Override
    public Graph getGraph()
    {
        return TDBFactory.createGraph() ;
    }

    @Override
    public Graph getGraph(ReificationStyle style)
    {
        return TDBFactory.createGraph() ;
    }

    // Standard only.
    @Override public void testStyle() { assertSame( ReificationStyle.Standard, 
                                                    getGraph( ReificationStyle.Standard ).getReifier().getStyle() ); }
    
    // Other styles.
    @Override public void testIntercept() {}
    @Override public void testMinimalExplode() {}
    @Override public void testDynamicHiddenTriples() {}
    
    // We don't support iterator remove yet.
    @Override public void testBulkClearReificationTriples() {}
    @Override public void testBulkClearReificationTriples2() {}
    
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