package edu.pdx.cecs.orcyclesensors;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.location.Location;
import android.util.Log;

public class EpocRecorder {

	private static final String MODULE_TAG = "EpocRecorder";

	private Context context;
	private final String bluetoothAddress;
	private boolean recordRawData;
	private long tripId;
	private String dataDir;
    private static Map<String, EpocRecorder> epocRecorders = new HashMap<String, EpocRecorder>();

    public enum State { IDLE, CONNECTING, RUNNING, PAUSED, FAILED };
	
    private State state = State.IDLE;

	private EpocRecorder(Context context, String bluetoothAddress, boolean recordRawData, long tripId, String dataDir) {
		this.context = context;
		this.bluetoothAddress = bluetoothAddress;
		this.recordRawData = recordRawData;
		this.tripId = tripId;
		this.dataDir = dataDir;
	}

	public static EpocRecorder create(Context context, String bluetoothAddress, boolean recordRawData, long tripId, String dataDir) {
		
		EpocRecorder epocRecorder = null;
		
		if (epocRecorders.containsKey(bluetoothAddress)) {
			epocRecorders.remove(bluetoothAddress);
		}

		epocRecorders.put(bluetoothAddress, epocRecorder = new EpocRecorder(context, bluetoothAddress, recordRawData, tripId, dataDir));

		return epocRecorder;
	}

	synchronized public void start(Context context) {
    	// state = State.CONNECTING; TODO: change back
    	state = State.RUNNING;
	}

	synchronized public void pause() {
		if (state == State.RUNNING) {
			state = State.PAUSED;
		}
		else {
			Log.e(MODULE_TAG, "Invalid state");
		}
	}

	synchronized public void resume() {
		if (state == State.PAUSED) {
			state = State.RUNNING;
		}
		else {
			Log.e(MODULE_TAG, "Invalid state");
		}
	}

	synchronized public void unregister() {
        closeRawDataFile();
        //signalReadings.clear();
        epocRecorders.remove(bluetoothAddress);
	}

	/**
	 * Write the result for each enabled sensor/group of enabled sensor signals
	 * @param tripData
	 * @param currentTimeMillis
	 * @param location
	 */
 	synchronized public void writeResult(TripData tripData, long currentTimeMillis, Location location) {
 	}
	
	synchronized public State getState() {
		return state;
	}
	
	protected void closeRawDataFile() {
	}
}
