package it.apc.tradingsimulator;

import it.apc.tradingsimulator.Fragment1.OnListChangedListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends FragmentActivity implements OnListChangedListener{

	public static String STOCK_LIST_TAG = "STOCKLIST";
	public static String SHARED_PREFS_FILE = "data";
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	public Fragment1 fragment1 = new Fragment1();
	public Fragment2 fragment2 = new Fragment2();
	private MenuItem balancem;
	private NumberFormat nf;
	protected double balance;
	private MenuItem refreshm;
	private boolean firstrun = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FontsOverride.setDefaultFont(this, "MONOSPACE", "Roboto_Light.ttf");
		
        final SharedPreferences prefs = getSharedPreferences(MainActivity.SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        setContentView(R.layout.activity_main);

        if (prefs.getBoolean("firstrun", true)) {
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			
			final View root = getLayoutInflater().inflate(R.layout.welcome, null);
			b.setView(root);
			
			b.setTitle(R.string.welcome_message);
			b.setCancelable(false);
			b.setNegativeButton(R.string.quit, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			b.setPositiveButton(android.R.string.ok, new OnClickListener() {@Override public void onClick(DialogInterface dialog, int which) {}});
			final AlertDialog dialog = b.create();
			dialog.show();
			//Overriding the handler immediately after show is probably a better approach than OnShowListener as described below
			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {            
				@Override
				public void onClick(View v) {
					EditText et = (EditText) root.findViewById(R.id.editText);
					if (et.getText().length() > 0 && !et.getText().toString().equals(".")) {
						balance = Double.valueOf(et.getText().toString());
						prefs.edit().putFloat("balance", (float) balance).commit();
						prefs.edit().putFloat("init_balance", (float) balance).commit();
						prefs.edit().putBoolean("firstrun", false).commit();
						mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
						mViewPager = (ViewPager) findViewById(R.id.pager);
						mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
						mViewPager.setAdapter(mSectionsPagerAdapter);
						nf = NumberFormat.getInstance();
						nf.setMaximumFractionDigits(2);
						nf.setGroupingUsed(true);
						dialog.dismiss();
					}
				}
			});
		}
        else {
			mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
			mViewPager = (ViewPager) findViewById(R.id.pager);
			mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
			mViewPager.setAdapter(mSectionsPagerAdapter);
			nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(2);
			nf.setGroupingUsed(true);
		}
	}

	private void noConnectionDialog() {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle(R.string.noconnection_title);
		b.setMessage(R.string.noconnection_message);
		b.setCancelable(false);
		b.setNegativeButton(R.string.quit, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				MainActivity.this.finish();
			}
		});
		b.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		balancem = menu.findItem(R.id.balance);
		refreshm = menu.findItem(R.id.action_update);
		refreshm.setVisible(false);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        //finish();
	        return true;
	    case R.id.action_update:
	    	fragment1.update();
	    	return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	public boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		fragment1.onActivityResult(arg0, arg1, arg2);
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private Fragment fragment;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (!isNetworkAvailable()) {
				noConnectionDialog();
				fragment = new Fragment();
			}
			else {
				switch (position) {
				case 0:
					fragment = new Fragment1();
					fragment1 = (Fragment1) fragment;
					break;
				case 1:
					fragment = new Fragment2();
					fragment2 = (Fragment2) fragment;
					break;
				}
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			}
			return null;
		}
	}

	@Override
	public void OnListChange(ArrayList<StockRow> l, double b) {
		refreshm.setVisible(true);
		fragment2.listChanged(l, b);
		balancem.setTitle(nf.format(b) + " $");
	}

	@Override
	public void OnListLoad() {
		if (!firstrun) {
			refreshm.setVisible(false);
		}
		firstrun = false;
		fragment2.loadingStarted();
	}
}
