/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            @package@
 * Web site           @website@
 * Created            Jan 29, 2007
 * Filename           $RCSfile: TestPackage.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2007-01-29 23:49:16 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/

// Package
///////////////
package jena.test;


// Imports
///////////////
import junit.framework.TestSuite;




/**
 * <p>
 * Suite of tests for command line utilities
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: TestPackage.java,v 1.1 2007-01-29 23:49:16 ian_dickinson Exp $)
 */
public class TestPackage
    extends TestSuite
{
    static public TestSuite suite() {
        return new TestPackage();
    }

    /** Creates new TestPackage */
    private TestPackage() {
        super("cmdline");
        addTest( new TestSuite( Test_schemagen.class  ) );
        addTest( new TestSuite( Test_rdfcat.class ));
    }

}


/*
 * (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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
