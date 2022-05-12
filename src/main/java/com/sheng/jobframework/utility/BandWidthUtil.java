package com.sheng.jobframework.utility;


import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.EnumVariant;
import com.jacob.com.Variant;

import com.sheng.jobframework.jobdom.ACElement;

public class BandWidthUtil extends ACElement {
    public BandWidthUtil() {
        super();
    }

    public static void monitorWindowsBW() {
        String host =
            "localhost"; //Technically you should be able to connect to other hosts, but it takes setup
        String connectStr =
            String.format("winmgmts:\\\\%s\\root\\CIMV2", host);
        String query =
            "SELECT * FROM Win32_PerfRawData_Tcpip_NetworkInterface"; //Started = 1 means the service is running.
        ActiveXComponent axWMI = new ActiveXComponent(connectStr);
        //Execute the query
        Variant vCollection = axWMI.invoke("ExecQuery", new Variant(query));

        //Our result is a collection, so we need to work though the.
        EnumVariant enumVariant = new EnumVariant(vCollection.toDispatch());
        Dispatch item = null;
        while (enumVariant.hasMoreElements()) {
            item = enumVariant.nextElement().toDispatch();
            //Dispatch.call returns a Variant which we can convert to a java form.
            String BytesReceivedPerSec =
                Dispatch.call(item, "BytesReceivedPerSec").toString();
            String BytesSentPerSec =
                Dispatch.call(item, "BytesSentPerSec").toString();
            String BytesTotalPerSec =
                Dispatch.call(item, "BytesTotalPerSec").toString();
            String Caption = Dispatch.call(item, "Caption").toString();
            String CurrentBandwidth =
                Dispatch.call(item, "CurrentBandwidth").toString();
            String Description = Dispatch.call(item, "Description").toString();
            String Frequency_Object =
                Dispatch.call(item, "Frequency_Object").toString();
            String Frequency_PerfTime =
                Dispatch.call(item, "Frequency_PerfTime").toString();
            String Frequency_Sys100NS =
                Dispatch.call(item, "Frequency_Sys100NS").toString();
            String Name = Dispatch.call(item, "Name").toString();
            String OutputQueueLength =
                Dispatch.call(item, "OutputQueueLength").toString();
            String PacketsOutboundDiscarded =
                Dispatch.call(item, "PacketsOutboundDiscarded").toString();
            String PacketsOutboundErrors =
                Dispatch.call(item, "PacketsOutboundErrors").toString();
            String PacketsPerSec =
                Dispatch.call(item, "PacketsPerSec").toString();
            String PacketsReceivedDiscarded =
                Dispatch.call(item, "PacketsReceivedDiscarded").toString();
            String PacketsReceivedErrors =
                Dispatch.call(item, "PacketsReceivedErrors").toString();
            String PacketsReceivedNonUnicastPerSec =
                Dispatch.call(item, "PacketsReceivedNonUnicastPerSec").toString();
            String PacketsReceivedPerSec =
                Dispatch.call(item, "PacketsReceivedPerSec").toString();
            String PacketsReceivedUnicastPerSec =
                Dispatch.call(item, "PacketsReceivedUnicastPerSec").toString();
            String PacketsReceivedUnknown =
                Dispatch.call(item, "PacketsReceivedUnknown").toString();
            String PacketsSentNonUnicastPerSec =
                Dispatch.call(item, "PacketsSentNonUnicastPerSec").toString();
            String PacketsSentPerSec =
                Dispatch.call(item, "PacketsSentPerSec").toString();
            String PacketsSentUnicastPerSec =
                Dispatch.call(item, "PacketsSentUnicastPerSec").toString();
            String Timestamp_Object =
                Dispatch.call(item, "Timestamp_Object").toString();
            String Timestamp_PerfTime =
                Dispatch.call(item, "Timestamp_PerfTime").toString();
            String Timestamp_Sys100NS =
                Dispatch.call(item, "Timestamp_Sys100NS").toString();

            System.out.println("Name = " + Name);
            System.out.println("BytesReceivedPerSec = " + BytesReceivedPerSec);
            System.out.println("BytesSentPerSec = " + BytesSentPerSec);
            System.out.println("BytesTotalPerSec = " + BytesTotalPerSec);
            System.out.println("Caption = " + Caption);
            System.out.println("CurrentBandwidth = " + CurrentBandwidth);
            System.out.println("Description = " + Description);
            System.out.println("Frequency_Object = " + Frequency_Object);
            System.out.println("Frequency_PerfTime = " + Frequency_PerfTime);
            System.out.println("Frequency_Sys100NS = " + Frequency_Sys100NS);
            System.out.println("OutputQueueLength = " + OutputQueueLength);
            System.out.println("PacketsOutboundDiscarded = " +
                               PacketsOutboundDiscarded);
            System.out.println("PacketsOutboundErrors = " +
                               PacketsOutboundErrors);
            System.out.println("PacketsPerSec = " + PacketsPerSec);
            System.out.println("PacketsReceivedDiscarded = " +
                               PacketsReceivedDiscarded);
            System.out.println("PacketsReceivedErrors = " +
                               PacketsReceivedErrors);
            System.out.println("PacketsReceivedNonUnicastPerSec = " +
                               PacketsReceivedNonUnicastPerSec);
            System.out.println("PacketsReceivedPerSec = " +
                               PacketsReceivedPerSec);
            System.out.println("PacketsReceivedUnicastPerSec = " +
                               PacketsReceivedUnicastPerSec);
            System.out.println("PacketsReceivedUnknown = " +
                               PacketsReceivedUnknown);
            System.out.println("PacketsSentNonUnicastPerSec = " +
                               PacketsSentNonUnicastPerSec);
            System.out.println("PacketsSentPerSec = " + PacketsSentPerSec);
            System.out.println("PacketsSentUnicastPerSec = " +
                               PacketsSentUnicastPerSec);
            System.out.println("Timestamp_Object = " + Timestamp_Object);
            System.out.println("Timestamp_PerfTime = " + Timestamp_PerfTime);
            System.out.println("Timestamp_Sys100NS = " + Timestamp_Sys100NS);
            System.out.println("-------------------------------------------");
        }
    }
}
