When CADP is updated (i.e., every month), it often breaks VBPMN due to the changes
made to the LNT language, that are not necessarily backward compatible.
To ease the usage of VBPMN and facilitate its migration from one version of CADP to
another, the following procedure has been created:

1) Go to the ``vbpmn_transformation/scripts'' folder (accessible from the current folder)

2) Check the version of CADP installed on your computer using the ``cadp_lib'' command

3) In the current directory, create a new folder whose name is the version name returned by the
``cadp_lib'' command executed in step 2) without the dash symbol (for example ``2023k'', or ``2024a'')

4) Copy all the files of the most recent folder (such as ``2023k'' or ``2024a'') inside your new folder

5) Make the necessary changes on these files in order to generate LNT specifications compliant with the current
CADP version (normally, only 3 files may be subject to changes: ``bpmntypes.lnt'', ``pif2lntv1.py'', and
``pif2lntv7.py''). To do so, check the latest changes on the CADP webpage ``https://cadp.inria.fr/changes.html''
or by using the ``$CADP/HISTORY'' file.

6) Modify the ``CHANGES.txt'' file according to concerned CADP items

7) Regenerate the WAR file and upload it to your Tomcat instance

8) OPTIONALLY: Remove directories corresponding to versions older than 1 year, as CADP license files last at
   most 1 year. For instance, if the current directory is ``2024-b'', you can remove all directories up to
   ``2023-a'' included.
