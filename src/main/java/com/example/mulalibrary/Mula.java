package com.example.mulalibrary;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class Mula extends AppCompatActivity {
    Context context;
    public String TAG = Mula.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mula);
    }

    public Mula() {

    }


    public void configMula(Context context, String phoneNumber) {
        Bundle bundle = new Bundle();
        bundle.putString("PHONE", phoneNumber);
        navigate(context, bundle);


    }

    private void navigate(Context context, Bundle bundle) {
        boolean isPluginInstalleed = isPackageInstalled("com.plugin.consumerapp", context);
        if (isPluginInstalleed) {
            Intent intent = new Intent(context, Main.class);
            intent.putExtras(bundle);
            context.startActivity(intent);
        } else {
            final String appPackageName = "com.cellulant.consumerapp"; // getPackageName() from Context or Activity object
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }
    }

    public static boolean isPackageInstalled(String packageName, Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            return packageManager.getApplicationInfo(packageName, 0).enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
