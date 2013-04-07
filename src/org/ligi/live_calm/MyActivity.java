package org.ligi.live_calm;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.androidquery.AQuery;
import com.dsi.ant.plugins.AntPluginMsgDefines;
import com.dsi.ant.plugins.AntPluginPcc;
import com.dsi.ant.plugins.AntPluginPcc.IDeviceStateChangeReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;


public class MyActivity extends Activity implements LocationListener {

    private AntPlusHeartRatePcc hrPcc = null;
    private AQuery mAQuery;
    private int mActMin = Integer.MAX_VALUE;
    private LocationManager locationManager;
    private Location mLastLocation;
    private Location mMinLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10, 1, this);

        mAQuery = new AQuery(this);

        AntPlusHeartRatePcc.requestAccess(this, this, false,
                new AntPluginPcc.IPluginAccessResultReceiver<AntPlusHeartRatePcc>() {
                    //Handle the result, connecting to events on success or reporting failure to user.
                    @Override
                    public void onResultReceived(AntPlusHeartRatePcc result, int resultCode,
                                                 int initialDeviceStateCode) {
                        switch (resultCode) {
                            case AntPluginMsgDefines.MSG_REQACC_RESULT_whatSUCCESS:
                                hrPcc = result;

                                String s = result.getDeviceName() + ": " + AntPlusHeartRatePcc.statusCodeToPrintableString(initialDeviceStateCode);
                                mAQuery.find(R.id.status).getTextView().setText(s);
                                subscribeToEvents();
                                break;

                            case AntPluginMsgDefines.MSG_REQACC_RESULT_whatCHANNELNOTAVAILABLE:
                                /*Toast.makeText(Activity_HeartRateSampler.this, "Channel Not Available", Toast.LENGTH_SHORT).show();
                                tv_status.setText("Error. Do Menu->Reset.");*/
                                break;
                            case AntPluginMsgDefines.MSG_REQACC_RESULT_whatOTHERFAILURE:
                                /*Toast.makeText(Activity_HeartRateSampler.this, "RequestAccess failed. See logcat for details.", Toast.LENGTH_SHORT).show();
                                tv_status.setText("Error. Do Menu->Reset.");*/
                                break;
                            case AntPluginMsgDefines.MSG_REQACC_RESULT_whatDEPENDENCYNOTINSTALLED:
                                /*tv_status.setText("Error. Do Menu->Reset.");
                                AlertDialog.Builder adlgBldr = new AlertDialog.Builder(Activity_HeartRateSampler.this);
                                adlgBldr.setTitle("Missing Dependency");
                                adlgBldr.setMessage("The required application\n\"" + AntPlusHeartRatePcc.getMissingDependencyName() + "\"\n is not installed. Do you want to launch the Play Store to search for it?");
                                adlgBldr.setCancelable(true);
                                adlgBldr.setPositiveButton("Go to Store", new OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        Intent startStore = null;
                                        startStore = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=" + AntPlusHeartRatePcc.getMissingDependencyPackageName()));
                                        startStore.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                        Activity_HeartRateSampler.this.startActivity(startStore);
                                    }
                                });
                                adlgBldr.setNegativeButton("Cancel", new OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        dialog.dismiss();
                                    }
                                });

                                final AlertDialog waitDialog = adlgBldr.create();
                                waitDialog.show();
                                break;
                            case AntPluginMsgDefines.MSG_REQACC_RESULT_whatUSERCANCELLED:
                                tv_status.setText("Cancelled. Do Menu->Reset.");
                                break;*/
                            default:
                                Toast.makeText(MyActivity.this, "Unrecognized result: " + resultCode, Toast.LENGTH_SHORT).show();
                                //tv_status.setText("Error. Do Menu->Reset.");
                                break;

                        }
                    }

                }, new IDeviceStateChangeReceiver() {
                    @Override
                    public void onDeviceStateChange(final int newDeviceState) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAQuery.find(R.id.status).getTextView().setText(hrPcc.getDeviceName() + ": " + AntPlusHeartRatePcc.statusCodeToPrintableString(newDeviceState));
                                if (newDeviceState == AntPluginMsgDefines.DeviceStateCodes.DEAD) {
                                    hrPcc = null;
                                }
                            }
                        });

                    }
                }
        );
    }

    private void subscribeToEvents() {
        hrPcc.subscribeHeartRateDataEvent(new AntPlusHeartRatePcc.IHeartRateDataReceiver() {
            @Override
            public void onNewHeartRateData(final int currentMessageCount,
                                           final int computedHeartRate, final long heartBeatCounter) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAQuery.find(R.id.rcvCount).getTextView().setText(String.valueOf(currentMessageCount));


                        if (computedHeartRate<mActMin) {
                            mActMin = computedHeartRate;
                            mMinLocation=mLastLocation;
                        }

                        String rateString=""+computedHeartRate;
                        if (mLastLocation!=null) {
                            rateString+= " at " + mLastLocation.toString();
                        }

                        mAQuery.find(R.id.act).getTextView().setText( rateString);
                        rateString=""+mActMin;
                        if (mLastLocation!=null) {
                            rateString+= " at " + mMinLocation.toString();
                        }
                        mAQuery.find(R.id.min).getTextView().setText(rateString);

                    }
                });
            }
        });


    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation=location;
        Log.i("","act_loc"+location.toString());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

}

