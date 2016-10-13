/*
 * ******************************************************
 * Copyright VMware, Inc. 2014. All Rights Reserved.
 * ******************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Based on the a script in William Lam's VirtuallyGhetto blog.  More info:
 * https://github.com/lamw/vghetto-scripts/blob/master/perl/alarmManagement.pl
 * http://www.virtuallyghetto.com/
 */

package com.vmware.sample;

import java.util.ArrayList;
import java.util.List;

import com.vmware.utils.VMwareConnection;
import com.vmware.vim25.AlarmInfo;
import com.vmware.vim25.AlarmState;
import com.vmware.vim25.ManagedEntityStatus;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.VimPortType;

/*
 * Coding Conventions Used Here:
 * 1. The connection to vCenter is managed with in the "main" method of this class.
 * 2. Many methods are listed as "throws Exception" which means that the exceptions are ignored
 *    and printed out at the call stack.  If used in real development, exceptions should be caught
 *    and recovered from.
 * 3. Managed Object Reference variables are named ending with "Ref".
 *
 * Also: Full path names are used for all java classes when they are first used (for declarations
 * or to call static methods).  This makes it easier to find their source code, so you can understand
 * it.  For example "com.vmware.utils.VMwareConnection conn" rather than "VMwareConnection conn".
 */

/**
 * Acknowledges red and yellow alarms associated with the given Datacenter
 */
public class GetAndAcknowledgeDatacenterAlarms {

    /**
     * Returns the red and yellow alarms found in the <code>datacenter</code>
     *
     * @param conn
     *            the connection with vCenter
     * @param datacenter
     *            the managed object reference for a datacenter
     * @return the read and yellow alarms
     * @throws Exception
     *             if an exception occurred
     */
    private static List<com.vmware.vim25.ObjectContent> getAlarmsFromDatacenter(
            com.vmware.utils.VMwareConnection conn, com.vmware.vim25.ManagedObjectReference datacenter)
            throws Exception {

        List<ObjectContent> alarmList = new ArrayList<ObjectContent>();
        com.vmware.vim25.ServiceContent serviceContent = conn.getServiceContent();
        ManagedObjectReference alarmManager = serviceContent.getAlarmManager();
        com.vmware.vim25.VimPortType vimPort = conn.getVimPort();

        // retrieve list of alarm state associated with the datacenter
        List<com.vmware.vim25.AlarmState> alarmStateList = vimPort.getAlarmState(alarmManager, datacenter);
        System.out.printf("Here follows the list of red and yellow alarm(s)%n", alarmStateList.size());
        for (AlarmState alarmState : alarmStateList) {
            com.vmware.vim25.ManagedEntityStatus status = alarmState.getOverallStatus();
            // check if the alarm states need some attention
            if (ManagedEntityStatus.RED.equals(status) || ManagedEntityStatus.YELLOW.equals(status)) {
                ObjectContent alarm = conn.findObject(alarmState.getAlarm(), "info");
                com.vmware.vim25.AlarmInfo alarmInfo = (AlarmInfo) alarm.getPropSet().get(0).getVal();
                alarmList.add(alarm);
                System.out.printf("Alarm: %s - Status: %s - Created: %s%n", alarmInfo.getName(),
                        status, alarmState.getTime());
            }
        }
        return alarmList;
    }

    /**
     * Acknowledges red and yellow alarms found in the given Datacenter
     *
     * @param conn
     *            the connection with vCenter
     * @param datacenterName
     *            the datacenter name
     * @throws Exception
     *             if an exception occurred
     */
    public static void acknowledgeDatacenterAlarms(VMwareConnection conn, String datacenterName)
            throws Exception {
        com.vmware.vim25.ServiceContent serviceContent = conn.getServiceContent();
        ManagedObjectReference alarmManager = serviceContent.getAlarmManager();
        VimPortType vimPort = conn.getVimPort();
        // retrieve the datacenter managed object reference
        ManagedObjectReference datacenterRef = conn
                .findObject("Datacenter", datacenterName, "name").getObj();
        if (datacenterRef == null) {
            throw new Exception("Unable to locate the datacenter: " + datacenterName);
        }
        // get a list of red and yellow alarms
        List<ObjectContent> listAlarm = getAlarmsFromDatacenter(conn, datacenterRef);
        System.out.printf("%nAcknowledging the following [%s] alarm(s)%n", listAlarm.size());
        for (ObjectContent alarm : listAlarm) {
            // acknowledge each alarm
            vimPort.acknowledgeAlarm(alarmManager, alarm.getObj(), datacenterRef);
            AlarmInfo alarmInfo = (AlarmInfo) alarm.getPropSet().get(0).getVal();
            System.out.printf("Alarm: %s%n", alarmInfo.getName());
        }

    }

    /**
     * Runs the GetAndAcknowledgeDatacenterAlarms sample code, which shows how to retrieve the red
     * and yellow alarms associated with the Datacenter
     * <p>
     * Run with a command similar to this:<br>
     * <code>java -cp vim25.jar com.vmware.general.GetNumOfCpuPerCluster <ip_or_name> <user> <password> <datacenterName></code><br>
     * <code>java -cp vim25.jar com.vmware.general.GetNumOfCpuPerCluster 10.20.30.40 JoeUser JoePasswd datacenterName</code>
     *
     * @param args
     *            the ip_or_name, user, password, and datacenterName
     * @throws Exception
     *             if an exception occurred
     */
    public static void main(String[] args) throws Exception {

        if (args.length != 4) {
            System.out.println("Wrong number of arguments, must provide three arguments:");
            System.out.println("[1] The server name or IP address");
            System.out.println("[2] The user name to log in as");
            System.out.println("[3] The password to use");
            System.out.println("[4] The datacenter name");
            System.exit(1);
        }

        // handle input info
        String serverName = args[0];
        String userName = args[1];
        String password = args[2];
        String datacenterName = args[3];

        com.vmware.utils.VMwareConnection conn = null;
        try {
            // Step-1. Create a connection to vCenter, using the name, user, and password
            conn = new VMwareConnection(serverName, userName, password);

            // Step-2. Acknowledge red and yellow alarms associated with a given datacenter
            acknowledgeDatacenterAlarms(conn, datacenterName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // close connection to vCenter
            if (conn != null) {
                conn.close();
            }
        }
    }

}
