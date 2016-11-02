package com.example.root.hackernewsapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
ArrayList<String> titleArticle;
static ArrayList<String> urlArticle;
ArrayAdapter<String> arrayAdapter;
    public class GetJSON extends AsyncTask<String,Void,String>
    {

        @Override
        protected String doInBackground(String... urls) {
            String result ="";
            URL url = null;
            HttpURLConnection urlConnection=null;
            try {
                url = new URL(urls[0]);

                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = urlConnection.getInputStream();

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int data = inputStreamReader.read();



                while(data != -1)
                {

                    char c = (char)data;

                    result+= c ;

                    data = inputStreamReader.read();

                }


                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "Failed";



        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String result="";
        ListView listView = (ListView) findViewById(R.id.topRatedNews);

        titleArticle = new ArrayList<String>();
        urlArticle = new ArrayList<String>();
        titleArticle.add("test");
        urlArticle.add("test");
        GetJSON getJSON = new GetJSON();
        arrayAdapter= new ArrayAdapter<String>(this,R.layout.textviewlayout,titleArticle);
        listView.setAdapter(arrayAdapter);

        try {
            result = getJSON.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty").get();
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 1 ; i<20;i++)
            {
                //Log.i("Article ",jsonArray.getString(i));

                GetJSON getArticle = new GetJSON();
                String articleInfo = getArticle.execute("https://hacker-news.firebaseio.com/v0/item/"+jsonArray.getString(i)+".json?print=pretty").get();
                JSONObject jsonObject = new JSONObject(articleInfo);

                if(jsonObject.has("url"))
                {
                    titleArticle.add(i,jsonObject.getString("title"));
                    urlArticle.add(i,jsonObject.getString("url"));
                    Log.i("title",titleArticle.get(i));
                    arrayAdapter.notifyDataSetChanged();

                }
                else{
                    titleArticle.add(i,"NO");
                    urlArticle.add(i,"NO");
                    arrayAdapter.notifyDataSetChanged();

                }



            }




        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(MainActivity.this,webViewActivity.class);
        i.putExtra("pos",position);
        startActivity(i);
    }
});

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
