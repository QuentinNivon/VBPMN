#
# Name:    vbpm.py - Classes for analyzing two PIF models 
#                     using CADP verification tools
# Authors: Pascal Poizat, Gwen Salaun
# Date:    2014-2015
###############################################################################

from pif2lnt import *  # this library allows to go from PIF to LNT and LTS
# import os.path


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

# This class represents the superclass of all classes performing some formal checking on two LTS models (stores in BCG format files)
class Checker:
    TERM_OK, TERM_ERROR, TERM_PROBLEM = (0, 1, 2)

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
    OPERATIONS = ["conservative", "inclusive", "exclusive","_"]
    OPERATIONS_DEFAULT = "conservative"
    SELECTIONS = ["first", "second", "all"]
    SELECTIONS_DEFAULT = "all"
    OPERATION_TO_BISIMULATOR = {"conservative": "equal", "inclusive": "smaller", "exclusive": "greater"}

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
        if operation not in ComparisonChecker.OPERATIONS or operation=='_':
            raise TypeError(
                "operation in creating %s should be in %s and _ is only for --hiding" % (self.__class__.__name__, ComparisonChecker.OPERATIONS))
        if renamed not in ComparisonChecker.SELECTIONS:
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
        equivalence_version = "strong"
        svl_commands = ""
        # if required, perform hiding (on BOTH models)
        # TODO: is this ok? shouldn't we all more freedom by hiding only in one? (OK FOR FASE'16) -> can do as for renaming
        if self.hiding is not None:
            equivalence_version = "branching"
            if self.exposemode:
                hidemode = "hide all but"
            else:
                hidemode = "hide"
            for model in [self.model1, self.model2]:
                svl_commands += SVL_HIDING_TEMPLATE % (model, hidemode, ','.join(self.hiding), model)
        # perform renaming
        # done AFTER having hidden TODO: is this ok? shouldn't we allow more freedom in the ordering of things?
        if len(self.renaming) > 0:
            renamings = []
            for renaming in self.renaming:
                (old, new) = renaming.split(":")
                renamings.append("%s -> %s" % (old, new))
            if self.renamed in ["first", "all"]:
                svl_command = SVL_RENAMING_TEMPLATE % (self.model1, ','.join(renamings), self.model1)
                svl_commands += svl_command
            if self.renamed in ["second", "all"]:
                svl_command = SVL_RENAMING_TEMPLATE % (self.model2, ','.join(renamings), self.model2)
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
            self.model1, ComparisonChecker.OPERATION_TO_BISIMULATOR[self.operation], equivalence_version, self.model2)
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
        script_filename = "compare.svl"
        result_filename = "res.txt"
        self.__genSVL(script_filename)
        call(SVL_CALL_COMMAND % (script_filename, result_filename), shell=True)
        res = call('grep TRUE %s' % result_filename, shell=True)
        if (res == Checker.TERM_ERROR):
            return False
        else:
            return True


# This class is used to perform model checking operations on two models (LTS stored in two BCG format files)
# wrt an MCL property (stored in an MCL file)
class FormulaChecker(Checker):
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
        for model in [self.model1, self.model2]:
            svl_commands += SVL_FORMULA_CHECKING_TEMPLATE % (model, self.f)
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
        script_filename = "check.svl"
        result_filename = "res.txt"
        self.__genSVL(script_filename)
        call(SVL_CALL_COMMAND % (script_filename, result_filename), shell=True, stdout=sys.stdout)
        # check the result, return false if at least one FALSE in the result
        res = call('grep FALSE %s' % result_filename, shell=True, stdout=sys.stdout)
        if (res == Checker.TERM_ERROR):
            return True
        else:
            return False

##############################################################################################
# if __name__ == '__main__':
