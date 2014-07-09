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

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class Fragment1 extends Fragment {

	protected static final String PACKAGE = "TRADINGSIMULATOR";
	private ListView lv;
	ArrayList<StockRow> rowlist = new ArrayList<StockRow>();
	HashMap<String, Stock> list = new HashMap<String, Stock>();
	private CustomAdapter adapter;
	private ProgressBar pb;
	private OnListChangedListener mCallback;
	private float balance;

	public Fragment1() {}

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment1, container, false);
//      load tasks from preference
        SharedPreferences prefs = getActivity().getSharedPreferences(MainActivity.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        
        balance = prefs.getFloat("balance", 0);
        
        try {
            list = (HashMap<String, Stock>) ObjectSerializer.deserialize(prefs.getString(MainActivity.STOCK_LIST_TAG, ObjectSerializer.serialize(new HashMap<String, Stock>())));
        } catch (IOException e) {
            e.printStackTrace();
        }
 		final CircleButton addbt = (CircleButton) rootView.findViewById(R.id.add);
       
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
					animation.setDuration(150);
					animation.addAnimation(new AlphaAnimation(1, 0));
					animation.addAnimation(new TranslateAnimation(0, 0, 0, 100));
					animation.setInterpolator(new AccelerateDecelerateInterpolator());
					animation.setAnimationListener(new AnimationListener() {
						@Override public void onAnimationStart(Animation animation) {}
						@Override public void onAnimationRepeat(Animation animation) {}
						@Override
						public void onAnimationEnd(Animation animation) {
							addbt.setVisibility(View.GONE);
							animfinished = true;
						}
					});
					animfinished = false;
					addbt.startAnimation(animation);
				}
				if(prevlvy > firstVisibleItem && animfinished && addbt.getVisibility() == View.GONE){
					AnimationSet animation = new AnimationSet(true);
					animation.setDuration(150);
					animation.addAnimation(new AlphaAnimation(0, 1));
					animation.addAnimation(new TranslateAnimation(0, 0, 100, 0));
					animation.setInterpolator(new AccelerateDecelerateInterpolator());
					animation.setAnimationListener(new AnimationListener() {
						@Override public void onAnimationStart(Animation animation) {}
						@Override public void onAnimationRepeat(Animation animation) {}
						@Override
						public void onAnimationEnd(Animation animation) {
							addbt.setVisibility(View.VISIBLE);
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
		pb = (ProgressBar) rootView.findViewById(R.id.progressBar);
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
				int[] screenLocation = new int[2];
	            v.getLocationOnScreen(screenLocation);
	            Intent subActivity = new Intent(getActivity(), AddStockActivity.class);
	            Bundle scaleBundle = ActivityOptions.makeScaleUpAnimation(v, screenLocation[0], screenLocation[1], v.getWidth(), v.getHeight()).toBundle();
                getActivity().startActivityForResult(subActivity, 0xADD, scaleBundle);
			}
		});
		return rootView;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0xADD && resultCode == Activity.RESULT_OK) {
			add((Stock) data.getSerializableExtra("STOCK"), false, false);
		}
		if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
			balance = data.getFloatExtra("balance", balance);
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
	    editor.putFloat("balance", balance);
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
        public void OnListChange(ArrayList<StockRow> list, float balance);
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
}
