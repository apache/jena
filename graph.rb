require 'gnuplot'

def parse(filename, xhash, yhash, skip)
	xhash[filename] = []
	yhash[filename] = []
	file = File.new(filename)
	file.each_line do |line|
		matches = /Add:\s+([\d,]+).*\((\d+)\)/.match(line)
		next unless matches
		x = matches[1].gsub(/,/,"").to_i
		y = matches[2].to_i
		next if x < skip
		xhash[filename] << x
		yhash[filename] << y
	end
end

xvals = {}
yvals = {}

terminal = nil

args = ARGV

if args.include?("-term")
	index = args.index("-term") + 1
	terminal = args[index]
	args = args - ["-term",terminal]
end

skip = 0

if args.include?("-skip")
	index = args.index("-skip") + 1
	skip = args[index]
	args = args - ["-skip",skip]
	skip = skip.to_i
end

args.each do |filename|
	parse(filename, xvals, yvals, skip)
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
