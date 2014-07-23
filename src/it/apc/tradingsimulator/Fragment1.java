package it.apc.tradingsimulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

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

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class Fragment1 extends Fragment {

	protected static final String PACKAGE = "TRADINGSIMULATOR";
	private ListView lv;
	ArrayList<StockRow> rowlist = new ArrayList<StockRow>();
	HashMap<String, Stock> list = new HashMap<String, Stock>();
	private CustomAdapter adapter;
	private ProgressPieView pb;
	private OnListChangedListener mCallback;
	private double balance;
	private ArrayList<Stock> addlist = new ArrayList<Stock>();
	private AddStockCustomAdapter addadapter;
	private ProgressPieView addpb;
	RelativeLayout sbox;
	private EditText et;
	private CircleButton addbt;
	protected boolean addstock = true;

	public Fragment1() {}

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment1_1, container, false);
//      load tasks from preference
        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        
        balance = prefs.getFloat("balance", 0);
        
        try {
            list = (HashMap<String, Stock>) ObjectSerializer.deserialize(prefs.getString(MainActivity.STOCK_LIST_TAG, ObjectSerializer.serialize(new HashMap<String, Stock>())));
        } catch (IOException e) {
            e.printStackTrace();
        }
 		addbt = (CircleButton) rootView.findViewById(R.id.add);
       
		lv = (ListView) rootView.findViewById(R.id.listView);
		lv.setOnScrollListener(new OnScrollListener() {
			private int prevlvy = 0;
			private boolean animfinished = true;

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}
			
			@Override
			public void onScroll(AbsListView v, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if(prevlvy < firstVisibleItem && animfinished && addbt.getVisibility() == View.VISIBLE){
					AnimationSet animation = new AnimationSet(true);
					animation.setDuration(200);
					animation.addAnimation(new AlphaAnimation(1, 0f));
					animation.addAnimation(new TranslateAnimation(0, 0, 0, 200));
					animation.setInterpolator(new AccelerateInterpolator());
					animation.setAnimationListener(new AnimationListener() {
						@Override public void onAnimationStart(Animation animation) {}
						@Override public void onAnimationRepeat(Animation animation) {}
						@Override public void onAnimationEnd(Animation animation) {
							addbt.setVisibility(View.GONE);
							animfinished = true;
						}
					});
					animfinished = false;
					addbt.startAnimation(animation);
				}
				if(prevlvy > firstVisibleItem && animfinished && addbt.getVisibility() == View.GONE){
					AnimationSet animation = new AnimationSet(true);
					animation.setDuration(200);
					animation.addAnimation(new AlphaAnimation(0f, 1));
					animation.addAnimation(new TranslateAnimation(0, 0, 200, 0));
					animation.setInterpolator(new AccelerateInterpolator());
					animation.setAnimationListener(new AnimationListener() {
						@Override public void onAnimationStart(Animation animation) {
							addbt.setVisibility(View.VISIBLE);
							}
						@Override public void onAnimationRepeat(Animation animation) {}
						@Override public void onAnimationEnd(Animation animation) {
							animfinished = true;
						}
					});
					animfinished = false;
					addbt.startAnimation(animation);
				}
				prevlvy = firstVisibleItem;
			}
		});

		adapter = new CustomAdapter(getActivity(), R.layout.stocksrow, rowlist);
		
		pb = (ProgressPieView) rootView.findViewById(R.id.progressBar);
		pb.animateProgressFill(1000, ValueAnimator.INFINITE);
		pb.setVisibility(View.GONE);
		
		new LoadListTask().execute(list);
		
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
				Intent ed = new Intent(getActivity(), StockActivity.class);
				ed.putExtra("STOCK", list.get(rowlist.get(arg2).getId()));
				ed.putExtra("balance", balance);
				int[] screenLocation = new int[2];
	            v.getLocationOnScreen(screenLocation);
	            Bundle scaleBundle = ActivityOptions.makeScaleUpAnimation(v, screenLocation[0], screenLocation[1], v.getWidth(), v.getHeight()).toBundle();
				getActivity().startActivityForResult(ed, 1, scaleBundle);
				getActivity().overridePendingTransition(R.anim.fade_in, 0);
			}
		});
		addbt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		        if (addstock) {
		        	addstock = false;
					lv.setEnabled(false);
					lv.setAlpha(0.25f);
					AnimationSet animation = new AnimationSet(true);
					animation.setDuration(400);
					animation.addAnimation(new AlphaAnimation(0, 1));
					animation.addAnimation(new TranslateAnimation(500, 0, 0, 0));
					animation.addAnimation(new ScaleAnimation(0, 1, 0, 1, ScaleAnimation.ABSOLUTE, ((View) v.getParent().getParent()).getWidth() / 3 * 2, ScaleAnimation.ABSOLUTE, ((View) v.getParent().getParent()).getHeight()));
					animation.setInterpolator(new AccelerateDecelerateInterpolator());
					animation.setAnimationListener(new AnimationListener() {
						@Override public void onAnimationStart(Animation animation) {
							sbox.setVisibility(View.VISIBLE);
						}
						@Override public void onAnimationRepeat(Animation animation) {}
						@Override public void onAnimationEnd(Animation animation) {}
					});
					sbox.startAnimation(animation);
					AnimationSet btanim = new AnimationSet(true);
					btanim.setDuration(800);
					btanim.addAnimation(new RotateAnimation(0, 45, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f));
					btanim.setInterpolator(new AccelerateDecelerateInterpolator());
					btanim.setFillAfter(true);
					ValueAnimator va = ValueAnimator.ofObject(new ArgbEvaluator(), Color.parseColor("#FF99CC00"), Color.RED);
					va.setDuration(800);
					va.addUpdateListener(new AnimatorUpdateListener() {
						@Override public void onAnimationUpdate(ValueAnimator animation) {
							addbt.setColor((int) animation.getAnimatedValue());
						}
					});
					addbt.startAnimation(btanim);
					va.start();
				}
		        else {
					onBackPressed();
				}
		    }
		});
		sbox = (RelativeLayout) rootView.findViewById(R.id.searchbox);
		sbox.setVisibility(View.GONE);
		rootView.findViewById(R.id.container).setOnTouchListener(new OnTouchListener() {
			@Override public boolean onTouch(View v, MotionEvent e) {
				if (e.getAction() == MotionEvent.ACTION_DOWN && sbox.getVisibility() == View.VISIBLE) {
		        	onBackPressed();
		        	return true;
				}
				return false;
			}
		});

		ListView addlv = (ListView) rootView.findViewById(R.id.listView1);
		addadapter = new AddStockCustomAdapter(getActivity(), R.layout.add_stock_row, addlist);
		addlv.setAdapter(addadapter);
		et = (EditText) rootView.findViewById(R.id.editText);
		addlv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Stock s = addlist.get(position);
				s.setId(s.getId().substring(0, (int) s.getQuantity()));
				s.setQuantity(0);
				onBackPressed();
				add(s, false, false);
			}
		});
		final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		ImageButton searchbtn = (ImageButton) rootView.findViewById(R.id.searchbtn);
		searchbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { 
				imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
				new LoadAddStockListTask().execute(et.getText().toString());
			}
		});
		addpb = (ProgressPieView) rootView.findViewById(R.id.progressBar2);
		addpb.animateProgressFill(1000, ValueAnimator.INFINITE);
		addpb.setVisibility(View.GONE);

		return rootView;
	}

	public void onBackPressed() {
    	addstock = true;
        AnimationSet animation = new AnimationSet(true);
		animation.setDuration(400);
		animation.addAnimation(new AlphaAnimation(1, 0));
		animation.addAnimation(new TranslateAnimation(0, 200, 0, 0));
		animation.addAnimation(new ScaleAnimation(1, 0, 1, 0, ScaleAnimation.RELATIVE_TO_SELF, 1, ScaleAnimation.RELATIVE_TO_SELF, 1));
		animation.setInterpolator(new AccelerateDecelerateInterpolator());
		animation.setAnimationListener(new AnimationListener() {
			@Override public void onAnimationStart(Animation animation) {}
			@Override public void onAnimationRepeat(Animation animation) {}
			@Override public void onAnimationEnd(Animation animation) {
				sbox.setVisibility(View.GONE);
			}
		});
		sbox.startAnimation(animation);
        AnimationSet btanim = new AnimationSet(true);
		btanim.setDuration(800);
		btanim.addAnimation(new RotateAnimation(45, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f));
		btanim.setInterpolator(new AccelerateDecelerateInterpolator());
		btanim.setFillAfter(true);
		ValueAnimator va = ValueAnimator.ofObject(new ArgbEvaluator(), Color.RED, Color.parseColor("#FF99CC00"));
		va.setDuration(800);
		va.addUpdateListener(new AnimatorUpdateListener() {
			@Override public void onAnimationUpdate(ValueAnimator animation) {
				addbt.setColor((int) animation.getAnimatedValue());
			}
		});
		addbt.startAnimation(btanim);
		va.start();
		addadapter.clear();
		addadapter.notifyDataSetChanged();
		et.setText("");
        lv.setEnabled(true);
        lv.setAlpha(1f);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0xADD && resultCode == Activity.RESULT_OK) {
			add((Stock) data.getSerializableExtra("STOCK"), false, false);
		}
		if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
			balance = data.getDoubleExtra("balance", balance);
			boolean todelete = data.getBooleanExtra("delete", false);
			add((Stock) data.getSerializableExtra("STOCK"), true, todelete);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@SuppressWarnings("unchecked")
	private void add(Stock s, boolean isEdit, boolean todelete) {
		if(!list.containsKey(s.getId())){
			//ADD NEW
			list.put(s.getId(), s);
			new LoadListTask().execute(list);
			new SaveTask().execute((Void) null);
		}
		else if (!isEdit){
			Toast.makeText(getActivity(), "Stock already added!", Toast.LENGTH_SHORT).show();
		}
		else if (isEdit) {
			if (todelete) {
				//DELETE
				list.remove(s.getId());
			}
			else {
				//EDIT
				list.remove(s.getId());
				list.put(s.getId(), s);
			}
			new LoadListTask().execute(list);
			new SaveTask().execute((Void) null);
		}
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
	    editor.putFloat("balance", (float) balance);
	    editor.commit();
	}
	
	private class SaveTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Save();
			return null;
		}
		
	}
	
	public interface OnListChangedListener {
        public void OnListChange(ArrayList<StockRow> list, double balance);
        public void OnListLoad();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnListChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
        }
    }
	
	private class LoadListTask extends AsyncTask<HashMap<String, Stock>, StockRow, Void> {

	    @Override
	    protected void onPostExecute(Void unused) {
	    	lv.setEnabled(true);
	        lv.setAlpha(1f);
	    	addbt.setEnabled(true);
	        addbt.setAlpha(1f);
	        pb.setVisibility(View.GONE);
	        Collections.sort(rowlist, new Comparator<StockRow>() {
	            public int compare(StockRow result1, StockRow result2) {
	                return result1.getId().compareTo(result2.getId());
	            }
	        });
	        mCallback.OnListChange(rowlist, balance);
		    adapter.notifyDataSetChanged();
			new SaveTask().execute((Void) null);
	    }

	    @Override
	    protected void onPreExecute() {
	        lv.setEnabled(false);
	        lv.setAlpha(0.25f);
	    	addbt.setEnabled(false);
	        addbt.setAlpha(0f);
	        pb.setVisibility(View.VISIBLE);
	        mCallback.OnListLoad();
	        adapter.clear();
	    }

	    @SuppressWarnings("unchecked")
		@Override
	    protected Void doInBackground(HashMap<String, Stock>... params) {
	    	
	    	HttpClient httpClient = new DefaultHttpClient();
	        HttpContext localContext = new BasicHttpContext();
	        if (!params[0].isEmpty()) {
				Set<String> ids = params[0].keySet();
				for (String stock : ids) {

					StockRow item = new StockRow(stock, "", params[0].get(stock).getLastPrice(), 0, 0, params[0].get(stock).getQuantity());

					HttpGet httpGet = new HttpGet(
							"http://download.finance.yahoo.com/d/quotes.csv?s="
									+ Uri.encode(stock) + "&f=n0l1c1p2&e=.csv");
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
					CSVReader csv = new CSVReader(reader);
					try {
						String[] data = csv.readNext();
						item.setName(data[0]);
						item.setPrice(Double.parseDouble(data[1]));
						params[0].get(stock).setLastPrice(Double.parseDouble(data[1]));
						list.put(stock, params[0].get(stock));
						item.setChange(Double.parseDouble(data[2]));
						String pchange = data[3];
						item.setPchange(Double.parseDouble(pchange.substring(0, pchange.length() - 1)));
					} catch (IOException ex) {
						ex.printStackTrace();
					} finally {
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					publishProgress(item);
				}
			}
			return (null);
	    }

	    @Override
	    protected void onProgressUpdate(StockRow... items) {
	        rowlist.add(items[0]);
	    }
	}
	
	@SuppressWarnings("unchecked")
	public void update(){
		new LoadListTask().execute(list);
	}
	
	
	private class LoadAddStockListTask extends AsyncTask<String, Stock, Void> {

		@Override
	    protected void onPostExecute(Void result) {
		    addadapter.notifyDataSetChanged();
		    addpb.setVisibility(View.GONE);
	    }

	    @Override
	    protected void onPreExecute() {
	        addadapter.clear();
	        addlist.clear();
	        addpb.setVisibility(View.VISIBLE);
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
	        addlist.add(s[0]);
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
				CSVReader csv = new CSVReader(reader);
				String[] data = csv.readNext();
				String name = data[0];
				item.setId(id + name);
				item.setLastPrice(Double.parseDouble(data[1]));
				item.setQuantity(id.length());
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
