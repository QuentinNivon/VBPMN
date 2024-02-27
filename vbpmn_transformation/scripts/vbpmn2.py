#
# Name:    vbpm.py - Classes for analyzing two PIF models 
#                     using CADP verification tools
# Authors: Pascal Poizat, Gwen Salaun
# Date:    2014-2015
###############################################################################

# TODO: support workflow a ; a by adding supplemental information in pif.xsd::Task
# TODO: support hiding in only one of the processes (as done for renaming) and different hiding for the two processes (useful?)
# TODO: support different renamings in the two processes (useful?)
# TODO: the way the things are computed, one should not compare a process with itself!
# TODO: support hiding, renaming, refinement, and context-awareness in property based comparison
# TODO: support => mode for property based comparison, not only /\
# TODO: optimize by calling bisimulator / evaluator4 with hiding/renaming options instead of generating new bcg files
# TODO: use SVL options to create buffers instead of writing them in LNT
# TODO: perform cleaning in SVL scripts

import sys
import pifcheck as pc
# from pif2lntv7 import *  # this library allows to go from PIF to LNT and LTS

# command to call SVL
# first argument is the script, second one is the result file
SVL_CALL_COMMAND = 'svl %s > %s'

# template for SVL scripts
# first argument is the SVL contents
SVL_CAESAR_TEMPLATE = '''%% CAESAR_OPEN_OPTIONS="-silent -warning"
%% CAESAR_OPTIONS="-more cat"
%s
'''

# template of the verification of a comparison between two models
# first argument is the first model (LTS in BCG format)
# second one is the comparison operation for bisimulator
# third one is the equivalence notion (strong, branching, ...)
# fourth one is the second model (LTS in BCG format)
SVL_COMPARISON_CHECKING_TEMPLATE = '''%% bcg_open "%s.bcg" bisimulator -%s -%s -diag "%s.bcg"
'''

# template of the verification of formula over a model
# first argument is the model file (LTS in BCG format)
# second one is the formula (MCG) file
SVL_FORMULA_CHECKING_TEMPLATE = '''%% bcg_open "%s.bcg" evaluator4 -diag "%s"
'''

# template for hiding in SVL
# first and fourth arguments are the model file (LTS in BCG format)
# second argument is the hiding mode (hiding or hiding all but)
# third argument is the list of elements to hide (or hide but)
SVL_HIDING_TEMPLATE = '''"%s.bcg" = total %s %s in "%s.bcg" ;
'''

# template for renaming in SVL
# first and third arguments are the model file (LTS in BCG format)
# second argument is the relabelling function
SVL_RENAMING_TEMPLATE = '''"%s.bcg" = total rename %s in "%s.bcg" ;
'''

# template for making a working copy in SVL
# first and second arguments are the source and target model file (LTS in BCG format)
SVL_COPY_TEMPLATE = '''%% bcg_io "%s.bcg" "%s.bcg"
'''

WORK_SUFFIX = "_work"

OPERATIONS_COMPARISON = ["conservative", "inclusive", "exclusive"]
OPERATIONS_PROPERTY = ["property-and", "property-implied"]
OPERATIONS = OPERATIONS_COMPARISON + OPERATIONS_PROPERTY
OPERATIONS_DEFAULT = "conservative"
SELECTIONS = ["first", "second", "all"]
SELECTIONS_DEFAULT = "all"
OPERATION_TO_BISIMULATOR = {"conservative": "equal", "inclusive": "smaller", "exclusive": "greater"}


# This class represents the superclass of all classes performing some formal checking on two LTS models (stores in BCG format files)
class Checker:
    CHECKER_FILE = "check.svl"
    DIAGNOSTIC_FILE = "res.txt"

    # sets up the Checker
    # @param model1 String, filename of the first model (LTS in a BCG file)
    # @param model2 String, filename of the second model (LTS in a BCG file)
    def __init__(self, model1, model2):
        self.model1 = model1
        self.model2 = model2

    # generates SVL script to check the property on both models
    # @param filename String, filename of the SVL script to create
    def __genSVL(self, filename):
        raise NotImplementedError("__genSVL method is not implemented in class %s" % self.__class__.__name__)

    # reification of a Checker as a callable object
    # @param args list, list of the unnamed arguments
    # @param kwargs dictionary, map name->value of the named arguments
    # @return boolean
    def __call__(self, *args, **kwargs):
        raise NotImplementedError("__call__ method is not implemented in class %s" % self.__class__.__name__)


# This class is used to perform comparison operations on two models (LTS stored in two BCG format files)
class ComparisonChecker(Checker):

    # sets up the ComparisonChecker
    # @param model1 String, filename of the first model (LTS in a BCG file)
    # @param model2 String, filename of the second model (LTS in a BCG file)
    # @param operation String, comparison operation (in ComparisonChecker.OPERATIONS)
    # @param hiding List<String>, elements to hide (or to expose, wrt exposemode)
    # @param exposemode boolean, expose mode if true element in hiding are hidden else they are exposed
    # @param renaming Dictionnary<String,String>, relabelling function
    # @param renamed String, which model to rename (first, second, or all)
    # @param syncsets [List<String>,List<String>], couple of list of alphabets to synchronize on (one for each model)
    # @param formula String, filename of the property file (MCL file)
    def __init__(self, model1, model2, operation,
                 hiding, exposemode,
                 renaming, renamed,
                 syncsets):
        Checker.__init__(self, model1, model2)
        if operation not in OPERATIONS or operation == '_':
            raise TypeError(
                "operation in creating %s should be in %s and _ is only for --hiding" % (
                    self.__class__.__name__, OPERATIONS))
        if renamed not in SELECTIONS:
            raise TypeError(
                "selection in creating %s should be in %s" % (self.__class__.__name__, ComparisonChecker.SELECTIONS))
        self.operation = operation
        self.hiding = hiding
        self.renamed = renamed
        self.exposemode = exposemode
        self.renaming = renaming
        self.syncsets = syncsets

    # generates SVL script to check the property on both models
    # @param filename String, filename of the SVL script to create
    def __genSVL(self, filename):
#        equivalence_version = "strong"
        equivalence_version = "branching"
        svl_commands = ""
        # add commands to make copies of the models and not change them
        workmodel1 = self.model1 + WORK_SUFFIX
        workmodel2 = self.model2 + WORK_SUFFIX
        svl_commands += SVL_COPY_TEMPLATE % (self.model1, workmodel1)
        svl_commands += SVL_COPY_TEMPLATE % (self.model2, workmodel2)
        # if required, perform hiding (on BOTH models)
        if self.hiding is not None:
            equivalence_version = "branching"
            if self.exposemode:
                hidemode = "hide all but"
            else:
                hidemode = "hide"
            svl_commands += SVL_HIDING_TEMPLATE % (workmodel1, hidemode, ','.join(self.hiding), workmodel1)
            svl_commands += SVL_HIDING_TEMPLATE % (workmodel2, hidemode, ','.join(self.hiding), workmodel2)
        # perform renaming
        # done AFTER having hidden TODO: is this ok? shouldn't we allow more freedom in the ordering of things?
        if len(self.renaming) > 0:
            renamings = []
            for renaming in self.renaming:
                (old, new) = renaming.split(":")
                renamings.append("%s -> %s" % (old, new))
            if self.renamed in ["first", "all"]:
                svl_command = SVL_RENAMING_TEMPLATE % (workmodel1, ','.join(renamings), workmodel1)
                svl_commands += svl_command
            if self.renamed in ["second", "all"]:
                svl_command = SVL_RENAMING_TEMPLATE % (workmodel2, ','.join(renamings), workmodel2)
                svl_commands += svl_command
        # if cont:
        #    f.write("\"" + self.name1 + ".bcg\" = \"" + self.fbcg + ".bcg\""),
        #    if (self.sync1 == []):
        #        f.write(" ||| ")
        #    else:
        #        f.write(" |[")
        #        dumpAlphabet(self.sync1, f, False)
        #        f.write("]| ")
        #    f.write("\"" + self.name1 + ".bcg\" ; \n")
        #    f.write("\"" + self.name2 + ".bcg\" = \"" + self.fbcg + ".bcg\""),
        #    if (self.sync2 == []):
        #        f.write(" ||| ")
        #    else:
        #        f.write(" |[")
        #        dumpAlphabet(self.sync2, f, False)
        #        f.write("]| ")
        #    f.write("\"" + self.name2 + ".bcg\" ; \n\n")
        # add the command to perform the comparison
        # equivalences are strong (by default) but we use branching in case of hiding
        svl_commands += SVL_COMPARISON_CHECKING_TEMPLATE % (
            workmodel1, OPERATION_TO_BISIMULATOR[self.operation], equivalence_version, workmodel2)
        #
        template = SVL_CAESAR_TEMPLATE % svl_commands
        f = open(filename, 'w')
        f.write(template)
        f.close()

    # checks if an equivalence or preorder yiels between two models
    # does it by generating first a SVL script and then calling it
    # @param args list, list of the unnamed arguments (NOT USED)
    # @param kwargs dictionary, map name->value of the named arguments (NOT USED)
    # @return boolean, true if it yiels, false else
    def __call__(self, *args, **kwargs):
        import sys
        self.__genSVL(Checker.CHECKER_FILE)
        call(SVL_CALL_COMMAND % (Checker.CHECKER_FILE, Checker.DIAGNOSTIC_FILE), shell=True)
        res = call('grep TRUE %s' % Checker.DIAGNOSTIC_FILE, shell=True)
        if (res == ReturnCodes.TERM_ERROR):
            return False
        else:
            return True


# This class is used to perform model checking operations on two models (LTS stored in two BCG format files)
# wrt an MCL property (stored in an MCL file)
class FormulaChecker(Checker):
    FORMULA_FILE = "formula.mcl"

    # sets up the FormulaChecker
    # @param model1 String, filename of the first model (LTS in a BCG file)
    # @param model2 String, filename of the second model (LTS in a BCG file)
    # @param formula String, filename of the property file (MCL file)
    def __init__(self, model1, model2, formula):
        Checker.__init__(self, model1, model2)
        self.formula = formula

    # generates SVL script to check the property on both models
    # @param filename String, filename of the SVL script to create
    def __genSVL(self, filename):
        svl_commands = ""
        svl_commands += SVL_FORMULA_CHECKING_TEMPLATE % (self.model1, "formula.mcl")
        if self.model1 != self.model2 :
            svl_commands += SVL_FORMULA_CHECKING_TEMPLATE % (self.model2, "formula.mcl")
        template = SVL_CAESAR_TEMPLATE % svl_commands
        #
        f = open(filename, 'w')
        f.write(template)
        f.close()

    # checks if a formula yields on two models
    # does it by generating first a SVL script and then calling it
    # @param args list, list of the unnamed arguments (NOT USED)
    # @param kwargs dictionary, map name->value of the named arguments (NOT USED)
    # @return boolean, true if no error(s) detected by SVL, false else.
    def __call__(self, *args, **kwargs):
        import sys
        f = open(FormulaChecker.FORMULA_FILE, 'w')
        f.write(self.formula) # TODO: not very clean ...
        f.close()
        self.__genSVL(Checker.CHECKER_FILE)
        call(SVL_CALL_COMMAND % (Checker.CHECKER_FILE, Checker.DIAGNOSTIC_FILE), shell=True, stdout=sys.stdout)
        # check the result, return false if at least one FALSE in the result
        res = call('grep FALSE %s' % Checker.DIAGNOSTIC_FILE, shell=True, stdout=sys.stdout)
        if res == ReturnCodes.TERM_ERROR:
            return True
        else:
            return False


##############################################################################################
if __name__ == '__main__':
    # set up parser
    import argparse

    parser = argparse.ArgumentParser(prog='vbpmn', description='Compares two PIF processes.')
    parser.add_argument('--version', action='version', version='%(prog)s 1.0')
    parser.add_argument('models', metavar='Model', nargs=2,
                        help='the models to compare (filenames of PIF files)')
    parser.add_argument('operation', metavar='OP',
                        choices=OPERATIONS,
                        help='the comparison operation')
    parser.add_argument('--formula', metavar='Formula',
                        help='temporal logic formula to check (used only if operation is in %s)' % OPERATIONS_PROPERTY)
    parser.add_argument('--hiding', nargs='*',
                        help='list of alphabet elements to hide or to expose (based on --exposemode)')
    parser.add_argument('--exposemode', action='store_true',
                        help='decides whether arguments for --hiding should be the ones hidden (default) or the ones exposed (if this option is set)')
    parser.add_argument('--context', metavar='Context',
                        help='context to compare with reference to (filename of a PIF file)')
    parser.add_argument('--renaming', metavar='old:new', nargs='*', default=[],
                        help='list of renamings')
    parser.add_argument('--renamed', nargs='?',
                        choices=SELECTIONS,
                        const=SELECTIONS_DEFAULT, default=SELECTIONS_DEFAULT,
                        help='gives the model to apply renaming to (first, second, or all(default))')
    parser.add_argument('--lazy', action='store_true',
                        help='does not recompute the BCG model if it already exists and is more recent than the PIF model')

    # parse arguments
    try:
        args = parser.parse_args()
        if args.operation in OPERATIONS_PROPERTY and args.formula is None:
            print("missing formula in presence of property based comparison")
            raise Exception()
        if args.operation not in OPERATIONS_PROPERTY and args.formula is not None:
            print("formula in presence of equivalence based comparison will not be used")
    except:
        parser.print_help()
        sys.exit(ReturnCodes.TERM_PROBLEM)

    #conditional import: use new version for inclusive unbalanced PIFs
    if pc.checkInclusiveUnbalanced(args.models[0], args.models[1]):
        from pif2lntv1 import *
        print("\n ------- Using VBPMN with pif2lntv1 ---------- \n")
    else:
        from pif2lntv7 import *
        print("\n ------- Using VBPMN with pif2lntv7 ---------- \n")

    # if in lazy mode, rebuild the BCG files only if needed
    if args.lazy:
        loader = Loader()
    else:
        loader = Generator()

    # (re)build first model
    pifModel1 = args.models[0]
    (res1, ltsModel1, model1Alphabet) = loader(pifModel1)
    # (re)build second model
    pifModel2 = args.models[1]
    (res2, ltsModel2, model2Alphabet) = loader(pifModel2)

    # if one of the two models could not be loader -> ERROR
    if not(res1==ReturnCodes.TERM_OK and res2==ReturnCodes.TERM_OK):
        print("error in loading models")
        sys.exit(ReturnCodes.TERM_PROBLEM)

    # checks if we compare up to a context
    # TODO Gwen : refine synchronization sets computation (_EM vs _REC)
    # TODO Pascal : what about if we have hiding and/or renaming + context-awareness? different alphabets should be used?
    if args.context is not None:
        pifContextModel = args.context
        print("converting " + pifContextModel + " to LTS..")
        (ltsContext, contextAlphabet) = loader(pifContextModel)
        syncset1 = [itm for itm in contextAlphabet if itm in model1Alphabet]
        syncset2 = [itm for itm in contextAlphabet if itm in model2Alphabet]
        print(syncset1, syncset2)
    else:
        syncset1, syncset2 = [], []

    # check whether we compare based on an equivalence or based on a property
    if args.operation in OPERATIONS_COMPARISON:
        comparator = ComparisonChecker(ltsModel1, ltsModel2, args.operation,
                                       args.hiding, args.exposemode,
                                       args.renaming, args.renamed,
                                       syncsets=[syncset1, syncset2])
    else:
        comparator = FormulaChecker(ltsModel1, ltsModel2, args.formula)

    # perform comparison and process result
    res = comparator()
    if not res:
        val = ReturnCodes.TERM_ERROR
    else:
        val = ReturnCodes.TERM_OK
    print(res)
    sys.exit(val)
