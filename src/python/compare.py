#
# Name:    compare.py - script for comparing two PIF models 
#
# Authors: Pascal Poizat, Gwen Salaun
# Date:    2014-2015
###############################################################################

#from subprocess import *
#import os.path

#import pyxb
#import time
#import pif
from vbpmn import *

##############################################################################################
if __name__ == '__main__':

    import sys
#    import pyxb
#    import os
#    import glob

    file1=sys.argv[1]
    file2=sys.argv[2]
    operation=sys.argv[3]
    val=0 # return value (0 -> true, 1 -> false, 2 -> wrong format)

    print "converting " + file1 + " to LTS.."
    (name1,alpha1)=Generator().generateLTS(file1)

    print "converting " + file2 + " to LTS.."
    (name2,alpha2)=Generator().generateLTS(file2)

    # comparison with respect to equivalence / simulation
    if (operation=="=") or (operation=="<") or (operation==">"):
        print "comparing " + file1 + " and " + file2 + " wrt. " + operation
        res=Comparator(name1,name2,operation,"","","",[],[]).compare(False,False,False)
#    elif (operation=="p"): # property
#        prop=sys.argv[4]
#        res=Checker(name1,name2,prop).check()
    elif (operation=="h"): # up-to-alphabet
        operation=sys.argv[4]
        fhid=sys.argv[5]
        res=Comparator(name1,name2,operation,fhid,"","",[],[]).compare(True,False,False)
    elif (operation=="r"): # up-to-renaming
        operation=sys.argv[4]
        fren=sys.argv[5]
        res=Comparator(name1,name2,operation,"",fren,"",[],[]).compare(False,True,False)
    elif (operation=="c"): # context-dependent
        operation=sys.argv[4]
        fpif=sys.argv[5]
        print "converting " + fpif + " to LTS.."
        (fbcg,alpha)=Generator().generateLTS(fpif)
        sync1=filter(lambda itm:itm in alpha1,alpha)  # TODO GWEN : refine synchronization sets
        sync2=filter(lambda itm:itm in alpha2,alpha)  #       computation.. _EM vs _REC :(
        print sync1, sync2
        res=Comparator(name1,name2,operation,"","",fbcg,sync1,sync2).compare(False,False,True)
    else:
        res=False
        val=2
        print "Error: wrong format, please look at the README file."

    if not(res):
        val=1
    print res
    sys.exit(val)
