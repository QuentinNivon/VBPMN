When CADP is updated (i.e., every month), it often breaks VBPMN due to the changes
made to the LNT language, that are not necessarily backward compatible.
To ease the usage of VBPMN and facilitate its migration from one version of CADP to
another, the following procedure has been created:

1) Go to the ``vbpmn_transformation/scripts'' folder (accessible from the current folder)

2) Check the version of CADP installed on your computer using the ``cadp_lib'' command

3) In the current directory, create a new folder whose name is the version name returned by the
``cadp_lib'' command executed in step 2) without the dash symbol (for example ``2023k'', or ``2024a'')

4) Copy all the files of an older folder (such as ``2023k'' or ``2024a'') inside your new folder

5) Make the necessary changes on these files in order to generate LNT specifications compliant with the current
CADP version (normally, only 3 files may be subject to changes: ``bpmntypes.lnt'', ``pif2lntv1.py'', and
``pif2lntv7.py''). To do so, check the latest changes on the CADP webpage ``https://cadp.inria.fr/changes.html''
or by using the ``$CADP/HISTORY'' file.

6) Go back to the previous directory (``vbpmn_transformation/scripts''), and open the ``p2lprovider.py'' file

7) On the first lines, below the existing versions, add a line with the name of the current CADP version (i.e.,
``V_2024_A = "2024-a"'')

8) Inside the ``if'' of the ``get_pif2lnt_module'', add a line
``
elif version == <my_version>:
    return build_import(version, process_is_balanced)
''

9) Save the file and close it

10) Go to the directory ``vbpmn_transformation/src/main/java/fr/inria/convecs/optimus/validator'' and open the
file ``VbpmnValidator.java''

11) Modify the file similarly to the what you did for the python file, that is,
    a) Add a new version constant at the beginning of the file
    b) Add an ``else if'' in the ``getBpmnTypesFilePath'' method to check the version

12) Save the file and close it

13) Regenerate the WAR file and upload it to your Tomcat instance
