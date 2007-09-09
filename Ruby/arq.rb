# A library to make ARQ queries more "Ruby-like"

require 'java'

include_class 'com.hp.hpl.jena.query.QueryExecutionFactory'
include_class 'com.hp.hpl.jena.query.QueryFactory'
include_class 'com.hp.hpl.jena.query.ResultSetFormatter'
include_class 'com.hp.hpl.jena.util.FileManager'
include_class 'java.lang.System'

class Result
  
  def initialize(solution)
    @solution = solution 
  end

  def method_missing(methodId)
    varName = methodId.to_s
    if ( ! @solution.contains(varName) )
      return nil
      end
    _get(varName)
  end 

  private
  def _get(varName)
    @solution.get(varName)
    end
end

class ResultSet
  
  def initialize(result_set)
    @rs = result_set
  end
  
  def each
    while @rs.hasNext
      yield Result.new(@rs.next)
    end
  end

end

class Query

  def Query.select(queryString, model)
    q = Query.new(queryString,model)
    q.select	
  end

  def initialize(query, model)
    @query = QueryFactory.create(query)
    @qe = QueryExecutionFactory.create(@query, model)
  end

  def select
    ResultSet.new @qe.execSelect
  end
  
  def put_select
    ResultSetFormatter.out(System.out, @qe.execSelect)
    @qe.close
  end

  def close
    @qe.close if !@qe.nil?
  end
end
