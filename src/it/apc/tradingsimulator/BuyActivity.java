package it.apc.tradingsimulator;

import java.text.NumberFormat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class BuyActivity extends Activity {

	private TextView bt;
	private TextView tt;
	private TextView et;
	private Button bb;
	private EditText qe;
	private double price;
	private double balance;
	private NumberFormat nf;
	private double bill = 0;
	private TextView pt;
	protected long q;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_buy);
		bt = (TextView) findViewById(R.id.balance);
		tt = (TextView) findViewById(R.id.bill);
		et = (TextView) findViewById(R.id.errorbox);
		pt = (TextView) findViewById(R.id.price);
		bb = (Button) findViewById(R.id.buy);
		qe = (EditText) findViewById(R.id.editText);
		price = getIntent().getDoubleExtra("price", 0);
		balance = getIntent().getDoubleExtra("balance", 0);
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		nf.setGroupingUsed(true);
		bt.setText(nf.format(balance) + "$");
		tt.setText(nf.format(bill) + "$");
		pt.setText(nf.format(price) + "$");
		et.setVisibility(View.GONE);
		bb.setEnabled(false);
		bb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.putExtra("q", q);
				setResult(Activity.RESULT_OK, i);
				finish();
			}
		});
		qe.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() > 0) {
					q = Long.parseLong(s.toString());
					bb.setEnabled(true);
				}
				else {
					q = 0;
				}
				bill = q * price;
				tt.setText(nf.format(bill) + "$");
				if (bill > balance || q <= 0) {
					et.setVisibility(View.VISIBLE);
					bb.setEnabled(false);
				}
				else {
					et.setVisibility(View.GONE);
					bb.setEnabled(true);
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
	}
}
