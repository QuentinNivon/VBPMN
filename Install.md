Using Tomcat WAR to Run VBPMN
==================================

 - Download the WAR from the VBPMN site

[**Download VBPMN**](https://quentinnivon.github.io/vbpmn/transformation.war) 

**Via Tomcat Manager**

- Go to the directory where tomcat is installed.
- In the `/conf` folder, add the manager role in the `tomcat-users.xml` file. 
Add the following lines to the xml file 
 `<user username="admin" password="admin" roles="tomcat, manager-gui, admin"/>`
- Now start the server using the startup scripts in `/bin` folder. Run the `startup.sh` script
- Once the server is up and running, access the [tomcat manager](http://localhost:8080/manager/html). Use admin/admin 
as username and password if prompted
- In the manager UI, scroll down to *WAR to deploy* location and `deploy` the WAR

**Manual Deploy**
- Go to the directory where tomcat is installed.
- Copy the WAR to `/webapps` folder.
- Start the server using the startup scripts in `/bin` folder. Run the `startup.sh` script

**Access the Application**
- Once the server is up and running, the application can be accessed at: http://localhost:8080/transformation/home.html 

**Tomcat Manager Undeploy**
- Once the server is up and running, access the [tomcat manager](http://localhost:8080/manager/html). Use admin/admin 
as username and password if prompted
- In the list manager UI, the list of applications deployed can be seen. VBPMN app is listed as `transformation`. 
- Click `undeploy` button adjacent to the `transformation` application to undeploy the app

**Manual Undeploy**
- Stop the server if it is running using shutdown scripts in `/bin` folder. Run the `shutdown.sh` script
- Delete the `transformation` folder and `transformation.war` from the `/webapps` folder of the tomcat location.
