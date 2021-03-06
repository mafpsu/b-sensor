/**
 *  ORcycle, Copyright 2014, 2015, PSU Transportation, Technology, and People Lab.
 *
 *  @author Robin Murray <robin5@pdx.edu>    (code)
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
 */

package edu.pdx.cecs.orcyclesensors;

import java.util.ArrayList;

import edu.pdx.cecs.orcyclesensors.shimmer.driver.ShimmerVerDetails;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Fragment_MainTrips extends Fragment {

	private static final String MODULE_TAG = "Fragment_MainTrips";
	
	private static final String EXTRA_ACTION_MODE_EDIT = "EXTRA_ACTION_MODE_EDIT";
	private static final String EXTRA_ACTION_MODE_SELECTED_ITEMS = "EXTRA_ACTION_MODE_SELECTED_ITEMS";
	private static final int FAKE_ECG_SENSOR_TYPE = 99;

	private SavedTripsAdapter savedTripsAdapter;
	private ListView lvSavedTrips;
	private MenuItem menuDelete;
	private MenuItem menuUpload;
	private MenuItem menuGenFakeTrip;
	private boolean resumeActionModeEdit;
	private long[] savedActionModeItems;

	private ActionMode editMode;
	private final ActionMode.Callback editModeCallback = new EditModeCallback();

	private Long storedID;

	private Cursor cursorTrips;
	private TripUploader uploader = null;
	
	// *********************************************************************************
	// *                          Fragment Life Cycle
	// *********************************************************************************

	public Fragment_MainTrips() {
	}

	/**
	 * Called to do the initial creation of the fragment
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			Log.v(MODULE_TAG, "Cycle: onCreate()");
	
			if (null != savedInstanceState) {
				resumeActionModeEdit = savedInstanceState.getBoolean(EXTRA_ACTION_MODE_EDIT, false);
				if (null == (savedActionModeItems = savedInstanceState.getLongArray(EXTRA_ACTION_MODE_SELECTED_ITEMS))) {
					savedActionModeItems = new long[0];
				}
			}
			else {
				resumeActionModeEdit = false;
				savedActionModeItems = new long[0];
			}
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Called once the fragment has been created in order for it
	 * to create it's user interface.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.v(MODULE_TAG, "Cycle: onCreateView()");

		View rootView = null;
		Intent intent;
		Bundle extras;
		
		try {
			if (null != (rootView = inflater.inflate(R.layout.activity_saved_trips, null))) {
	
				lvSavedTrips = (ListView) rootView.findViewById(R.id.listViewSavedTrips);
				lvSavedTrips.setOnItemClickListener(new SavedTrips_OnItemClickListener());
				
				setHasOptionsMenu(true);

				if (null != (intent = getActivity().getIntent())) {
					if (null != (extras = intent.getExtras())) {
						if (!extras.getBoolean(Controller.EXTRA_KEEP_ME, false)) {
							cleanTrips();
						}
					}
				}
			}
		}
		catch (Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			Log.v(MODULE_TAG, "Cycle: onResume");
			populateTripList();
			if (resumeActionModeEdit) {
				startActionModeEdit();
			}
		}
		catch(Exception ex) {
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
			if (editMode != null) {
				// record action mode state
				savedInstanceState.putBoolean(EXTRA_ACTION_MODE_EDIT, true);
				if (null != savedTripsAdapter) {
					long[] selectedItems = savedTripsAdapter.getSelectedItemsArray();
					if(selectedItems.length > 0) {
						savedInstanceState.putLongArray(EXTRA_ACTION_MODE_SELECTED_ITEMS, selectedItems);
					}
				}
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		super.onSaveInstanceState(savedInstanceState);
	}
	
	/**
	 * Creates menu items
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		try {
		// Inflate the menu items for use in the action bar
		inflater.inflate(R.menu.edit, menu);
		super.onCreateOptionsMenu(menu, inflater);
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	/**
	 * Handles menu item selections
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			// Handle presses on the action bar items
			switch (item.getItemId()) {
			
			case R.id.action_edit:
				return startActionModeEdit();
				
			default:
				return super.onOptionsItemSelected(item);
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		return false;
	}
	
	// *********************************************************************************
	// *                             Fragment Actions
	// *********************************************************************************

	public void populateTripList() {
		
		// Get data source
		final DbAdapter mDb = new DbAdapter(getActivity());
		mDb.open();

		try {
			cursorTrips = mDb.fetchAllTrips();

			savedTripsAdapter = new SavedTripsAdapter(getActivity(),
					R.layout.saved_trips_list_item, cursorTrips,
					getResources().getColor(R.color.default_color), 
					getResources().getColor(R.color.pressed_color));

			lvSavedTrips.setAdapter(savedTripsAdapter);
			lvSavedTrips.invalidate();
			
		} catch (SQLException ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		} finally {
			mDb.close();
		}
	}

	private void cleanTrips() {

		final DbAdapter mDb = new DbAdapter(getActivity());

		mDb.open();
		try {

			// Clean up any bad trips & coords from crashes
			int cleanedTrips = 0;
			// cleanedTrips = mDb.cleanTripsCoordsTables();
			if (cleanedTrips > 0) {
				Toast.makeText(getActivity(), "" + cleanedTrips + " bad trip(s) removed.",
						Toast.LENGTH_SHORT).show();
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
		finally {
			mDb.close();
		}
	}

	private void retryTripUpload(long tripId) {
		uploader = new TripUploader(getActivity(), MyApplication.getInstance().getUserId());
		Fragment_MainTrips f2 = (Fragment_MainTrips) getActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":1");

		uploader.setFragmentMainTrips(f2);
		uploader.execute(tripId);
	}

	private void deleteTrip(long tripId) {
		DbAdapter mDbHelper = new DbAdapter(getActivity());
		mDbHelper.open();
		try {
			mDbHelper.deleteAllCoordsForTrip(tripId);
			mDbHelper.deletePauses(tripId);
			mDbHelper.deleteTrip(tripId);
		}
		finally {
			mDbHelper.close();
		}
		populateTripList();
	}

	private void clearSelections() {
		
		int numListViewItems = lvSavedTrips.getChildCount();
		
		savedTripsAdapter.clearSelectedItems();

		// Reset all list items to their normal color
		for (int i = 0; i < numListViewItems; i++) {
			lvSavedTrips.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.default_color));
		}
	}
	
	// *********************************************************************************
	// *                           Item Click Listener
	// *********************************************************************************

	private final class SavedTrips_OnItemClickListener implements
			AdapterView.OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
			try {
				cursorTrips.moveToPosition(pos);
				if (editMode == null) {
					
					int status = cursorTrips.getInt(cursorTrips.getColumnIndex(DbAdapter.K_TRIP_STATUS));
					
					String title = getActivity().getString(R.string.fmt_dialog_title, id);
					
					if (status == TripData.STATUS_SENT) {
						transitionToTripMapActivity(id);
					} else if (status == TripData.STATUS_COMPLETE) {
						dialogTripNotUploaded(title, R.string.fmt_dialog_message_complete, 
								R.string.fmt_alt_button_skip, id);
					} else if (status == TripData.STATUS_INCOMPLETE) {
						dialogTripNotUploaded(title, R.string.fmt_dialog_message_incomplete, 
								R.string.fmt_alt_button_review, id);
					}
				} else {
					
					savedTripsAdapter.toggleSelection(id);
					if (savedTripsAdapter.isSelected(id)) {
						v.setBackgroundColor(getResources().getColor(R.color.pressed_color));
					} else {
						v.setBackgroundColor(getResources().getColor(R.color.default_color));
					}

					menuDelete.setEnabled(savedTripsAdapter.numSelectedItems() > 0);
					editMode.setTitle(savedTripsAdapter.numSelectedItems() + " Selected");
				}
			} catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	// *********************************************************************************
	// *                              Edit Action Mode
	// *********************************************************************************

	/**
	 * Starts the edit action mode.
	 * @return true if new action mode was started, false otherwise.
	 */
	private boolean startActionModeEdit() {
		if (editMode != null) {
			return false;
		}
		// Start the CAB using the ActionMode.Callback defined above
		editMode = getActivity().startActionMode(editModeCallback);
		return true;
	}

	private final class EditModeCallback implements ActionMode.Callback {
		
		/**
		 * Called when the action mode is created; startActionMode() was called
		 */
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			try {
				// Inflate a menu resource providing context menu items
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.saved_trips_context_menu, menu);
				savedTripsAdapter.setSelectedItems(savedActionModeItems);
				return true;
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return false;
		}

		/**
		 * Called each time the action mode is shown. Always
		 * called after onCreateActionMode, but may be called
		 * multiple times if the mode is invalidated.
		 */
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			try {
				int numSelectedItems = savedTripsAdapter.getSelectedItems().size();

				menuDelete = menu.findItem(R.id.action_delete_saved_trips);
				menuDelete.setEnabled(numSelectedItems > 0);
				menuUpload = menu.findItem(R.id.action_upload_saved_trips);
				menuGenFakeTrip = menu.findItem(R.id.action_generate_fake_trip);
				menuGenFakeTrip.setEnabled(numSelectedItems == 0);

				// determine upload status
				int flag = 1;
				for (int i = 0; i < lvSavedTrips.getCount(); i++) {
					cursorTrips.moveToPosition(i);
					flag = flag
							* (cursorTrips.getInt(cursorTrips.getColumnIndex("status")) - 1);
					if (flag == 0) {
						storedID = cursorTrips.getLong(cursorTrips.getColumnIndex("_id"));
						Log.v(MODULE_TAG, "" + storedID);
						break;
					}
				}
				menuUpload.setEnabled(flag != 1);

				mode.setTitle(numSelectedItems + " Selected");
				
				return true;
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return false; // Return false if nothing is done
		}

		/**
		 * Called when the user selects a contextual menu item
		 */
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			try {
				switch (item.getItemId()) {
				
				case R.id.action_delete_saved_trips:
					
					// delete selected trips
					actionDeleteSelectedTrips(savedTripsAdapter.getSelectedItems());
					mode.finish(); // Action picked, so close the CAB
					return true;

				case R.id.action_upload_saved_trips:

					try {
						retryTripUpload(storedID);
					}
					catch(Exception ex) {
						Log.e(MODULE_TAG, ex.getMessage());
					}
					mode.finish(); // Action picked, so close the CAB
					return true;
				
				case R.id.action_generate_fake_trip:
					try {
						TripGenerator.generateFakeTrip(getActivity());
						populateTripList();
					}
					catch(Exception ex) {
						Log.e(MODULE_TAG, ex.getMessage());
					}
					mode.finish(); // Action picked, so close the CAB
					return true;
					
				default:
					return false;
				}
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
			return false;
		}

		/**
		 * Called when the user exits the action mode
		 */
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			try {
				editMode = null;
				clearSelections();
			}
			catch(Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}		
	
	private void actionDeleteSelectedTrips(ArrayList<Long> tripIds) {
		try {
			// delete selected trips
			for (long tripId: tripIds) {
				try {
					deleteTrip(tripId);
				}
				catch(Exception ex) {
					Log.e(MODULE_TAG, ex.getMessage());
				}
			}
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	// *********************************************************************************
	// *                            Dialog Trip not Uploaded
	// *********************************************************************************

	private void dialogTripNotUploaded(String title, int message, int altButton, final long tripId) {
		try {
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(title);
			builder.setMessage(message);
			builder.setNegativeButton(R.string.fmt_upload_button, new DialogTripNotUploaded_ButtonOk(tripId));
			builder.setPositiveButton(altButton, new DialogTripNotUploaded_ButtonCancel(tripId));
			final AlertDialog alert = builder.create();
			alert.show();
		}
		catch(Exception ex) {
			Log.e(MODULE_TAG, ex.getMessage());
		}
	}

	private final class DialogTripNotUploaded_ButtonOk implements
			DialogInterface.OnClickListener {
		
		private final long tripId;

		private DialogTripNotUploaded_ButtonOk(long tripId) {
			this.tripId = tripId;
		}

		public void onClick(DialogInterface dialog, int id) {
			try {
				dialog.cancel();
				retryTripUpload(tripId);
			} catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	private final class DialogTripNotUploaded_ButtonCancel implements
			DialogInterface.OnClickListener {
		
		private final long tripId;

		private DialogTripNotUploaded_ButtonCancel(long tripId) {
			this.tripId = tripId;
		}

		public void onClick(DialogInterface dialog, int id) {
			try {
				dialog.cancel();
				transitionToTripMapActivity(tripId);
				// continue
			} catch (Exception ex) {
				Log.e(MODULE_TAG, ex.getMessage());
			}
		}
	}

	// *********************************************************************************
	// *                                       Transitions
	// *********************************************************************************

	/**
	 * Launches Activity_TripMap
	 */
	private void transitionToTripMapActivity(long tripId) {
		Intent intent = new Intent(getActivity(), Activity_TripMap.class);
		intent.putExtra(Controller.EXTRA_TRIP_ID, tripId);
		
		// Usually the Activity_TripMap will try to upload the trip 
		// unless EXTRA_IS_NEW_TRIP is set to false. In this activity, we have just
		// tried to upload the trip, so there is no need to do it again
		intent.putExtra(Controller.EXTRA_IS_NEW_TRIP, false);
	
		intent.putExtra(Controller.EXTRA_TRIP_SOURCE, Controller.EXTRA_TRIP_SOURCE_MAIN_TRIPS);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		getActivity().finish();
	}
}
