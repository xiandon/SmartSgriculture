package com.enlern.pen.sms.hikvision;

import com.hikvision.netsdk.AlarmCallBack_V30;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_ALARMER;
import com.hikvision.netsdk.NET_DVR_ALARMINFO_V30;
import com.hikvision.netsdk.NET_DVR_BASE_ALARM;

public class AlarmTest {
	private static AlarmCallBack_V30 AlarmCbf = null;
    private static void processAlarmData(int lCommand, NET_DVR_ALARMER Alarmer, NET_DVR_BASE_ALARM AlarmInfo)
    {
    	String sIP = new String(Alarmer.sDeviceIP);
    	System.out.println("recv alarm from:" + sIP);
    	if(lCommand == HCNetSDK.COMM_ALARM_V30)
    	{
    		NET_DVR_ALARMINFO_V30 strAlarmInfo = (NET_DVR_ALARMINFO_V30)AlarmInfo;
        	System.out.println("recv alarm V30:" + strAlarmInfo.dwAlarmType);
    	}
    }
	public static void Test_SetupAlarm(int iUserID)
	{
 		if(AlarmCbf == null)
		{
			AlarmCbf = new AlarmCallBack_V30()
			{
				public void fMSGCallBack(int lCommand, NET_DVR_ALARMER Alarmer, NET_DVR_BASE_ALARM AlarmInfo)
				{
					   processAlarmData(lCommand, Alarmer, AlarmInfo);	  	
				}
			};
		} 

 		HCNetSDK.getInstance().NET_DVR_SetDVRMessageCallBack_V30(AlarmCbf);
		
		int iAlarmHandle = HCNetSDK.getInstance().NET_DVR_SetupAlarmChan_V30(iUserID);
 		if (-1 == iAlarmHandle)
		{
			System.out.println("NET_DVR_SetupAlarmChan_V30 failed!" + HCNetSDK.getInstance().NET_DVR_GetLastError());
		}
 		else
 		{
 			System.out.println("NET_DVR_SetupAlarmChan_V30 succeed!");
 		}
		try {
			Thread.sleep(10*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!HCNetSDK.getInstance().NET_DVR_CloseAlarmChan_V30(iAlarmHandle))
		{
			System.out.println("NET_DVR_CloseAlarmChan_V30 failed! error:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
		}
 		else
 		{
 			System.out.println("NET_DVR_CloseAlarmChan_V30 succeed!");
 		}
	

	}
}
