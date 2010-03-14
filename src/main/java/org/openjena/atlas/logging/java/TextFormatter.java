/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.logging.java;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/** A pattern-like log formatter */
public class TextFormatter extends Formatter
{
    @Override
    public String format(LogRecord record) {
        String loggerName = record.getLoggerName();
        if(loggerName == null) {
            loggerName = "root";
        }
        
        int i = loggerName.lastIndexOf('.') ; 
        String loggerNameShort = loggerName.substring(i+1) ;
            
        // %tT (%5$tT) is %5$tH:%5$tM:%5$tS
        // %tF is 2008-11-22 "%tY-%tm-%td"
        return String.format("%5$tT %3$-5s %2$-10s :: %6$s\n", 
                             loggerName,                        // 1
                             loggerNameShort,                   // 2
                             record.getLevel(),                 // 3
                             Thread.currentThread().getName(),  // 4
                             new Date(record.getMillis()),      // 5
                             record.getMessage()) ;             // 6
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