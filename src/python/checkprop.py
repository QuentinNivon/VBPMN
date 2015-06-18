#
# Name:    checkprop.py - script for comparing two PIF models wrt. a given property
#
# Authors: Pascal Poizat, Gwen Salaun
# Date:    2014-2015
###############################################################################

from vbpmn import *
import sys

##############################################################################################
if __name__ == '__main__':

    if len(sys.argv)!=4:
        res=False
        val=2
        print "Error: wrong number of parameters, please look at the README file."

    else:
        # TODO Gwen: verifier le format des parametres ?
        file1=sys.argv[1]
        file2=sys.argv[2]
        val=0 # return value (0 -> true, 1 -> false, 2 -> wrong format)

        print "converting " + file1 + " to LTS.."
        (name1,alpha1)=Generator().generateLTS(file1)

        print "converting " + file2 + " to LTS.."
        (name2,alpha2)=Generator().generateLTS(file2)

        prop=sys.argv[3]
        res=Checker(name1,name2,prop).check()

    if not(res):
        val=1
    print res
    sys.exit(val)
