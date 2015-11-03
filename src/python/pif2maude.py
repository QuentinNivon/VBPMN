#
# Name:    pif2maude.py - Generates a Maude file given a BPMN process
#
# Authors: Pascal Poizat, Gwen Salaun
# Date:    2015
###############################################################################


import sys
from pif2lnt import *  # this library allows to load Python objects from PIF
import random
from subprocess import call

# Note: this transformation supports the constructs available in the Maude
#  BPMN model, namely tasks, start/end events, and/xor/or split/join gateways.

class MaudeDumper:

    def __init__(self, filename):
        self.filename=filename

    def dumpMaude(self):
        proc = Process()
        # load PIF model
        proc.buildProcessFromFile(self.filename)
        #proc.dump()

        fmaude = proc.name + ".maude"
        f = open(fmaude, 'w')
        f.write("---- Example "+proc.name+" in BPMN\n\n")
        f.write("load bpmn.maude\n\n")
        f.write("mod BPMN-EX is ---- "+proc.name+" \n")
        f.write("  pr BPMN-SEM . \n")
        f.write("  ops ")
        for fl in proc.flows:
            f.write(fl.ident+" ")
        f.write(": -> FId . \n")
        f.write("  ops ")
        f.write(proc.initial.ident+" ")
        for nd in proc.finals:
            f.write(nd.ident+" ")
        for nd in proc.nodes:
            f.write(nd.ident+" ")
        f.write(": -> NId . \n\n")

        f.write("  op fls : -> Set{Flow} . \n")
        f.write("  op nds : -> Set{Node} . \n")

        f.write("  eq init = token("+proc.initial.outgoingFlows[0].ident+",0) . \n\n")

        # generating flow objects
        f.write("  eq fls \n")
        f.write("    = ( \n")
        countfls=len(proc.flows)
        for fl in proc.flows:
            countfls=countfls-1
            t=random.randint(0,4)
            f.write("        flow("+fl.ident+","+str(t)+")")  # we generate a random time !
            if (countfls>0):
                f.write(",")
            f.write("\n")
        f.write("      ) . \n\n")

        # generating node objects
        f.write("  eq nds \n")
        f.write("    = ( \n")
        # initial node
        f.write("        start("+proc.initial.ident+","+proc.initial.outgoingFlows[0].ident+"),\n")
        # final nodes
        for fin in proc.finals:
            f.write("        end("+fin.ident+","+fin.incomingFlows[0].ident+"),\n")

        countnds=len(proc.nodes)
        for nd in proc.nodes:
            countnds=countnds-1
            nd.dumpMaude(f)   # these functions are defined for each object in pif2lnt
            if (countnds>0):
                f.write(",")
            f.write("\n")
        f.write("      ) . \n\n")

        f.write("endm\n")
        f.close()


##############################################################################################
if __name__ == '__main__':

    # script format -> python pif2maude.py x.pif OR python pif2maude.py all
    #  the second option translates all pif files in the current directory

    firstarg = sys.argv[1]
    if (firstarg=="all"):
        call('ls *.pif >& tmp.txt', shell=True)
        ff = open('tmp.txt', 'r')
        res=ff.readlines()
        for f in res:
            f2=f.rstrip('\n')
            print f2
            md = MaudeDumper(f2)
            md.dumpMaude()
    else:
        md = MaudeDumper(firstarg)
        md.dumpMaude()

# TODO: launch the translation only if all operators in the PIF instance are
#  supported inthe Maude encoding.

