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

    if len(sys.argv) != 4:
        res = False
        val = Checker.TERM_PROBLEM
        print "Error: wrong number of parameters, please look at the README.md file."

    else:
        # TODO Gwen: verifier le format des parametres ?
        pifModel1 = sys.argv[1]
        pifModel2 = sys.argv[2]
        formula = sys.argv[3]
        val = Checker.TERM_OK

        print "converting " + pifModel1 + " to LTS.."
        (ltsModel1, _) = Generator().generateLTS(pifModel1)

        print "converting " + pifModel2 + " to LTS.."
        (ltsModel2, _) = Generator().generateLTS(pifModel2)

        formulaChecker = FormulaChecker(ltsModel1, ltsModel2, formula)
        res = formulaChecker()

    if not res:
        val = Checker.TERM_ERROR
    print res
    sys.exit(val)
