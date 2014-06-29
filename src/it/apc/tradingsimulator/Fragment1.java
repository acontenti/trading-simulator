package it.apc.tradingsimulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

public class Fragment1 extends Fragment {

	private ListView lv;
	ArrayList<StockRow> rowlist = new ArrayList<StockRow>();
	ArrayList<Stock> list = new ArrayList<Stock>();
	private CustomAdapter adapter;
	private ProgressBar pb;

	public Fragment1() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment1, container, false);
//      load tasks from preference
        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        try {
            list = (ArrayList<Stock>) ObjectSerializer.deserialize(prefs.getString(MainActivity.STOCK_LIST_TAG, ObjectSerializer.serialize(new ArrayList<Stock>())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
		lv = (ListView) rootView.findViewById(R.id.listView);
		adapter = new CustomAdapter(getActivity(), R.layout.stocksrow, rowlist);
		pb = (ProgressBar) rootView.findViewById(R.id.progressBar);
		pb.setVisibility(View.GONE);
		
		new LoadListTask().execute(list);
		
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				
			}
		});
		CircleButton addbt = (CircleButton) rootView.findViewById(R.id.add);
		addbt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				list.add(new Stock("GOOG", 0, 0));
				list.add(new Stock("AAPL", 0, 10));
				list.add(new Stock("MSFT", 0, 0));
				new LoadListTask().execute(list);
			}
		});
		return rootView;
	}
	
	public void Save(){
		//save the task list to preference
	    SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
	    Editor editor = prefs.edit();
	    try {
	        editor.putString(MainActivity.STOCK_LIST_TAG, ObjectSerializer.serialize(list));
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    editor.commit();
	}
	
	private class LoadListTask extends AsyncTask<ArrayList<Stock>, StockRow, Void> {

	    @Override
	    protected void onPostExecute(Void unused) {
	    	lv.setEnabled(true);
	        lv.setAlpha(1f);
	        pb.setVisibility(View.GONE);
	    }

	    @Override
	    protected void onPreExecute() {
	        lv.setEnabled(false);
	        lv.setAlpha(0.25f);
	        pb.setVisibility(View.VISIBLE);
	    }

	    @Override
	    protected Void doInBackground(ArrayList<Stock>... params) {
	    	
	    	HttpClient httpClient = new DefaultHttpClient();
	        HttpContext localContext = new BasicHttpContext();
	        
	        for (int j = 0; j < params[0].size(); j++) {
				
	        	Stock stock = params[0].get(j);
	        	
	        	StockRow item = new StockRow(stock.getId(), "", stock.getLastPrice(), 0, 0, stock.getQuantity());
		        
	        	HttpGet httpGet = new HttpGet("http://download.finance.yahoo.com/d/quotes.csv?s=" + stock.getId() + "&f=n0l1c1p2&e=.csv");
		        HttpResponse response = null;
				try {
					response = httpClient.execute(httpGet, localContext);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				InputStream is = null;
				try {
					is = response.getEntity().getContent();
				} catch (IllegalStateException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				try {
			        String line = reader.readLine();
			        String[] RowData = line.split(",");
			        String name = RowData[0];
			        item.setName(name.substring(1, name.length() - 1));
			        item.setPrice(Double.parseDouble(RowData[1]));
			        stock.setLastPrice(Double.parseDouble(RowData[1]));
			        list.set(j, stock);
			        item.setChange(Double.parseDouble(RowData[2]));
			        String pchange = RowData[3];
			        item.setPchange(Double.parseDouble(pchange.substring(1, pchange.length() - 2)));
			    }
			    catch (IOException ex) {
			        // handle exception
			    }
			    finally {
			        try {
			            is.close();
			        }
			        catch (IOException e) {
			            // handle exception
			        }
			    }
        	
	            publishProgress(item);
	        }

	        return (null);
	    }

	    @Override
	    protected void onProgressUpdate(StockRow... items) {
	        rowlist.add(items[0]);
	        adapter.notifyDataSetChanged();
	    }
	}
}
