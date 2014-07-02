package it.apc.tradingsimulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class StockActivity extends Activity {

	private Stock stock;
	private String id;
	private int q = 0;
	private TextView nt;
	private ProgressBar pb;
	private ScrollView sv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_action_go));
		stock = (Stock) getIntent().getSerializableExtra("STOCK");
		id = stock.getId();
		q  = stock.getQuantity();
		nt = (TextView) findViewById(R.id.name);
		pb = (ProgressBar) findViewById(R.id.progressBar);
		pb.setVisibility(View.GONE);
		sv = (ScrollView) findViewById(R.id.scrollView1);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        finish();
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy() {
		Intent data = new Intent();
		data.putExtra("STOCK", stock);
		setResult(Activity.RESULT_OK, data);
		finish();
		super.onDestroy();
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, 0);
	}
	
	private class LoadDetailsTask extends AsyncTask<Void, Void, HashMap<String, Object>> {

	    @Override
	    protected void onPostExecute(HashMap<String, Object> map) {
	    	sv.setEnabled(true);
	        sv.setVisibility(View.VISIBLE);
	        pb.setVisibility(View.GONE);
	    }

	    @Override
	    protected void onPreExecute() {
	    	sv.setEnabled(false);
	        sv.setVisibility(View.INVISIBLE);
	        pb.setVisibility(View.VISIBLE);
	    }

		@Override
	    protected HashMap<String, Object> doInBackground(Void... params) {
	    	
			HashMap<String, Object> map = new HashMap<String, Object>();
			
	    	HttpClient httpClient = new DefaultHttpClient();
	        HttpContext localContext = new BasicHttpContext();

			HttpGet httpGet = new HttpGet("http://download.finance.yahoo.com/d/quotes.csv?s="
							+ Uri.encode(id) + "&f=n0l1c1p2v0h0g0j1o0p0x0k0j0f6&e=.csv");
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
			CSVReader csvr = new CSVReader(reader);
			try {
				String[] b = csvr.readNext();
				map.put("name", b[0]);
				map.put("price", Double.parseDouble(b[1]));
				map.put("change", b[2]);
				map.put("pchange", b[3]);
				map.put("volume", b[4]);
				map.put("maxd", b[5]);
				map.put("mind", b[6]);
				map.put("capitalization", b[7]);
				map.put("open", b[8]);
				map.put("close", b[9]);
				map.put("stockexchange", b[10]);
				map.put("maxy", b[11]);
				map.put("miny", b[12]);
				int mFloat = Integer.parseInt(b[13]) * 1000000 + Integer.parseInt(b[14]) * 1000 + Integer.parseInt(b[15]);
				map.put("float", mFloat);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
/*			try {
				String line = reader.readLine();
				String[] RowData = line.split(",");
				String name = RowData[0];
				item.setName(name.substring(1, name.length() - 1));
				item.setPrice(Double.parseDouble(RowData[1]));
				params[0].get(stock).setLastPrice(
						Double.parseDouble(RowData[1]));
				list.put(stock, params[0].get(stock));
				item.setChange(Double.parseDouble(RowData[2]));
				String pchange = RowData[3];
				item.setPchange(Double.parseDouble(pchange.substring(1, pchange.length() - 2)));
			} catch (IOException ex) {
				// handle exception
			} finally {
*/			try {
				is.close();
			} catch (IOException e) {
				// handle exception
			}
//			}
			return map;
	    }

	    @Override
	    protected void onProgressUpdate(Void... items) {
	    	
	    }
	}

}
