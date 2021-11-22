package mwg.wb.business;

import java.net.InetAddress;
import java.net.UnknownHostException;

import mwg.wb.common.DataCenterType;
import mwg.wb.common.DidxHelper;

public class DataCenterHelper {

	public static int GetDataCenter(int DataCenterConfig) {

		int DataCenter = 0;

		try {
			if (DataCenterConfig == 54) {// webservice
				DataCenter = DataCenterType.Backup;
			}
			if (DataCenterConfig == 30 || DataCenterConfig == 31 || DataCenterConfig == 6 || DataCenterConfig == 7 || 
					DataCenterConfig == 130 || DataCenterConfig == 131 || DataCenterConfig == 132 || DataCenterConfig == 133) {
				DataCenter = DataCenterType.Main;// 4, CHINH 5.71
			}
			if (DataCenterConfig == 32 || DataCenterConfig == 56 || DataCenterConfig == 8 || DataCenterConfig == 9 || 
					DataCenterConfig == 134 || DataCenterConfig == 135 || DataCenterConfig == 136 || DataCenterConfig == 137) {
				DataCenter = DataCenterType.Backup;// 2, PHU 3.71
			}
			if (DataCenterConfig == 198) {
				// DataCenter = 2;//backup
				DataCenter = 4;// main
			}
			if (DataCenterConfig == 123) {
				DataCenter = DataCenterType.Beta;// beta
			}

		} catch (Exception e) {

		}
		if (DataCenter == 0) {
			if (DidxHelper.isBeta()) {
				DataCenter = DataCenterType.Beta; //
				// DataCenter = 3;
			} else if (DidxHelper.isLocal()) {
				DataCenter = DataCenterType.Local; //
				// DataCenter = 3;
			} else if (DidxHelper.isHanh()) {
				DataCenter = DataCenterType.Local; //
				// DataCenter = 3;
			} else if (DidxHelper.isPhi()) {// for test
				// DataCenter = DataCenterType.Main; //
				DataCenter = DataCenterType.Main;
				// DataCenter = 3;
			} else if (DidxHelper.isDat()) {// for test
				// DataCenter = DataCenterType.Main; //
				DataCenter = DataCenterType.Main;
				// DataCenter = 3;
			}
			else if (DidxHelper.isLoc()) {//for test
				try {
					String ip = InetAddress.getLocalHost().getHostAddress();
					if(ip.startsWith("10")) DataCenter = DataCenterType.Beta;
					else if(ip.startsWith("172")) DataCenter = DataCenterType.Main;
					else DataCenter = DataCenterType.Beta;
				} catch (UnknownHostException e) {
					e.printStackTrace();
					DataCenter = DataCenterType.Beta;
				}
			} else if (DidxHelper.isStaging()) {
				DataCenter = 4;// DataCenterType.Staging;

			} else {
				DataCenter = DataCenterType.Beta;
			}

		}

		return DataCenter;
	}
}
