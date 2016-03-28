package com.example.dell.augmentedreality;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyPingCallback;
import com.kinvey.java.Query;
import com.kinvey.java.core.KinveyClientCallback;


public class Main2Activity extends ActionBarActivity {
    Client mKinveyClient ;
    OrderAdapter adapter;
    SharedPreferences sharedpreferences;
    ListView list_view;
    Button add;
    Order [] my_orders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        add=(Button)findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callNewOrderDialog();;
            }
        });
//        sharedpreferences=getSharedPreferences("owner_name",MODE_PRIVATE);
//        mKinveyClient = new Client.Builder("kid_by43WGIXJZ", "418fd57b1e974341bef79a3845c32927"
//                , this).build();
//        mKinveyClient.ping(new KinveyPingCallback() {
//            public void onFailure(Throwable t) {
//                Log.e("Kinvey", "Kinvey Ping Failed", t);
//            }
//
//            public void onSuccess(Boolean b) {
//                Log.d("Kinvey", "Kinvey Ping Success");
//                loadData(1);
//            }
//        });


    }
    public void loadData(final int first){

        Order order = new Order();
        Query q = mKinveyClient.query();
        Log.i("error shared preference value ", sharedpreferences.getString("owner_name", "").toString());
        q.equals("owner_name", sharedpreferences.getString("owner_name",""));

        AsyncAppData<Order> myEvents = mKinveyClient.appData("Order", Order.class);
        myEvents.get(q, new KinveyListCallback<Order>() {
            @Override
            public void onSuccess(Order[] orders) {
                my_orders=orders;
                Toast.makeText(getApplicationContext(), orders.length + "", Toast.LENGTH_SHORT).show();
                list_view=(ListView)findViewById(R.id.orders_list);
                adapter=new OrderAdapter(Main2Activity.this,orders,1);
                list_view.setAdapter(adapter);
                if(first==1)
                    completeSettingAdapter();
            }

            @Override
            public void onFailure(Throwable throwable) {
                Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                //   Log.i("error", throwable.getMessage());
            }
        });
    }
    public void completeSettingAdapter(){
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Order order=my_orders[position];
                if(order.getRequests()==0) {
                    Toast.makeText(getApplicationContext(), "There is no Requests", Toast.LENGTH_SHORT).show();
                }
                else{
                    order.setRequests(order.getRequests() - 1);
                    AsyncAppData<Order> myevents = mKinveyClient.appData("Order", Order.class);
                    myevents.save(order, new KinveyClientCallback<Order>() {
                        @Override
                        public void onFailure(Throwable e) {
                            Log.i("TAG", "failed to save event data" + e.getMessage());
                            Log.i("TAG", sharedpreferences.getString("owner_name", ""));
                        }

                        @Override
                        public void onSuccess(Order r) {
                            Log.d("TAG", "saved data for entity " + r.getName());
                            Toast.makeText(getApplicationContext(), "You Have finished one Request", Toast.LENGTH_SHORT).show();
                            loadData(0 );
                        }
                    });

                }
            }

        });
    }
    ////////////
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void callNewOrderDialog()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(
                new ContextThemeWrapper(this, R.style.AlertDialogCustom));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText edittext = new EditText(getApplicationContext());
        edittext.setEllipsize(TextUtils.TruncateAt.END);
        edittext.setSingleLine();
        edittext.setHint("Name");

        final EditText edittext2 = new EditText(getApplicationContext());
        edittext2.setHint("Price");
        layout.addView(edittext);
        layout.addView(edittext2);
        alert.setView(layout);
        alert.setTitle("New Order");
        alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Order x=new Order();
                if(edittext.getText()!=null)
                     x.setName((edittext.getText().toString()));
                if(edittext2.getText()!=null) {
                    try {
                        int p = Integer.parseInt(edittext2.getText().toString());
                        x.setPrice(p);
                        x.setOwnerName(sharedpreferences.getString("owner_name",""));
                        //////////////
                        AsyncAppData<Order> myevents = mKinveyClient.appData("Order", Order.class);
                        myevents.save(x, new KinveyClientCallback<Order>() {
                            @Override
                            public void onFailure(Throwable e) {
                                Log.e("TAG", "failed to save event data", e);
                            }

                            @Override
                            public void onSuccess(Order r) {
                                Log.d("TAG", "saved data for entity " + r.getName());
                                Toast.makeText(getApplicationContext(),"Your Order was Created Sucessfully",
                                        Toast.LENGTH_SHORT).show();
                                loadData(0);
                            }
                        });
                        /////////////
                    }
                    catch (Exception e){
                        Toast.makeText(getApplicationContext(),"Please enter valid numbers in price fields",Toast.LENGTH_SHORT).show();

                    }
                }


            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });

        alert.show();



    }
    //////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.logout) {
            mKinveyClient.user().logout().execute();
            SharedPreferences.Editor edit=sharedpreferences.edit();
            edit.putString("owner_name","");
            edit.commit();
            Intent i=new Intent(Main2Activity.this,Start.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
