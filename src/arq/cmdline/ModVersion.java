/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.List;

import com.hp.hpl.jena.Jena;

import com.hp.hpl.jena.sparql.util.Utils;

import com.hp.hpl.jena.query.ARQ;

public class ModVersion implements ArgModuleGeneral
{
    protected final ArgDecl versionDecl = new ArgDecl(ArgDecl.NoValue, "version") ;
    protected boolean version = false ;
    protected boolean printAndExit = false ;
    
    private List classes = new ArrayList() ; 
    
    public ModVersion(boolean printAndExit)
    {
        this.printAndExit = printAndExit ;
        addClass(Jena.class) ;
        addClass(ARQ.class) ;
    }
    
    public void addClass(Class c) { classes.add(c) ; }
    
    public void registerWith(CmdGeneral cmdLine)
    {
        cmdLine.add(versionDecl, "--version", "Version information") ;
    }

    public void processArgs(CmdArgModule cmdLine)
    {
        if ( cmdLine.contains(versionDecl) )
            version = true ;
        if ( version && printAndExit )
            printVersionAndExit() ;
    }

    public boolean getVersionFlag() { return version ; }
    
    public void printVersion()
    {
        for ( Iterator iter = classes.iterator() ; iter.hasNext() ; )
        {
            Class c = (Class)iter.next();
            String x = Utils.classShortName(c) ;
            fields(x, c) ;    
        }
    }  
     
    public void printVersionAndExit()
    {
        printVersion() ;
        System.exit(0) ;
    }
    private static String[] fields = { /*"NAME",*/ "VERSION", "BUILD_DATE" } ;
    
    private static void fields(String prefix, Class cls)
    {
        for (int i=0; i < fields.length; i++)
            printField(prefix, fields[i], cls) ;

    }

    private static String field(String fieldName, Class cls)
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
    
    private static void printField(String prefix, String fieldName, Class cls)
    {
        System.out.println(prefix+": "+fieldName+": " + field(fieldName, cls)) ;
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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