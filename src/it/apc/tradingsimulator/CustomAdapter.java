package it.apc.tradingsimulator;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAdapter extends ArrayAdapter<StockRow> {

	public CustomAdapter(Context context, int resource, ArrayList<StockRow> items) {
	    super(context, resource, items);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	
	    View v = convertView;
	    if (v == null) {
	        LayoutInflater vi;
	        vi = LayoutInflater.from(getContext());
	        v = vi.inflate(R.layout.stocksrow, null);
	    }
	
	    StockRow p = getItem(position);
	
	    if (p != null) {
	        TextView tt0 = (TextView) v.findViewById(R.id.id);
	        TextView tt1 = (TextView) v.findViewById(R.id.name);
	        TextView tt2 = (TextView) v.findViewById(R.id.price);
	        TextView tt3 = (TextView) v.findViewById(R.id.change);
	        TextView tt4 = (TextView) v.findViewById(R.id.pchange);
	        ImageView im = (ImageView) v.findViewById(R.id.image);
	        
	        tt0.setText(p.getId());
	        tt1.setText(p.getName());
	        tt2.setText(String.valueOf(p.getPrice()));
	        tt3.setText(String.valueOf(p.getChange()));
	        tt4.setText(p.getPchange() + "%");
	        if (p.getChange() < 0) {
				tt3.setTextColor(Color.RED);
				tt4.setTextColor(Color.RED);
			}
	        else {
	        	tt3.setText("+" + tt3.getText());
	        	tt3.setTextColor(Color.parseColor("#77aa11"));
	        	tt4.setText("+" + tt4.getText());
				tt4.setTextColor(Color.parseColor("#77aa11"));
			}
	        if (p.getQuantity() <= 0) {
	        	im.setVisibility(View.INVISIBLE);
	        }
	        else {
	        	im.setVisibility(View.VISIBLE);
			}
	    }
	
	    
	    return v;
	}
	
}