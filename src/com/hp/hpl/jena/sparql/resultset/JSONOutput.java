/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;

import java.io.OutputStream;

import com.hp.hpl.jena.query.ResultSet;

public class JSONOutput extends OutputBase
{
    public void format(OutputStream out, ResultSet resultSet)
    {
        // Use direct string output - more control
    
        JSONOutputResultSet jsonOut =  new JSONOutputResultSet(out) ;
        ResultSetApply a = new ResultSetApply(resultSet, jsonOut) ;
        a.apply() ;
        
//        // Do it by building an in-memory JSON object structure
//        JSONObject json = JSONObjectResult.resultSet(resultSet) ;
//        try {
//            PrintWriter w = FileUtils.asPrintWriterUTF8(out) ;
//            String s = json.toString(2) ;
//            w.print(s) ;
//            if ( ! s.endsWith("\n") )
//                w.println() ;
//            w.flush() ;
//        } catch (Exception ex) { throw new ResultSetException(ex.getMessage(), ex) ; } 
    }

    public void format(OutputStream out, boolean booleanResult)
    {
        JSONOutputASK jsonOut = new JSONOutputASK(out) ;
        jsonOut.exec(booleanResult) ;
    }
    

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