/**
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

package org.apache.jena.tools.schemagen;

// Imports
///////////////

import java.lang.reflect.Method;
import java.util.List;

import jena.schemagen;
import jena.schemagen.OptionDefinition;

import com.hp.hpl.jena.rdf.model.*;


/**
 * <p>An extension to the option class built in to {@link schemagen}, in which we
 * allow a two-level defaults hierarchy. Each option is tested against the local
 * object. If the result is <code>true</code> or non-null, or if the object has
 * no parent options object, then the result stands. Otherwise, the option value
 * is delegated to the parent. This allows us to specify global defaults for an
 * entire group of files to be processed with maven, while still allowing each
 * file to have its own local options.
 * </p>
*/
public class SchemagenOptions
    extends schemagen.SchemagenOptionsImpl
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /** The parent options for this options instance */
    private SchemagenOptions parent;

    /* Constructors                    */

    public SchemagenOptions() throws SchemagenOptionsConfigurationException {
        this(null, null);
    }

    public SchemagenOptions(String defaultOutputDir)
            throws SchemagenOptionsConfigurationException {
        this(defaultOutputDir, null);
    }

    public SchemagenOptions(String defaultOutputDir, Source options)
            throws SchemagenOptionsConfigurationException {
        super( new String[]{} );

        //set output to default, source options may override
        if (defaultOutputDir != null) {
            setOption( OPT.OUTPUT, defaultOutputDir );
        }

        //set schemagen options from Maven plugin config Source
        if (options != null) {
            configure(options);
        }
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /**
     * Set the parent options object for this object
     * @param parent Parent options object, or null
     */
    protected void setParent( SchemagenOptions parent ) {
        this.parent = parent;
    }

    /**
     * Return the parent options object, or null
     * @return The parent options object if defined
     */
    public SchemagenOptions getParent() {
        return parent;
    }

    /**
     * Return true if this options object has a parent
     * @return True if parent is defined
     */
    public boolean hasParent() {
        return getParent() != null;
    }

    /**
     * Get the value of the given option, as a string. If the option is not defined
     * locally, return the value of the same option of the parent, if the parent
     * is non-null. Otherwise, return <code>null</code>
     * @param option The name of the option to retrieve
     * @return The value of the option as a string, or null if the option is not defined. If
     * the parent is non-null and the option is not defined, delegate the <code>getOption</code>
     * to the parent.
     */
    public String getStringOption( OPT option ) {
        String v = getStringValue( option );
        return (v != null) ? v : (parent != null ? parent.getStringOption( option ) : null);
    }

    /**
     * Get the value of the given option, as an RDF node. If the option is not defined
     * locally, return the value of the same option of the parent, if the parent
     * is non-null. Otherwise, return <code>null</code>
     * @param option The name of the option to retrieve
     * @return The value of the option as an RDFNode, or null if the option is not defined. If
     * the parent is non-null and the option is not defined, delegate the <code>getOption</code>
     * to the parent.
     */
    public RDFNode getOption( OPT option ) {
        RDFNode v = getValue( option );
        return (v != null) ? v : (parent != null ? parent.getOption( option ) : null);
    }

    /**
     * Set the value of the given option in the local options list
     * @param optionName The option to set, as a string value
     * @param value
     */
    public void setOption( String optionName, String value ) {
        setOption( asOption( optionName ), value );
    }

    /**
     * Set the value of the given option in the local options list
     * @param option The option to set
     * @param value The string value of the option
     */
    public void setOption( OPT option, String value ) {
        OptionDefinition od = getOpt( option );
        getConfigRoot().addProperty( od.getDeclarationProperty(), value );
    }

    /**
     * Set the value of the given option in the local options list
     * @param option The option to set
     * @param value The Boolean value of the option
     */
    public void setOption( OPT option, boolean value ) {
        OptionDefinition od = getOpt( option );
        getConfigRoot().addProperty( od.getDeclarationProperty(), ResourceFactory.createTypedLiteral( value ) );
    }

    /**
     * Set the value of the given option in the local options list
     * @param option The option to set
     * @param value The Resource value of the option
     */
    public void setOption( OPT option, Resource value ) {
        OptionDefinition od = getOpt( option );
        getConfigRoot().addProperty( od.getDeclarationProperty(), value );
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /**
     * Configure SchemagenOptions from Source object
     * @param options Options from Maven configuration
     * @throws SchemagenOptionsConfigurationException
     */
    protected void configure(Source options) throws SchemagenOptionsConfigurationException {
        for(Method method : options.getClass().getMethods()) {
            SchemagenOption schemagenOptionAnnotation =
                    method.getAnnotation(SchemagenOption.class);
            if (schemagenOptionAnnotation != null) {
                Object optionValue = null;
                try {
                    optionValue = method.invoke(options);
                } catch (Exception e) {
                    throw new SchemagenOptionsConfigurationException(e);
                }
                OPT option = schemagenOptionAnnotation.opt();
                if (optionValue != null) {
                    if (optionValue instanceof String) {
                        setOption(option, (String) optionValue);
                    } else if (optionValue instanceof Boolean) {
                        setOption(option, (Boolean) optionValue);
                    } else {
                        throw new IllegalArgumentException("Schemagen options of type "
                                + optionValue.getClass().getCanonicalName()
                                + " are not allowed");
                    }
                }
            }
        }
    }

    protected OPT asOption( String optString ) {
        return OPT.valueOf( optString );
    }

    /**
     * Return true if the given option is set to true, either locally or
     * in the parent options object.
     */
    @Override
    protected boolean isTrue( OPT option ) {
        return super.isTrue( option ) || (hasParent() && getParent().isTrue( option ));
    }

    /**
     * Return true if the given option has a value, either locally or
     * in the parent options object.
     */
    @Override
    protected boolean hasValue( OPT option ) {
        return super.hasValue( option ) || (hasParent() && getParent().hasValue( option ));
    }

    /**
     * Return the value of the option or null, , either locally or
     * from the parent options object.
     */
    @Override
    protected RDFNode getValue( OPT option ) {
        RDFNode v = super.getValue( option );
        return (v == null && hasParent()) ? getParent().getValue( option ) : v;
    }

    /**
     * Return the value of the option or null, , either locally or
     * from the parent options object.
     */
    @Override
    protected String getStringValue( OPT option ) {
        String v = super.getStringValue( option );
        return (v == null && hasParent()) ? getParent().getStringValue( option ) : v;
    }

    /**
     * Return true if the given option has a resource value, either locally or
     * in the parent options object.
     */
    @Override
    protected boolean hasResourceValue( OPT option ) {
        return super.hasResourceValue( option ) || (hasParent() && getParent().hasResourceValue( option ));
    }

    /**
     * Return the value of the option or null, , either locally or
     * from the parent options object.
     */
    @Override
    protected Resource getResource( OPT option ) {
        Resource r =  super.getResource( option );
        return (r == null && hasParent()) ? getParent().getResource( option ) : r;
    }

    /**
     * Return all values for the given options as Strings, either locally or
     * from the parent options object.
     */
    @Override
    protected List<String> getAllValues( OPT option ) {
        List<String> l = super.getAllValues( option );
        return (l.isEmpty() && hasParent()) ? getParent().getAllValues( option ) : l;
    }

}