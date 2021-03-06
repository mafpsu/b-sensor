/**
 *  ORcycle, Copyright 2014, 2015, PSU Transportation, Technology, and People Lab.
 *
 *  ORcycle 2.2.0 has introduced new app features: safety focus with new buttons
 *  to report safety issues and crashes (new questionnaires), expanded trip
 *  questionnaire (adding questions besides trip purpose), app utilization
 *  reminders, app tutorial, and updated font and color schemes.
 *
 *  @author Bryan.Blanc <bryanpblanc@gmail.com>    (code)
 *  @author Miguel Figliozzi <figliozzi@pdx.edu> and ORcycle team (general app
 *  design and features, report questionnaires and new ORcycle features)
 *
 *  For more information on the project, go to
 *  http://www.pdx.edu/transportation-lab/orcycle and http://www.pdx.edu/transportation-lab/app-development
 *
 *  Updated/modified for Oregon pilot study and app deployment.
 *
 *  ORcycle is free software: you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation, either version 3 of the License, or any later version.
 *  ORcycle is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 *  A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License along with
 *  ORcycle. If not, see <http://www.gnu.org/licenses/>.
 *
 *************************************************************************************
 *
 **	 Cycle Altanta, Copyright 2012 Georgia Institute of Technology
 *                                    Atlanta, GA. USA
 *
 *   @author Christopher Le Dantec <ledantec@gatech.edu>
 *   @author Anhong Guo <guoanhong15@gmail.com>
 *
 *   Updated/Modified for Atlanta's app deployment. Based on the
 *   CycleTracks codebase for SFCTA.
 *
 *   CycleTracks, Copyright 2009,2010 San Francisco County Transportation Authority
 *                                    San Francisco, CA, USA
 *
 * 	 @author Billy Charlton <billy.charlton@sfcta.org>
 *
 *   This file is part of CycleTracks.
 *
 *   CycleTracks is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CycleTracks is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CycleTracks.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

import android.content.Context;

public interface IRecordService {

	public final static int STATE_IDLE = 0;
	public final static int STATE_WAITING_FOR_DEVICE_CONNECT = 1;
	public final static int STATE_RECORDING = 2;
	public final static int STATE_PAUSED = 3;
	public final static int STATE_FULL = 4;
	public final static int STATE_DEVICE_CONNECT_FAILED = 5;

	public final static int DEVICES_STATE_NOT_ALL_CONNECTED = 0;
	public final static int DEVICES_STATE_ATLEAST_ONE_FAILED_CONNECT = 1;
	public final static int DEVICES_STATE_ALL_CONNECTED = 2;
	
	public int getState();

	public void startRecording(TripData trip, 
			ArrayList<AntDeviceInfo> devices, 
			ArrayList<SensorItem> sensors,
			ArrayList<ShimmerDeviceInfo> shimmerDeviceInfos,
			ArrayList<EpocDeviceInfo> epocDeviceInfos,
			long minTimeBetweenReadings,
			boolean recordRawData, String dataFileDir) throws Exception;

	public void cancelRecording();

	public long finishRecording(); // returns trip-id

	public long getCurrentTripID(); // returns trip-id

	public int pauseId();

	public void pauseRecording(int pauseId);

	public void resumeRecording();

	public void reset();

	public void setListener(IRecordServiceListener mia);
}
