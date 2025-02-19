package com.allegra.android.segreteria;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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

public class MovieUpdateDeleteActivity extends AppCompatActivity {
    private static String STRING_EMPTY = "";
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DATA = "data";
    private static final String KEY_MOVIE_ID = "id_messaggio";
    private static final String KEY_MOVIE_NAME = "tipologia";
    private static final String KEY_GENRE = "descrizione";
    private static final String BASE_URL = "http://www.mywebtools.altervista.org/message/";
    private String movieId;
    private EditText movieNameEditText;
    private EditText genreEditText;

    private String movieName;
    private String genre;
    private String year;
    private String rating;
    private Button deleteButton;
    private Button updateButton;
    private int success;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_update_delete);
        Intent intent = getIntent();
        movieNameEditText = (EditText) findViewById(R.id.txtMovieNameUpdate);
        genreEditText = (EditText) findViewById(R.id.txtGenreUpdate);


        movieId = intent.getStringExtra(KEY_MOVIE_ID);
        new FetchMovieDetailsAsyncTask().execute();
        deleteButton = (Button) findViewById(R.id.btnDelete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDelete();
            }
        });
        updateButton = (Button) findViewById(R.id.btnUpdate);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
                    updateMovie();

                } else {
                    Toast.makeText(MovieUpdateDeleteActivity.this,
                            "IMPOSSIBILE CONNETTERSI A INTERNET",
                            Toast.LENGTH_LONG).show();

                }

            }
        });


    }

    /**
     * Fetches single movie details from the server
     */
    private class FetchMovieDetailsAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            pDialog = new ProgressDialog(MovieUpdateDeleteActivity.this);
            pDialog.setMessage("Sto caricando, attendi...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(KEY_MOVIE_ID, movieId);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "get_movie_details.php", "GET", httpParams);
            try {
                int success = jsonObject.getInt(KEY_SUCCESS);
                JSONObject movie;
                if (success == 1) {
                    //Parse the JSON response
                    movie = jsonObject.getJSONObject(KEY_DATA);
                    movieName = movie.getString(KEY_MOVIE_NAME);
                    genre = movie.getString(KEY_GENRE);




                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    //Populate the Edit Texts once the network activity is finished executing
                    movieNameEditText.setText(movieName);
                    genreEditText.setText(genre);


                }
            });
        }


    }

    /**
     * Displays an alert dialogue to confirm the deletion
     */
    private void confirmDelete() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MovieUpdateDeleteActivity.this);
        alertDialogBuilder.setMessage("Vuoi veramente effettuare una cancellazione?");
        alertDialogBuilder.setPositiveButton("Cancella",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
                            //If the user confirms deletion, execute DeleteMovieAsyncTask
                            new DeleteMovieAsyncTask().execute();
                        } else {
                            Toast.makeText(MovieUpdateDeleteActivity.this,
                                    "Cancellazione >>> Nessuna connessione",
                                    Toast.LENGTH_LONG).show();

                        }
                    }
                });

        alertDialogBuilder.setNegativeButton("Cancel", null);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * AsyncTask to delete a message
     */
    private class DeleteMovieAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            pDialog = new ProgressDialog(MovieUpdateDeleteActivity.this);
            pDialog.setMessage("STO CANCELLANDO, ATTENDERE...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            //Set movie_id parameter in request
            httpParams.put(KEY_MOVIE_ID, movieId);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "delete_movie.php", "POST", httpParams);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
            } catch (JSONException e) {
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
                        Toast.makeText(MovieUpdateDeleteActivity.this,
                                "RECORD CANCELLATO", Toast.LENGTH_LONG).show();
                        Intent i = getIntent();
                        //send result code 20 to notify about movie deletion
                        setResult(20, i);
                        finish();

                    } else {
                        Toast.makeText(MovieUpdateDeleteActivity.this,
                                "ERRORE DURANTE LA CANCELLAZIONE",
                                Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }

    /**
     * Checks whether all files are filled. If so then calls UpdateMovieAsyncTask.
     * Otherwise displays Toast message informing one or more fields left empty
     */
    private void updateMovie() {


        if (!STRING_EMPTY.equals(movieNameEditText.getText().toString()) &&
                !STRING_EMPTY.equals(genreEditText.getText().toString()) )
        {

            movieName = movieNameEditText.getText().toString();
            genre = genreEditText.getText().toString();
            new UpdateMovieAsyncTask().execute();
        }
        else
            {
            Toast.makeText(MovieUpdateDeleteActivity.this,
                    "UNO DEI CAMPI E' VUOTO!",
                    Toast.LENGTH_LONG).show();

        }


    }
    /**
     * AsyncTask for updating a movie details
     */

    private class UpdateMovieAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            pDialog = new ProgressDialog(MovieUpdateDeleteActivity.this);
            pDialog.setMessage("STO CARICANDO I DATI SUL SERVER ATTENDI...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            //Populating request parameters
            httpParams.put(KEY_MOVIE_ID, movieId);
            httpParams.put(KEY_MOVIE_NAME, movieName);
            httpParams.put(KEY_GENRE, genre);

            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "update_movie.php", "POST", httpParams);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
            } catch (JSONException e) {
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
                        Toast.makeText(MovieUpdateDeleteActivity.this,
                                "RECORD AGGIORNATO", Toast.LENGTH_LONG).show();
                        Intent i = getIntent();
                        //send result code 20 to notify about movie update
                        setResult(20, i);
                        finish();

                    } else {
                        Toast.makeText(MovieUpdateDeleteActivity.this,
                                "ERRORE DURANTE L'AGGIORNAMENTO",
                                Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }
}