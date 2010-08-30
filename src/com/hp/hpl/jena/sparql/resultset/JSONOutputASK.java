/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;


import static com.hp.hpl.jena.sparql.resultset.JSONResults.dfBoolean ;
import static com.hp.hpl.jena.sparql.resultset.JSONResults.dfHead ;

import java.io.OutputStream ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.json.io.JSWriter ;

/** JSON Output (ASK format)
 * 
 * @author Elias Torres (<a href="mailto:elias@torrez.us">elias@torrez.us</a>)
 * Rewritten to remove use of org.json: Andy Seaborne (2010)
 */


public class JSONOutputASK
{
    private OutputStream outStream ;
    
    public JSONOutputASK(OutputStream outStream) {
        this.outStream = outStream;
        
    }

    public void exec(boolean result)
    {
        JSWriter out = new JSWriter(outStream) ;
        
        out.startOutput() ;
        
        out.startObject() ;
        out.key(dfHead) ;
        out.startObject() ;
        out.finishObject() ;
        out.pair(dfBoolean, result) ;
        out.finishObject() ;
        
        out.finishOutput() ;

        IO.flush(outStream) ;
    }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics ltd.
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