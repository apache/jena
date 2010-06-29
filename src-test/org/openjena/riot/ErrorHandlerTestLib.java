/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot;

import java.util.ArrayList ;
import java.util.List ;

public class ErrorHandlerTestLib
{
    public static class ExFatal extends RuntimeException {}

    public static class ExError extends RuntimeException {}

    public static class ExWarning extends RuntimeException {}

    public static class ErrorHandlerEx implements ErrorHandler
    {
        public void warning(String message, long line, long col)
        { throw new ExWarning() ; }
    
        public void error(String message, long line, long col)
        { throw new ExError() ; }
    
        public void fatal(String message, long line, long col)
        { throw new ExFatal() ; }
    }

    // Error handler that records messages
    public static class ErrorHandlerMsg implements ErrorHandler
    {
        public List<String> msgs = new ArrayList<String>() ;
    
        public void warning(String message, long line, long col)
        { msgs.add(message) ; }
    
        public void error(String message, long line, long col)
        { msgs.add(message) ; }
    
        public void fatal(String message, long line, long col)
        { msgs.add(message) ; throw new ExFatal() ; }
    }

}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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