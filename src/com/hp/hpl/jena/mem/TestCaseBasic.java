/*
    (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
    [See end of file]
    $Id: TestCaseBasic.java,v 1.3 2003-09-09 14:24:43 chris-dollin Exp $
*/
 
package com.hp.hpl.jena.mem;

/**
 *
 * @author  bwm
 * @version $Name: not supported by cvs2svn $ $Revision: 1.3 $ $Date: 2003-09-09 14:24:43 $
 */

public class TestCaseBasic extends com.hp.hpl.jena.regression.TestCaseBasic {

    public TestCaseBasic(String name) {
        super(name);
    }
    
    public void setUp() {
        m1 = new ModelMem();
        m2 = new ModelMem();
        m3 = new ModelMem();
        m4 = new ModelMem();
    }
}

/*
 *  (c) Copyright 2000, 2001 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
 *
 * $Id: TestCaseBasic.java,v 1.3 2003-09-09 14:24:43 chris-dollin Exp $
 */
