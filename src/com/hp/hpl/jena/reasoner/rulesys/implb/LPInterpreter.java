/******************************************************************
 * File:        LPBRuleEngine.java
 * Created by:  Dave Reynolds
 * Created on:  21-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: LPInterpreter.java,v 1.1 2003-07-21 20:41:18 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.*;

import org.apache.log4j.Logger;

/**
 * Bytecode interpeter engine for the LP version of the backward
 * chaining rule system. An instance of this is forked off for each
 * parallel query.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-07-21 20:41:18 $
 */
public class LPInterpreter {
    
//  =======================================================================
//  Variables

    /** The engine which is using this interpreter */
    protected LPBRuleEngine engine;
    
    /** Set to true to flag that derivations should be logged */
    protected boolean recordDerivations;
    
    /** True if the engine has terminated */
    protected boolean isComplete = false;
    
    /** log4j logger*/
    static Logger logger = Logger.getLogger(LPInterpreter.class);
    
//  =======================================================================
//  Constructors

    /**
     * Constructor.
     * @param engine the engine which is calling this interpreter
     * @param goal the query to be satisfied
     */
    public LPInterpreter(LPBRuleEngine engine, TriplePattern goal) {
        this.engine = engine;
        // TODO initialize using rulestore
    }

//  =======================================================================
//  Control methods
    
    /**
     * Stop the current work. This is called if the top level results iterator has
     * either finished or the calling application has had enough. 
     */
    public synchronized void close() {
        isComplete = true;
        engine.detach(this);
    }
           
    /**
     * Set to true to enable derivation caching
     */
    public void setDerivationLogging(boolean recordDerivations) {
        this.recordDerivations = recordDerivations;
    }
    
    /**
     * Return the next result from this engine.
     * @return either a StateFlag or  a result Triple
     */
    public Object next() {
        return StateFlag.FAIL;
    }
    
//  =======================================================================
//  Engine implementation  

}



/*
    (c) Copyright Hewlett-Packard Company 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/