/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: ReificationStyle.java,v 1.2 2003-09-08 13:43:49 chris-dollin Exp $
*/

package com.hp.hpl.jena.shared;

/**
    Reification styles have two boolean components: whether the
    graph+reifier will intercept reification triples or not [if not, the only
    in-Jena reification is through the reifyAs operation], and whether or
    not reification triples will be visible in the graph.
*/
public class ReificationStyle
    {
    public static final ReificationStyle Standard = new ReificationStyle( true, false );
    public static final ReificationStyle Convenient = new ReificationStyle( true, true );
    public static final ReificationStyle Minimal = new ReificationStyle( false, true );
    
    private boolean intercept;
    private boolean conceal;
    
    public ReificationStyle( boolean intercept, boolean conceal )
        {
        this.intercept = intercept;
        this.conceal = conceal;
        }
        
    public boolean intercepts()
        { return intercept; }
        
    public boolean conceals()
        { return conceal; }
        
    public String toString()
        { return "<style int=" + intercept + ", con=" + conceal + ">"; }
    }
/*
    (c) Copyright 2003, Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/