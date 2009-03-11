/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package logging;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import lib.StrUtils;
import org.slf4j.LoggerFactory;
import dev.Run;

public class LogMain
{
    public static void main(String ... args) throws SecurityException, IOException
    {
        org.slf4j.Logger logger_slf4j = LoggerFactory.getLogger(Run.class) ;
        
        String s = StrUtils.strjoinNL(
                                      //"handlers=java.util.logging.ConsoleHandler,logging.ConsoleHandlerStdout",
                                      //"handlers=logging.ConsoleHandlerStdout" ,
                                      "handlers=java.util.logging.ConsoleHandler" ,
                                      
                                      "logging.ConsoleHandlerStdout.level=ALL",
                                      "logging.ConsoleHandlerStdout.formatter=logging.TextFormatter",
                                      
                                      "java.util.logging.ConsoleHandler.level=INFO",
                                      "java.util.logging.ConsoleHandler.formatter=logging.TextFormatter"
                                      ) ;   
        
        LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(s.getBytes("UTF-8"))) ;

        //System.setProperty("java.util.logging.config.file", "logging.properties") ;
        //LogManager.getLogManager().readConfiguration() ;
        
        Logger log = Logger.getLogger(Run.class.getName()) ;
        //log.setLevel(Level.WARNING) ;
        log.info("Hello World") ;
        
        // Because the parent has the plumbed in ConsoleHandler
        log.setUseParentHandlers(false) ;
        log.addHandler(new ConsoleHandlerStdout()) ;
        log.info("Hello World (part 2)") ;
         
//        // -- Remove any ConsoleHanlder
//        Handler[] handlers = log.getHandlers() ;
//        for ( Handler h : handlers )
//        {
//            if ( h instanceof ConsoleHandler )
//                log.removeHandler(h) ;
//        }
        log.info("Hello World (part 3)") ;
        System.out.println("(End)") ;
        
        
//        // ---- 
//        System.setProperty("log4j.configuration", "file:log4j.properties") ;
//        
//        
//        org.apache.log4j.Logger log4j = org.apache.log4j.Logger.getLogger(Run.class);
//        log4j.setLevel(org.apache.log4j.Level.ALL) ;
//        log4j.info("Log4j direct") ;
        
        // Must have the right logger adapter on the classpath: slf4j-log4j... or slf4j-
        
        logger_slf4j.error("org.slf4j") ;
        
        System.exit(0) ;
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