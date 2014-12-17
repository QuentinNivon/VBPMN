
if __name__ == '__main__':
    SUFFIX = ".bpmn"
    import subprocess
    import sys
    import os
    if (len(sys.argv)!=2):
        print ("wrong number of arguments")
        sys.exit(1)
    else:
        resource = sys.argv[1]
    # check if bpmn
    elements = resource.split(SUFFIX)
    if (len(elements)>=2 and elements[-1]==''):
        filename = resource[:-(len(SUFFIX))]
        basefilename = os.path.basename(filename)
        dirfilename = os.path.dirname(filename)
    else:
        print ("wrong resource type")
        sys.exit(1)
    print ("... cleaning up files for %s"%filename)
    process = subprocess.Popen('rm -f %s.xml %s.lnt %s*.svl %s*.bcg %s*.aut *.o *.log cp_example* svl* %s.xml cpExa*'%(basefilename,basefilename,basefilename,basefilename,basefilename, filename),shell=True)
    process.communicate()
    print ("-- files cleaned up")
