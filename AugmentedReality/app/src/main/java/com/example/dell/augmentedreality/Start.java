package com.example.dell.augmentedreality;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyPingCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;


public class Start extends ActionBarActivity {
    Button log_in;
    Button log_out;
    Button start;
    Client  mKinveyClient;
    SharedPreferences sharedpreferences;
    EditText name;
    EditText password;
    boolean success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ///////////////////////////////
        sharedpreferences = getSharedPreferences("owner_name", Context.MODE_PRIVATE);
        checkLoggedIn();
        /////////////////////////////////
        setContentView(R.layout.activity_start);
        ///////////////////////sign sout any previous user
        mKinveyClient = new Client.Builder("kid_by43WGIXJZ", "418fd57b1e974341bef79a3845c32927"
                , this).build();
        mKinveyClient.ping(new KinveyPingCallback() {
            public void onFailure(Throwable t) {
                Log.e("Kinvey", "Kinvey Ping Failed", t);
            }

            public void onSuccess(Boolean b) {
                Log.d("Kinvey", "Kinvey Ping Success");
                //mKinveyClient.push().initialize(getApplication());
            }
        });
        name=(EditText)findViewById(R.id.name);
        password=(EditText)findViewById(R.id.password);
        start=(Button)findViewById(R.id.start);
        log_in=(Button)findViewById(R.id.log_in);
        log_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name.getText()==null|| name.getText().equals("")){
                    Toast.makeText(getApplicationContext(),"Please Enter a valid Name",Toast.LENGTH_SHORT).show();
                }
                else{
                    if(password.getText()==null || password.getText().equals("")){
                        Toast.makeText(getApplicationContext(),"Invalid Password",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        LogIn(name.getText().toString(),password.getText().toString(),0);//he is an owner
                    }
                }

            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKinveyClient.user().logout().execute();
                LogIn("student","student",1);

            }
        });

    }
    ////////////
    public void checkLoggedIn(){
        if(sharedpreferences.getString("owner_name","")!=null&&!sharedpreferences.getString("owner_name","").equals("")){
            if(!sharedpreferences.getString("owner_name","").equals("student")){//if it is not equal student
                Intent i=new Intent(Start.this,Main2Activity.class);
                startActivity(i);
                finish();
            }
            else{
                mKinveyClient = new Client.Builder("kid_by43WGIXJZ", "418fd57b1e974341bef79a3845c32927"
                        , this).build();
                mKinveyClient.ping(new KinveyPingCallback() {
                    public void onFailure(Throwable t) {
                        Log.e("Kinvey", "Kinvey Ping Failed", t);
                    }

                    public void onSuccess(Boolean b) {
                        Log.d("Kinvey", "Kinvey Ping Success");
                        SignOut();
                    }
                });

            }

        }
    }
    void LogIn(String name,String pass,int start){
        final int start1=start;
        mKinveyClient.user().login(name, pass, new KinveyUserCallback() {
            @Override
            public void onFailure(Throwable t) {
                CharSequence text = "Wrong username or password.";
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.i("error", t.getMessage());success=false;


            }
            @Override
            public void onSuccess(User u) {
                CharSequence text = "Welcome back," + u.getUsername() + ".";
                SharedPreferences.Editor edit=sharedpreferences.edit();
                edit.putString("owner_name", u.getUsername());
                edit.commit();
                if(start1==0) {
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                    Intent my_intent = new Intent(getApplicationContext(), Main2Activity.class);
                    startActivity(my_intent);
                }
                else {
                    Intent i=new Intent(getApplicationContext(),ImageTargets.class);
                    startActivity(i);
                    finish();
                }

            }
        });

    }
    void SignOut(){
        mKinveyClient.user().logout().execute();
    }


            //////////////



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
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
