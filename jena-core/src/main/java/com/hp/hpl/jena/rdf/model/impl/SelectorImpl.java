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
    @Override
    public boolean isSimple()
        { return true; }
    
}
