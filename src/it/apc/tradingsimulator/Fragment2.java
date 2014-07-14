package it.apc.tradingsimulator;

import java.text.NumberFormat;
import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.echo.holographlibrary.Line;
import com.echo.holographlibrary.LineGraph;
import com.echo.holographlibrary.LineGraph.OnPointClickedListener;
import com.echo.holographlibrary.LinePoint;
import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieGraph.OnSliceClickedListener;
import com.echo.holographlibrary.PieSlice;

public class Fragment2 extends Fragment {

	private LineGraph lg;
	private PieGraph pg;
	private ProgressBar pb;
	ArrayList<StockRow> list = new ArrayList<StockRow>();
	private boolean firstrun = true;
	private LinearLayout box;
	private double balance = 0;
	private int colorBase = 0;
	private float hue = 0;
	private TextView balt;
	private TextView init;
	private TextView shvt;
	private TextView tvlt;
	private SharedPreferences prefs;
	private double initbalance = 0;
	private NumberFormat nf;
	private double sharesvalue;
	private double totvalue;
	private TextView chpt;
	private double change;
	private double pchange;

	public Fragment2() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment2, container, false);
		box = (LinearLayout) rootView.findViewById(R.id.box);
		box.setVisibility(View.INVISIBLE);
		box.setEnabled(false);
		pb = (ProgressBar) rootView.findViewById(R.id.progressBar);
		VerticalViewPager vvp = (VerticalViewPager) rootView.findViewById(R.id.verticalViewPager1);
		WizardPagerAdapter a = new WizardPagerAdapter();
		vvp.setAdapter(a);
		balt = (TextView) rootView.findViewById(R.id.balance);
		init = (TextView) rootView.findViewById(R.id.initialbalance);
		shvt = (TextView) rootView.findViewById(R.id.sharesvalue);
		tvlt = (TextView) rootView.findViewById(R.id.totalvalue);
		chpt = (TextView) rootView.findViewById(R.id.change);
		prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
		initbalance  = (double) prefs.getFloat("init_balance", 0);
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		nf.setGroupingUsed(true);
		return rootView;
	}
	
	public void listChanged(ArrayList<StockRow> rowlist, double b) {
		list = rowlist;
		balance = b;
		balt.setText(nf.format(balance) + " $");
		init.setText(nf.format(initbalance) + " $");
		sharesvalue = 0;
		for (StockRow stockRow : rowlist) {
			sharesvalue += (double) (stockRow.getQuantity() * stockRow.getPrice());
		}
		shvt.setText(nf.format(sharesvalue) + " $");
		totvalue = sharesvalue + balance;
		tvlt.setText(nf.format(totvalue) + " $");
		change = balance - initbalance;
		pchange = change / initbalance * 100;
		String sign = (change < 0 ? "" : "+");
		chpt.setText(sign + nf.format(change) + " $     (" + sign + nf.format(pchange) + "%)");
		int color = (change < 0 ? Color.RED : Color.parseColor("#77AA11"));
		chpt.setTextColor(color);
		
		updateChart();
		
		pb.setVisibility(View.GONE);
		box.setVisibility(View.VISIBLE);
		box.setEnabled(true);
	}
	
	public void loadingStarted() {
		if (!firstrun) {
			pb.setVisibility(View.VISIBLE);
			box.setVisibility(View.INVISIBLE);
			box.setEnabled(false);
			colorBase = 0;
			hue = 0;
		}
		firstrun = false;
	}

	private void updateChart() {
		if (pg != null) {
			pg.removeSlices();
			for (StockRow s : list) {
				if (s.getQuantity() != 0) {
					PieSlice slice = new PieSlice();
					slice.setValue(Math.abs(s.getQuantity()));
					slice.setColor(Color.HSVToColor(new float[] {hue , 0.7f, 0.9f}));
					hue += 97;
					hue %= 360;
					slice.setTitle(s.getName());
					pg.addSlice(slice);
				}
			}
		}
	}
	
	public int generateRandomColor(int light) {

	    int red = (colorBase  & 3) << 6;
	    int green = (colorBase & 0xc) << 4;
	    int blue = (colorBase & 0x30) << 2;
	    
	    // mix the color
        red = (red + light) / 1;
        green = (green + light) / 1;
        blue = (blue + light) / 1;

	    int color = Color.rgb(red, green, blue);
		colorBase += 29;
	    return color;
	}
	
	class WizardPagerAdapter extends PagerAdapter {

		@Override
	    public Object instantiateItem(ViewGroup collection, int position) {
			View v = null;
			switch (position) {
			case 0:
				lg = new LineGraph(getActivity());
				lg.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 200));
				
				final Line l = new Line();
				LinePoint p = new LinePoint();
				p.setX(0);
				p.setY(5);
				l.addPoint(p);
				p = new LinePoint();
				p.setX(8);
				p.setY(8);
				l.addPoint(p);
				p = new LinePoint();
				p.setX(10);
				p.setY(4);
				l.addPoint(p);
				l.addPoint(p);
				p = new LinePoint();
				p.setX(18);
				p.setY(18);
				l.addPoint(p);
				p = new LinePoint();
				p.setX(20);
				p.setY(24);
				l.addPoint(p);
				l.addPoint(p);
				p = new LinePoint();
				p.setX(25);
				p.setY(2);
				l.addPoint(p);
				p = new LinePoint();
				p.setX(30);
				p.setY(4);
				l.addPoint(p);
				l.setColor(Color.parseColor("#FFBB33"));

				lg.addLine(l);
				lg.setRangeY(0, 30);
				lg.setLineToFill(0);
				lg.setOnPointClickedListener(new OnPointClickedListener() {
					@Override
					public void onClick(int lineIndex, int pointIndex) {
						Toast.makeText(getActivity(), nf.format(l.getPoint(pointIndex).getY()), Toast.LENGTH_SHORT).show();
					}
				});
				
				v = lg;
				break;
			case 1:
				pg = new PieGraph(getActivity());
				pg.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 200));
				
				pg.setOnSliceClickedListener(new OnSliceClickedListener() {
					@Override
					public void onClick(int index) {
						if (index >= 0) {
							Toast.makeText(getActivity(), pg.getSlice(index).getTitle(), Toast.LENGTH_SHORT).show();
						}
					}
				});
				v = pg;
				break;
			}
	        ((VerticalViewPager) collection).addView(v,0);
	        return v;
	    }

	    @Override
	    public void destroyItem(ViewGroup collection, int position, Object view) {
	        ((VerticalViewPager) collection).removeView((View) view);
	    }
	    
	    @Override
	    public int getCount() {
	        return 2;
	    }

	    @Override
	    public boolean isViewFromObject(View arg0, Object arg1) {
	        return arg0 == ((View) arg1);
	    }
	}
}
