from subprocess import *
import os # Verification purpose
import pathlib # Verification purpose

CADP_KEYWORD = "VERSION"

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
            if (self.process_is_balanced):
                self.current_version_import = self.get_current_version_directory() + "." + "pif2lntv1"
            else:
                self.current_version_import = self.get_current_version_directory() + "." + "pif2lntv7"
        return self.current_version_import

def _get_current_version():
    # Run ``cadp_lib'' command to retrieve the CADP version installed on the machine
    raw_version = check_output(["cadp_lib"], text=True)
    left_index = raw_version.index(CADP_KEYWORD) + len(CADP_KEYWORD)
    right_index = raw_version.index('"')
    # ``version'' should contain something like "2023-k"
    version = raw_version[left_index:right_index].strip()

    return version
