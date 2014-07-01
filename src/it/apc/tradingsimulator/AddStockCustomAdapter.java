package it.apc.tradingsimulator;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AddStockCustomAdapter extends ArrayAdapter<Stock> {

	public AddStockCustomAdapter(Context context, int resource, ArrayList<Stock> items) {
	    super(context, resource, items);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	
	    View v = convertView;
	    if (v == null) {
	        LayoutInflater vi;
	        vi = LayoutInflater.from(getContext());
	        v = vi.inflate(R.layout.add_stock_row, null);
	    }
	
	    Stock p = getItem(position);
	
	    if (p != null) {
	        TextView tt0 = (TextView) v.findViewById(R.id.id);
	        TextView tt1 = (TextView) v.findViewById(R.id.name);
	        TextView tt2 = (TextView) v.findViewById(R.id.price);
	        
	        tt0.setText(p.getId().substring(0, p.getQuantity()));
	        tt1.setText(p.getId().substring(p.getQuantity()));
	        tt2.setText(String.valueOf(p.getLastPrice()));
	    }
	
	    
	    return v;
	}
	
}