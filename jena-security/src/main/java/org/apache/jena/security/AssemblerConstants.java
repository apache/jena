package org.apache.jena.security;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public interface AssemblerConstants {
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
	/**
	 * Property named URI+"Evaluator"
	 */
    public static final Property EVALUATOR_ASSEMBLER = ResourceFactory.createProperty( URI+"Evaluator" ); 
    /**
	 * Property named URI+"evaluatorImpl"
	 */
	public static final Property EVALUATOR_IMPL =  
			ResourceFactory.createProperty( URI + "evaluatorImpl" );
	
	/**
	 * Property named URI+"evaluatorClass"
	 */
	public static final Property EVALUATOR_CLASS =  
			ResourceFactory.createProperty( URI + "evaluatorClass" );
	   /**
		 * Property named URI+"evaluatorImpl"
		 */
	public static final Property ARGUMENT_LIST =  
				ResourceFactory.createProperty( URI + "args" );
	
	// message formats
    public static final String NO_X_PROVIDED = "No %s provided for %s";
}
