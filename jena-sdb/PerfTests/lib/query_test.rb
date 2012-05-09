class QueryTest

    require 'benchmark'
    include Benchmark
    require 'java'
    
    StoreFactory = com.hp.hpl.jena.sdb.store.StoreFactory
    QueryFactory = com.hp.hpl.jena.query.QueryFactory
    DatasetFactory = com.hp.hpl.jena.query.DatasetFactory
    QueryExecutionFactory = com.hp.hpl.jena.query.QueryExecutionFactory
    ResultSetFactory = com.hp.hpl.jena.query.ResultSetFactory
    DatasetStore = com.hp.hpl.jena.sdb.store.DatasetStore
    QueryEngineFactorySDB = com.hp.hpl.jena.sdb.engine.QueryEngineFactorySDB

    def initialize
        @stores = []
        @queries = []
        @width = 0
        @qef = QueryEngineFactorySDB.new
    end
    
    def store(store)
        @stores << store
    end
    
    def query(queryfile)
        @queries << queryfile
        @width = queryfile.size if queryfile.size > @width
    end
    
    def run
        @stores.each do |store|
            #model = StoreFactory.create_model(store)
            ds = DatasetStore.new(StoreFactory.create(store))
            
            puts "\n\n**** #{store} ****\n\n" 
            
            print '[ Warm up '
            @queries.each { |queryfile| do_query(ds, queryfile); print '.' }
            print " ]\n\n"
            Benchmark.bm(@width + 3, ">total:") do |x|
                total = nil
                @queries.each do |queryfile|
                    res = x.report(queryfile.dup) { do_query(ds, queryfile) }
                    if total then
                      total += res
                    else
                      total = res
                    end
                end
                [total]
            end
        end
    end
    
    def do_query(dataset, queryfile)
        query = QueryFactory.read(queryfile)
        
        qe = @qef.create(query, dataset)
        
        results = qe.exec_select
        
        if results.has_next # ensure execution
            results.next
        end
        qe.close
    end

end

def test(&config)
  qt = QueryTest.new
  
  qt.instance_eval(&config)
  
  qt.run
end
