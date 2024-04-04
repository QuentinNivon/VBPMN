from subprocess import *
import os           # Verification purpose
import pathlib      # Verification purpose

##
##
## The ``_get_current_version()'' function is using the output
## of the ``cadp_lib'' script that returns information about
## the installed version of CADP.
## The correctness of its behaviour is thus strongly linked to
## the immutability of the output of the ``cadp_lib'' script.
## In case of changes in this output, please adapt the function
## ``_get_current_version()'' accordingly.
##
##

class P2LProvider:
    def __init__(self, process_is_balanced):
        self.process_is_balanced = process_is_balanced
        self.current_version = None
        self.current_version_directory = None
        self.current_version_import = None

    def get_current_version(self):
        if self.current_version is None:
            self.current_version = _get_current_version()

        return self.current_version

    def get_current_version_directory(self):
        if self.current_version_directory is None:
            self.current_version_directory = self.get_current_version().replace('-', '')

        # START VERIFICATION OF VERSION DIRECTORY
        path_to_web_inf_dir = str(pathlib.Path(__file__).parent.resolve())
        version_directory = os.path.join(path_to_web_inf_dir, self.current_version_directory)
        
        if not os.path.isdir(version_directory):
            raise Exception("Directory |" + version_directory + "| does not exist!")
        # END VERIFICATION OF VERSION DIRECTORY

        return self.current_version_directory

    def get_current_version_import(self):
        if self.current_version_import is None:
            if self.process_is_balanced:
                self.current_version_import = self.get_current_version_directory() + ".pif2lntv1"
            else:
                self.current_version_import = self.get_current_version_directory() + ".pif2lntv1"

        return self.current_version_import

def _get_current_version():
    # Run ``cadp_lib'' command to retrieve the CADP version installed on the machine.
    # ``cadp_lib'' output is supposed to be ``VERSION <version_code> "<version_name>"''.
    # Thus, retrieving the version code from its output can be done by extracting the
    # second word of the sentence using ``<version_code>.split(" ")[1]''.
    # This may be subject to changes in further versions of CADP.
    
    raw_version = check_output(["cadp_lib", "-1"], text=True)
    version = raw_version.split(" ")[1].strip()

    return version
