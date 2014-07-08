package it.apc.tradingsimulator;

import java.util.ArrayList;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.echo.holographlibrary.Line;
import com.echo.holographlibrary.LineGraph;
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
	private float balance;

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
		
		return rootView;
	}
	
	public void listChanged(ArrayList<StockRow> rowlist, float balance) {
		list = rowlist;
		this.balance = balance;
		pb.setVisibility(View.GONE);
		box.setVisibility(View.VISIBLE);
		box.setEnabled(true);
		updateChart();
		//TODO update UI
	}
	
	public void loadingStarted() {
		if (!firstrun) {
			pb.setVisibility(View.VISIBLE);
			box.setVisibility(View.INVISIBLE);
			box.setEnabled(false);
		}
		firstrun = false;
	}

	private void updateChart() {
		if (pg != null) {
			pg.removeSlices();
			for (StockRow s : list) {
				if (s.getQuantity() != 0) {
					PieSlice slice = new PieSlice();
					slice.setColor(Color.parseColor("#99CC00"));
					slice.setValue(s.getQuantity());
					slice.setTitle(s.getName());
					pg.addSlice(slice);
				}
			}
		}
	}
	
	class WizardPagerAdapter extends PagerAdapter {

		@Override
	    public Object instantiateItem(ViewGroup collection, int position) {
			View v = null;
			switch (position) {
			case 0:
				lg = new LineGraph(getActivity());
				lg.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 200));
				
				Line l = new Line();
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
				l.setColor(Color.parseColor("#FFBB33"));

				lg.addLine(l);
				lg.setRangeY(0, 10);
				lg.setLineToFill(0);
				
				v = lg;
				break;
			case 1:
				pg = new PieGraph(getActivity());
				pg.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 200));
				pg.setOnSliceClickedListener(new OnSliceClickedListener() {
					@Override
					public void onClick(int index) {
						Toast.makeText(getActivity(), pg.getSlice(index).getTitle(), Toast.LENGTH_SHORT).show();
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
