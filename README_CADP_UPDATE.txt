When CADP is updated (i.e., every month), it often breaks VBPMN due to the changes
made to the LNT language, that are not necessarily backward compatible.
To ease the usage of VBPMN and facilitate its migration from one version of CADP to
another, the following procedure has been created:

1) Go to the ``vbpmn_transformation/scripts'' folder (accessible from the current folder)

2) Check the version of CADP installed on your computer using the ``cadp_lib'' command

3) In the current directory, create a new folder whose name is the version name returned by the
   ``cadp_lib'' command executed in step 2) without the dash symbol (for example ``2023k'', or ``2024a'')

4) Copy all the files of the most recent folder (such as ``2023k'' or ``2024a'') inside your new folder

5) Find your version on the ``https://cadp.inria.fr/changes.html'' webpage and check the ``HISTORY file item''
   column.
   If it contains black items only, open the ``CHANGES.txt'' file an replace its content by
   ``Same as version <most_recent_previous_version>'' (i.e., ``Same as version 2024-a "Eindhoven"''). Then,
   go directly to step 8.
   Otherwise, if it contains AT LEAST ONE red item, open the ``$CADP/HISTORY'' file and copy the content
   of the corresponding item inside the changes file, following the provided syntax of the file (see previous
   ``CHANGES.txt'' files for examples). Then, apply steps 6 & 7.

6) /!\ OPTIONAL /!\ Make the necessary changes on these files in order to generate LNT specifications compliant
   with the current CADP version (normally, only 3 files may be subject to changes: ``bpmntypes.lnt'',
   ``pif2lntv1.py'', and ``pif2lntv7.py'').
   To do so, check the latest changes on the CADP webpage ``https://cadp.inria.fr/changes.html''
   or by using the ``$CADP/HISTORY'' file.

7) /!\ OPTIONAL /!\ Run the script ``check_compatibility.sh'' to verify that your changes are compliant with
   the new CADP version. If the script output errors, repeat steps 6) and 7) until the script returns no error.

8) Regenerate the WAR file and upload it to your Tomcat instance

9) /!\ OPTIONAL /!\ Remove directories corresponding to versions older than 1 year. As CADP license files
   last at most 1 year, removing older folders is safe as they are no longer used by anybody. For instance,
   if the current directory is ``2024-b'', you can remove all directories up to ``2023-a'' included.
