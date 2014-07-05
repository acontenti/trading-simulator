package it.apc.tradingsimulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class StockActivity extends Activity {

	private Stock stock;
	private String id;
	private long q = 0;
	private ProgressBar pb;
	private LinearLayout sv;
	HashMap<String, Object> data = new HashMap<String, Object>();
	HashMap<String, String> time = new HashMap<String, String>();
	HashMap<String, String> type = new HashMap<String, String>();
	private Spinner times;
	private Spinner types;
	private TextView nt;
	private TextView set;
	private TextView pt;
	private TextView ct;
	private TextView ost;
	private TextView vt;
	private TextView cpt;
	private TextView vlt;
	private TextView ot;
	private TextView lct;
	private TextView mxdt;
	private TextView mndt;
	private TextView mxyt;
	private TextView mnyt;
	private ImageView chart;
	private CircleButton sell;
	private CircleButton buy;
	private CircleButton delete;
	public boolean firstrun = true;
	private double price = 0;
	private double balance = 0;
	protected boolean todelete = false;
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
		set = (TextView) findViewById(R.id.stockexchange);
		pt = (TextView) findViewById(R.id.price);
		ct = (TextView) findViewById(R.id.change);
		ost = (TextView) findViewById(R.id.owned);
		vt = (TextView) findViewById(R.id.value);
		cpt = (TextView) findViewById(R.id.capitalization);
		vlt = (TextView) findViewById(R.id.volume);
		ot = (TextView) findViewById(R.id.open);
		lct = (TextView) findViewById(R.id.close);
		mxdt = (TextView) findViewById(R.id.maxd);
		mndt = (TextView) findViewById(R.id.mind);
		mxyt = (TextView) findViewById(R.id.maxy);
		mnyt = (TextView) findViewById(R.id.miny);
		chart = (ImageView) findViewById(R.id.chart);
		pb = (ProgressBar) findViewById(R.id.progressBar);
		pb.setVisibility(View.GONE);
		sv = (LinearLayout) findViewById(R.id.box);
		OnItemSelectedListener listener = new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				new LoadDetailsTask().execute((Void) null);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		};
		times = (Spinner) findViewById(R.id.chartTimes);
		times.setOnItemSelectedListener(listener);
		types = (Spinner) findViewById(R.id.chartTypes);
		types.setOnItemSelectedListener(listener);
		String[] chartTimes = getResources().getStringArray(R.array.chartTimes);
		String[] chartTimesValues = getResources().getStringArray(R.array.chartTimesValues);
		for (int i = 0; i < chartTimes.length; i++) {
			time.put(chartTimes[i], chartTimesValues[i]);
		}
		String[] chartTypes = getResources().getStringArray(R.array.chartTypes);
		String[] chartTypesValues = getResources().getStringArray(R.array.chartTypesValues);
		for (int i = 0; i < chartTypes.length; i++) {
			type.put(chartTypes[i], chartTypesValues[i]);
		}
		buy = (CircleButton) findViewById(R.id.circleButton_buy);
		buy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showBuyPrompt(v);
			}
		});
		sell = (CircleButton) findViewById(R.id.circleButton_sell);
		sell.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (q > 0) {
					showSellPrompt(v);
				}
				else {
					AlertDialog.Builder builder = new AlertDialog.Builder(StockActivity.this);
					builder.setMessage(R.string.dialog_message).setTitle(android.R.string.dialog_alert_title);
					builder.setPositiveButton(R.string.dialog_continue, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				               showSellPrompt(v);
				           }
				       });
					builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				               // User cancelled the dialog
				           }
				       });
					builder.show();
				}
			}
		});
		delete = (CircleButton) findViewById(R.id.circleButton_delete);
		delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (q == 0) {
					todelete = true;
				}
				else {
					AlertDialog.Builder builder = new AlertDialog.Builder(StockActivity.this);
					builder.setTitle(R.string.dialog_cantdelete);
					builder.setMessage(String.format(getString(q > 0 ? R.string.sellyourshares : R.string.coveryourpos), id));
					builder.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				               // User cancelled the dialog
				           }
				       });
					builder.show();
				}
			}
		});
		new LoadDetailsTask().execute((Void) null);
	}
	
	protected void showBuyPrompt(View v) {
		Intent bi = new Intent(this, BuyActivity.class);
		bi.putExtra("price", price);
		bi.putExtra("balance", balance);
		int[] screenLocation = new int[2];
        v.getLocationOnScreen(screenLocation);
        Bundle scaleBundle = ActivityOptions.makeScaleUpAnimation(v, screenLocation[0], screenLocation[1], v.getWidth(), v.getHeight()).toBundle();
		startActivityForResult(bi, 0xB, scaleBundle);
	}

	protected void showSellPrompt(View v) {
		Intent bi = new Intent(this, SellActivity.class);
		bi.putExtra("price", price);
		bi.putExtra("q", q);
		int[] screenLocation = new int[2];
        v.getLocationOnScreen(screenLocation);
        Bundle scaleBundle = ActivityOptions.makeScaleUpAnimation(v, screenLocation[0], screenLocation[1], v.getWidth(), v.getHeight()).toBundle();
		startActivityForResult(bi, 0xB, scaleBundle);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0xB && resultCode == Activity.RESULT_OK) {
			q += data.getLongExtra("q", 0);
			new LoadDetailsTask().execute((Void) null);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        finish();
	        return true;
	    case R.id.action_update:
	    	new LoadDetailsTask().execute((Void) null);
	    	return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy() {
		Intent data = new Intent();
		data.putExtra("STOCK", stock);
		data.putExtra("delete", todelete);
		setResult(Activity.RESULT_OK, data);
		finish();
		super.onDestroy();
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}
	
	private class LoadDetailsTask extends AsyncTask<Void, Void, HashMap<String, Object>> {

	    @Override
	    protected void onPostExecute(HashMap<String, Object> map) {
	        updateData(map);
	    	sv.setEnabled(true);
	        sv.setVisibility(View.VISIBLE);
	        sv.setAlpha(1f);
	        pb.setVisibility(View.GONE);
			buy.setEnabled(true);
			buy.setAlpha(1f);
			sell.setEnabled(true);
			sell.setAlpha(1f);
			delete.setEnabled(true);
			delete.setAlpha(1f);
			stock.setLastPrice(price);
	    }

	    @Override
	    protected void onPreExecute() {
	    	if (firstrun) {
				firstrun = false;
				sv.setVisibility(View.INVISIBLE);
			}
	    	else {
				sv.setAlpha(0.5f);
			}
			sv.setEnabled(false);
			pb.setVisibility(View.VISIBLE);
			buy.setEnabled(false);
			sell.setEnabled(false);
			delete.setEnabled(false);
			stock.setQuantity(q);
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
				String url = "http://chart.finance.yahoo.com/z?s=" + Uri.encode(id) + "&t=" + time.get(times.getSelectedItem()) + "&q=" + type.get(types.getSelectedItem()) + "&l=off&z=l";
				map.put("chart", getBitmapFromURL(url));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			try {
				is.close();
			} catch (IOException e) {}
			
			return map;
	    }
		
		public Bitmap getBitmapFromURL(String src) {
		    try {
		        URL url = new URL(src);
		        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		        connection.setDoInput(true);
		        connection.connect();
		        InputStream input = connection.getInputStream();
		        Bitmap myBitmap = BitmapFactory.decodeStream(input);
		        return myBitmap;
		    } catch (IOException e) {
		        e.printStackTrace();
		        return null;
		    }
		}

	    @Override
	    protected void onProgressUpdate(Void... items) {}
	}

	public void updateData(HashMap<String, Object> map) {
		setTitle(id);
		nt.setText((CharSequence) map.get("name"));
		set.setText((CharSequence) map.get("stockexchange"));
		pt.setText(String.valueOf(map.get("price")) + "$");
		price = (double) map.get("price");
		ct.setText((CharSequence) map.get("change") + "$ (" + (CharSequence) map.get("pchange") + ")");
		if (ct.getText().toString().startsWith("-")) {
			ct.setTextColor(Color.RED);
		}
		else {
			ct.setTextColor(Color.parseColor("#77aa11"));
		}
		ost.setText(String.valueOf(q));
		vt.setText(String.valueOf((double) q * (double) map.get("price")) + "$");
		ot.setText((CharSequence) map.get("open") + "$");
		lct.setText((CharSequence) map.get("close") + "$");
		cpt.setText((CharSequence) map.get("capitalization") + "$");
		vlt.setText((CharSequence) map.get("volume"));
		mxdt.setText((CharSequence) map.get("maxd") + "$");
		mndt.setText((CharSequence) map.get("mind") + "$");
		mxyt.setText((CharSequence) map.get("maxy") + "$");
		mnyt.setText((CharSequence) map.get("miny") + "$");
		chart.setImageBitmap((Bitmap) map.get("chart"));
	}

}
