package programmingclub.daiict.classes.tech_news_classes;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import programmingclub.daiict.R;

/**
 * Created by omkar13 on 12/17/2015.
 */
public class TechnologyAppRssFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ProgressBar progressBar;
    private ListView listView;
    private View view;

    FeedReaderDbHelper mDbHelper;
    SQLiteDatabase db;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mDbHelper = new FeedReaderDbHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.rss_news_list_layout, container, false);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            listView = (ListView) view.findViewById(R.id.listView);
            listView.setOnItemClickListener(this);
            startService();
        } else {
            // If we are returning from a configuration change:
            // "view" is still attached to the previous view hierarchy
            // so we need to remove it and re-attach it to the current one
            ViewGroup parent = (ViewGroup) view.getParent();
            parent.removeView(view);
        }
        return view;
    }

    private void startService() {
        Intent intent = new Intent(getActivity(), TechnologyAppRssService.class);
        intent.putExtra(TechnologyAppRssService.RECEIVER, resultReceiver);
        getActivity().startService(intent);
    }

    /**
     * Once the {@link TechnologyAppRssService} finishes its task, the result is sent to this ResultReceiver.
     */
    private final ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
        @SuppressWarnings("unchecked")
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData)
        {
            List<RssItem> items = (List<RssItem>) resultData.getSerializable(TechnologyAppRssService.ITEMS);

            if (items != null)
            {
                RssAdapter adapter = new RssAdapter(getActivity(), items);
                listView.setAdapter(adapter);

                //if the rssitems list is available then use it to create the database
                db = mDbHelper.getWritableDatabase();
                ContentValues values = new ContentValues(); //contentValues to hold the rssitem info when it is being put in database

                //now fill in the table till list has items

                ListIterator<RssItem> li=items.listIterator(); //this is the li which we will use

                //when info is available, then fill in the database
                //before that delete previous data
                db.delete(DatabaseContract.tableDefinition.TABLE_NAME2, null, null);

                while (li.hasNext())
                {
                    RssItem temp=li.next();
                    values.put(DatabaseContract.tableDefinition.COLUMN_NAME_TITLE2,temp.getTitle());
                    values.put(DatabaseContract.tableDefinition.COLUMN_NAME_LINK2, temp.getLink());

                    db.insert(DatabaseContract.tableDefinition.TABLE_NAME2, null, values); //row added
                    values.clear();
                }

            }
            else
            {
                items=new ArrayList<RssItem>();

                //rss list is empty then retrieve information from the database.
                //we will try to get cursor if cursor is empty then db is empty

               db=mDbHelper.getWritableDatabase();
                String query="SELECT * FROM "+DatabaseContract.tableDefinition.TABLE_NAME2+" WHERE 1;"; //query syntax to retrive the entire database.

               Cursor c =db.rawQuery(query,null);

               if(c.moveToFirst()==false)
                {Toast.makeText(getActivity(), "An error occured while downloading the rss feed.",
                        Toast.LENGTH_LONG).show();}
                else
                {
                    String title=null;
                    String link=null;
                    while(!c.isAfterLast())
                    {

                        if (c.getString(c.getColumnIndex("title2"))!=null)
                        {
                            title=c.getString(c.getColumnIndex("title2"));
                        }

                        if (c.getString(c.getColumnIndex("link2"))!=null)
                        {
                            link=c.getString(c.getColumnIndex("link2"));
                        }

                        items.add(new RssItem(title,link));
                        c.moveToNext();
                    }

                    RssAdapter adapter = new RssAdapter(getActivity(), items);
                    listView.setAdapter(adapter);
                }
            }
            progressBar.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        };
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RssAdapter adapter = (RssAdapter) parent.getAdapter();
        RssItem item = (RssItem) adapter.getItem(position);
        String url = item.getLink();
        Intent intent=new Intent(getActivity(),WebViewActivity.class );
        intent.putExtra("url",url);
        startActivity(intent);

    }

}
