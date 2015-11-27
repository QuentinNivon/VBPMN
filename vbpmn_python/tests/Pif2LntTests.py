#
# IMPORTANT NOTE:
# working directory for this test should be the project root (e.g., /Users/pascalpoizat/IdeaProjects/vbpmn)
#
# too bad python unittest module does not support data providers (as in JUnit or TestNG),
# so we just iterate by hand and write bad tricks in the test function not just to stop at the first fail
# TODO use existing extensions to unittest with decorators

import unittest
import os, fnmatch
from pif2lnt import *

FILES_PATH = "out/test/vbpmn/pif/"
WORKING_PATH = os.getcwd()
SUFFIX_PATTERN = "*.pif"  # should be a pattern, e.g., "*.pif", not a suffix, e.g., ".pif"

# set True to perform PIF->LNT transformation only if needed (no output model or older than input one)
# set False else (transformation will be done in any case)
# IMPORTANT : SHOULD BE FALSE IN GENERAL FOR TESTS, LAZY MODE IS JUST FOR DEBUGGING STEPS
LAZYMODE = True

BIGSUFFIX1 = "_big10.pif"  # time > 10 min
BIGSUFFIX2 = "_bigXX.pif"  # out of memory

# put in this list the suffixes of files to exclude from provider, e.g., "_big10.pif", "_generated.pif", etc.
EXCLUSIONS = [BIGSUFFIX1, BIGSUFFIX2]

# put in this list the suffixes of files to delete after tests are done, e.g., ".o", "evaluator.bcg", etc.
USELESS = [".o", ".f", ".t", ".err", ".lnt", ".lotos", ".svl", ".log", "_raw.bcg", "_work.bcg",
           "bisimulator", "bisimulator.bcg", "evaluator4", "evaluator.bcg", "generator"]


def is_useless(filename):
    """
    checks if a filename is useless (has a suffix in USELESS)
    :param filename: filename to check
    :return: nothing
    """
    rtr = False
    for suffix in USELESS:
        if filename.endswith(suffix):
            rtr = True
            break
    return rtr


def is_excluded(filename):
    """
    checks if a filename should be excluded from tests (has a suffix in EXCLUSIONS), to be used by tests providers
    :param filename: filename to check
    :return: nothing
    """
    rtr = False
    for suffix in EXCLUSIONS:
        if filename.endswith(suffix):
            rtr = True
            break
    return rtr


def test_provider_1():
    """
    test provider to find all filenames matching some suffix under some directory
    :return: a list of couples (path, filename)
    """
    rtr = []
    for dirpath, dirnames, filenames in os.walk(FILES_PATH):
        for filename in filenames:
            if fnmatch.fnmatch(filename, SUFFIX_PATTERN) and not is_excluded(filename):
                rtr.append((dirpath, filename))
    return rtr


class MyTestCase(unittest.TestCase):
    def setUp(self):  # overriden, do not change name
        """
        set up of tests (performed BEFORE each test)
        :return: nothing
        """
        # os.chdir(WORKING_PATH)
        pass  # nothing to do for set up of tests

    def tearDown(self):  # overriden, do not change name
        """
        tear down of tests (performed AFTER each test)
        deletes all useless files to clean up the tests directories
        :return: nothing
        """
        os.chdir(WORKING_PATH)
        for dirpath, dirnames, filenames in os.walk(FILES_PATH):
            for filename in filenames:
                if is_useless(filename):
                    os.remove(os.path.join(dirpath, filename))

    def test_pif2lnt(self):
        """
        tries to transform all pif files into lnt/bcg
        :return:
        """
        print "test_pif2lnt"
        rtr = True
        results_ok = []
        results_ko = []
        for (dirpath, filename) in test_provider_1():
            try:
                pifFilename = os.path.join(dirpath, filename)
                print pifFilename
                os.chdir(os.path.join(WORKING_PATH, dirpath))
                if LAZYMODE:
                    gen = Loader()
                else:
                    gen = Generator()
                (result, name, alphabet) = gen(filename)
                results_ok.append(pifFilename)
            except:
                rtr = False
                results_ko.append(pifFilename)
        if not rtr:
            self.fail("error for some files: %s" % results_ko)


if __name__ == '__main__':
    WORKING_PATH = os.getcwd()
    unittest.main()
