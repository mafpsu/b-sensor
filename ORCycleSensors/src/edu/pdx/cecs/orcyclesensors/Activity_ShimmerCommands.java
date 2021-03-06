package edu.pdx.cecs.orcyclesensors;

import edu.pdx.cecs.orcyclesensors.shimmer.android.Shimmer;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.Configuration;
import edu.pdx.cecs.orcyclesensors.shimmer.driver.ShimmerVerDetails;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class Activity_ShimmerCommands extends Activity {
	

	public static final String MODULE_TAG = "Activity_ShimmerCommands";
	public static final String EXTRA_BLUETOOTH_ADDRESS = "EXTRA_BLUETOOTH_ADDRESS";
	public static final String mDone = "Done";

	private static final int REQUEST_ENABLE_BT = 1;
	private final String[] samplingRate = new String [] {"8","16","51.2","102.4","128","204.8","256","512","1024","2048"};
    private final String[] accelRangeArray = {"+/- 1.5g","+/- 6g"};

    private LinearLayout ll_asc0;
    private LinearLayout ll_asc1;
    private LinearLayout ll_asc2;
    private LinearLayout ll_asc3;
    private CheckBox cBoxLowPowerMag;
    private CheckBox cBoxLowPowerAccel;
    private CheckBox cBoxLowPowerGyro;
    private CheckBox cBox5VReg;
    private CheckBox cBoxInternalExpPower;
    private Button buttonGyroRange;
    private Button buttonMagRange;
    private Button buttonGsr;
    private Button buttonPressureResolution;
    private Button buttonSampleRate;
    private Button buttonAccRange;
    private Button buttonBattVoltLimit;
    private Button buttonToggleLED;
    private Button buttonDone;
    private Button buttonTryAgain;
    private String mBluetoothAddress;
    
	private ShimmerService mService = null;
    private int shimmerVersion;	

	// The Handler that gets information back from the BluetoothChatService
    private Handler shimmerMessageHandler = new ShimmerMessageHandler();
	
    /**
     * Initialize Activity and inflate the UI
     * @param savedInstanceState
     */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			// Set window features
	        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
			setContentView(R.layout.activity_shimmer_commands);
	
			// Get bluetooth address
			BluetoothAdapter mBluetoothAdapter = null;
	        if(null == (mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter())) {
	        	Toast.makeText(this, "Device does not support Bluetooth\nExiting...", Toast.LENGTH_LONG).show();
	        	finish();
	        }
	        else if(!mBluetoothAdapter.isEnabled()) {     	
	        	Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	        	startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	    	}
	        else if ((null == mBluetoothAddress) || mBluetoothAddress.equals("")) {
				if (null != savedInstanceState) {
					mBluetoothAddress = savedInstanceState.getString(EXTRA_BLUETOOTH_ADDRESS, "");
				}
				else {
					mBluetoothAddress = getIntent().getStringExtra(EXTRA_BLUETOOTH_ADDRESS);
				}
			}
	
			if ((null == mBluetoothAddress) || mBluetoothAddress.equals("")) {
	        	Toast.makeText(this, "Device Bluetooth address not set\nExiting...", Toast.LENGTH_LONG).show();
	        	finish();
			}
			else {
		        mService = MyApplication.getInstance().getShimmerService();
				setTitle(mBluetoothAddress + " " + getString(R.string.assl_title_connecting));
		        setProgressBarIndeterminateVisibility(true);
			}
	    	
	        
	        // Set an EditText view to get user input 
	        final EditText editTextBattLimit = new EditText(getApplicationContext());
	        editTextBattLimit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
	        
	        ll_asc0 = (LinearLayout) findViewById(R.id.ll_asc0);
	        ll_asc1 = (LinearLayout) findViewById(R.id.ll_asc1);
	        ll_asc2 = (LinearLayout) findViewById(R.id.ll_asc2);
	        ll_asc3 = (LinearLayout) findViewById(R.id.ll_asc3);
	        buttonPressureResolution = (Button) findViewById(R.id.buttonPressureAccuracy);
			buttonGyroRange = (Button) findViewById(R.id.buttonGyroRange);
			buttonMagRange = (Button) findViewById(R.id.buttonMagRange);
			buttonGsr = (Button) findViewById(R.id.buttonGSR);
			buttonSampleRate = (Button) findViewById(R.id.buttonRate);
			buttonAccRange = (Button) findViewById(R.id.buttonAccel);
			buttonToggleLED = (Button) findViewById(R.id.buttonToggleLED);
			buttonBattVoltLimit = (Button) findViewById(R.id.buttonBattLimit);
			buttonDone = (Button) findViewById(R.id.buttonDone);
			buttonDone.setText("Cancel");
			buttonTryAgain = (Button) findViewById(R.id.buttonTryAgain);
			buttonTryAgain.setVisibility(View.GONE);
			buttonTryAgain.setOnClickListener(new ButtonTryAgain_OnClickListener());
	
			cBox5VReg = (CheckBox) findViewById(R.id.checkBox5VReg);
			cBoxLowPowerMag = (CheckBox) findViewById(R.id.checkBoxLowPowerMag);
			cBoxLowPowerAccel = (CheckBox) findViewById(R.id.checkBoxLowPowerAccel);
			cBoxLowPowerGyro = (CheckBox) findViewById(R.id.checkBoxLowPowerGyro);
			cBoxInternalExpPower  = (CheckBox) findViewById(R.id.CheckBoxIntExpPow);
	
	        buttonAccRange.setText("Accel Range" + "\n" + "(N/A)");
	        buttonGsr.setText("GSR Range" + "\n(N/A)");                  
	        
	        // ------------------------------------------------------------------------------
			
	        buttonToggleLED.setOnClickListener(new ButtonToggleLED_OnClickListener());
	
	        // ------------------------------------------------------------------------------
			
	        final AlertDialog.Builder dialogBattLimit = new AlertDialog.Builder(this);
			dialogBattLimit.setTitle("Battery Limit");
			dialogBattLimit.setMessage("Introduce the battery limit to be set");
			dialogBattLimit.setPositiveButton("Ok", new DialogBattLimit_OnClickListener(editTextBattLimit));
	        
			buttonBattVoltLimit.setText("Set Batt Limit " + "\n" + "(N/A V)");
			buttonBattVoltLimit.setOnClickListener(new ButtonBattVoltLimit_OnClickListener(editTextBattLimit, dialogBattLimit));
	
	        // ------------------------------------------------------------------------------
	    	
			final AlertDialog.Builder dialogRate = new AlertDialog.Builder(this);		 
	        dialogRate.setTitle("Sample Rate");
	        dialogRate.setItems(samplingRate, new DialogRate_OnClickListener());
	    	
	    	buttonSampleRate.setOnClickListener(new ButtonSampleRate_OnClickListener(dialogRate));
	
	        // ------------------------------------------------------------------------------
	    	
	    	final AlertDialog.Builder dialogAccelShimmer2 = new AlertDialog.Builder(this);		 
	    	dialogAccelShimmer2.setTitle("Accelerometer range");
	    	dialogAccelShimmer2.setItems(accelRangeArray, new DialogAccelShimmer2_OnClickListener());
	
	        // ------------------------------------------------------------------------------
	    	
	    	final AlertDialog.Builder dialogAccelShimmer3 = new AlertDialog.Builder(this);		 
	    	dialogAccelShimmer3.setTitle("Accelerometer range");
	    	dialogAccelShimmer3.setItems(Configuration.Shimmer3.ListofAccelRange, new DialogAccelShimmer3_OnClickListener());
	
	        buttonAccRange.setOnClickListener(new ButtonAccRange_OnClickListener(dialogAccelShimmer3, dialogAccelShimmer2));
	
	        // ------------------------------------------------------------------------------
	        
	        final AlertDialog.Builder dialogGyroRangeShimmer3 = new AlertDialog.Builder(this);		 
	        dialogGyroRangeShimmer3.setTitle("Gyroscope Range");
	        dialogGyroRangeShimmer3.setItems(Configuration.Shimmer3.ListofGyroRange, new DialogGyroRangeShimmer3_OnClickListener());
	        
	        buttonGyroRange.setOnClickListener(new ButtonGyroRange_OnClickListener(dialogGyroRangeShimmer3));
	
	        // ------------------------------------------------------------------------------
	        
	        final AlertDialog.Builder dialogMagRangeShimmer2 = new AlertDialog.Builder(this);		 
	        dialogMagRangeShimmer2.setTitle("Magnetometer Range");
	        dialogMagRangeShimmer2.setItems(Configuration.Shimmer2.ListofMagRange, new DialogMagRangeShimmer2_OnClickListener());
	        
	        // ------------------------------------------------------------------------------
	        
	        final AlertDialog.Builder dialogMagRangeShimmer3 = new AlertDialog.Builder(this);		 
	        dialogMagRangeShimmer3.setTitle("Magnetometer Range");
	        dialogMagRangeShimmer3.setItems(Configuration.Shimmer3.ListofMagRange, new DialogMagRangeShimmer3_OnClickListener());
	        
	        buttonMagRange.setOnClickListener(new ButtonMagRange_OnClickListener(dialogMagRangeShimmer3, dialogMagRangeShimmer2));
	
	        // ------------------------------------------------------------------------------
	        
	        final AlertDialog.Builder dialogPressureResolutionShimmer3 = new AlertDialog.Builder(this);		 
	        dialogPressureResolutionShimmer3.setTitle("Pressure Resolution");
	        dialogPressureResolutionShimmer3.setItems(Configuration.Shimmer3.ListofPressureResolution, new DialogPressureResolutionShimmer3_OnClickListener());
	        
	        buttonPressureResolution.setOnClickListener(new ButtonPressureResolution_OnClickListener(dialogPressureResolutionShimmer3));
	        
	        // ------------------------------------------------------------------------------
	        // The Gsr Range is the same for the Shimmer 3 and the Shimmer 2 so we only need to do one dialog
	        final AlertDialog.Builder dialogGsrRange = new AlertDialog.Builder(this);		 
	        dialogGsrRange.setTitle("Gsr Range");
	        dialogGsrRange.setItems(Configuration.Shimmer3.ListofGSRRange, new DialogGsrRange_OnClickListener());
	        
	        buttonGsr.setOnClickListener(new ButtonGsr_OnClickListener(dialogGsrRange));
	        
	        buttonDone.setOnClickListener(new ButtonDone_OnClickListener());
	        
	        ll_asc0.setVisibility(View.GONE);
	        ll_asc1.setVisibility(View.GONE);
	        ll_asc2.setVisibility(View.GONE);
	        ll_asc3.setVisibility(View.GONE);
		}
  		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
  		}
	}

    /**
     * 
     */
	@Override
	public void onResume() {
		super.onResume();

		try {
	  		mService.setMessageHandler(shimmerMessageHandler);
			mService.connectShimmer(mBluetoothAddress, "Device");
		}
  		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
  		}
	}

	/**
	 * 
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		try {
			switch (requestCode) {

			case REQUEST_ENABLE_BT:

				// When the request to enable Bluetooth returns
				if (resultCode == Activity.RESULT_OK) {
					// setMessage("\nBluetooth is now enabled");
					Toast.makeText(this, "Bluetooth is now enabled", Toast.LENGTH_SHORT).show();
				} else {
					// User did not enable Bluetooth or an error occured
					Toast.makeText(this, "Bluetooth not enabled\nExiting...", Toast.LENGTH_SHORT).show();
					finish();
				}
				break;
			}
		} 
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Save UI state changes to the savedInstanceState variable.
	 * This bundle will be passed to onCreate, onCreateView, and
	 * onCreateView if the parent activity is killed and restarted
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		try {
			savedInstanceState.putString(EXTRA_BLUETOOTH_ADDRESS, mBluetoothAddress);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		super.onSaveInstanceState(savedInstanceState);
	}
	
	/**
	 * 
	 */
	@Override
	public void onPause() {
		super.onPause();
		try {
			if ((null != mBluetoothAddress) && !mBluetoothAddress.equals("") && (null != mService)) {
				mService.disconnectShimmer(mBluetoothAddress);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	// *********************************************************************************
	// *                          Button OnClickListeners
	// *********************************************************************************

	private final class CBoxInternalExpPower_OnCheckedChangeListener implements OnCheckedChangeListener {
		
		private final Shimmer shimmer;

		private CBoxInternalExpPower_OnCheckedChangeListener(Shimmer shimmer) {
			this.shimmer = shimmer;
		}

		public void onCheckedChanged(CompoundButton arg0, boolean checked) {
			try {
				if (checked){
					shimmer.writeInternalExpPower(1);
				} else {
					shimmer.writeInternalExpPower(0);
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class CBoxLowPowerGyro_OnCheckedChangeListener implements OnCheckedChangeListener {
		private final Shimmer shimmer;

		private CBoxLowPowerGyro_OnCheckedChangeListener(Shimmer shimmer) {
			this.shimmer = shimmer;
		}

		public void onCheckedChanged(CompoundButton arg0, boolean checked) {
			try {
				if (checked){
					shimmer.enableLowPowerGyro(true);
				} else {
					shimmer.enableLowPowerGyro(false);
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class CBoxLowPowerAccel_OnCheckedChangeListener implements OnCheckedChangeListener {
		
		private final Shimmer shimmer;

		private CBoxLowPowerAccel_OnCheckedChangeListener(Shimmer shimmer) {
			this.shimmer = shimmer;
		}

		public void onCheckedChanged(CompoundButton arg0, boolean checked) {
			try {
				if (checked){
					shimmer.enableLowPowerAccel(true);
				} else {
					shimmer.enableLowPowerAccel(false);
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class CBox5VReg_OnCheckedChangeListener implements OnCheckedChangeListener {
		
		public void onCheckedChanged(CompoundButton arg0, boolean checked) {
			try {
				if (checked){
					mService.write5VReg(mBluetoothAddress, 1);
				} else {
					mService.write5VReg(mBluetoothAddress, 0);
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class CBoxLowPowerMag_OnCheckedChangeListener implements OnCheckedChangeListener {
		
		public void onCheckedChanged(CompoundButton arg0, boolean checked) {
			try {
				if (checked){
					mService.enableLowPowerMag(mBluetoothAddress, true);
				} else {
					mService.enableLowPowerMag(mBluetoothAddress, false);
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class ButtonDone_OnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			try {
				finish();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class ButtonTryAgain_OnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			try {
				setTitle(mBluetoothAddress + " " + getString(R.string.assl_title_connecting));
		        setProgressBarIndeterminateVisibility(true);
				buttonTryAgain.setVisibility(View.GONE);
				mService.connectShimmer(mBluetoothAddress, "Device");
			}
	  		catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
	  		}
		}
	}

	private final class ButtonGsr_OnClickListener implements OnClickListener {
		
		private final Builder dialogGsrRange;

		private ButtonGsr_OnClickListener(Builder dialogGsrRange) {
			this.dialogGsrRange = dialogGsrRange;
		}

		@Override
		public void onClick(View v) {
			try {
				dialogGsrRange.show();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class ButtonPressureResolution_OnClickListener implements OnClickListener {
		
		private final Builder dialogPressureResolutionShimmer3;

		private ButtonPressureResolution_OnClickListener(Builder dialogPressureResolutionShimmer3) {
			this.dialogPressureResolutionShimmer3 = dialogPressureResolutionShimmer3;
		}

		@Override
		public void onClick(View v) {
			try {
				dialogPressureResolutionShimmer3.show();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class ButtonMagRange_OnClickListener implements OnClickListener {
		
		private final Builder dialogMagRangeShimmer3;
		private final Builder dialogMagRangeShimmer2;

		private ButtonMagRange_OnClickListener(Builder dialogMagRangeShimmer3, Builder dialogMagRangeShimmer2) {
			this.dialogMagRangeShimmer3 = dialogMagRangeShimmer3;
			this.dialogMagRangeShimmer2 = dialogMagRangeShimmer2;
		}

		@Override
		public void onClick(View v) {
			try {
				if (mService.getShimmerVersion(mBluetoothAddress) == ShimmerVerDetails.HW_ID.SHIMMER_3)
					dialogMagRangeShimmer3.show();
				else
					dialogMagRangeShimmer2.show();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class ButtonGyroRange_OnClickListener implements OnClickListener {
		
		private final Builder dialogGyroRangeShimmer3;

		private ButtonGyroRange_OnClickListener(Builder dialogGyroRangeShimmer3) {
			this.dialogGyroRangeShimmer3 = dialogGyroRangeShimmer3;
		}

		@Override
		public void onClick(View v) {
			try {
				dialogGyroRangeShimmer3.show();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class ButtonAccRange_OnClickListener implements OnClickListener {
		
		private final Builder dialogAccelShimmer3;
		private final Builder dialogAccelShimmer2;

		private ButtonAccRange_OnClickListener(Builder dialogAccelShimmer3, Builder dialogAccelShimmer2) {
			this.dialogAccelShimmer3 = dialogAccelShimmer3;
			this.dialogAccelShimmer2 = dialogAccelShimmer2;
		}

		@Override
		public void onClick(View v) {
			try {
				if (mService.getShimmerVersion(mBluetoothAddress)!=ShimmerVerDetails.HW_ID.SHIMMER_3)
					dialogAccelShimmer2.show();
				else
					dialogAccelShimmer3.show();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class ButtonSampleRate_OnClickListener implements OnClickListener {
		
		private final Builder dialogRate;

		private ButtonSampleRate_OnClickListener(Builder dialogRate) {
			this.dialogRate = dialogRate;
		}

		@Override
		public void onClick(View v) {
			try {
				dialogRate.show();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class ButtonBattVoltLimit_OnClickListener implements OnClickListener {
		
		private final EditText editTextBattLimit;
		private final Builder dialogBattLimit;

		private ButtonBattVoltLimit_OnClickListener(EditText editTextBattLimit, Builder dialogBattLimit) {
			this.editTextBattLimit = editTextBattLimit;
			this.dialogBattLimit = dialogBattLimit;
		}

		public void onClick(View arg0) {
			try {
				// This is done in order to avoid an error when the dialog is
				// displayed again after being cancelled
				if (editTextBattLimit.getParent() != null) {
					ViewGroup parentViewGroup = (ViewGroup) editTextBattLimit.getParent();
					parentViewGroup.removeView(editTextBattLimit);
				}
	
				dialogBattLimit.setView(editTextBattLimit);
				dialogBattLimit.show();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class ButtonToggleLED_OnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			try {
				mService.toggleLED(mBluetoothAddress);
				Toast.makeText(getApplicationContext(), "Toggle LED", Toast.LENGTH_SHORT).show();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	// *********************************************************************************
	// *                          Dialog OnClickListeners
	// *********************************************************************************

	private final class DialogGsrRange_OnClickListener implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int item) {
			
			try {
				Log.d("Shimmer",Configuration.Shimmer3.ListofGSRRange[item]);
			    int gsrRange = 0;
			    if (Configuration.Shimmer3.ListofGSRRange[item] == Configuration.Shimmer3.ListofGSRRange[0]){
			    	gsrRange = 0;
			    } else if (Configuration.Shimmer3.ListofGSRRange[item] == Configuration.Shimmer3.ListofGSRRange[1]){
			    	gsrRange = 1;
			    } else if (Configuration.Shimmer3.ListofGSRRange[item] == Configuration.Shimmer3.ListofGSRRange[2]){
			    	gsrRange = 2;
			    } else if (Configuration.Shimmer3.ListofGSRRange[item] == Configuration.Shimmer3.ListofGSRRange[3]){
			    	gsrRange = 3;
			    } else if (Configuration.Shimmer3.ListofGSRRange[item] == Configuration.Shimmer3.ListofGSRRange[4]){
			    	gsrRange = 4;
			    }
	
			    mService.writeGSRRange(mBluetoothAddress, gsrRange);
			    Toast.makeText(getApplicationContext(), "Gsr range changed. New range = "+Configuration.Shimmer3.ListofGSRRange[item], Toast.LENGTH_SHORT).show();
			    buttonGsr.setText("GSR Range"+"\n"+"("+Configuration.Shimmer3.ListofGSRRange[item]+")");
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogPressureResolutionShimmer3_OnClickListener implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int item) {
			try {
				Log.d("Shimmer", Configuration.Shimmer3.ListofPressureResolution[item]);
				int pressureRes = 0;
	
				if (Configuration.Shimmer3.ListofPressureResolution[item] == "Low") {
					pressureRes = 0;
				} else if (Configuration.Shimmer3.ListofPressureResolution[item] == "Standard") {
					pressureRes = 1;
				} else if (Configuration.Shimmer3.ListofPressureResolution[item] == "High") {
					pressureRes = 2;
				} else if (Configuration.Shimmer3.ListofPressureResolution[item] == "Very High") {
					pressureRes = 3;
				}
	
				mService.writePressureResolution(mBluetoothAddress, pressureRes);
				Toast.makeText(getApplicationContext(), "Pressure resolution changed. New resolution = " + Configuration.Shimmer3.ListofPressureResolution[item], Toast.LENGTH_SHORT).show();
				buttonPressureResolution.setText("Pressure Res" + "\n" + "(" + Configuration.Shimmer3.ListofPressureResolution[item] + ")");
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogMagRangeShimmer3_OnClickListener implements DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int item) {
			try {
				 Log.d("Shimmer",Configuration.Shimmer3.ListofMagRange[item]);
				 int magRange=0;
			  
				 if (Configuration.Shimmer3.ListofMagRange[item]==Configuration.Shimmer3.ListofMagRange[0]){
					magRange=1;
				} else if (Configuration.Shimmer3.ListofMagRange[item]==Configuration.Shimmer3.ListofMagRange[1]){
					magRange=2;
				} else if (Configuration.Shimmer3.ListofMagRange[item]==Configuration.Shimmer3.ListofMagRange[2]){
					magRange=3;
				} else if (Configuration.Shimmer3.ListofMagRange[item]==Configuration.Shimmer3.ListofMagRange[3]){
					magRange=4;
				} else if (Configuration.Shimmer3.ListofMagRange[item]==Configuration.Shimmer3.ListofMagRange[4]){
					magRange=5;
				} else if (Configuration.Shimmer3.ListofMagRange[item]==Configuration.Shimmer3.ListofMagRange[5]){
					magRange=6;
				} else if (Configuration.Shimmer3.ListofMagRange[item]==Configuration.Shimmer3.ListofMagRange[6]){
					magRange=7;
				}

		    	mService.writeMagRange(mBluetoothAddress, magRange);
			    Toast.makeText(getApplicationContext(), "Magnometer rate changed. New rate = "+Configuration.Shimmer3.ListofMagRange[item], Toast.LENGTH_SHORT).show();
			    buttonMagRange.setText("Mag Range"+"\n"+"("+Configuration.Shimmer3.ListofMagRange[item]+")");
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogMagRangeShimmer2_OnClickListener implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int item) {
			try {
				Log.d("Shimmer",Configuration.Shimmer2.ListofMagRange[item]);
				int magRange=0;
				if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 0.8Ga"){
					magRange=0;
				} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 1.3Ga"){
					magRange=1;
				} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 1.9Ga"){
					magRange=2;
				} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 2.5Ga"){
					magRange=3;
				} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 4.0Ga"){
					magRange=4;
				} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 4.7Ga"){
					magRange=5;
				} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 5.6Ga"){
					magRange=6;
				} else if (Configuration.Shimmer2.ListofMagRange[item]=="+/- 8.1Ga"){
						magRange=7;
				}
 
				mService.writeMagRange(mBluetoothAddress, magRange);
				Toast.makeText(getApplicationContext(), "Magnometer rate changed. New rate = "+Configuration.Shimmer2.ListofMagRange[item], Toast.LENGTH_SHORT).show();
				buttonMagRange.setText("Mag Range"+"\n"+"("+Configuration.Shimmer2.ListofMagRange[item]+")");
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogGyroRangeShimmer3_OnClickListener implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int item) {
			try {
				Log.d("Shimmer",Configuration.Shimmer3.ListofGyroRange[item]);
			    int gyroRange=0;
			  
			    if (Configuration.Shimmer3.ListofGyroRange[item]==Configuration.Shimmer3.ListofGyroRange[0]){
				    gyroRange=0;
			    } else if (Configuration.Shimmer3.ListofGyroRange[item]==Configuration.Shimmer3.ListofGyroRange[1]){
			    	gyroRange=1;
			    } else if (Configuration.Shimmer3.ListofGyroRange[item]==Configuration.Shimmer3.ListofGyroRange[2]){
			    	gyroRange=2;
			    } else if (Configuration.Shimmer3.ListofGyroRange[item]==Configuration.Shimmer3.ListofGyroRange[3]){
			    	gyroRange=3;
			    }

			    mService.writeGyroRange(mBluetoothAddress, gyroRange);
			    Toast.makeText(getApplicationContext(), "Gyroscope rate changed. New rate = "+Configuration.Shimmer3.ListofGyroRange[item], Toast.LENGTH_SHORT).show();
			    buttonGyroRange.setText("Gyro Range"+"\n"+"("+Configuration.Shimmer3.ListofGyroRange[item]+")");
            }
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogAccelShimmer3_OnClickListener implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int item) {
			try {
				Log.d("Shimmer",Configuration.Shimmer3.ListofAccelRange[item]);
				int accelRange=0;
				
				if (Configuration.Shimmer3.ListofAccelRange[item]=="+/- 2g"){
					accelRange=0;
				} else if (Configuration.Shimmer3.ListofAccelRange[item]=="+/- 4g"){
					accelRange=1;
				} else if (Configuration.Shimmer3.ListofAccelRange[item]=="+/- 8g"){
					accelRange=2;
				} else if (Configuration.Shimmer3.ListofAccelRange[item]=="+/- 16g"){
					accelRange=3;
				}

			    if(accelRange==0)
			    	cBoxLowPowerAccel.setEnabled(false);
			    else
			    	cBoxLowPowerAccel.setEnabled(true);
			    
			    mService.writeAccelRange(mBluetoothAddress, accelRange);
				Toast.makeText(getApplicationContext(), "Accelerometer rate changed. New rate = "+Configuration.Shimmer3.ListofAccelRange[item], Toast.LENGTH_SHORT).show();
				buttonAccRange.setText("Accel Range"+"\n"+"("+Configuration.Shimmer3.ListofAccelRange[item]+")");
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogAccelShimmer2_OnClickListener implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int item) {
			try {
			 	Log.d("Shimmer",accelRangeArray[item]);
		    	int accelRange=0;

			    if (accelRangeArray[item]=="+/- 1.5g"){
			    	accelRange=0;
			    } else if (accelRangeArray[item]=="+/- 6g"){
			    	accelRange=3;
			    }
			    mService.writeAccelRange(mBluetoothAddress, accelRange);
			    Toast.makeText(getApplicationContext(), "Accelerometer rate changed. New rate = "+accelRangeArray[item], Toast.LENGTH_SHORT).show();
			    buttonAccRange.setText("Accel Range"+"\n"+"("+accelRangeArray[item]+")");
            }
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogRate_OnClickListener implements DialogInterface.OnClickListener {
		
		public void onClick(DialogInterface dialog, int item) {
			try {
				 Log.d("Shimmer",samplingRate[item]);
				 double newRate = Double.valueOf(samplingRate[item]);
				 mService.writeSamplingRate(mBluetoothAddress, newRate);
				 Toast.makeText(getApplicationContext(), "Sample rate changed. New rate = "+newRate+" Hz", Toast.LENGTH_SHORT).show();
				 buttonSampleRate.setText("Sampling Rate "+"\n"+"("+newRate+" Hz)");
            }
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogBattLimit_OnClickListener implements DialogInterface.OnClickListener {
		
		private final EditText editTextBattLimit;

		private DialogBattLimit_OnClickListener(EditText editTextBattLimit) {
			this.editTextBattLimit = editTextBattLimit;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			try {
				double newLimit = Double.parseDouble(editTextBattLimit.getText().toString());
				mService.setBattLimitWarning(mBluetoothAddress, newLimit);
				Toast.makeText(getApplicationContext(), "Battery limit changed. New limit = " + newLimit + " V", Toast.LENGTH_SHORT).show();
				buttonBattVoltLimit.setText("Set Batt Limit " + "\n" + "(" + newLimit + " V)");
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	// *********************************************************************************
	// *                          Shimmer Message Handler
	// *********************************************************************************

	private final class ShimmerMessageHandler extends Handler {
		
		public void handleMessage(Message msg) {

			try {
				switch (msg.what) {
	            
	            case Shimmer.MESSAGE_STATE_CHANGE:
	            	
	                switch (msg.arg1) {
	
	                case Shimmer.STATE_CONNECTED: //this has been deprecated
	                    break;
	                    
	                case Shimmer.MSG_STATE_FULLY_INITIALIZED:
	            		buttonDone.setText("Done");
	        			buttonTryAgain.setVisibility(View.GONE);
	                	updateView();
	                    break;
	
	                case Shimmer.STATE_CONNECTING:
	                    break;
	                    
	                case Shimmer.STATE_NONE:
	                    setProgressBarIndeterminateVisibility(false);
	        			setTitle(R.string.assl_title_not_connected);
	            		buttonDone.setText("Cancel");
	        			buttonTryAgain.setVisibility(View.VISIBLE);
	                    break;
	                }
	                break;
	            
	            case Shimmer.MESSAGE_READ:
	                break;
	
	            case Shimmer.MESSAGE_ACK_RECEIVED:
	            	break;
	
	            case Shimmer.MESSAGE_DEVICE_NAME:
	                break;
	
	            case Shimmer.MESSAGE_TOAST:
	                break;
	            }
	        }
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	public void updateView() {
        setProgressBarIndeterminateVisibility(false);
		Shimmer shimmer = null;
		if (null != (shimmer = mService.getShimmer(mBluetoothAddress))) {
			setTitle(R.string.asc_title_connected);
	  		Log.d("ShimmerService", "service connected");
	  		mService = MyApplication.getInstance().getShimmerService();
	  		shimmerVersion = mService.getShimmerVersion(mBluetoothAddress);
	  		
	    	double mSamplingRateV = mService.getSamplingRate(mBluetoothAddress);
	    	int mAccelerometerRangeV = mService.getAccelRange(mBluetoothAddress);
	    	int mGSRRangeV = mService.getGSRRange(mBluetoothAddress);
	    	final double batteryLimit = mService.getBattLimitWarning(mBluetoothAddress);

			buttonGsr.setText("GSR Range" + "\n" + Configuration.Shimmer3.ListofGSRRange[mGSRRangeV]);
			buttonSampleRate.setText("Sampling Rate " + "\n(" + Double.toString(mSamplingRateV) + ") Hz");
			buttonBattVoltLimit.setText("Set Batt Limit " + "\n" + "(" + Double.toString(batteryLimit) + " V)");

	        if (mAccelerometerRangeV == 0) {
				if (shimmerVersion != ShimmerVerDetails.HW_ID.SHIMMER_3) {
					buttonAccRange.setText("Accel Range" + "\n" + "(+/- 1.5g)");
				}
				else {
					buttonAccRange.setText("Accel Range" + "\n" + "(+/- 2g)");
				}
			}
			else if (mAccelerometerRangeV == 1) {
				buttonAccRange.setText("Accel Range" + "\n" + "(+/- 4g)");
			}
			else if (mAccelerometerRangeV == 2) {
				buttonAccRange.setText("Accel Range" + "\n" + "(+/- 8g)");
			} 
			else if (mAccelerometerRangeV == 3) {
				if (shimmerVersion != ShimmerVerDetails.HW_ID.SHIMMER_3) {
					buttonAccRange.setText("Accel Range" + "\n" + "(+/- 6g)");
				}
				else {
					buttonAccRange.setText("Accel Range" + "\n" + "(+/- 16g)");
				}
			}
	        
	  		if (shimmer.getInternalExpPower()==1){
	  			cBoxInternalExpPower.setChecked(true);
	  		} else {
	  			cBoxInternalExpPower.setChecked(false);
	  		}
	  		
	  		if (shimmer.isLowPowerMagEnabled()){
	    		cBoxLowPowerMag.setChecked(true);
	    	}
	    	
	    	if (shimmer.isLowPowerAccelEnabled()){
	    		cBoxLowPowerAccel.setChecked(true);
	    	}
	    	
	    	if (shimmer.isLowPowerGyroEnabled()){
	    		cBoxLowPowerGyro.setChecked(true);
	    	}

	    	if (mService.getShimmerVersion(mBluetoothAddress)==ShimmerVerDetails.HW_ID.SHIMMER_3){
	        	buttonGsr.setVisibility(View.VISIBLE);
	        	cBox5VReg.setEnabled(false);
	        	String currentGyroRange = "("+Configuration.Shimmer3.ListofGyroRange[shimmer.getGyroRange()]+")";
	        	buttonGyroRange.setText("Gyro Range"+"\n"+currentGyroRange);
	        	String currentMagRange = "("+Configuration.Shimmer3.ListofMagRange[shimmer.getMagRange()-1]+")";
	    		buttonMagRange.setText("Mag Range"+"\n"+currentMagRange);
	    		String currentPressureResolution = "("+Configuration.Shimmer3.ListofPressureResolution[shimmer.getPressureResolution()]+")";
	    		buttonPressureResolution.setText("Pressure Res"+"\n"+currentPressureResolution);
	        	
	        	if (shimmer.getAccelRange()==0){
	        		cBoxLowPowerAccel.setEnabled(false);
	        	}
	        	
	        	//currently not supported for the moment 
	    		buttonPressureResolution.setEnabled(true);
	    	}
	    	else {
	    		cBoxInternalExpPower.setEnabled(false);
	    		buttonPressureResolution.setEnabled(false);
	    		buttonGyroRange.setEnabled(false);
	    		cBoxLowPowerAccel.setEnabled(false);
	    		cBoxLowPowerGyro.setEnabled(false);
	    		String currentMagRange = "("+Configuration.Shimmer2.ListofMagRange[shimmer.getMagRange()]+")";
	    		buttonMagRange.setText("Mag Range"+"\n"+currentMagRange);
	    	}
	    	
	  		//update the view
	  		if (mService.get5VReg(mBluetoothAddress)==1){
	  			cBox5VReg.setChecked(true);
	  		}
	  		
	  		cBox5VReg.setOnCheckedChangeListener(new CBox5VReg_OnCheckedChangeListener());
	  		
	  		cBoxLowPowerAccel.setOnCheckedChangeListener(new CBoxLowPowerAccel_OnCheckedChangeListener(shimmer));
	  		
	  		cBoxLowPowerGyro.setOnCheckedChangeListener(new CBoxLowPowerGyro_OnCheckedChangeListener(shimmer));
	  		
	  		cBoxInternalExpPower.setOnCheckedChangeListener(new CBoxInternalExpPower_OnCheckedChangeListener(shimmer));

	  		cBoxLowPowerMag.setChecked(mService.isLowPowerMagEnabled(mBluetoothAddress));
	  		
	  		cBoxLowPowerMag.setOnCheckedChangeListener(new CBoxLowPowerMag_OnCheckedChangeListener());

	        ll_asc0.setVisibility(View.VISIBLE);
	        ll_asc1.setVisibility(View.VISIBLE);
	        ll_asc2.setVisibility(View.VISIBLE);
	        ll_asc3.setVisibility(View.VISIBLE);
		}
		else {
			setTitle(R.string.assl_title_not_connected);
		}
	}
}
