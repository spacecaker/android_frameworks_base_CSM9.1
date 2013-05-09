package com.android.spacecaker.CSMBatteryMod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.util.Log;
import android.view.View;

public class DataController extends BroadcastReceiver {
	private Context mContext;
	private BatteryController bc  ;
	 Handler handler ;

	public DataController(Context context,BatteryController bc) {
		mContext = context;
		this.bc=bc;
		bc.val=getVal();
		bc.anim=getAnim();
		bc.vis=getVis();
		bc.DoWork(bc.Receiver);
		IntentFilter ifilter = new IntentFilter();
		ifilter.addAction("com.ghareeb.OGMod.DATA_CHANGED");
		mContext.registerReceiver(this, ifilter);
		
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals("com.ghareeb.OGMod.DATA_CHANGED")){
			if (intent.hasExtra("data")){
				saveData(intent.getStringExtra("data"),	bc.anim,bc.vis);
				bc.val=intent.getStringExtra("data").toLowerCase().replace(" ", "_");;
			}
			if (intent.hasExtra("Anim")){
				saveData(bc.val, intent.getIntExtra("Anim",-1),bc.vis);
				bc.anim=intent.getIntExtra("Anim",-1);
			}
			if (intent.hasExtra("Visibility")){
				saveData(bc.val,bc.anim, intent.getIntExtra("Visibility",View.GONE));
				bc.vis=intent.getIntExtra("Visibility",View.GONE);
			}
			bc.DoWork(bc.Receiver);
		}
	}
	
	public void saveData(String val,int anim,int vis) {
		try {
			SharedPreferences sp = mContext.getSharedPreferences("OG_Mod",
					Context.MODE_WORLD_READABLE | Context.MODE_MULTI_PROCESS);
			Editor editor = sp.edit();
			editor.putString("BatteryStyle", val);
			editor.putInt("Visibility", vis);
			editor.putInt("Anim",anim);
			editor.apply();
			editor.commit();
			Log.d("OGMod", "saveData(" + val + "," + anim + "," + vis + ")");

		} catch (Exception a) {
			Log.d("OGMod", "saveData(" + val + "," + anim + "," + vis + ") - Err:" + a.toString());
			a.printStackTrace();
		}
	}
	
	public String getVal() {
		String val = "Default";
		try {
			SharedPreferences sp = mContext.getSharedPreferences("OG_Mod",
					Context.MODE_WORLD_READABLE);
			val = sp.getString("BatteryStyle", "Default");
		} catch (Exception a) {
			Log.d("OGMod", "getVal() - Err:" + a.toString());
		
			a.printStackTrace();
		}
		return val.toLowerCase().replace(" ", "_");
	}

	public int getAnim() {
		int val = -1;
		try {
			SharedPreferences sp = mContext.getSharedPreferences("OG_Mod",
					Context.MODE_WORLD_READABLE);
			val = sp.getInt("Anim", -1);
		} catch (Exception a) {
			Log.d("OGMod", "getAnim() - Err:" + a.toString());
			a.printStackTrace();
		}
		return val;
	}
	public int getVis() {
		int val = View.GONE;
		try {
			SharedPreferences sp = mContext.getSharedPreferences("OG_Mod",
					Context.MODE_WORLD_READABLE);
			val = sp.getInt("Visibility", View.GONE);
		} catch (Exception a) {
			Log.d("OGMod", "getVis() - Err:" + a.toString());
			a.printStackTrace();
		}
		return val;
	}
}
