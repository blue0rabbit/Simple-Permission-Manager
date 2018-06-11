package pm.ad.permissionmanager;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by blue rabbit on 2018-01-28.
 */

public class SingleItem extends Activity //Klasa sluzy do wyswietlenia wylaczonych procesow
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_item);
        TextView app = (TextView)findViewById(R.id.apps);
        String nm = getIntent().getExtras().get("tokill").toString();
        app.setText(nm);

    }

}
