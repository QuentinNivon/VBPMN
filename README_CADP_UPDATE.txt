When CADP is updated (i.e., every month), it often breaks VBPMN due to the changes
made to the LNT language, that are not necessarily backward compatible.
To ease the usage of VBPMN and facilitate its migration from one version of CADP to
another, the following procedure has been created (author: Quentin NIVON/quentin.nivon@inria.fr):

1) Check the version of CADP installed on your computer using the ``cadp_lib -1'' command.

2) Go to the ``vbpmn_transformation/src/main/java/fr/inria/convecs/optimus/py_to_java/cadp_compliance''
   directory (accessible from the current directory).

3) In this directory, create a new folder whose name is the version name returned by the ``cadp_lib''
   command executed in step 1 without the dash symbol and preceded by an underscore (e.g., ``_2023k'',
   or ``_2024a'').

4) Copy all the files of the most recent folder (such as ``_2023k'' or ``_2024a'') inside your new folder and
   resolve all the packages names issues for the copied classes (switch imports to the newly created package).

5) Find your CADP version on the ``https://cadp.inria.fr/changes.html'' webpage and check the ``HISTORY file
   item'' column.
   If it contains black items only, open the ``CHANGES.txt'' file of the current folder and replace its content
   by ``Same as version <most_recent_previous_version>'' (i.e., ``Same as version 2024-a "Eindhoven"''). Then,
   go directly to step 8.
   Otherwise, if it contains AT LEAST ONE red item, open the ``$CADP/HISTORY'' file and copy the content
   of the corresponding item(s) inside the changes file, following the provided syntax of the file (see previous
   ``CHANGES.txt'' files for examples). Then, apply steps 6 & 7.

6) /!\ OPTIONAL /!\
   Modify the files ``BpmnTypesBuilder.java'' and ``Pif2Lnt.java'' according to the changes of the actual CADP
   version.
   To do so, analyse precisely the changes that you pasted in the ``CHANGES.txt'' file on step 5.

7) /!\ OPTIONAL /!\
   Run the ``check_compatibility.sh'' script or the JUnit test located at
   ``vbpmn_transformation/src/test/java/fr/inria/convecs/optimus/compatibility/FullTests.java'' to verify that
   your changes are compliant with the new version of CADP. If the script output errors, repeat steps 6) and 7)
   until the script returns no error.

8) Regenerate the WAR file (manually or using the ``generate_jar.sh'' script) and deploy it to your Tomcat
   instance (can be done automatically using the ``generate_jar_and_deploy.sh'' script).
   If generated manually, replace the old WAR file in ``vbpmn_dist'' by the new one.
   In any case, upload the new WAR file to the VBPMN website so that people can use VBPMN with the latest
   CADP version.

9) /!\ OPTIONAL /!\
   Remove the directories of ``vbpmn_transformation/src/main/java/fr/inria/convecs/optimus/py_to_java/cadp_compliance''
   corresponding to CADP versions older than 1 year. As CADP license files last at most 1 year, removing older
   folders is safe as they are no longer used by anybody. For instance, if the current directory is ``_2024b'',
   you can remove all directories up to ``_2023a'' included.

10) /!\ OPTIONAL - ADDING NEW TESTS /!\
    To add new test cases to the compatibility tests performed to check whether VBPMN is compliant with a new version
    of CADP, please follow these rules:
    - If needed, add your new PIF process to the ``vbpmn_transformation/src/main/resources/pif_examples'' directory.
    - Navigate to the ``vbpmn_transformation/src/test/java/fr/inria/convecs/optimus/compatibility/unit_tests'' directory
    - Copy/paste one of the ``Test<number>.java'' file and modify its top properties (PIF_FILE_1, PIF_FILE_2, etc.)
      according to your needs
    - You can also modify the body of the ``test()'' function if needed, but that may not be necessary in most cases.
    - That is all! Your test has been added to the test suit! :-) You can verify it by performing step 7) ;-)
