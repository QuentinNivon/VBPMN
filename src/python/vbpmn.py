#
# Name:    vbpm.py - Classes for analyzing two PIF models 
#                     using CADP verification tools
# Authors: Pascal Poizat, Gwen Salaun
# Date:    2014-2015
###############################################################################

from pif2lnt import * # this library allows to go from PIF to LNT and LTS
# import os.path

# This class compares two LTSs wrt. a certain operation
class Comparator:

    # two names corresponding to the LTSs to be compared, one comparison operation, hide/rename/context files
    def __init__(self, n1, n2, op, fhide, fren, fbcg, sync1=[], sync2=[]):
        self.name1=n1
        self.name2=n2
        self.operation=op
        self.fhide=fhide  # file.hid
        self.fren=fren    # file.ren
        self.fbcg=fbcg    # file.bcg
        self.sync1=sync1  # first synchronization set, useful for context-dependent check
        self.sync2=sync2  # second synchronization set, useful for context-dependent check


    # generates SVL code to check the given operation
    def genSVL(self, filename, hide, ren, cont):
        f=open(filename, 'w')
        f.write("% CAESAR_OPEN_OPTIONS=\"-silent -warning\"\n% CAESAR_OPTIONS=\"-more cat\"\n\n")
        if hide:
            f.write("\""+self.name1+".bcg\" = total hide using \""+self.fhide+"\" in \""+self.name1+".bcg\" ; \n") 
            f.write("\""+self.name2+".bcg\" = total hide using \""+self.fhide+"\" in \""+self.name2+".bcg\" ; \n\n") 
        if ren:
            f.write("\""+self.name1+".bcg\" = total rename using \""+self.fren+"\" in \""+self.name1+".bcg\" ; \n") 
            f.write("\""+self.name2+".bcg\" = total rename using \""+self.fren+"\" in \""+self.name2+".bcg\" ; \n\n") 
        if cont:
            f.write("\""+self.name1+".bcg\" = \""+self.fbcg+".bcg\""),
            if (self.sync1==[]):
                f.write(" ||| ")
            else:
                f.write(" |[")
                dumpAlphabet(sync1,f,False)
                f.write("]| ")
            f.write("\""+self.name1+".bcg\" ; \n") 
            f.write("\""+self.name2+".bcg\" = \""+self.fbcg+".bcg\""),
            if (self.sync2==[]):
                f.write(" ||| ")
            else:
                f.write(" |[")
                dumpAlphabet(sync2,f,False)
                f.write("]| ")
            f.write("\""+self.name2+".bcg\" ; \n\n")

        if (self.operation=="="):
            f.write("% bcg_open \""+self.name1+".bcg\" bisimulator -equal -strong \""+self.name2+".bcg\" \n\n")
        elif (self.operation==">"):
            f.write("% bcg_open \""+self.name1+".bcg\" bisimulator -greater -strong \""+self.name2+".bcg\" \n\n")
        elif (self.operation=="<"):
            f.write("% bcg_open \""+self.name1+".bcg\" bisimulator -smaller -strong \""+self.name2+".bcg\" \n\n")
        else:
            print self.operation + " is not yet implemented"
        f.write("\n\n")
        f.close()

    # generates and calls the generated SVL file
    def compare(self, hide, ren, cont):
        import sys

        fname="compare.svl"
        self.genSVL(fname, hide, ren, cont)
        call('svl '+fname+ ' > res.txt', shell=True)
        res=call('grep TRUE res.txt', shell=True)

        if (res==1):
            return False
        else:
            return True

# This class checks both process LTSs wrt. a certain MCL property
class Checker:

    # two names corresponding to the LTSs to be compared and a property in an MCL file
    def __init__(self,n1,n2,f):
        self.name1=n1
        self.name2=n2
        self.f=f

    # generates SVL code to check the property on both LTSs
    def genSVL(self,filename):
        f=open(filename, 'w')
        f.write("% CAESAR_OPEN_OPTIONS=\"-silent -warning\"\n% CAESAR_OPTIONS=\"-more cat\"\n\n")
        f.write("% bcg_open \""+self.name1+".bcg\" evaluator \""+self.f+"\" \n\n")
        f.write("% bcg_open \""+self.name2+".bcg\" evaluator \""+self.f+"\" \n\n")
        f.write("\n\n")
        f.close()

    # generates and calls the generated SVL file
    def check(self, debug = False):
        import sys
        
        fname="check.svl"
        self.genSVL(fname)
        call('svl '+fname+ ' > res.txt', shell=True, stdout=sys.stdout)
        res=call('grep FALSE res.txt', shell=True, stdout=sys.stdout)

        # we return False if at least one FALSE in res.txt
        if (res==1):
            return True
        else:
            return False


##############################################################################################
#if __name__ == '__main__':

