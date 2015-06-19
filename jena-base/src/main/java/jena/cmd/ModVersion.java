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

package jena.cmd;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.Lib;

public class ModVersion extends ModBase
{
    protected final ArgDecl versionDecl = new ArgDecl(ArgDecl.NoValue, "version") ;
    protected boolean version = false ;
    protected boolean printAndExit = false ;
    
    private Version versionMgr = new Version() ; 
    
    public ModVersion(boolean printAndExit)
    {
        this.printAndExit = printAndExit ;
    }
    
    public void addClass(Class<?> c) { versionMgr.addClass(c) ; }
    
    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.add(versionDecl, "--version", "Version information") ;
    }

    @Override
    public void accept(CmdArgModule cmdLine)
    {
        if ( cmdLine.contains(versionDecl) )
            version = true ;
        // The --version flag causes us to print and exit. 
        if ( version && printAndExit )
            printVersionAndExit() ;
    }

    public boolean getVersionFlag() { return version ; }
    
    public void printVersion()
    {
        versionMgr.print(IndentedWriter.stdout);
    }  
     
    public void printVersionAndExit()
    {
        printVersion() ;
        System.exit(0) ;
    }
    
    /** Manage version information for subsystems */
    private static class Version
    {
        private List<Class< ? >> classes = new ArrayList<>() ;
        
        /**
         * Add a class to the version information
         * @param c Class
         */
        public void addClass(Class< ? > c)
        {
            if ( ! classes.contains(c) ) 
                classes.add(c) ;
        }
        
        private static String FIELD_VERSION = "VERSION";
        
        private static String FIELD_BUILD_DATE = "BUILD_DATE";
        
        private static String[] fields = { /*"NAME",*/ FIELD_VERSION, FIELD_BUILD_DATE } ;

        /**
         * Prints version information for all registered classes to the given writer
         * @param writer Writer to print version information to
         */
        public void print(IndentedWriter writer)
        {
            for ( Class<?> c : classes )
            {
                String x = Lib.classShortName( c );
                fields( writer, x, c );
            }
        }

        private static void fields(IndentedWriter writer, String prefix, Class< ? > cls)
        {
            for ( String field : fields )
            {
                printField( writer, prefix, field, cls );
            }
        }
        
        private static String field(String fieldName, Class< ? > cls)
        {
            try
            {
                Field f = cls.getDeclaredField(fieldName) ;
                return f.get(null).toString() ;
            } catch (IllegalArgumentException ex)
            {
                ex.printStackTrace();
            } catch (IllegalAccessException ex)
            {
                ex.printStackTrace();
            } catch (SecurityException ex)
            {
                ex.printStackTrace();
            } catch (NoSuchFieldException ex)
            {
                ex.printStackTrace();
            }
            return "<error>" ;
        }
            
        private static void printField(IndentedWriter out, String prefix, String fieldName, Class< ? > cls)
        {
            out.print(prefix) ;
            out.print(": ") ;
            out.pad(12) ;
            out.print(fieldName) ;
            out.print(": ") ;
            out.print(field(fieldName, cls)) ;
            out.println() ;
            out.flush();
        }
    }
}
