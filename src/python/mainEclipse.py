
# xxx ESSAI VIA PYTHON + SCRIPT
# xxx #!/usr/bin/python
# xxx # -*- coding: utf-8 -*-

if __name__ == '__main__':
    import sys
    import os
    import subprocess
    import fcntl
    import select
    import choreo_xml_model
    from realizabilityChecking import *
    import pyxb

    # YOU MAY CHANGE THIS
    HOME = "/home/linaye"
    DIRECTORY = HOME+"/workspace/VerChor_Develop"
    CADP_BASE= HOME+"/Desktop/cadp"
    CADP_ARCH = "iX86"
    # DO NOT CHANGE BELOW

    CADP_CMD1 = CADP_BASE+"/bin."+CADP_ARCH
    CADP_CMD2 = CADP_BASE+"/com"
    PATH = "%s:%s:%s"%(os.environ["PATH"],CADP_CMD1,CADP_CMD2)
    SUFFIX = ".bpmn"
    SUFFIX2 = ".xml"
    EXEC = "/usr/bin/python"
    SCRIPTS = {}
    #SCRIPTS["realizability"] = DIRECTORY+"/check_realizability-v2.py"
    SCRIPTS["SynchronizabilityRealizability"] = DIRECTORY+"/check_synchronizability_realizability.py"
    SCRIPTS["LTSgeneration"]=DIRECTORY+"/generate-lts-v2.py"
    SCRIPTS["Controlgeneration"]=DIRECTORY+"/CGEclipse.py"

    # check if argument
    if (len(sys.argv)!=3):
        print "wrong number of arguments"
        sys.exit(1)
    else:
        resource = sys.argv[1]
	#print "resource "+resource
        action_name = sys.argv[2]
        if action_name not in SCRIPTS.keys():
            print "%s : unknown action" %action_name
            sys.exit(1)
        else:
            action = SCRIPTS[action_name]

    # check if bpmn
    elements = resource.split(SUFFIX)
    if (len(elements)>=2 and elements[-1]==''):
        filename = resource[:-(len(SUFFIX))]
        full_filename = filename+SUFFIX2
    else:
        print "wrong resource type"
        sys.exit(1)

    # begin

    print "current resource is: %s"%resource
    checker = Checker()
    if action_name=="LTSgeneration":
        choreo = Choreography()
        choreo.buildChoreoFromFile(full_filename, True)

        choreo.computeSyncSets(True)
        choreo.genLNT()
        filename=choreo.name
        choreo.genSVL(False, False)

        
        checker.cleanUp(choreo)
        checker.generateLTS(choreo, False)

        print ("generated LTS for", full_filename)
        sys.exit(0)
    if action_name=="SynchronizabilityRealizability":
        choreo = Choreography()
        choreo.buildChoreoFromFile(full_filename, True)

        checker.cleanResults(choreo)
        result = checker.isSynchronizableP(choreo, False)

        if result:
            print "choreography is synchronizable"
            print "======\n"
            #sys.exit(0)
        else:
            print "The choreography is not synchronizable"
            print "======\n"
            #sys.exit(1)
        #process = subprocess.Popen (["python", "check_realizability-v2.py", infile], shell = False, stderr=subprocess.STDOUT, stdout=sys.stdout)
        #print process.stdout
        result = checker.isRealizableP(choreo, False)
        
        if result:
            print "choreography is realizable in this form"
            print "======\n"
            sys.exit(0)
        else:
            print "choreography is not realizable in this form"
            print "======\n"
            sys.exit(1)
    if action_name=="Controlgeneration":
        process = subprocess.Popen ([EXEC, action, full_filename], env = {'PATH': PATH, 'CADP': CADP_BASE}, shell = False, stderr=subprocess.STDOUT, stdout=sys.stdout)
        process.communicate()
        sys.exit(0)
    #process = subprocess.Popen ([EXEC, action, full_filename], env = {'PATH': PATH, 'CADP': CADP_BASE}, shell = False, stderr=subprocess.STDOUT, stdout=subprocess.PIPE)
    
        
#
# ESSAI 1
#
#out, err = process.communicate()
#print out
#
# ESSAI 2
#
#for line in iter(process.stdout.readline, ""):
#    sys.stdout.write(line)
#    sys.stdout.flush()
#
# ESSAI 3
#
# complete = False
# while True:
#    out = process.stdout.read(1)
#    if out == '' and process.poll() != None:
#        break
#    if out != '':
#        sys.stdout.write(out)
#        sys.stdout.flush()
#
# ESSAI 4
#
# fcntl.fcntl(
#     process.stdout.fileno(),
#     fcntl.F_SETFL,
#     fcntl.fcntl(process.stdout.fileno(), fcntl.F_GETFL) | os.O_NONBLOCK,
# )
# while process.poll() == None:
#     readx = select.select([process.stdout.fileno()], [], [])[0]
#     if readx:
#         chunk = process.stdout.read()
#         print chunk
