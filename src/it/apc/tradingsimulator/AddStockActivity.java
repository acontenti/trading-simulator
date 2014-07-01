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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;


public class AddStockActivity extends Activity {

	private ArrayList<Stock> list = new ArrayList<Stock>();
	private AddStockCustomAdapter adapter;
	private ProgressBar pb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addstocks);
		ListView lv = (ListView) findViewById(R.id.listView1);
		adapter = new AddStockCustomAdapter(this, R.layout.add_stock_row, list);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent out = new Intent();
				Stock s = list.get(position);
				s.setId(s.getId().substring(0, s.getQuantity()));
				s.setQuantity(0);
				out.putExtra("STOCK", s);
				setResult(Activity.RESULT_OK, out);
				finish();
			}
		});
		final EditText et = (EditText) findViewById(R.id.editText);
		final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		et.requestFocus();
		ImageButton searchbtn = (ImageButton) findViewById(R.id.searchbtn);
		searchbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { 
				imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
				new LoadListTask().execute(et.getText().toString());
			}
		});
		pb = (ProgressBar) findViewById(R.id.progressBar);
		pb.setVisibility(View.GONE);
	}
	
	
	private class LoadListTask extends AsyncTask<String, Stock, Void> {

		@Override
	    protected void onPostExecute(Void result) {
		    adapter.notifyDataSetChanged();
		    pb.setVisibility(View.GONE);
	    }

	    @Override
	    protected void onPreExecute() {
	        adapter.clear();
	        list.clear();
	        pb.setVisibility(View.VISIBLE);
	    }

		@Override
	    protected Void doInBackground(String... params) {
			String q = params[0];
	        JSONParser jParser = new JSONParser(); // get JSON data from URL JSONArray json = jParser.getJSONFromUrl(url);
	        JSONArray json = jParser.getJSONFromUrl("http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=" + q + "&callback=YAHOO.Finance.SymbolSuggest.ssCallback", q.length());

	        for (int i = 0; i < json.length(); i++) {

				try {
					JSONObject c = json.getJSONObject(i);
					String id = c.getString("symbol");
					publishProgress(getStock(id));
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}
	        return null;
	    }

	    @Override
	    protected void onProgressUpdate(Stock... s) {
	        list.add(s[0]);
	    }
	    
	    protected Stock getStock(String id){
	    	Stock item = new Stock(id, 0, 0);
		    HttpClient httpClient = new DefaultHttpClient();
	        HttpContext localContext = new BasicHttpContext();
			HttpGet httpGet = new HttpGet("http://download.finance.yahoo.com/d/quotes.csv?s=" + Uri.encode(id) + "&f=n0l1&e=.csv");
			HttpResponse response = null;
			try {
				response = httpClient.execute(httpGet, localContext);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
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
				if (RowData.length == 2) {
					String name = RowData[0];
					item.setId(id + name.substring(1, name.length() - 1));
					item.setLastPrice(Double.parseDouble(RowData[1]));
					item.setQuantity(id.length());
				}
				if (RowData.length == 3) {
					String name = RowData[0];
					String name2 = RowData[1];
					item.setId(id + name.substring(1) + "," + name2.substring(0, name2.length() - 1));
					item.setLastPrice(Double.parseDouble(RowData[2]));
					item.setQuantity(id.length());
				}
			} catch (IOException ex) {
			} finally {
				try {
					is.close();
				} catch (IOException e) {}
			}
			return item;
	    }
	}
}
