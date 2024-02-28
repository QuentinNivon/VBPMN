from subprocess import *

CADP_KEYWORD = "VERSION"
V_2023_K = "2023-k"

def get_pif2lnt_module(process_is_balanced):
    # Run ``cadp_lib'' command to retrieve the CADP version installed on the machine
    raw_version = check_output(["cadp_lib"], text=True)
    left_index = raw_version.index(CADP_KEYWORD) + len(CADP_KEYWORD)
    right_index = raw_version.index('"')
    # ``version'' should contain something like "2023-k"
    version = raw_version[left_index:right_index].strip()

    # Double check if version is managed (this ``if'' construct is unnecessary)
    if False \
        or version == V_2023_K:
        return build_import(version, process_is_balanced)
    else:
        raise Exception("CADP version |" + version + "| is not yet managed!")

def build_import(version, process_is_balanced):
    if process_is_balanced:
        return version.replace('-', '') + "." + "pif2lntv1"
    else:
        return version.replace('-', '') + "." + "pif2lntv7"
