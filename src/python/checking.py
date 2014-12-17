

if __name__ == '__main__':

	import sys
	import os
        from time import time
        from controllerGeneration import *
	from subprocess import Popen, PIPE, call
        SUFFIX = ".bcg"
	

	if len(sys.argv) == 2:
		infile = sys.argv[1]
		# check if CIF (xml suffix)
        	elements = infile.split(SUFFIX)
       	 	if (len(elements)>=2 and elements[-1]==''):
            		filename = infile[:-(len(SUFFIX))]
                        
            		basefilename = os.path.basename(filename)
            		dirfilename = os.path.dirname(filename)
        	else:
            		print "%s: wrong resource type (should be .bcg)" % infile
            		sys.exit(1)

                cdm='cp '+infile+' tmp/'
		call(cdm, shell=True)
		s3=basefilename+".bcg"
                s4=basefilename+"_bpmnlts_min.aut"
                
                p3=Popen(['bcg_io', s3, s4], cwd="tmp/",  stdout=PIPE)
	        p3.wait()
                
                s4=tmp+s4
		p4=Popen(['python', 'aut2cp.py', s4, basefilename], stdout=PIPE)
		p4.wait()
                
		#import cpExa
                sys.path.append("tmp/")
                cpExa1=basefilename+"_cpExa"  
                exec("from "+cpExa1+" import *")
		ex=ExternalExamples()
		checker = Checker()
                
                
		liste = [ 
			ex.cp_example()
                        ]
                
		map (lambda ex: checker.checkChoreo(ex, True, False), liste)
                
