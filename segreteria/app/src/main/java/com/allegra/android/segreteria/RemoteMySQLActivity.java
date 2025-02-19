package com.allegra.android.segreteria;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.allegra.android.db_work.helper.CheckNetworkStatus;


public class RemoteMySQLActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_my_sql);
        //first
        Button viewAllBtn = (Button) findViewById(R.id.viewAllBtn);
        Button addNewBtn = (Button) findViewById(R.id.addNewBtn);
        viewAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)//push the button/s
            {
                //Check for network connectivity
                if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
                    Intent i = new Intent(getApplicationContext(), ListMessageActivity.class);
                    startActivity(i);
                }
                else {
                    //Display error message if not connected to internet
                    Toast.makeText(RemoteMySQLActivity.this,
                            "IMPOSSIBILE CONNETTERSI A INTERNET",
                            Toast.LENGTH_LONG).show();

                }

            }
        });

        addNewBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Check for network connectivity
                if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext()))
                {
                    Intent i = new Intent(getApplicationContext(), AddMessageActivity.class);
                    startActivity(i);
                }
                else
                    {
                    //Display error message if not connected to internet
                    Toast.makeText(RemoteMySQLActivity.this,
                            "IMPOSSIBILE CONNETTERSI A INTERNET",
                            Toast.LENGTH_LONG).show();
                    }

            }
        });

    }

}
