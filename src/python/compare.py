#
# Name:    compare.py - script for comparing two PIF models 
#
# Authors: Pascal Poizat, Gwen Salaun
# Date:    2014-2015
###############################################################################

import sys
from vbpmn import *

##############################################################################################
if __name__ == '__main__':

    # set up parser
    import argparse
    parser = argparse.ArgumentParser(prog='VBPMN-compare', description='Compares two PIF processes.')
    parser.add_argument('--version', action='version', version='%(prog)s 1.0')
    parser.add_argument('models', metavar='Model', nargs=2,
                        help='the models to compare (filenames of PIF files)')
    parser.add_argument('operation', metavar='OP',
                        choices=ComparisonChecker.OPERATIONS,
                        help='the comparison operation')
    parser.add_argument('--hiding', nargs='*',
                        help='list of alphabet elements to hide or to expose (based on --exposemode)')
    parser.add_argument('--exposemode', nargs='?', choices=[True, False], const=True, default=False,
                        help='decides whether arguments for --hiding should be the ones hidden (default) or the ones exposed (if this option is set)')
    parser.add_argument('--context', metavar='Context',
                        help='context to compare with reference to (filename of a PIF file)')
    parser.add_argument('--renaming', metavar='old:new', nargs='*', default=[],
                        help='list of renamings')
    parser.add_argument('--renamed', nargs='?',
                        choices=ComparisonChecker.SELECTIONS,
                        const=ComparisonChecker.SELECTIONS_DEFAULT, default=ComparisonChecker.SELECTIONS_DEFAULT,
                        help='gives the model to apply renaming to (first, second, or all(default))')

    # parse arguments
    try:
        args = parser.parse_args()
    except:
        parser.print_help()
        sys.exit(Checker.TERM_PROBLEM)

    # (re)build first model
    pifModel1 = args.models[0]
    print "converting " + pifModel1 + " to LTS.."
    (ltsModel1, model1Alphabet) = Generator().generateLTS(pifModel1)
    # (re)build second model
    pifModel2 = args.models[1]
    print "converting " + pifModel2 + " to LTS.."
    (ltsModel2, model2Alphabet) = Generator().generateLTS(pifModel2)

    # checks if we compare up to a context
    # TODO Gwen : refine synchronization sets computation (_EM vs _REC)
    # TODO Pascal : what about if we have hiding and/or renaming + context-awareness? different alphabets should be used?
    if args.context is not None:
        pifContextModel = args.context
        print "converting " + pifContextModel + " to LTS.."
        (ltsContext, contextAlphabet) = Generator().generateLTS(pifContextModel)
        syncset1 = filter(lambda itm: itm in model1Alphabet, contextAlphabet)
        syncset2 = filter(lambda itm: itm in model2Alphabet, contextAlphabet)
        print syncset1, syncset2
    else:
        syncset1, syncset2 = [], []

    # perform comparison
    comparator = ComparisonChecker(ltsModel1, ltsModel2, args.operation,
                                   args.hiding, args.exposemode,
                                   args.renaming, args.renamed,
                                   syncsets=[syncset1, syncset2])
    res = comparator()
    if not res:
        val = Checker.TERM_ERROR
    else:
        val = Checker.TERM_OK
    print res
    sys.exit(val)
