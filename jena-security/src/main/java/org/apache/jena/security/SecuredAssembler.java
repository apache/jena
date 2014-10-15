/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.security;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.JA;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerGroup;
import com.hp.hpl.jena.assembler.assemblers.ModelAssembler;
import com.hp.hpl.jena.assembler.exceptions.AssemblerException;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.util.MappingRegistry;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Assembler for a secured model.
 *  
 * <p>
 * The assembler file should include the following
 * <code><pre>
 * <>; ja:loadClass	"org.apache.jena.security.SecuredAssembler" .
 * 
 * sec:Model rdfs:subClassOf ja:NamedModel .
 * </pre></code>
 * 
 * The model definition should include something like.
 * 
 * <code><pre>
 * [] a ja:Model ;
 *    sec:baseModel jena_model ;
 *    ja:modelName "modelName";
 *    sec:evaluatorFactory "javaclass";
 *    .
 * </pre></code>
 * 
 * Terms used in above example:
 * <dl>
 * <dt>
 * jena_model</dt><dd>Another model defined in the assembler file.
 * </dd><dt>
 * "modelName"</dt><dd>The name of the model as identified in the security manager
 * </dd><dt>
 * "javaclass"</dt><dd>The name of a java class that implements a Evaluator Factory.  The Factory must have 
 * static method <code>getInstance()</code> that returns a SecurityEvaluator.
 * </dd>
 * </dl>
 * </p>
 * 
 */
public class SecuredAssembler extends ModelAssembler {
	private static boolean initialized;
	
	public static final String URI = "http://apache.org/jena/security/Assembler#";
	/**
	 * Property named URI+"evaluatorFactory"
	 */
	public static final Property EVALUATOR_FACTORY =  
			ResourceFactory.createProperty( URI + "evaluatorFactory" );
	/**
	 * Property named URI+"Model"
	 */
	public static final Property SECURED_MODEL = ResourceFactory.createProperty( URI + "Model" ); 
	/**
	 * Property named URI+"baseModel"
	 */	
    public static final Property BASE_MODEL = ResourceFactory.createProperty( URI + "baseModel" ); 
	
    // message formats
    private static final String NO_X_PROVIDED = "No %s provided for %s";
    private static final String ERROR_FINDING_FACTORY = "Error finding factory class %s:  %s";
    
	static { init() ; }
    
	/**
	 * Initialize the assembler.
	 * Registers the prefix "sec" with the uri http://apache.org/jena/security/Assembler#
	 * and registers this assembler with the uri http://apache.org/jena/security/Assembler#Model
	 */
    static synchronized public void init()
    {
        if ( initialized )
            return ;
        MappingRegistry.addPrefixMapping("sec", URI) ;
        registerWith(Assembler.general) ;
        initialized = true ;
    }
    
    /**
     * Register this assembler in the assembler group.
     * @param group The assembler group to register with.
     */
    static void registerWith(AssemblerGroup group)
    {
    	if ( group == null )
            group = Assembler.general ;
        group.implementWith( SECURED_MODEL, new SecuredAssembler()) ;  
    }
	
	@Override
	public Model open(Assembler a, Resource root, Mode mode) {

		Resource rootModel = getUniqueResource( root, BASE_MODEL );
		if (rootModel == null)
		{
			throw new AssemblerException( root, String.format( NO_X_PROVIDED, BASE_MODEL, root ));
		}
		Model baseModel = a.openModel(rootModel, mode); 
	
		Literal modelName = getUniqueLiteral( root, JA.modelName );
		if (modelName == null)
		{
			throw new AssemblerException( root, String.format( NO_X_PROVIDED, JA.modelName, root ));
		}
		
		Literal factoryName = getUniqueLiteral( root, EVALUATOR_FACTORY );
		if (factoryName == null)
		{
			throw new AssemblerException( root, String.format( NO_X_PROVIDED, EVALUATOR_FACTORY, root ));
		}
		SecurityEvaluator securityEvaluator = null;
		try
		{
			Class<?> factoryClass = Class.forName( factoryName.getString() );
			Method method = factoryClass.getMethod("getInstance" );
			if ( ! SecurityEvaluator.class.isAssignableFrom(method.getReturnType()))
			{
				throw new AssemblerException( root, String.format( "%s (found at %s for %s) getInstance() must return an instance of SecurityEvaluator", factoryName, EVALUATOR_FACTORY, root ));
			}
			if ( ! Modifier.isStatic( method.getModifiers()))
			{
				throw new AssemblerException( root, String.format( "%s (found at %s for %s) getInstance() must be a static method", factoryName, EVALUATOR_FACTORY, root ));			
			}
			securityEvaluator = (SecurityEvaluator) method.invoke( null );
		}
		catch (SecurityException e)
		{
			throw new AssemblerException( root, String.format( ERROR_FINDING_FACTORY, factoryName, e.getMessage() ), e);			
		}
		catch (IllegalArgumentException e)
		{
			throw new AssemblerException( root, String.format( ERROR_FINDING_FACTORY, factoryName, e.getMessage() ), e);			
		}
		catch (ClassNotFoundException e)
		{
			throw new AssemblerException( root, String.format( "Class %s (found at %s for %s) could not be loaded", factoryName, EVALUATOR_FACTORY, root ));
		}
		catch (NoSuchMethodException e)
		{
			throw new AssemblerException( root, String.format( "%s (found at %s for %s) must implement a static getInstance() that returns an instance of SecurityEvaluator", factoryName, EVALUATOR_FACTORY, root ));
		}
		catch (IllegalAccessException e)
		{
			throw new AssemblerException( root, String.format( ERROR_FINDING_FACTORY, factoryName, e.getMessage() ), e);			
		}
		catch (InvocationTargetException e)
		{
			throw new AssemblerException( root, String.format( ERROR_FINDING_FACTORY, factoryName, e.getMessage() ), e);			
		}

		return Factory.getInstance(securityEvaluator, modelName.asLiteral().getString(), baseModel);
			
	}

	@Override
	protected Model openEmptyModel(Assembler a, Resource root, Mode mode) {
		return open(a, root, mode);
	}


}
