/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import org.apache.commons.logging.LogFactory;

/** Convenience logging for warn and fatal.  Classes don't need to import
 *  the logging system, or have excessive pirvate static loggers for non-performance
 *  critical messages.
 */

public class ALog
{
    static public void warn(Object caller, String msg)
    {
        warn(caller.getClass(), msg) ;
    }

    static public void warn(Class cls, String msg)
    {
        LogFactory.getLog(cls).warn(msg) ;
    }

    static public void warn(Object caller, String msg, Throwable th)
    {
        warn(caller.getClass(), msg, th) ;
    }

    static public void warn(Class cls, String msg, Throwable th)
    {
        LogFactory.getLog(cls).warn(msg, th) ;
    }

    static public void fatal(Object caller, String msg)
    {
        fatal(caller.getClass(), msg) ;
    }

    static public void fatal(Class cls, String msg)
    {
        LogFactory.getLog(cls).fatal(msg) ;
    }

    static public void fatal(Object caller, String msg, Throwable th)
    {
        fatal(caller.getClass(), msg, th) ;
    }

    static public void fatal(Class cls, String msg, Throwable th)
    {
        LogFactory.getLog(cls).fatal(msg, th) ;
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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