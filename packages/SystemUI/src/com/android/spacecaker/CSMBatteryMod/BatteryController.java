package com.android.spacecaker.CSMBatteryMod;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class BatteryController extends BroadcastReceiver {
	private static final String TAG = "StatusBar.BatteryController";

	private Context mContext;
	private ArrayList<ImageView> mIconViews = new ArrayList<ImageView>();
	private ArrayList<TextView> mLabelViews = new ArrayList<TextView>();
	private ArrayList<BatteryStateChangeCallback> mChangeCallbacks = new ArrayList<BatteryStateChangeCallback>();

	public interface BatteryStateChangeCallback {
		public void onBatteryLevelChanged(int level, boolean pluggedIn);
	}

	public Intent Receiver;
	public int anim = -1;
	public int vis = 8;
	public String val = "Default";
	Context ctx;
	Resources res;

	public BatteryController(Context context) {
		mContext = context;
		new DataController(context, this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		Receiver = context.registerReceiver(this, filter);
		try {
			ctx = mContext.createPackageContext("com.ghareeb.battery",
					Context.CONTEXT_INCLUDE_CODE
							| Context.CONTEXT_IGNORE_SECURITY);
			res = ctx.getResources();
		} catch (NameNotFoundException e) {
			Log.d("OGMod", "com.ghareeb.battery not found");
			e.printStackTrace();
		}

	}

	public int getResID(String name, String Type) {
		return mContext.getResources().getIdentifier(name, Type,
				mContext.getPackageName());
	}

	public int getImgID(String name) {
		return ctx.getResources().getIdentifier(name, "drawable",
				ctx.getPackageName());
	}

	long lastClick = System.currentTimeMillis();
	int clicks;

	public void addIconView(ImageView v) {
		mIconViews.add(v);
		DoWork(Receiver);
		v.setOnClickListener(new ImageView.OnClickListener() {
			public void onClick(View v) {
				if (lastClick + 3000 >= System.currentTimeMillis()) {
					clicks++;
					lastClick = System.currentTimeMillis();
				} else {
					lastClick = System.currentTimeMillis();
					clicks = 1;
				}
				if (clicks == 3) {
					lastClick = 0;
					clicks = 0;
					Intent i = new Intent("com.ghareeb.battery.Settings");
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(i);
				}
				Log.d("OGMod", "Battery Clicked , " + clicks + " , "
						+ lastClick);
			}
		});
	}

	public void addLabelView(TextView v) {
		mLabelViews.add(v);
		DoWork(Receiver);
	}

	public void addStateChangedCallback(BatteryStateChangeCallback cb) {
		mChangeCallbacks.add(cb);
	}

	public void onReceive(Context context, Intent intent) {
		Receiver = intent;
		DoWork(intent);
	}

	public void DoWork(Intent intent) {
		try {

			final String action = intent.getAction();
			if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
				final int level = intent.getIntExtra(
						BatteryManager.EXTRA_LEVEL, 0);
				final boolean plugged = intent.getIntExtra(
						BatteryManager.EXTRA_PLUGGED, 0) != 0;
				final int icon = plugged ? 1 : 0;
				int N = mIconViews.size();
				for (int i = 0; i < N; i++) {
					ImageView v = mIconViews.get(i);
					if (icon == 0) {
						setImage(v, level);
					} else if (icon == 1) {
						setChargeImage(v, level);
					} else {
						v.setImageResource(icon);
						v.setImageLevel(level);
					}
					v.setContentDescription(mContext.getString(
							getResID("accessibility_battery_level", "string"),
							level));
				}
				N = mLabelViews.size();
				for (int i = 0; i < N; i++) {
					TextView v = mLabelViews.get(i);
					v.setVisibility(vis);
					v.setText(mContext.getString(
							getResID(
									"status_bar_settings_battery_meter_format",
									"string"), level));
				}

				for (BatteryStateChangeCallback cb : mChangeCallbacks) {
					cb.onBatteryLevelChanged(level, plugged);
				}
			}

		} catch (Exception e) {
			Log.d("OGMod", "DoWork Error- " + val + "," + anim + "," + vis
					+ " - " + e.toString());

			for (int i = 0; i < e.getStackTrace().length; i++) {
				Log.d("OGMod",
						"DoWork Error1 - " + e.getStackTrace()[i].toString()+","+ e.getStackTrace()[i].getLineNumber());
			}

		}
	}

	private int CheckValue(int value) {
		int id = 0;
		do {
			id = getImgID("stat_sys_battery_" + val + "_" + value);
			if (id == 0) {
				value++;
			}
		} while ((id == 0) && (value <= 100));
		return value;
	}
	private void setImage(ImageView view, int value) {
		value = CheckValue(value);
		Drawable d1 = res.getDrawable(getImgID("stat_sys_battery_" + val + "_"
				+ value));
		view.setImageDrawable(d1);
	}
	private void setChargeImage(ImageView view, int value) {
		if (anim == -1) {
			Drawable d = res.getDrawable(getImgID("stat_sys_battery_" + val
					+ "_charge_anim" + value));
			view.setImageDrawable(d);
		} else if (anim == 1) {
			value = CheckValue(value);
			Drawable d1 = res.getDrawable(getImgID("stat_sys_battery_" + val
					+ "_" + value));
			AnimationDrawable Animation = new AnimationDrawable();
			Animation.addFrame(d1, 2000);
			int duration = 40;
			for (int i = 0; i <= 100; i++) {
				try {
					Drawable d2 = res.getDrawable(getImgID("stat_sys_battery_"
							+ val + "_charge_anim" + i));
					Animation.addFrame(d2, duration);
				} catch (Exception e) {
					duration = 80;
				}
			}
			view.setImageDrawable(Animation);
			Animation = (AnimationDrawable) view.getDrawable();
			Animation.setOneShot(false);
			Animation.start();
		} else if (anim == 2) {
			Drawable d1 = res.getDrawable(getImgID("stat_sys_battery_" + val
					+ "_" + value));
			Drawable d2 = res.getDrawable(getImgID("stat_sys_battery_" + val
					+ "_charge_anim" + value));
			AnimationDrawable Animation = new AnimationDrawable();
			Animation.addFrame(d1, 1000);
			Animation.addFrame(d2, 1000);
			view.setImageDrawable(Animation);
			Animation = (AnimationDrawable) view.getDrawable();
			Animation.setOneShot(false);
			Animation.start();
		}
	}

}
