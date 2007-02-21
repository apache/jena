/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: OntModelSpecObsolete.java,v 1.2 2007-02-21 09:17:01 chris-dollin Exp $
*/

package com.hp.hpl.jena.ontology;

import java.util.*;

import com.hp.hpl.jena.assembler.AssemblerHelp;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.impl.DriverMap;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.WrappedReasonerFactory;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.vocabulary.*;

/**
    This class holds the necessary machinery for OntModelSpec to continue
    to implement construction-from-ModelSpec-RDF until the next release
    of Jena, after which it will be excised. 
    
    @author kers
 */

public abstract class OntModelSpecObsolete extends ModelSpecImpl
    {
    public OntModelSpecObsolete( ModelMaker maker )
        { super( maker ); }

    public abstract Model implementCreateModelOver( String name );

    protected abstract Model doCreateModel();

    /**
        Answer the RDFS property used to attach this ModelSpec to its ModelMaker; used
        by the parent classes when constructing the RDF description for this Spec.

        @return JenaModelSpec.importMaker
    */
    public Property getMakerProperty() 
        {
        return JenaModelSpec.importMaker;
        }
    
    /**
        Answer a ModelMaker described by the <code>makerProperty</code> of
        <code>root</code> in <code>description</code>; if there is no such statement,
        answer a memory-model maker.
     */
    protected static ModelMaker getMaker( Model description, Resource root, Property makerProperty )    
        {
        Statement mStatement = description.getProperty( root, makerProperty );
        return mStatement == null
            ? ModelFactory.createMemModelMaker()
            : createMaker( mStatement.getResource(), description );
        }
    
    /**
        Answer the unique subject with the given rdf:type.
        
        @param m the model in which the typed subject is sought
        @param type the RDF type the subject must have
        @return the unique S such that (S rdf:type type)
        @exception BadDescriptionException if there's not exactly one subject
    */        
    public static Resource findRootByType( Model description, Resource type )
        { 
        Model m = withSpecSchema( description );
        StmtIterator it = m.listStatements( null, RDF.type, type );
        if (!it.hasNext()) throw new BadDescriptionNoRootException( m, type );
        Resource root = it.nextStatement().getSubject();
        if (it.hasNext()) throw new BadDescriptionMultipleRootsException( m, type );
        return root; 
        }
        
    public static ModelMaker createMaker( Resource root, Model d )
        { return createMakerByRoot( root, withSpecSchema( d ) ); }
        
    public static ModelMaker createMakerByRoot( Resource root, Model fullDesc )
        { 
        Resource type = findMakerType( root, fullDesc );
        ModelMakerCreator mmc = findCreator( type );
        if (mmc == null) throw new RuntimeException( "no maker type" );  
        return mmc.create( fullDesc, root ); 
        }

    private static ModelMakerCreator findCreator( Resource type )
        {
        if (type.equals( JenaModelSpec.MakerSpec )) return new MemMakerCreator();
        if (type.equals( JenaModelSpec.FileMakerSpec )) return new FileMakerCreator();
        if (type.equals( JenaModelSpec.MemMakerSpec )) return new MemMakerCreator();
        if (type.equals( JenaModelSpec.RDBMakerSpec )) return new RDBMakerCreator();
        return null;
        }

    private static Resource findMakerType( Resource root, Model fullDesc )
        {
        return AssemblerHelp.findSpecificType( (Resource) root.inModel( fullDesc ), JenaModelSpec.MakerSpec );
        }

    public static Model withSpecSchema( Model m )
        {
        return withSchema( m, JenaModelSpec.getSchema() );
        }

    /**
        answer a limited RDFS closure of <code>m union schema</code>.
    */
    public static Model withSchema( Model m, Model schema )
        {
        Model result = ModelFactory.createDefaultModel();
        result.add( m );
        addJMSSubclassesFrom( result, schema );        
        addDomainTypes( result, m, schema );
        addSupertypesFrom( result, schema );
        addSupertypesFrom( result, m );
        return result;
        }
    protected static boolean notRDF( Resource resource )
        {
        if (resource.isAnon()) return true;
        if (resource.getNameSpace().equals( RDF.getURI() )) return false;
        if (resource.getNameSpace().equals( RDFS.getURI() )) return false;
        return true;
        }

    protected static final RDFNode nullObject = (RDFNode) null;

    protected static void addJMSSubclassesFrom( Model result, Model schema )
        {
        for (StmtIterator it = schema.listStatements( null, RDFS.subClassOf, nullObject ); it.hasNext();)
            { 
            Statement s = it.nextStatement();
            if (notRDF( s.getSubject() ) && notRDF( s.getResource() )) result.add( s ); 
            }
        }

    protected static void addDomainTypes( Model result, Model m, Model schema )
        {
        for (StmtIterator it = schema.listStatements( null, RDFS.domain, nullObject ); it.hasNext();)
            {
            Statement s = it.nextStatement();
            Property property = (Property) s.getSubject().as( Property.class );
            for (StmtIterator x = m.listStatements( null, property, nullObject ); x.hasNext();)
                {
                Statement t = x.nextStatement();
                // System.err.println( ">> adding domain type: subject " + t.getSubject() + ", type " + s.getObject() + " because of property " + property );
                result.add( t.getSubject(), RDF.type, s.getObject() );
                }
            }
        }

    protected static void addSupertypesFrom( Model result, Model source )
        {
        Model temp = ModelFactory.createDefaultModel();
        for (StmtIterator it = result.listStatements( null, RDF.type, nullObject ); it.hasNext();)
            {
            Statement s = it.nextStatement();
            for (StmtIterator subclasses = source.listStatements( s.getResource(), RDFS.subClassOf, nullObject ); subclasses.hasNext();)
                {
                RDFNode type = subclasses.nextStatement().getObject();
                // System.err.println( ">> adding super type: subject " + s.getSubject() + ", type " + type );
                temp.add( s.getSubject(), RDF.type, type );
                }
            }
        result.add( temp );
        }

    /**
         Answer a ReasonerFactory described by the properties of the resource
         <code>R</code> in the model <code>rs</code>. Will throw 
         NoReasonerSuppliedException if no jms:reasoner is supplied, or
         NoSuchReasonerException if the reasoner value isn't known to
         ReasonerRegistry. If any <code>ruleSetURL</code>s are supplied, the
         reasoner factory must be a RuleReasonerFactory, and is wrapped so that
         the supplied rules are specific to this Factory.
    */
    public static ReasonerFactory getReasonerFactory( Resource R, Model rs )
        {
        StmtIterator r = rs.listStatements( R, JenaModelSpec.reasoner, (RDFNode) null );
        if (r.hasNext() == false) throw new NoReasonerSuppliedException();
        Resource rr = r.nextStatement().getResource();
        String rrs = rr.getURI();
        ReasonerFactory rf = ReasonerRegistry.theRegistry().getFactory( rrs );
        if (rf == null) throw new NoSuchReasonerException( rrs );
        return new WrappedReasonerFactory( rf, ((Resource) R.inModel( rs )) );
        }

    /**
        Answer a ReasonerFactory as described by the reasonsWith part of this discription,
        or null if no reasoner specification has been supplied.

        @param description the description of this OntModel
        @param root the root of this OntModel's description
        @return  a ReasonerFactory with URI given by root's reasonsWith's reasoner.
    */
    public static ReasonerFactory getReasonerFactory( Model description, Resource root ) 
        {
        Statement factStatement = description.getProperty( root, JenaModelSpec.reasonsWith );
        if (factStatement == null) return null;
        return OntModelSpec.getReasonerFactory( factStatement.getResource(), description );
        }

    /**
        Answer an OntDocumentManager satisfying the docManager part of this description.
        Currently restricted to one where the object of JenaModelSpec.docManager is registered with
        the value table held in ModelSpecImpl. If there's no such property, or if its bnode
        has no associated value, returns null.

         @param description the description of the OntModel
         @param root the root of the description
         @return the OntDocumentManager of root's JenaModelSpec.docManager
    */
    public static OntDocumentManager getDocumentManager( Model description, Resource root ) 
        {
        Statement docStatement = description.getProperty( root, JenaModelSpec.docManager );
        if (docStatement == null) return null;
        Resource manager = docStatement.getResource();
        Statement policy = description.getProperty( manager, JenaModelSpec.policyPath );
        if (policy == null)
            return (OntDocumentManager) getValue( manager );
        else
            return new OntDocumentManager( policy.getString() );
        }

    /**
     * </p>Answer the ModelMaker to be used to construct models that are used for
     * the imports of an OntModel. The ModelMaker is specified by the properties of
     * the resource which is the object of the root's <code>jms:importMaker</code> property.
     * If no importMaker is specified, a MemModelMaker is returned as a default.</p>
     * @param description the description model for this OntModel
     * @param root the root of the description for the OntModel
     * @return a ModelMaker fitting the given description
     */
    public static ModelMaker getImportMaker( Model description, Resource root ) 
        {
        return getMaker( description, root, JenaModelSpec.importMaker );
        }

    /**
     * </p>Answer the ModelMaker to be used to construct models that are used for
     * the base model of an OntModel. The ModelMaker is specified by the properties of
     * the resource which is the object of the root's <code>jms:maker</code> property.
     * If no importMaker is specified, a MemModelMaker is returned as a default.</p>
     * @param description the description model for this OntModel
     * @param root the root of the description for the OntModel
     * @return a ModelMaker fitting the given description
     */
    public static ModelMaker getBaseMaker( Model description, Resource root ) 
        {
        return getMaker( description, root, JenaModelSpec.maker );
        }

    /**
        Answer the value of the jms:modelName property of <code>root</code>,
        or <code>null</code> id there isn't one.
    */
    protected static String getBaseModelName( Model description, Resource root ) 
        {
        Statement s = description.getProperty( root, JenaModelSpec.modelName );
        return s == null ? null : s.getString();
        }

    /**
        Answer the URI string of the ontology language in this description.

        @param description the Model from which to extract the description
        @return the language string
        @exception something if the value isn't a URI resource
    */
    public static String getLanguage( Model description, Resource root ) 
        {
        Statement langStatement = description.getRequiredProperty( root, JenaModelSpec.ontLanguage );
        return langStatement.getResource().getURI();
        }
    
    /**
        Augment the description with that of our language
        @param d the description to augment
        @param me the resource to use to represent this OntModelSpec
        @param langURI the language URI
    */
    protected void addLanguageDescription( Model d, Resource me, String langURI ) 
        {
        d.add( me, JenaModelSpec.ontLanguage, d.createResource( langURI ) );
        }

    protected void addImportsDescription( Model d, Resource me, ModelMaker m ) 
        {
        Resource importSelf = d.createResource();
        d.add( me, JenaModelSpec.importMaker, importSelf );
        m.addDescription( d, importSelf );
        }
    
    /**
        Augment the description with that of our document manager [as a Java value]
        @param d the description to augment
        @param me the resource to use to represent this OntModelSpec
        @param man the document manager
    */
    protected  void addManagerDescription( Model d, Resource me, OntDocumentManager man ) 
        {
        d.add( me, JenaModelSpec.docManager, createValue( man ) );
        }

    /**
        Augment the description with that of our reasoner factory
        @param d the description to augment
        @param me the resource to use to represent this OntModelSpec
        @param rf the reasoner factory to describe
    */
    protected void addReasonerDescription( Model d, Resource me, ReasonerFactory rf ) 
        {
        Resource reasonerSelf = d.createResource();
        d.add( me, JenaModelSpec.reasonsWith, reasonerSelf );
        if (rf != null)
            d.add( reasonerSelf, JenaModelSpec.reasoner, d.createResource( rf.getURI() ) );
        }

    protected Model addDescription( Model d, Resource self, ModelMaker importsMaker, String language, OntDocumentManager documentManager, ReasonerFactory reasonerFactory )
        {
        addMakerDescription( d, self );
        addImportsDescription( d, self, importsMaker );
        addLanguageDescription( d, self, language );
        addManagerDescription( d, self, documentManager );
        addReasonerDescription( d, self, reasonerFactory );
        return d;
        }

    public Model getDescription()
        { return getDescription( ResourceFactory.createResource() ); }

    public Model getDescription( Resource root )
        { return addDescription( ModelFactory.createDefaultModel(), root ); }

    public Model addMakerDescription( Model desc, Resource root )
        {
        Resource makerRoot = desc.createResource();
        desc.add( root, JenaModelSpec.maker, makerRoot );
        maker.addDescription( desc, makerRoot );
        return desc;
        }

    /**
        The map which associates bnodes with Java values.
    */        
    private static Map values = new HashMap();
    
    /**
        Answer a new bnode Resource associated with the given value. The mapping from
        bnode to value is held in a single static table, and is not intended to hold many
        objects; there is no provision for garbage-collecting them [this might eventually be
        regarded as a bug].
        
        @param value a Java value to be remembered 
        @return a fresh bnode bound to <code>value</code>
    */
    public static Resource createValue( Object value )
        {
        Resource it = ResourceFactory.createResource();
        values.put( it, value );
        return it;    
        }
        
    /**
        Answer the value bound to the supplied bnode, or null if there isn't one or the
        argument isn't a bnode.
        
        @param it the RDF node to be looked up in the <code>createValue</code> table.
        @return the associated value, or null if there isn't one.
    */
    public static Object getValue( RDFNode it )
        { return values.get( it ); }
    
    public interface ModelMakerCreator
        {
        /**
            Answer a ModelMaker who's description is found hanging from a given root
            in a given model.
            @param desc the model containing the description
            @param root the root of the description
            @return a ModelMaker satisfying the description
         */
        ModelMaker create( Model desc, Resource root );
        }
    
    private static class MakerCreator
        {
        /**
            Answer the reification style possessed by <code>root</code> in 
            <code>desc</code> under the property <code>JenaModelSpec.reificationMode</code>.
            If no such property exists, default to <code>Standard</code>.
            
            @param desc the model in which to search
            @param root the entity which may have the property
            @return the reification style given by the property, or Standard if none
         */
        public ReificationStyle style( Model desc, Resource root )
            {
            Statement st = desc.getProperty( root, JenaModelSpec.reificationMode );
            return st == null ? ReificationStyle.Standard : JenaModelSpec.findStyle( st.getObject() );
            } 
        }
    
    public static class MemMakerCreator extends MakerCreator implements ModelMakerCreator
        {
        /**
            Answer the MemModelMaker with the reification style specified as the
            JenaModelSpec.reificationMode property of the root, or Standard if none.
        */
        public ModelMaker create( Model desc, Resource root ) 
            { return ModelFactory.createMemModelMaker( style( desc, root ) ); }
        }
    

    public static class FileMakerCreator extends MakerCreator implements ModelMakerCreator
        {
        /**
            Answer a FileModelMaker with reification style given by the JenaModelSpec.reificationMode
            of the root and file base given by the JenaModelSpec.fileBase of the root. The latter
            defaults to "/tmp", which probably counts as a bug.
            
            TODO replace /tmp with the non-implementation-specific temporary directory.
         */
        public ModelMaker create( Model desc, Resource root ) 
            { 
            Statement fb = desc.getProperty( root, JenaModelSpec.fileBase );
            String fileBase = fb == null ? "/tmp" : fb.getString();
            return ModelFactory.createFileModelMaker( fileBase, style( desc, root ) );
            }
        }
    
    public static class RDBMakerCreator implements ModelMakerCreator
        {
        public ModelMaker create( Model desc, Resource root )
            {
            return ModelFactory.createModelRDBMaker( createConnection( desc, root ) );
            }
    
        public static IDBConnection createConnection( Model description, Resource root )
            {
            Resource connection = description.listStatements( root, JenaModelSpec.hasConnection, (RDFNode) null ).nextStatement().getResource();
            String url = getURL( description, connection, JenaModelSpec.dbURL );
            String user = getString( description, connection, JenaModelSpec.dbUser );
            String password = getString( description, connection , JenaModelSpec.dbPassword );
            String className = getClassName( description, connection );
            String dbType = getDbType( description, connection );
            loadDrivers( dbType, className );
            return ModelFactory.createSimpleRDBConnection( url, user, password, dbType );
            }
    
        public static String getDbType( Model description, Resource connection )
            { return getString( description, connection, JenaModelSpec.dbType ); }
    
        public static String getClassName( Model description, Resource root )
            {
            Statement cnStatement = description.getProperty( root, JenaModelSpec.dbClass );
            if (cnStatement == null)
                return DriverMap.get( getDbType( description, root ) );
            else
                return cnStatement == null ? null : cnStatement.getString();
            }
        
        public static String getURL( Model description, Resource root, Property p )
            {
            return description.getRequiredProperty( root, p ).getResource().getURI();
            }
    
        public static String getString( Model description, Resource root, Property p )
            {
            return description.getRequiredProperty( root, p ).getString();
            }
        
        public static void loadDrivers( String dbType, String className )
            {
            try
                {
                Class.forName( "com.hp.hpl.jena.db.impl.Driver_" + dbType );
                if (className != null) Class.forName( className );
                }
            catch (ClassNotFoundException c)
                { throw new JenaException( c ); }
            }
        }
    }

