package com.example.booklistingapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //widgets
    private EditText mGener,mMax;
    private Button mShow;
    private ProgressBar mLoading;
    private RecyclerView mRecyclerView;

    //vars
    private static final String TAG = "MainActivity";
    private CustomAdapter customAdapter;
    private String stringUrl;
    private ArrayList<Book> mBookList = new ArrayList<>();
    private URL url;
    private String jsonResponse;

    //conts
    private static final String items = "items";
    private static final String volumeInfo = "volumeInfo";
    private static final String author = "authors";
    private static final String title = "title";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find the widgets
        mGener = findViewById(R.id.gener);
        mMax = findViewById(R.id.max);
        mShow = findViewById(R.id.show);
        mLoading = findViewById(R.id.loading);
        mRecyclerView = findViewById(R.id.recycler_view);
        mLoading.setVisibility(View.INVISIBLE);

        setupRecyclerView();

        mShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoading.setVisibility(View.VISIBLE);
                stringUrl = getStringUrl();
                //start the background thread
                new BookTask().execute(stringUrl);


            }
        });




    }

    class BookTask extends AsyncTask<String,Void, ArrayList<Book>>
    {

        //this task will be done in Background
        @Override
        protected ArrayList<Book> doInBackground(String... strings) {

            url = createUrl(stringUrl);

            try {
                jsonResponse = makeHttpRequest(url);
            }
            catch (IOException e) {
                Log.e(TAG, "onCreate: Error while making HTTP Request",e );
                e.printStackTrace();
            }

            try {
                 extractFeaturesFromJson(jsonResponse);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return mBookList;
        }

        //this will be done on Main Thread
        @Override
        protected void onPostExecute(ArrayList<Book> books) {
            super.onPostExecute(books);
            Log.d(TAG, "onPostExecute: Book List: " + books.toString());
            mLoading.setVisibility(View.GONE);
            customAdapter.notifyDataSetChanged();
        }
    }

    private void setupRecyclerView()
    {

        customAdapter = new CustomAdapter(this , mBookList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(customAdapter);
    }

    private void extractFeaturesFromJson(String jsonResponse) throws JSONException {

        JSONObject root = new JSONObject(jsonResponse);
        JSONArray items = root.getJSONArray("items");

        for(int i=0; i<10; i++)
        {
            JSONObject item = items.getJSONObject(i);
            Book book = new Book();
            //setTitle
            book.setTitle(item.getJSONObject(volumeInfo).getString(title));
            String authors = item.getString(author);

            book.setAuthor(authors);
            book.setDescription(item.getJSONObject(volumeInfo).getString("description"));
            book.setImageLink(item.getJSONObject(volumeInfo).getJSONObject("imageLinks").getString("thumbnail"));
            book.setInfoLink(item.getJSONObject(volumeInfo).getString("infoLink"));

            if(item.getJSONObject("accessInfo").getJSONObject("pdf").getBoolean("isAvailable"))
            {
                book.setDownloadLink(item.getJSONObject("accessInfo")
                        .getJSONObject("pdf").getString("acsTokenLink"));
            }
            else {
                book.setDownloadLink(null);
            }

            mBookList.add(book);

        }




    }

    private String getStringUrl()
    {
        StringBuilder builder = new StringBuilder();
        String gener = mGener.getText().toString().trim();
        String max = mMax.getText().toString().trim();

        if(!TextUtils.isEmpty(gener) && !TextUtils.isEmpty(max))
        {
            builder.append("https://www.googleapis.com/books/v1/volumes?q").append(gener).
                    append("&maxResults=").append(max);

            return builder.toString();
        }
        else {
            Toast.makeText(this, "Enter something", Toast.LENGTH_SHORT).show();
            return null;
        }


    }

    private URL createUrl(String stringUrl)
    {
        URL url = null;
        try
        {
            url = new URL(stringUrl);
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    private String makeHttpRequest(URL url) throws IOException
    {
        String jsonResponse = null;
        if(url == null)
        {
            Log.i(TAG, "makeHttpRequest: URL received is null");
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try
        {
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();

            if(urlConnection.getResponseCode() == 200)
            {
                Log.i(TAG, "makeHttpRequest: Everything is okay");
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(urlConnection != null)
            {
                urlConnection.disconnect();
            }
            if(inputStream != null)
            {
                inputStream.close();
            }
        }

        return jsonResponse;
    }

    private String readFromStream(InputStream inputStream) throws IOException
    {
        StringBuilder output = new StringBuilder();
        if(inputStream != null)
        {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream , Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line = reader.readLine();
            while (line != null)
            {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
