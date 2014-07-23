package it.apc.tradingsimulator;

import java.text.NumberFormat;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SellActivity extends Activity {

	private TextView qt;
	private TextView tt;
	private TextView et;
	private Button bb;
	private EditText qe;
	private double price;
	private NumberFormat nf;
	private double bill = 0;
	private TextView pt;
	protected long q;
	protected long pq;
	private double balance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sell);
		qt = (TextView) findViewById(R.id.balance);
		tt = (TextView) findViewById(R.id.bill);
		et = (TextView) findViewById(R.id.errorbox);
		pt = (TextView) findViewById(R.id.price);
		bb = (Button) findViewById(R.id.buy);
		qe = (EditText) findViewById(R.id.editText);
		price = getIntent().getDoubleExtra("price", 0);
		q = getIntent().getLongExtra("q", 0);
		balance = getIntent().getDoubleExtra("balance", 0);
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		nf.setGroupingUsed(true);
		qt.setText(String.valueOf(q));
		tt.setText(nf.format(bill) + "$");
		pt.setText(nf.format(price) + "$");
		if (q > 0) {
			et.setVisibility(View.GONE);
		}
		bb.setEnabled(false);
		bb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.putExtra("q", -pq);
				setResult(Activity.RESULT_OK, i);
				finish();
			}
		});
		qe.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() > 0 && !(Long.parseLong(s.toString()) == 0)) {
					pq = Long.parseLong(s.toString());
					bb.setEnabled(true);
				}
				else {
					pq = 0;
					bb.setEnabled(false);
				}
				bill = pq * price;
				tt.setText(nf.format(bill) + "$");
				if (pq > q || q <= 0) {
					et.setVisibility(View.VISIBLE);
					if (bill > balance) {
						bb.setEnabled(false);
						et.setTextColor(Color.RED);
						et.setText(getString(R.string.error_balance_shortsale));
					}
					else {
						bb.setEnabled(bb.isEnabled());
						getResources();
						et.setTextColor(Resources.getSystem().getColor(android.R.color.holo_orange_dark));
						et.setText(getString(R.string.warning_shortsell));
					}
				}
				else {
					et.setVisibility(View.GONE);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
	}
}
