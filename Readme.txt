This sample code shows how to acknowledge red and yellow alarms associated with a given datacenter.

How To Run

In order to run this sample code you must provide four arguments:
[1] The server name or IP address
[2] The user name to log in as
[3] The password to use
[4] The datacenter name

You will need to get the vim25.jar library from the VMware vSphere JDK.  It is in the VMware-vSphere-SDK-5.5.0\vsphere-ws\java\JAXWS\lib directory.

You can run this sample code by downloading the zip file below, unzipping it and running a command
similar to the following:
java -cp vim25.jar com.vmware.sample.GetAndAcknowledgeDatacenterAlarms <ip_or_name> <user> <password> <datacenterName>
For example
java -cp vim25.jar com.vmware.sample.GetAndAcknowledgeDatacenterAlarms 10.20.30.40 JoeUser JoePassword datacenterName

Output

You will see the output similar to the following when you run the sample:
Here follows the list of red and yellow alarm(s)
Alarm: myAlarmDatacenter - Status: RED - Created: 2014-01-17T00:48:01.336501Z

Acknowledging the following [1] alarm(s)
Alarm: myAlarmDatacenter
