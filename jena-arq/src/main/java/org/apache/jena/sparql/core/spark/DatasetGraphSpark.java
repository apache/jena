package org.apache.jena.sparql.core.spark;

import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mosaic.IDFactory;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.storage.StorageLevel;

public class DatasetGraphSpark implements DatasetGraph {

	private static final IDFactory ID_FACTORY = IDFactory.valueOf(DatasetGraphSpark.class);
	
	public static final Symbol JAVA_SPARK_CONTEXT = Symbol.create(ID_FACTORY.suffix("javaSparkContext")); 
	
	public static final Symbol STORAGE_LEVEL = Symbol.create(ID_FACTORY.suffix("storageLevel")); 
	
	public static final Symbol HADOOP_CONFIGURATION = Symbol.create(ID_FACTORY.suffix("hadoopConfiguration")); 
	
	private JavaSparkContext javaSparkContext;
	
	private StorageLevel storageLevel;
	
	private Configuration hadoopConfiguration;

	private final Context context;
	
	
	
	
	public DatasetGraphSpark(final Context context) {
		super();
		this.context = context;
	}

	@Override
	public Graph getDefaultGraph() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Graph getGraph(Node graphNode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsGraph(Node graphNode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDefaultGraph(Graph g) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addGraph(Node graphName, Graph graph) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeGraph(Node graphName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterator<Node> listGraphNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void add(Quad quad) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(Quad quad) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void add(Node g, Node s, Node p, Node o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(Node g, Node s, Node p, Node o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteAny(Node g, Node s, Node p, Node o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterator<Quad> find() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Quad> find(Quad quad) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean contains(Node g, Node s, Node p, Node o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Quad quad) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Lock getLock() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean supportsTransactions() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * Transactional
	 */

	@Override
	public void begin(ReadWrite readWrite) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void commit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void abort() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void end() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isInTransaction() {
		// TODO Auto-generated method stub
		return false;
	}

}
