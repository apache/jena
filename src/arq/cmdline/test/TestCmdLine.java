/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline.test;
import java.util.* ; 

import arq.cmdline.*;
import junit.framework.*;

/** 
 * @author Andy Seaborne
 */

public class TestCmdLine extends TestCase
{
    public void test_Simple1()
    {
        String args[] = new String[]{ ""} ;
        CmdLineArgs cl = new CmdLineArgs(args) ;
        cl.process() ;
    }
    
    public void test_Flag1()
    {
        String args[] = new String[]{ ""} ;
        CmdLineArgs cl = new CmdLineArgs(args) ;
        ArgDecl argA = new ArgDecl(false, "-a") ;
        cl.add(argA) ;
        cl.process() ;
        assertTrue("-a argument found" , ! cl.contains(argA) ) ; 
    }
    
    public void test_Flag2()
    {
        String args[] = new String[]{ "-a"} ;
        CmdLineArgs cl = new CmdLineArgs(args) ;
        ArgDecl argA = new ArgDecl(false, "-a") ;
        cl.add(argA) ;
        cl.process() ;
        assertTrue("No -a argument found" , cl.contains(argA) ) ; 
    }

    public void test_Flag3()
    {
        String args[] = new String[]{ "-a", "filename"} ;
        CmdLineArgs cl = new CmdLineArgs(args) ;
        ArgDecl argA = new ArgDecl(false, "-a") ;
        cl.add(argA) ;
        cl.process() ;
        assertTrue("No -a argument found" , cl.contains(argA) ) ; 
    }
    
    public void test_Arg1()
    {
        String args[] = new String[]{ ""} ;
        CmdLineArgs cl = new CmdLineArgs(args) ;
        ArgDecl argA = new ArgDecl(true, "-arg") ;
        cl.add(argA) ;
        cl.process() ;
        assertTrue("-arg argument found" , ! cl.contains(argA) ) ; 
    }
    
    public void test_Arg2()
    {
        String args[] = new String[]{ "-arg=ARG", "filename"} ;
        CmdLineArgs cl = new CmdLineArgs(args) ;
        ArgDecl argA = new ArgDecl(true, "-arg") ;
        cl.add(argA) ;
        cl.process() ;
        assertTrue("No -arg= argument found" , cl.contains(argA) ) ; 
        assertEquals("", cl.getValue(argA) , "ARG") ;
        assertEquals("", cl.getArg("arg").getValue() , "ARG") ;
    }
    
    public void test_nArg1()
    {
        String args[] = new String[]{ "-arg=V1", "--arg=V2", "-v"} ;
        CmdLineArgs cl = new CmdLineArgs(args) ;
        ArgDecl argA = new ArgDecl(true, "-arg") ;
        cl.add(argA) ;
        ArgDecl argV = new ArgDecl(false, "-v") ;
        cl.add(argV) ;
        cl.process() ;
        assertTrue("No -arg= argument found" , cl.contains(argA) ) ;
        
        Iterator<String> iter = cl.getValues("arg").iterator() ;
        assertEquals("Argument 1", iter.next() , "V1") ;
        assertEquals("Argument 2", iter.next() , "V2") ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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