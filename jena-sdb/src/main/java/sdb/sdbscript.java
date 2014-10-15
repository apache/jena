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

package sdb;

import java.lang.reflect.Method;

import com.hp.hpl.jena.sdb.SDB ;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class sdbscript
{
    private static Logger log = LoggerFactory.getLogger(sdbscript.class) ;
    
    public static void main(String... a)
    {
        SDB.init();
        if ( a.length == 0 )
            a = new String[]{ "script.rb" } ;

        staticByReflection("org.jruby.Main", "main", a) ;
    }
    
    private static void staticByReflection(String className, String methodName, String[] args)
    {
        Class<?> cmd = null ;
        try { cmd = Class.forName(className) ; }
        catch (ClassNotFoundException ex)
        {
            log.error(String.format("Class not found: %s", className)) ;
            return  ; 
        }

        Method method = null ;
        try { method = cmd.getMethod(methodName, new Class[]{args.getClass()}) ; }
        catch (NoSuchMethodException ex)
        {
            log.error(String.format("Class '%s' found but not the method '%s'",
                                    className, methodName)) ;
            return ;
        }

        try 
        {
            method.invoke(null, (Object)args) ;
        } catch (Exception ex)
        {
            log.error(String.format("Exception invoking '%s.%s'",  className, methodName), ex) ;
            return ;
        }
    }
}
