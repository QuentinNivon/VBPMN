tmp="tmp/"
if __name__ == '__main__':

	import re
	# import numpy as np
        import sys

        infile = sys.argv[1]
        basefile= sys.argv[2]
        #print infile
        #print basefile 

	string=open(infile).read()
	string=string[4:]
	#string="begin "+string
	new_str=re.sub('[^a-zA-Z0-9\n\.\ \_]', '', string)
	# new_str=re.sub('\_', ' ', new_str)
	open(tmp+basefile+'_try.aut','w').write(new_str)

##################################################
     #   data=open(basefile+"_try.aut").read()
     #   ins=open(basefile+"_try.aut", "r")
     #   lines=[]
     #   for line in ins
     #           lines.append(line)
     #   ins.close
     #   for j in range(0, len(lines))
     #           data[j]=lines[j].split(" ")
########################################################
        
######################################################
        lines=tuple(open(tmp+basefile+"_try.aut", "r"))
        data=[[0 for x in xrange(3)] for x in xrange(len(lines))]
        for j in range(0, len(lines)):
              data[j]=[s.strip() for s in lines[j].split(' ')]
##################################################

      #	data=np.genfromtxt(basefile+"_try.aut", delimiter=" ", dtype=None)
      
        print data
                                             
	setPeers=list()
	setStates=list()
	setStates.append(data[0][0])
	for i in range(1,len(data)):
		if data[i][1]!='exit':
			tem=data[i][1].split('_')
			tem.remove(tem[2])
			for t in tem:
				if t not in setPeers:
					setPeers.append(t)
		if data[i][0] not in setStates:
			setStates.append(data[i][0])
 		if data[i][2] not in setStates:
			setStates.append(data[i][2])
        outfile=tmp+basefile+"_cpExa.py"
	f=open(outfile, 'w')
        f.write("from controllerGeneration import *\n")
	f.write("class ExternalExamples:\n\n")
	f.write("	# Builds CP instance\n")
        cp=basefile
        cp1="cp_example"
	f.write("	def "+cp1+"(self):\n\n")
	f.write("		# states\n")
	for s in setStates:
		f.write("		st"+str(s)+"=State(\""+str(s)+"\")\n")
        f.write("		#cp init\n")
	f.write("		"+cp+"=CP(\""+cp+"\", st"+str(setStates[0])+")\n")
	f.write("		# add states\n")
        for s in setStates:
		f.write("		"+cp+".addState(st"+str(s)+")\n")
	f.write("		# Peers\n")
	for p in setPeers:
		f.write("		"+p+"=Peer(\""+p+"\")\n")  
	f.write("		# add transitions\n") 
	for i in range(1, len(data)):
		if data[i][1]!='exit': 
			tem1=data[i][1].split('_')
			f.write("		"+cp+".addTransition(Transition(st"+str(data[i][0])+",Label("+tem1[0]+","+tem1[1]+",\""+tem1[2]+"\"),st"+str(data[i][2])+"))\n")
        f.write("		\n\n\n")
	f.write("		return "+cp+"\n") 
    
