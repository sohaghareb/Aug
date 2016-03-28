package com.example.dell.augmentedreality;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.dell.augmentedreality.*;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyPingCallback;
import com.kinvey.java.Query;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.KinveyDeleteResponse;
import com.nineoldandroids.animation.Animator;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.ObjectTracker;
import com.qualcomm.vuforia.STORAGE_TYPE;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vuforia;
import com.kinvey.java.model.KinveyMetaData;

import java.util.ArrayList;
import java.util.Vector;

import SampleApplication.SampleApplicationControl;
import SampleApplication.SampleApplicationException;
import SampleApplication.SampleApplicationSession;
import SampleApplication.utils.LoadingDialogHandler;
import SampleApplication.utils.SampleApplicationGLView;
import SampleApplication.utils.Texture;


public class ImageTargets extends Activity implements SampleApplicationControl
{
    private View mFlashOptionView;
    TextView  mtext;
    Button menu_button;
    Button request_order;
    Order[] my_orders;
    private boolean mFlash = false;
    private Vector<Texture> mTextures;
    private DataSet mCurrentDataset;
    private int mCurrentDatasetSelectionIndex = 0;
    private int mStartDatasetsIndex = 0;
    private int mDatasetsNumber = 0;
    private ArrayList<String> mDatasetStrings = new ArrayList<String>();
    private static final String LOGTAG = "ImageTargets";
    // Our renderer:f
    private ImageTargetRenderer mRenderer;
    private SampleApplicationGLView mGlView;
    private GestureDetector mGestureDetector;
    private RelativeLayout mUILayout;
    boolean mIsDroidDevice = false;
    private boolean mSwitchDatasetAsap = false;
    SampleApplicationSession vuforiaAppSession;
    ListView menu_list;
    LinearLayout menu;
    Client mKinveyClient;
    SharedPreferences sharedpreferences;

    //////////////
    private void startLoadingAnimation()
    {
        mUILayout = (RelativeLayout) View.inflate(this, R.layout.camera_overlay,
                null);

        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);

        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
                .findViewById(R.id.loading_indicator);

        // Shows the loading indicator at start
        loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);

        // Adds the inflated layout to the view
        addContentView(mUILayout, new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT));
        menu=(LinearLayout)findViewById(R.id.menu);
        menu_list=(ListView)findViewById(R.id.menu_list);

    }
    //////////



    @Override
    public boolean doLoadTrackersData()
    {
        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        if (mCurrentDataset == null)
            mCurrentDataset = objectTracker.createDataSet();

        if (mCurrentDataset == null)
            return false;

        if (!mCurrentDataset.load(mDatasetStrings.get(mCurrentDatasetSelectionIndex),
                STORAGE_TYPE.STORAGE_APPRESOURCE)) {
            Log.i("soha","error can not load");
            return false;
        }

        if (!objectTracker.activateDataSet(mCurrentDataset)) {
            Log.i("soha","error canot activate");
            return false;
        }

        int numTrackables = mCurrentDataset.getNumTrackables();
        for (int count = 0; count < numTrackables; count++)
        {
            Trackable trackable = mCurrentDataset.getTrackable(count);
            String name = "Current Dataset : " + trackable.getName();
            Log.i("name ",trackable.getName());
            trackable.setUserData(name);
            Log.d(LOGTAG, "UserData:Set the following user data "
                    + (String) trackable.getUserData());
        }

        return true;
    }

    @Override
    public boolean doUnloadTrackersData()
    {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
                .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        if (mCurrentDataset != null && mCurrentDataset.isActive())
        {
            if (objectTracker.getActiveDataSet().equals(mCurrentDataset)
                    && !objectTracker.deactivateDataSet(mCurrentDataset))
            {
                result = false;
            } else if (!objectTracker.destroyDataSet(mCurrentDataset))
            {
                result = false;
            }

            mCurrentDataset = null;
        }

        return result;
    }

    @Override
    public void onInitARDone(SampleApplicationException exception) {

        if (exception == null)
        {
            initApplicationAR();

            mRenderer.mIsActive = true;

            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.MATCH_PARENT));

            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();
            mtext.bringToFront();

            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);

            try
            {
                vuforiaAppSession.startAR(CameraDevice.CAMERA.CAMERA_DEFAULT);
            } catch (SampleApplicationException e)
            {
                Log.e(LOGTAG, e.getString());
            }

            boolean result = CameraDevice.getInstance().setFocusMode(
                    CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

//            if (result)
//                mContAutofocus = true;
//            else
//                Log.e(LOGTAG, "Unable to enable continuous autofocus");

//            mSampleAppMenu = new SampleAppMenu(this, this, "Image Targets",
//                    mGlView, mUILayout, null);
//            setSampleAppMenuSettings();

        } else
        {
            Log.e(LOGTAG, exception.getString());
           // showInitializationErrorMessage(exception.getString());
        }

    }


    @Override
    public void onQCARUpdate(State state)
    {
        if (mSwitchDatasetAsap)
        {
            mSwitchDatasetAsap = false;
            TrackerManager tm = TrackerManager.getInstance();
            ObjectTracker ot = (ObjectTracker) tm.getTracker(ObjectTracker
                    .getClassType());
            if (ot == null || mCurrentDataset == null
                    || ot.getActiveDataSet() == null)
            {
                Log.d(LOGTAG, "Failed to swap datasets");
                return;
            }

            doUnloadTrackersData();
            doLoadTrackersData();
        }
    }


    @Override
    public boolean doInitTrackers()
    {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null)
        {
            Log.e(
                    LOGTAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else
        {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        return result;
    }


    @Override
    public boolean doStartTrackers()
    {
        // Indicate if the trackers were started correctly
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.start();

        return result;
    }


    @Override
    public boolean doStopTrackers()
    {
        // Indicate if the trackers were stopped correctly
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(
                ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.stop();

        return result;
    }


    @Override
    public boolean doDeinitTrackers()
    {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());

        return result;
    }

    LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.camera_overlay);
        startLoadingAnimation();
        vuforiaAppSession = new SampleApplicationSession(this);
        //startLoadingAnimation();
        mDatasetStrings.add("Am_1.xml");
        vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mGestureDetector = new GestureDetector(this, new GestureListener());

        // Load any sample specific textures:
        mTextures = new Vector<Texture>();
        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith(
                "droid");
       // mtext=(TextView)findViewById(R.id.my_text);
        //////////////
        sharedpreferences=getSharedPreferences("owner_name", MODE_PRIVATE);
        mKinveyClient = new Client.Builder("kid_by43WGIXJZ", "418fd57b1e974341bef79a3845c32927"
                , this).build();
        mKinveyClient.ping(new KinveyPingCallback() {
            public void onFailure(Throwable t) {
                Log.e("Kinvey", "Kinvey Ping Failed", t);
            }

            public void onSuccess(Boolean b) {
                Log.d("Kinvey", "Kinvey Ping Success");
            }
        });
        //////////////


    }
    // Process Single Tap event to trigger autofocus
    private class GestureListener extends
            GestureDetector.SimpleOnGestureListener
    {
        // Used to set autofocus one second after a manual focus is triggered
        private final Handler autofocusHandler = new Handler();


        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }


        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            // Generates a Handler to trigger autofocus
            // after 1 second
            autofocusHandler.postDelayed(new Runnable()
            {
                public void run()
                {
                    boolean result = CameraDevice.getInstance().setFocusMode(
                            CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);

                    if (!result)
                        Log.e("SingleTapUp", "Unable to trigger focus");
                }
            }, 1000L);

            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    private Boolean exit = false;
    @Override
    public void onBackPressed() {
        if (exit) {
            mKinveyClient.user().logout().execute();
            finish(); // finish activity
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }
//////////////////
    public void loadMenu(String shop ,final int first ){
        if(shop.equalsIgnoreCase("piking")){
            Query q = mKinveyClient.query();
            Log.i("error shared preference value ", "piking");
            q.equals("owner_name", shop);
           Log.i("soha bug", sharedpreferences.getString("owner_name", ""));
            Log.i("soha bug 2",shop);
            final AsyncAppData<Order> myEvents = mKinveyClient.appData("Order", Order.class);
            myEvents.get(q, new KinveyListCallback<Order>() {
                @Override
                public void onSuccess(Order[] orders) {
                    my_orders = orders;
                    Log.i("soha load ", "load data from piking succeded " + orders);
                    OrderAdapter adapter = new OrderAdapter(ImageTargets.this, orders, 0);
                    menu_list.setAdapter(adapter);
                    menu_button.setVisibility(View.VISIBLE);
                    if(first==1)
                        loadMenuHelper();
                    ///////////////////////////////////////       ////////////////////// /////////////
                }

                @Override
                public void onFailure(Throwable throwable) {
                    //  Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.i("error", "failed to load menu" + throwable.getMessage());
                }
            });
        }

    }
    public void loadMenuHelper(){
        menu_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Order order = my_orders[position];
                int requests = order.getRequests();
                ////////////have the handler
                AlertDialog.Builder alert = new AlertDialog.Builder(
                        new ContextThemeWrapper(ImageTargets.this, R.style.AlertDialogCustom));

                LinearLayout layout = new LinearLayout(ImageTargets.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                alert.setTitle("There are " + requests + "Before you");
                alert.setView(layout);
                alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ///////////////
                        order.setRequests(order.getRequests() + 1);
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
                                Toast.makeText(getApplicationContext(), "Your Order was Created Sucessfully", Toast.LENGTH_SHORT).show();
                                loadMenu(order.getOwner_name(),0);
                            }
                        });

                    }
                    ////////////


                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                alert.show();
                ////////////////

            }
        });
        ///////////////
    }
    /////////////////////////
    public void Animte(int no){

        if(no==0){//animate text
            YoYo.YoYoString  rope = YoYo.with(Techniques.RollIn)
                    .duration(2500)
                    .interpolate(new AccelerateDecelerateInterpolator())
                    .withListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            View mTarget = findViewById(R.id.my_text);
                            YoYo.YoYoString  rope = YoYo.with(Techniques.Shake).duration(1000).playOn(mTarget);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            //Toast.makeText(MyActivity.this, "canceled", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    })
                    .playOn(mtext);
        }
        else {//animtae layout
            if(menu.getVisibility()==View.VISIBLE){//if it is visible
                 YoYo.with(Techniques.FlipOutX).duration(2500).interpolate(new AccelerateDecelerateInterpolator())
                        .withListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                menu.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                //Toast.makeText(MyActivity.this, "canceled", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        })
                        .playOn(menu);;
            }
            else{//if it is not visible
                menu.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FlipInX).duration(2500).playOn(menu);
            }

        }
    }
    ////////////////////
protected void onResume()
{
    Log.d("onResume", "onResume");
    super.onResume();
    ImageTargetRenderer.mainActivityHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
           //
            String text = (String) msg.obj;
            if(text.contains("piking")) {
                if(mtext.getText()!=null&&!mtext.getText().equals("Piking")) {
                    mtext.setText("Piking");
                   // mtext.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.slide_in_left));
                    Animte(0);
                    loadMenu("piking",1);

                }
            }
            else if(text.contains("friends"))
                mtext.setText("Friends");
            else if (text.contains("voda"))
                mtext.setText("Vodafone");
            else if(text.contains("bread"))
                mtext.setText("BreadBasket");
            else if(text.contains("laroma"))
                mtext.setText("Laroma");
            else if(text.contains("mob"))
                mtext.setText("Mobinil");
            else
                mtext.setText(text);


        }
    };
    if(mtext!=null)



    // This is needed for some Droid devices to force portrait
    if (mIsDroidDevice)
    {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    try
    {
        vuforiaAppSession.resumeAR();
    } catch (SampleApplicationException e)
    {
        Log.e(LOGTAG, e.getString());
    }

    // Resume the GL view:
    if (mGlView != null)
    {
        mGlView.setVisibility(View.VISIBLE);
        mGlView.onResume();
    }
}
    public void onConfigurationChanged(Configuration config)
    {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);

        vuforiaAppSession.onConfigurationChanged();
    }


    // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause()
    {
        Log.d(LOGTAG, "onPause");
        super.onPause();

        if (mGlView != null)
        {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }

        // Turn off the flash
        if (mFlashOptionView != null && mFlash)
        {
            // OnCheckedChangeListener is called upon changing the checked state
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            {
                ((Switch) mFlashOptionView).setChecked(false);
            } else
            {
                ((CheckBox) mFlashOptionView).setChecked(false);
            }
        }

        try
        {
            vuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
    }


    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy() {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();

        try {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e) {
            Log.e(LOGTAG, e.getString());
        }

        // Unload texture:
        mTextures.clear();
        mTextures = null;

        System.gc();
    }
    private void initApplicationAR()
    {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);
        mRenderer = new ImageTargetRenderer(this, vuforiaAppSession);
        mRenderer.setTextures(mTextures);
        mGlView.setRenderer(mRenderer);
        //////i added to make to put a text
        mtext=(TextView)findViewById(R.id.my_text);
        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/CookieMonster.ttf");
        mtext.setTypeface(font);
        /// mtext.setText("sohaaaaaaaaaaa");
        menu_button=(Button)findViewById(R.id.menu_button);
        menu_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mtext.getText() != null) {
                    ////call Anim function
                    Animte(1);
                }
            }
        });

    }


    /////////////////////

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
