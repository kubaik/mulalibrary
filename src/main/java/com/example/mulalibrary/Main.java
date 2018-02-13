package com.example.mulalibrary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.plugin.consumerapp.IMyAidlInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main extends AppCompatActivity {
    private PackageBroadcastReceiver packageBroadcastReceiver;
    private IntentFilter packageFilter;
    private ArrayList<HashMap<String, String>> services;
    private SimpleAdapter itemAdapter;
    private LayoutInflater inflater;
    private Handler uiHandler;
    private PluginServiceConnection pluginServiceConnection[] = new PluginServiceConnection[4];
    private IMyAidlInterface iResPlugin[] = new IMyAidlInterface[4];
    private OnClickListenerProxy listeners[] = new OnClickListenerProxy[4];
    private static final String LOG_TAG = "RESPLUGINAPP";
    private static final String ACTION_PICK_RESPLUGIN = "com.plugin.intent.action.PICK_RESPLUGIN";
    private static final String KEY_PKG = "pkg";
    private static final String KEY_SERVICENAME = "servicename";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        inflater = LayoutInflater.from(this);


        packageBroadcastReceiver = new PackageBroadcastReceiver();
        packageFilter = new IntentFilter();
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addCategory(Intent.CATEGORY_DEFAULT);
        packageFilter.addDataScheme("package");
        uiHandler = new Handler();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Intent in = getIntent();
        Bundle b = in.getExtras();
        if (b != null) {
            String phoneNumber = b.getString("PHONE");
            Log.d(LOG_TAG, "@fillPluginList: phoneNumber " + phoneNumber);
        } else {
            Log.d(LOG_TAG, "@fillPluginList: bundle is null ");

        }
        fillPluginList(getApplicationContext());
    }

    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
        registerReceiver(packageBroadcastReceiver, packageFilter);
        bindPluginServices();
    }

    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
        unregisterReceiver(packageBroadcastReceiver);
        releasePluginServices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    class PackageBroadcastReceiver extends BroadcastReceiver {
        private static final String LOG_TAG = "PackageBroadcastReceiver";

        @SuppressLint("LongLogTag")
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive: " + intent);
            services.clear();
            releasePluginServices();
            fillPluginList(getApplicationContext());
            bindPluginServices();
        }
    }

    private void releasePluginServices() {
        for (int i = 0; i < services.size(); ++i) {
            unbindService(pluginServiceConnection[i]);
            pluginServiceConnection[i] = null;
        }
    }

    private void bindPluginServices() {
        for (int i = 0; i < services.size(); ++i) {
            pluginServiceConnection[i] = new PluginServiceConnection();
            Intent intent = new Intent();
            HashMap<String, String> data = services.get(i);
            intent.setClassName(data.get(KEY_PKG), data.get(KEY_SERVICENAME));
            Bundle bundle = new Bundle();
            bundle.putString("PHONE", "254721596868");
            intent.putExtras(bundle);
            Log.d(LOG_TAG, "@fill bindPluginServices: " + intent);
            bindService(intent, pluginServiceConnection[i], Context.BIND_AUTO_CREATE);
        }
    }

    public void fillPluginList(Context context) {
        services = new ArrayList<HashMap<String, String>>();
        PackageManager packageManager = context.getPackageManager();
        Intent baseIntent = new Intent(ACTION_PICK_RESPLUGIN);
        baseIntent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
        List<ResolveInfo> list = packageManager.queryIntentServices(baseIntent,
                PackageManager.MATCH_ALL);
        Log.d(LOG_TAG, "@fillPluginList: " + list);
        Log.d(LOG_TAG, "@fillPluginList list.size() : " + list.size());
        int i;
        for (i = 0; i < list.size(); ++i) {
            ResolveInfo info = list.get(i);
            ServiceInfo sinfo = info.serviceInfo;
            Log.d(LOG_TAG, "@fillPluginList: i: " + i + "; sinfo: " + sinfo);
            if (sinfo != null) {
                HashMap<String, String> item = new HashMap<String, String>();
                item.put(KEY_PKG, sinfo.packageName);
                item.put(KEY_SERVICENAME, sinfo.name);
                Log.d(LOG_TAG, "@fillPluginList: packageName : " + sinfo.packageName + " descriptionRes " + sinfo.descriptionRes + " processName" + sinfo.processName);
                services.add(item);
                listeners[i] = new OnClickListenerProxy(i);
                if (i <= 4) {
                    inflateToView(i,
                            packageManager,
                            sinfo.packageName);
                    registerButtonListener(i);
                }
            }
        }
        for (; i < 4; ++i) {
            initField(i);
        }
        Log.d(LOG_TAG, "@services: " + services);
    }

    private void inflateToView(int rowCtr,
                               PackageManager packageManager,
                               String packageName) {
        try {
            Log.d(LOG_TAG, "@fill rowCtr : " + rowCtr);
            Log.d(LOG_TAG, "@fill packageManager : " + packageManager);
            Log.d(LOG_TAG, "@fill packageName : " + packageName);
            ApplicationInfo info = packageManager.getApplicationInfo(packageName, 0);
            Resources res = packageManager.getResourcesForApplication(info);
            XmlResourceParser xres = res.getLayout(0x7f0b00aa);//0x7f0a001d //0x7f0801ca //0x7f0a001f
            int parentId = selectRow(rowCtr);
            ViewGroup parentView = (ViewGroup) findViewById(parentId);
            parentView.removeAllViews();
            View view = inflater.inflate(xres, parentView);
            adjustSubViewIds(parentView, idxToIdOffset(rowCtr));
            Intent serviceIntent = new Intent()
                    .setComponent(new ComponentName(
                            packageName,
                            packageName + ".MulaPluginService"));
            Bundle bundle = new Bundle();
            bundle.putString("PHONE", "254721596767");
            serviceIntent.putExtras(bundle);
            startService(serviceIntent);


        } catch (PackageManager.NameNotFoundException ex) {
            Log.e(LOG_TAG, "NameNotFoundException", ex);
        }
    }

    private void adjustSubViewIds(ViewGroup parent, int idOffset) {
        for (int i = 0; i < parent.getChildCount(); ++i) {
            View v = parent.getChildAt(i);
            if (v instanceof ViewGroup)
                adjustSubViewIds((ViewGroup) v, idOffset);
            else {
                int id = v.getId();
                if (id != View.NO_ID)
                    v.setId(id + idOffset);
            }
        }
    }

    private void initField(int rowCtr) {
        int parentId = selectRow(rowCtr);
        ViewGroup parentView = (ViewGroup) findViewById(parentId);
        TextView tv = new TextView(this);
        tv.setText("Main Application");
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(24);
        parentView.removeAllViews();
        parentView.addView(tv);
    }

    class PluginServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className,
                                       IBinder boundService) {
            int idx = getServiceConnectionIndex();
            Log.d(LOG_TAG, "onServiceConnected: ComponentName: " + className + "; idx: " + idx);
            if (idx >= 0) {
                iResPlugin[idx] = IMyAidlInterface.Stub.asInterface((IBinder) boundService);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            int idx = getServiceConnectionIndex();
            Log.d(LOG_TAG, "onServiceDisconnected: ComponentName: " + className + "; idx: " + idx);
            if (idx >= 0)
                iResPlugin[idx] = null;
        }

        private int getServiceConnectionIndex() {
            for (int i = 0; i < pluginServiceConnection.length; ++i)
                if (this == pluginServiceConnection[i])
                    return i;
            return -1;
        }
    }

    ;

    class OnClickListenerProxy implements View.OnClickListener {
        public OnClickListenerProxy(int idx) {
            this.idx = idx;
            idOffset = idxToIdOffset(idx);
        }

        public void onClick(View v) {
            int id = v.getId() - idOffset;
            Log.d(LOG_TAG, "onClick: [" + idx + "]: " + Integer.toHexString(id));
            if (iResPlugin[idx] != null) {
                Bundle state = captureState(idx);
                Bundle result = null;
                try {
                    result = iResPlugin[idx].onClick(id, state);
                } catch (RemoteException ex) {
                    Log.e(LOG_TAG, "onClick", ex);
                }
                Log.d(LOG_TAG, "onClick result: " + result);
                if (result != null) {
                    UIUpdateTask task = new UIUpdateTask(idx, result);
                    uiHandler.post(task);
                }
            }
        }

        int idOffset;
        int idx;
    }

    class UIUpdateTask implements Runnable {
        UIUpdateTask(int idx, Bundle update) {
            this.idx = idx;
            this.update = update;
        }

        public void run() {
            applyUpdates(idx, update);
        }

        int idx;
        Bundle update;
    }

    private void applyUpdates(int idx, Bundle update) {
        int parentId = selectRow(idx);
        ViewGroup parentView = (ViewGroup) findViewById(parentId);
        int idOffset = idxToIdOffset(idx);
        applyUpdates(parentView, idOffset, update);
    }

    private void applyUpdates(ViewGroup parent, int idOffset, Bundle update) {
        for (int i = 0; i < parent.getChildCount(); ++i) {
            View v = parent.getChildAt(i);
            if (v instanceof ViewGroup)
                applyUpdates((ViewGroup) v, idOffset, update);
            else if (v instanceof TextView) {
                TextView tv = (TextView) v;
                int id = tv.getId();
                Log.d(LOG_TAG, "applyUpdates; id: " + Integer.toHexString(id));
                if (id != View.NO_ID) {
                    id = id -= idOffset;
                    String updateObj = update.getString(Integer.toString(id));
                    if (updateObj != null)
                        tv.setText(updateObj);
                }
            }
        }
    }

    private int idxToIdOffset(int idx) {
        return (idx + 1) * 100;
    }

    private int selectRow(int rowCtr) {
        int rowId = R.id.slot1;
        switch (rowCtr) {
            case 0:
                rowId = R.id.slot1;
                break;

            case 1:
                rowId = R.id.slot2;
                break;

            case 2:
                rowId = R.id.slot3;
                break;

            case 3:
                rowId = R.id.slot4;
                break;
        }
        return rowId;
    }

    private void registerButtonListener(int rowCtr) {
        int parentId = selectRow(rowCtr);
        ViewGroup parentView = (ViewGroup) findViewById(parentId);
        registerButtonListener(parentView, listeners[rowCtr]);
    }

    private void registerButtonListener(ViewGroup parent, View.OnClickListener listener) {
        for (int i = 0; i < parent.getChildCount(); ++i) {
            View v = parent.getChildAt(i);
            if (v instanceof ViewGroup)
                registerButtonListener((ViewGroup) v, listener);
            else if (v instanceof Button) {
                Button b = (Button) v;
                b.setOnClickListener(listener);
            }
        }
    }


    // Captures the EditText states in a row's subview
    private Bundle captureState(int rowCtr) {
        int parentId = selectRow(rowCtr);
        ViewGroup parentView = (ViewGroup) findViewById(parentId);
        Bundle state = new Bundle();
        int idOffset = idxToIdOffset(rowCtr);
        captureState(parentView, idOffset, state);
        return state;
    }

    private void captureState(ViewGroup parent, int idOffset, Bundle state) {
        for (int i = 0; i < parent.getChildCount(); ++i) {
            View v = parent.getChildAt(i);
            if (v instanceof ViewGroup)
                captureState((ViewGroup) v, idOffset, state);
            else if (v instanceof TextView) {
                TextView e = (TextView) v;
                int id = e.getId() - idOffset;
                state.putString(Integer.toString(id), e.getText().toString());
            }
        }
    }
}
