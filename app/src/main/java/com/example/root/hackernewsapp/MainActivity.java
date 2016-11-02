package com.example.root.hackernewsapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    Map<Integer,String> articleURLs = new HashMap<Integer,String>();
    Map<Integer,String> articleTitles = new HashMap<Integer,String>();
    ArrayList<Integer> articleIds = new ArrayList<Integer>();
    SQLiteDatabase articlesDB ;
    ArrayList<String> titles = new ArrayList<String>();
    ArrayList<String> urls = new ArrayList<>();

    ArrayAdapter<String> arrayAdapter;

    String result="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ListView listViewNews = (ListView) findViewById(R.id.topRatedNews);

        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,titles);

        listViewNews.setAdapter(arrayAdapter);

        listViewNews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(MainActivity.this,webViewActivity.class);
                Log.e("url",urls.get(position));
                i.putExtra("url",urls.get(position));

                startActivity(i);

            }
        });

        articlesDB=this.openOrCreateDatabase("ARTICLESDB",MODE_PRIVATE,null);

        articlesDB.execSQL("CREATE TABLE IF NOT EXISTS articles(id INTEGER PRIMARY KEY , articleID INTEGER , url VARCHAR , title VARCHAR , content VARCHAR )");

        updateListView();



        GetJSON getJSON = new GetJSON();

       // articlesDB.execSQL("DELETE FROM articles");

        try {
         getJSON.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateListView()
    {
        Cursor cursor = articlesDB.rawQuery("SELECT * FROM articles ORDER BY articleID DESC",null);


        int idIndex = cursor.getColumnIndex("articleID");
        int urlIndex = cursor.getColumnIndex("url");
        int titleIndex = cursor.getColumnIndex("title");
        cursor.moveToFirst();
        Log.e("cursor size:",String.valueOf(cursor.getCount()));

        titles.clear();
        urls.clear();

        while (!cursor.isAfterLast())
        {

            titles.add(cursor.getString(titleIndex));

            urls.add(cursor.getString(urlIndex));



            Log.e("articleID: ", String.valueOf(cursor.getInt(idIndex)));
            Log.e("articleURL: ",cursor.getString(urlIndex));
            Log.e("articleTitle: ",cursor.getString(titleIndex));

            cursor.moveToNext();


        }

        arrayAdapter.notifyDataSetChanged();
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




                JSONArray jsonArray = new JSONArray(result);

                articlesDB.execSQL("DELETE FROM articles");

                for(int i = 0 ; i<20;i++)
                {
                    //Log.i("Article ",jsonArray.getString(i));
                    String articleID = jsonArray.getString(i);

                     url = new URL("https://hacker-news.firebaseio.com/v0/item/"+articleID+".json?print=pretty");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    inputStream = urlConnection.getInputStream();
                    inputStreamReader = new InputStreamReader(inputStream);
                    String articleInfo ="";
                    data = inputStreamReader.read();

                    while (data != -1)
                    {

                        char c = (char)data;
                        articleInfo+=c;
                        data = inputStreamReader.read();

                    }


                    JSONObject jsonObject = new JSONObject(articleInfo);

                    if(jsonObject.has("url"))
                    {   String articleTitle = jsonObject.getString("title");
                        String articleURL = jsonObject.getString("url");

                       /* url = new URL(articleURL);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        inputStream = urlConnection.getInputStream();
                        inputStreamReader = new InputStreamReader(inputStream);

                        data = inputStreamReader.read();
                        String urlContent ="";
                        while (data != -1)
                        {

                            char c = (char)data;
                            urlContent+=c;
                            data = inputStreamReader.read();

                        }*/




                        articleIds.add(Integer.parseInt(articleID));

                        articleTitles.put(Integer.parseInt(articleID),articleTitle);

                        articleURLs.put(Integer.parseInt(articleID),articleURL);

                        String sqlQueryInsert = "INSERT INTO articles (articleID , url , title) VALUES (? , ? , ?)";

                        SQLiteStatement statement = articlesDB.compileStatement(sqlQueryInsert);

                        statement.bindString(1,articleID);

                        statement.bindString(2,articleURL);

                        statement.bindString(3,articleTitle);

                        statement.execute();


                    }
                    else{
                        continue;
                    }



                }

                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return "Failed";



        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            updateListView();

        }
    }
}
