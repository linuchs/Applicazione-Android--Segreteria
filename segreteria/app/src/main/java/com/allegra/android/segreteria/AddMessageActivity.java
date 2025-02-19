package com.allegra.android.segreteria;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.allegra.android.db_work.helper.CheckNetworkStatus;
import com.allegra.android.db_work.helper.HttpJsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddMessageActivity extends AppCompatActivity {
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_MOVIE_NAME = "movie_name";
    private static final String KEY_GENRE = "genre";
 ;
    private static final String BASE_URL = "http://www.mywebtools.altervista.org/message/";

    private static String STRING_EMPTY = "";
    private EditText movieNameEditText;
    private EditText genreEditText;

    private String movieName;
    private String genre;

    private Button addButton;
    private int success;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_message);
        movieNameEditText = (EditText) findViewById(R.id.txtMovieNameAdd);
        genreEditText = (EditText) findViewById(R.id.txtGenreAdd);

        addButton = (Button) findViewById(R.id.btnAdd);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
                    addMovie();
                } else {
                    Toast.makeText(AddMessageActivity.this,
                            "IMPOSSIBILE CONNETTERSI A INTERNET!!!",
                            Toast.LENGTH_LONG).show();

                }

            }
        });

    }

    /**
     * Checks whether all files are filled. If so then calls AddMovieAsyncTask.
     * Otherwise displays Toast message informing one or more fields left empty
     */
    private void addMovie() {
        if (!STRING_EMPTY.equals(movieNameEditText.getText().toString()) &&
                !STRING_EMPTY.equals(genreEditText.getText().toString()) ) {

            movieName = movieNameEditText.getText().toString();
            genre = genreEditText.getText().toString();

            new AddMovieAsyncTask().execute();
        } else {
            Toast.makeText(AddMessageActivity.this,
                    "UNO O PIÙ CAMPI VUOTI!",
                    Toast.LENGTH_LONG).show();

        }


    }

    /**
     * AsyncTask for adding a movie
     */
    private class AddMovieAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display proggress bar
            pDialog = new ProgressDialog(AddMessageActivity.this);
            pDialog.setMessage("STO AGGIUNGENTO UN RECORD...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            //Populating request parameters
            httpParams.put(KEY_MOVIE_NAME, movieName);
            httpParams.put(KEY_GENRE, genre);

            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "add_movie.php", "POST", httpParams);
            try
            {
                success = jsonObject.getInt(KEY_SUCCESS);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    if (success == 1) {
                        //Display success message
                        Toast.makeText(AddMessageActivity.this,
                                "RECORD AGGIUNTO!", Toast.LENGTH_LONG).show();
                        Intent i = getIntent();
                        //send result code 20 to notify about movie update
                        setResult(20, i);
                        //Finish ths activity and go back to listing activity
                        finish();

                    } else {
                        Toast.makeText(AddMessageActivity.this,
                                "E' STATO RISCONTRATO UN ERRORE",
                                Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }
}
