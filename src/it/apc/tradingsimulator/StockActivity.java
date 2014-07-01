package it.apc.tradingsimulator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

public class StockActivity extends Activity {

	private Stock stock;
	private String id;
	private int q = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_action_go));
		stock = (Stock) getIntent().getSerializableExtra("STOCK");
		id = stock.getId();
		q  = stock.getQuantity();
		setTitle(id);
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
}
