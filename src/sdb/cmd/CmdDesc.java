/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb.cmd;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import arq.cmdline.CmdLineArgs;

import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.assembler.AssemblerVocab;
import com.hp.hpl.jena.sdb.util.AssemblerUtils;
import com.hp.hpl.jena.sdb.util.Pair;
import com.hp.hpl.jena.util.FileManager;

public class CmdDesc
{
    String classname = null ;
    List<Pair<String, String>> namedArgs = new ArrayList<Pair<String, String>>() ;
    List<String> posnArgs  = new ArrayList<String>() ;
    
    public static CmdDesc read(String filename)
    {
        AssemblerVocab.init() ;
        Model m = FileManager.get().loadModel(filename) ;
        
        return worker(m) ;
    }
    
    public static void run(String filename)
    {
        CmdDesc desc = CmdDesc.read(filename) ;
        System.out.println(desc) ;
        try {
            String cmd = desc.getCmd() ;
            Class c = Class.forName(cmd) ;
            Method m = c.getMethod("main", new Class[]{String[].class}) ;
            m.invoke(null, new Object[]{desc.asStringArray()}) ;
        } catch (Exception ex) { ex.printStackTrace(System.err) ; }
    }
    
    private static CmdDesc worker(Model m)
    {
        Resource r = AssemblerUtils.getResourceByType(m, AssemblerVocab.CommandLineType) ;
        if ( r == null )
            throw new SDBException("Can't find command line description") ;
        return (CmdDesc)AssemblerBase.general.open(r) ;
    }
    
    public void addNamedArg(String name, String value)
    { namedArgs.add(new Pair<String, String>(name, value)) ; }
    
    public void addNamedArg(String name) { addNamedArg(name, null) ; }

    public void addPosn(String x) { posnArgs.add(x) ; }

    public String getCmd() { return classname ; } 

    public void setCmd(String cname)
    { classname = cname ; }
    
    public void set(CmdLineArgs cmdLineArgs)
    {
        for ( Pair<String, String> p : namedArgs )
            cmdLineArgs.addArg(p.car(), p.cdr()) ;
        for ( String a : posnArgs )
            cmdLineArgs.addPositional(a) ;
    }
    
    public String[] asStringArray()
    {
        List<String> x = new ArrayList<String>() ;
        for ( Pair<String, String> p : namedArgs )
        {
            x.add(p.car()) ;
            x.add(p.cdr()) ;
        }
        
        for ( String a : posnArgs )
            x.add(a) ;

        return x.toArray(new String[x.size()]) ;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder() ;
        sb.append(getCmd()) ;
        String sep = " " ;
        for ( Pair<String, String> p : namedArgs )
        {
            sb.append(sep).append(p.car()).append("=").append(p.cdr());
            sep = " " ;
        }
        for ( String a : posnArgs )
        {
            sb.append(sep).append(a) ;
            sep = " " ;
        }
        return sb.toString() ;
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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