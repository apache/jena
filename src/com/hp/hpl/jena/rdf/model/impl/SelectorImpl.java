/*
 *  (c) Copyright 2000  Hewlett-Packard Development Company, LP
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
 * SimpleSelector.java
 *
 * Created on 25 August 2000, 10:12
 */

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;

/** A general selector class for use when querying models.
 * 
 * <p>OBSOLETE: use SimpleSelector. This implementation is a stub that provides
 * only constructors.
 * 
 * <p>An instance of this class is passed with query calls to models.  The model
 * will use the <CODE>test</CODE> method of this class to decide whether
 * a statement should be included in the selection.</p>
 * <p>Instances of this class can be provided with subject, predicate and object
 *   constraints.  If a subject, a predicate or an object are provided,
 *   the model implementation <b>may</b> restrict the statements that it tests
 *   to statements whose subject, predicate and object match those provided in
 *   the constructor.  This can provide for considerably more efficient
 * searching.  However, the model implementation is not required to do this.
 *  If no subject, predicate or object are provided in
 * the constructor, then all statements in the model must be tested.</p>
 * <p>This class is designed to be subclassed by the application, defining
 * defining further selection criteria of its own by providing its own
 * <CODE>selects</CODE> method.</p>
 * <p>The <CODE>test</CODE> method first verifies that a statement satisfies
 * any subject, predicate or object constraints and the calls the <CODE>
 * selects</CODE> method to test for any application supplied constraint.  The
 * default <CODE>selects</CODE> method simply returns true.</p>
 * @author bwm, kers
 * @version Release='$Name: not supported by cvs2svn $ $Revision: 1.5 $ $Date: 2003-08-27 13:05:53 $
 */

public final class SelectorImpl extends SimpleSelector  {

    /** Create a selector.  Since no subject, predicate or object constraints are
     * specified a model will test all statements.
     */
   public  SelectorImpl() {
        super();
    }
    
    /** Create a selector.  A model <b>may</b> restrict statements that are tested using
     * the <CODE>selects</CODE> method to those whose subject matches the
     * subject parameter, whose predicate matches the predicate parameter and whose
     * object matches the object paramater.  Any null parameter is considered to
     * match anything.
     * @param subject if not null, the subject of selected statements
     * must equal this argument.
     * @param predicate if not null, the predicate of selected statements
     * must equal this argument.
     * @param object if not null, the object of selected statements
     * must equal this argument.
     */
    public SelectorImpl(Resource subject, Property predicate, RDFNode object) {
        super( subject, predicate, object );
    }
    
    /** Create a selector.  A model <b>may</b> restrict statements that are tested using
     * the <CODE>selects</CODE> method to those whose subject matches the
     * subject parameter, whose predicate matches the predicate parameter and whose
     * object matches the object paramater.  Any null parameter is considered to
     * match anything.
     * @param subject if not null, the subject of selected statements
     * must equal this argument.
     * @param predicate if not null, the predicate of selected statements
     * must equal this argument.
     * @param object if not null, the object of selected statements
     * must equal this argument.
     */    
    public SelectorImpl(Resource subject, Property predicate, boolean object) {
        this(subject, predicate, String.valueOf( object ) );
    }
    
    /** Create a selector.  A model <b>may</b> restrict statements that are tested using
     * the <CODE>selects</CODE> method to those whose subject matches the
     * subject parameter, whose predicate matches the predicate parameter and whose
     * object matches the object paramater.  Any null parameter is considered to
     * match anything.
     * @param subject if not null, the subject of selected statements
     * must equal this argument.
     * @param predicate if not null, the predicate of selected statements
     * must equal this argument.
     * @param object  the object of selected statements
     * must equal this argument.
     */        
    public SelectorImpl(Resource subject, Property predicate, long object) {
        this(subject, predicate, String.valueOf( object ) );
    }
    
    /** Create a selector.  A model <b>may</b> restrict statements that are tested using
     * the <CODE>selects</CODE> method to those whose subject matches the
     * subject parameter, whose predicate matches the predicate parameter and whose
     * object matches the object paramater.  Any null parameter is considered to
     * match anything.
     * @param subject if not null, the subject of selected statements
     * must equal this argument.
     * @param predicate if not null, the predicate of selected statements
     * must equal this argument.
     * @param object the object of selected statements
     * must equal this argument.
     */        
    public SelectorImpl(Resource subject, Property predicate, char object) {
        this(subject, predicate, String.valueOf( object ) );
    }
    
    /** Create a selector.  A model <b>may</b> restrict statements that are tested using
     * the <CODE>selects</CODE> method to those whose subject matches the
     * subject parameter, whose predicate matches the predicate parameter and whose
     * object matches the object paramater.  Any null parameter is considered to
     * match anything.
     * @param subject if not null, the subject of selected statements
     * must equal this argument.
     * @param predicate if not null, the predicate of selected statements
     * must equal this argument.
     * @param object the object of selected statements
     * must equal this argument.
     */        
    public SelectorImpl(Resource subject, Property predicate, float object) {
        this(subject, predicate, String.valueOf( object ) );
    }
    
    /** Create a selector.  A model <b>may</b> restrict statements that are tested using
     * the <CODE>selects</CODE> method to those whose subject matches the
     * subject parameter, whose predicate matches the predicate parameter and whose
     * object matches the object paramater.  Any null parameter is considered to
     * match anything.
     * @param subject if not null, the subject of selected statements
     * must equal this argument.
     * @param predicate if not null, the predicate of selected statements
     * must equal this argument.
     * @param object the object of selected statements
     * must equal this argument.
     */        
    public SelectorImpl(Resource subject, Property predicate, double object) {
        this(subject, predicate, String.valueOf( object ) );
    }
    
    /** Create a selector.  A model <b>may</b> restrict statements that are tested using
     * the <CODE>selects</CODE> method to those whose subject matches the
     * subject parameter, whose predicate matches the predicate parameter and whose
     * object matches the object paramater.  Any null parameter is considered to
     * match anything.
     * @param subject if not null, the subject of selected statements
     * must equal this argument.
     * @param predicate if not null, the predicate of selected statements
     * must equal this argument.
     * @param object the object of selected statements
     * must equal this argument - a null string matches the empty string
     */        
    public SelectorImpl(Resource subject, Property predicate, String object) {
        this( subject, predicate, object, "" );
    }
    
    /** Create a selector.  A model <b>may</b> restrict statements that are tested using
     * the <CODE>selects</CODE> method to those whose subject matches the
     * subject parameter, whose predicate matches the predicate parameter and whose
     * object matches the object paramater.  Any null parameter is considered to
     * match anything.
     * @param subject if not null, the subject of selected statements
     * must equal this argument.
     * @param predicate if not null, the predicate of selected statements
     * must equal this argument.
     * @param object the object of selected statements
     * must equal this argument - the null string matches the empty string
     * @param language the language of the object constraint
     */        
    public SelectorImpl(Resource subject, Property predicate, 
                      String object, String language) {
        super( subject, predicate, object, language );
    }
    
    /** Create a selector.  A model <b>may</b> restrict statements that are tested using
     * the <CODE>selects</CODE> method to those whose subject matches the
     * subject parameter, whose predicate matches the predicate parameter and whose
     * object matches the object paramater.  Any null parameter is considered to
     * match anything.
     * @param subject if not null, the subject of selected statements
     * must equal this argument.
     * @param predicate if not null, the predicate of selected statements
     * must equal this argument.
     * @param object if not null, the object of selected statements
     * must equal this argument.
     */        
    public SelectorImpl(Resource subject, Property predicate, Object object) {
        super( subject, predicate, object );
    }
        
    /**
        Answer true to the question "is this a simple selector". Otherwise the default for
        SimpleSelector subclasses, false, would apply.
    */
    public boolean isSimple()
        { return true; }
    
}