package it.apc.tradingsimulator;

import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;

public class MainActivity extends FragmentActivity {

	public static String STOCK_LIST_TAG = "STOCKLIST";
	public static String SHARED_PREFS_FILE = "data";
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	public Fragment fragment1 = new Fragment();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FontsOverride.setDefaultFont(this, "MONOSPACE", "Roboto_Light.ttf");
		setContentView(R.layout.activity_main);
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
		mViewPager.setAdapter(mSectionsPagerAdapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		fragment1.onActivityResult(arg0, arg1, arg2);
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private Fragment fragment = new Fragment();

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				fragment  = new Fragment1();
				fragment1 = fragment;
				break;
			case 1:
				fragment = new Fragment2();
				break;
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
}
