require 'gnuplot'

def parse(filename, xhash, yhash)
	xhash[filename] = []
	yhash[filename] = []
	file = File.new(filename)
	file.each_line do |line|
		matches = /Add: (\d+).*\((\d+)\)/.match(line)
		next unless matches
		xhash[filename] << matches[1].to_i
		yhash[filename] << matches[2].to_i
	end
end

xvals = {}
yvals = {}

terminal = nil

args = ARGV

if ARGV.include?("-term")
	index = ARGV.index("-term") + 1
	terminal = ARGV[index]
	args = ARGV - ["-term",terminal]
end

args.each do |filename|
	parse(filename, xvals, yvals)
end

Gnuplot.open do |gp|
 	Gnuplot::Plot.new( gp ) do |plot|
    	plot.title  "Performance"
    	plot.ylabel "triples/sec"
    	plot.xlabel "triples"
    	plot.set "terminal",terminal if terminal
    	    	
    	args.each do |filename|
			
    		plot.data << Gnuplot::DataSet.new( [ xvals[filename], yvals[filename] ] ) do |ds|
      			ds.with = "lines"
      			ds.title = filename
    		end
    	end
  	end
end
