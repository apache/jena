/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: SimpleSelector.java,v 1.10 2003-07-22 07:19:55 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.graph.*;

/** 
    A general selector class for use when querying models.
 
    <p>An instance of this class is passed with query calls to models.  The model
    will use the <CODE>test</CODE> method of this class to decide whether
    a statement should be included in the selection.
    
    <p>Instances of this class can be provided with subject, predicate and object
    constraints.  If a subject, a predicate or an object are provided,
    the model implementation <b>may</b> restrict the statements that it tests
    to statements whose subject, predicate and object match those provided in
    the constructor.  This can provide for considerably more efficient
    searching.  However, the model implementation is not required to do this.
    If no subject, predicate or object are provided in
    the constructor, then all statements in the model must be tested.
 
    <p>This class is designed to be subclassed by the application, defining
    defining further selection criteria of its own by providing its own
    <CODE>selects</CODE> method.
    
    <p>A direct instance of SimpleSelector returns <code>true</code> for the
    Selector::isSimple() predicate. Instances of subclasses of SimpleSelector
    return <code>false</code>, since the only reason to have such subclasses
    is to provide a non-trivial <code>test</code> predicate or S/P/O tests other
    than equality.
 
    <p>The <CODE>test</CODE> method first verifies that a statement satisfies
    any subject, predicate or object constraints and the calls the <CODE>
    selects</CODE> method to test for any application supplied constraint.  The
    default <CODE>selects</CODE> method simply returns true.
    
    @author bwm
    @version Release='$Name: not supported by cvs2svn $ $Revision: 1.10 $ $Date: 2003-07-22 07:19:55 $
*/

public class SimpleSelector extends Object implements Selector {

    protected Resource subject;
    protected Property predicate;
    protected RDFNode  object;
    
    /** Create a selector.  Since no subject, predicate or object constraints are
     * specified a model will test all statements.
     */
   public  SimpleSelector() {
        subject = null;
        predicate = null;
        object = null;
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
    public SimpleSelector(Resource subject, Property predicate, RDFNode object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
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
    public SimpleSelector(Resource subject, Property predicate, boolean object) {
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
    public SimpleSelector(Resource subject, Property predicate, long object) {
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
    public SimpleSelector(Resource subject, Property predicate, char object) {
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
    public SimpleSelector(Resource subject, Property predicate, float object) {
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
    public SimpleSelector(Resource subject, Property predicate, double object) {
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
    public SimpleSelector(Resource subject, Property predicate, String object) {
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
    public SimpleSelector(Resource subject, Property predicate, 
                      String object, String language) {
        this.subject = subject;
        this.predicate = predicate;
        if (object != null) {
          this.object = literal( object, language );
        } else {
          this.object = null;
        }
    }
    
    private Literal literal( String s, String lang )
        { return new LiteralImpl( Node.createLiteral( s, lang, false ), (Model) null ); }
    
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
    public SimpleSelector(Resource subject, Property predicate, Object object) {
        this.subject = subject;
        this.predicate = predicate;
        if (object != null) {
          this.object = literal( object.toString(), "" );
        } else {
          this.object = null;
        }
    }
    
    /** Return the subject constraint of this selector.
     * @return the subject constraint
     */
    public Resource getSubject() { return subject; }
    /** Return the predicate constraint of this selector.
     * @return the predicate constraint
     */
    public Property getPredicate() { return predicate; }
    /** Return the object constraint of this selector.
     * @return the object constraint
     */
    public RDFNode  getObject() { return object; }
    
    /**
        Answer true iff this Selector is completely characterised by its
        S/P/O triple. Subclasses will by default return false, so this method need not
        be over-ridden (the only reason for subclassing SimpleSelector is to make
        a test not dependent only on the S/P/O identity).
        
        @return true iff this selector only depends on S/P/O identity.
    */
    public boolean isSimple()
        { return this.getClass() == SimpleSelector.class; }
        
    /** Test whether a statement should be included in a selection.  This method
     * tests whether the supplied statement satisfies the subject, predicate and
     * object constraints of the selector and then tests whether it matches the
     * application provided <CODE>selects</CODE> method.
     * @param s the statement to be tested
     * @return true if the statement satisfies the subject, object
     * and predicate constraints and the selects constraint.
     */
    public boolean test(Statement s) {
       return (subject == null || subject.equals(s.getSubject()))
            && (predicate == null || predicate.equals(s.getPredicate()))
            && (object == null || object.equals(s.getObject()))
            && selects(s);
    }
    
    /** This method is designed to be over ridden by subclasses to define application
     * specific constraints on the statements selected.
     * @param s the statement to be tested
     * @return true if the statement satisfies the constraint
     */
    public boolean selects(Statement s) {
        return true;
    }
    
}
/*
 *  (c) Copyright Hewlett-Packard Company 2000 - 2003
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