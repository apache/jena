class QueryTest

    require 'benchmark'
    require 'java'
    
    StoreFactory = com.hp.hpl.jena.sdb.store.StoreFactory
    QueryFactory = com.hp.hpl.jena.query.QueryFactory
    DatasetFactory = com.hp.hpl.jena.query.DatasetFactory
    QueryExecutionFactory = com.hp.hpl.jena.query.QueryExecutionFactory
    
    def initialize
        @stores = []
        @queries = []
    end
    
    def store(store)
        @stores << store
    end
    
    def query(queryfile)
        @queries << queryfile
    end
    
    def run
        @stores.each do |store|
            model = StoreFactory.create_model(store)
            ds = DatasetFactory.create(model)
            
            Benchmark.bmbm do |x|
                @queries.each do |queryfile|
                    x.report(queryfile.dup) { do_query(ds, queryfile) } # dup because of bug in bmbm
                end
            end
        end
    end
    
    def do_query(dataset, queryfile)
        query = QueryFactory.read(queryfile)
        
        qe = QueryExecutionFactory.create(query, dataset)
        
        results = qe.exec_select
        
        while results.has_next # exhaust results
            results.next
        end
    end

end

def test(&config)
  qt = QueryTest.new
  
  qt.instance_eval(&config)
  
  qt.run
end