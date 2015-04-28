/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.atlas.logging.java;

import java.util.logging.Formatter ;
import java.util.logging.Handler ;
import java.util.logging.LogManager ;
import java.util.logging.LogRecord ;


public class ConsoleHandlerStdout extends Handler 
{

    private void configure()
    {
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();
        
        String cls = manager.getProperty(cname+".formatter") ;
        Formatter fmt = null ;
        
        try {
            if (cls != null) {
                Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(cls);
                fmt = (Formatter) clz.newInstance();
            }
        } catch (Exception ex) {
            // We got one of a variety of exceptions in creating the
            // class or creating an instance.
            // Drop through.
        }
        if ( fmt == null )
            fmt = new TextFormatter() ;
        setFormatter(fmt) ;
    }
    
    public ConsoleHandlerStdout()
    {
        configure() ;
    }
    
    @Override
    public void close() throws SecurityException
    {}

    @Override
    public void flush()
    { System.out.flush(); }

    @Override
    public void publish(LogRecord record)
    {
        if ( ! super.isLoggable(record) )
            return ;
        String s = getFormatter().format(record) ;
        System.out.print(s) ;
    }

}
