package it.apc.tradingsimulator;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.echo.holographlibrary.LineGraph;
import com.echo.holographlibrary.PieGraph;

public class Fragment2 extends Fragment {

	private LineGraph lg;
	private PieGraph pg;
	private ProgressBar pb;
	ArrayList<StockRow> list = new ArrayList<StockRow>();
	private boolean firstrun = true;

	public Fragment2() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment2, container, false);
		lg = (LineGraph) rootView.findViewById(R.id.lgraph);
		pg = (PieGraph) rootView.findViewById(R.id.pgraph);
		pb = (ProgressBar) rootView.findViewById(R.id.progressBar);
		return rootView;
	}
	
	public void listChanged(ArrayList<StockRow> rowlist) {
		list = rowlist;
		pb.setVisibility(View.GONE);
		//TODO update UI
	}
	
	public void loadingStarted() {
		if (!firstrun) {
			firstrun = false;
			pb.setVisibility(View.VISIBLE);
		}
	}
}
