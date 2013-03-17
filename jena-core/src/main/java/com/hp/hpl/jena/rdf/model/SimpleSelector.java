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
        { return new LiteralImpl( NodeFactory.createLiteral( s, lang, false ), (ModelCom) null ); }
    
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
    @Override
    public Resource getSubject() { return subject; }
    /** Return the predicate constraint of this selector.
     * @return the predicate constraint
     */
    @Override
    public Property getPredicate() { return predicate; }
    /** Return the object constraint of this selector.
     * @return the object constraint
     */
    @Override
    public RDFNode  getObject() { return object; }
    
    /**
        Answer true iff this Selector is completely characterised by its
        S/P/O triple. Subclasses will by default return false, so this method need not
        be over-ridden (the only reason for subclassing SimpleSelector is to make
        a test not dependent only on the S/P/O identity).
        
        @return true iff this selector only depends on S/P/O identity.
    */
    @Override
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
    @Override
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
