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

package arq;
import java.util.Iterator ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;
import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdLineArgs ;

public class TestCmdLine extends BaseTest
{
    @Test public void test_Simple1()
    {
        String args[] = new String[]{""} ;
        CmdLineArgs cl = new CmdLineArgs(args) ;
        cl.process() ;
    }
    
    @Test public void test_Flag1()
    {
        String args[] = new String[]{ ""} ;
        CmdLineArgs cl = new CmdLineArgs(args) ;
        ArgDecl argA = new ArgDecl(false, "-a") ;
        cl.add(argA) ;
        cl.process() ;
        assertTrue("-a argument found" , ! cl.contains(argA) ) ; 
    }
    
    @Test public void test_Flag2()
    {
        String args[] = new String[]{ "-a"} ;
        CmdLineArgs cl = new CmdLineArgs(args) ;
        ArgDecl argA = new ArgDecl(false, "-a") ;
        cl.add(argA) ;
        cl.process() ;
        assertTrue("No -a argument found" , cl.contains(argA) ) ; 
    }

    @Test public void test_Flag3()
    {
        String args[] = new String[]{ "-a", "filename"} ;
        CmdLineArgs cl = new CmdLineArgs(args) ;
        ArgDecl argA = new ArgDecl(false, "-a") ;
        cl.add(argA) ;
        cl.process() ;
        assertTrue("No -a argument found" , cl.contains(argA) ) ; 
    }
    
    @Test public void test_Arg1()
    {
        String args[] = new String[]{ ""} ;
        CmdLineArgs cl = new CmdLineArgs(args) ;
        ArgDecl argA = new ArgDecl(true, "-arg") ;
        cl.add(argA) ;
        cl.process() ;
        assertTrue("-arg argument found" , ! cl.contains(argA) ) ; 
    }
    
    @Test public void test_Arg2()
    {
        String args[] = new String[]{ "-arg=ARG", "filename"} ;
        CmdLineArgs cl = new CmdLineArgs(args) ;
        ArgDecl argA = new ArgDecl(true, "arg") ;
        cl.add(argA) ;
        cl.process() ;
        assertTrue("No -arg= argument found" , cl.contains(argA) ) ; 
        assertEquals("", cl.getValue(argA) , "ARG") ;
        assertEquals("", cl.getArg("arg").getValue() , "ARG") ;
    }
    
    @Test public void test_nArg1()
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
