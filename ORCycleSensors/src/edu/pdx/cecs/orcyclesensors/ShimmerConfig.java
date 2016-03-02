package edu.pdx.cecs.orcyclesensors;

import edu.pdx.cecs.orcyclesensors.shimmer.android.Shimmer;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.ShimmerVerDetails;

public class ShimmerConfig {

	private final String bluetoothAddress;
	private final int version;

	private final double samplingRate;
	private final int accelerometerRange;
	private final int mGSRRange;
	private final double batteryLimit;
	private final int internalExpPower;
	private final boolean isLowPowerMagEnabled;
	private final boolean isLowPowerAccelEnabled;
	private final boolean isLowPowerGyroEnabled;
	private final int gyroRange;
	private final int magRange;
	private final int pressureResolution;
	private final int referenceElectrode;
	private final int leadOffDetection;
	private final int leadOffCurrent;
	private final int leadOffComparator;
	private final int fiveVReg;
	private final int exgGain;
	private final int exgRes;
	
	public ShimmerConfig(Shimmer shimmer, String bluetoothAddress, int shimmerVersion) {

		this.version = shimmerVersion;
		this.bluetoothAddress = bluetoothAddress;
		this.samplingRate = shimmer.getSamplingRate();
		this.accelerometerRange = shimmer.getAccelRange();
		this.mGSRRange = shimmer.getGSRRange();
		this.batteryLimit = shimmer.getBattLimitWarning();
		this.internalExpPower = shimmer.getInternalExpPower();
		this.isLowPowerMagEnabled = shimmer.isLowPowerMagEnabled();
		this.isLowPowerAccelEnabled = shimmer.isLowPowerAccelEnabled();
		this.isLowPowerGyroEnabled = shimmer.isLowPowerGyroEnabled();
		this.fiveVReg = shimmer.get5VReg();

    	// Shimmer version dependent
    	if (shimmerVersion == ShimmerVerDetails.HW_ID.SHIMMER_3){
        	gyroRange = shimmer.getGyroRange();
        	magRange = shimmer.getMagRange();
        	pressureResolution = shimmer.getPressureResolution();
        	referenceElectrode = shimmer.getEXGReferenceElectrode();
        	leadOffDetection = shimmer.getLeadOffDetectionMode();
        	leadOffCurrent = shimmer.getLeadOffDetectionCurrent();
        	leadOffComparator = shimmer.getLeadOffComparatorTreshold();
        	exgGain = getEXGGain(shimmer);
        	exgRes = getEXGResolution(shimmer);
    	}
    	else {
        	gyroRange = -1;
        	magRange = -1;
        	pressureResolution = -1;
        	referenceElectrode = -1;
        	leadOffDetection = -1;
        	leadOffCurrent = -1;
        	leadOffComparator = -1;
        	exgGain = -1;
        	exgRes = -1;
    	}
	}
	
	public String getBluetoothAddress() {
		return this.bluetoothAddress;
	}

	private int getEXGGain(Shimmer shimmer){
		
		int gain = -1;
		int gainEXG1CH1 = shimmer.getEXG1CH1GainValue();
		int gainEXG1CH2 = shimmer.getEXG1CH2GainValue();
		int gainEXG2CH1 = shimmer.getEXG2CH1GainValue();
		int gainEXG2CH2 = shimmer.getEXG2CH2GainValue();
		if(gainEXG1CH1 == gainEXG1CH2 && gainEXG1CH1 == gainEXG2CH1 && gainEXG1CH1 == gainEXG2CH2) //if all the chips are set to the same gain value
			gain = gainEXG1CH1;
		
		return gain;
	}
	
	private int getEXGResolution(Shimmer shimmer){
		
		int res = -1;

		long enabledSensors = shimmer.getEnabledSensors();
		if ((enabledSensors & Shimmer.SENSOR_EXG1_24BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_24BIT)>0){
			res = 24;
		}
		if ((enabledSensors & Shimmer.SENSOR_EXG1_16BIT)>0 && (enabledSensors & Shimmer.SENSOR_EXG2_16BIT)>0){
			res = 16;
		}
		
		return res;
	}

	public double getSamplingRate() {
		return this.samplingRate;
	}

	public int getAccelerometerRange() {
		return this.accelerometerRange;
	}
	
	public int getGSRRange() {
		return this.mGSRRange;
	}
	
	public double getBatteryLimit() {
		return this.batteryLimit;
	}

	public int getInternalExpPower() {
		return this.internalExpPower;
	}

	public boolean isLowPowerMagEnabled() {
		return this.isLowPowerMagEnabled;
	}
	
	public boolean isLowPowerAccelEnabled() {
		return this.isLowPowerAccelEnabled;
	}

	public boolean isLowPowerGyroEnabled() {
		return this.isLowPowerGyroEnabled;
	}

	public int getGyroRange() {
		return this.gyroRange;
	}

	public int getMagRange() {
		return this.magRange;
	}

	public int getPressureResolution() {
		return this.pressureResolution;
	}

	public int getReferenceElectrode() {
		return this.referenceElectrode;
	}

	public int getLeadOffDetection() {
		return this.leadOffDetection;
	}

	public int getLeadOffCurrent() {
		return this.leadOffCurrent;
	}

	public int getLadOffComparator() {
		return this.leadOffComparator;
	}

	public int get5VReg() {
		return this.fiveVReg;
	}

	public int getExgGain() {
		return this.exgGain;
	}

	public int getExgRes() {
		return this.exgRes;
	}
}