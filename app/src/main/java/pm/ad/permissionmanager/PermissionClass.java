package pm.ad.permissionmanager;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Process;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.ActivityManager.MOVE_TASK_WITH_HOME;
import static android.os.Process.myPid;
import static pm.ad.permissionmanager.ExecuteAsRootBase.canRunRootCommands;

public class PermissionClass extends Activity
{
    ArrayList<String> AppName = new ArrayList<>();
    ArrayList<PackageInfo> PackageName = new ArrayList<PackageInfo>();
    ListView list;
    String[] dang;
    Button disable;
    List<ActivityManager.RunningAppProcessInfo> activityes;
    ArrayList<Integer> ToKill = new ArrayList<>();
    ArrayList<PackageVal> PackageValList = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dang = getResources().getStringArray(R.array.dangarray); //Tablica stringow z niebiezpiecznymi permisjami na podstawie: https://developer.android.com/guide/topics/permissions/requesting.html#normal-dangerous
        list = (ListView) findViewById(R.id.listView1); //ustawiamy nasza liste aplikacji
        disable = (Button) findViewById(R.id.button4);
        PermissionsReturn();    //uruchamiamy funkcje do wyszukiwania permisji
        PermissionAdapter permissionsAdapter = new PermissionAdapter(this, PackageValList); //obiekt naszego adapteru
        list.setAdapter(permissionsAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() //nacisniecie na element listy
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
               Intent in = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + PackageName.get(i).packageName.toString()));//uruchamiamy aktywnosc prowadzaca do ustawien danej aplikacji
                in.addCategory(Intent.CATEGORY_DEFAULT);
                in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(in);

            }

        });
        disable.setOnClickListener(new View.OnClickListener() { //przycisk wylaczajacy dane procesy
            @Override
            public void onClick(View view)
            {

                killProcesses(comparator()); //funkcja zabijajaca procesy
                System.out.println(activityes.toArray());
                Intent intent = new Intent(getApplicationContext(), SingleItem.class);
                intent.putExtra("tokill", AppName); //wstawiamy nasza liste zabitych aplikacji do nastepnej aktywnosci
                Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
        });
    }
/* Metoda sprawdzająca czy dana aplikacja jest aplikacją systemową. Taką z marszu odrzucamy i uznajemy ją za bezpieczną */
    private static boolean isThisASystemPackage(PackageInfo pkgInfo)
    {
        return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1;
    }


    protected void PermissionsReturn()
    {
        PackageManager pm = this.getPackageManager(); //Tworzymy obiekt Package Manager, który służy nam do zalezienia informacji o zaisntalowanych aplikacjach
        List<PackageInfo> appinstall = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS); //Dzieki fladze GET_PERMISSIONS pobieramy informacje o zezwoleniach w tych packages.
        for (PackageInfo pInfo : appinstall)  //Przechodzimy przez wszystkie aplikacje
        {

            if (!(isThisASystemPackage(pInfo))) //Sprawdzamy, czy jest to aplikacja systemowa
            {
                String[] reqPermission = pInfo.requestedPermissions; //Sciagamy do tablicy wszystkie zazadane przez tą apliakcje zezwolenia

                if (!(reqPermission == (null))) //jesli takowe są
                {
                    PackageVal packageVal = new PackageVal(); //Ustawiamy obiekt pomocniczej klasy
                    PackageName.add(pInfo); //Dodajemy do list packages, ktore pomoga nam do "zabicia" aplikacji

                    for (int i = 0; i < reqPermission.length; i++)  //iterujemy po calej tablicy zezwolen
                    {
                        for (int j = 0; j < dang.length; j++)  //iterujemy po naszej zdefiniowanej listy nebezpiecznych zezwolen
                        {
                            if ((reqPermission[i].compareTo(dang[j])) == 0) ; //jesli ktoraz z permisji jest niebiezpieczna
                            {
                                Log.d("Installed Applications", pInfo.applicationInfo.loadLabel(pm).toString());  //wypisujemy (w celach testowych glownie, mozna usunac)
                                Log.d("packegename", pInfo.packageName.toString());  //jw
                                Log.d("permission list", " " + dang[j]); //jw
                                packageVal.name = pInfo.applicationInfo.loadLabel(pm).toString(); //ustawiamy nazwe pomocniczej klasy
                                packageVal.packageName = pInfo.packageName.toString(); //ustawiamy nazwe paczki
                                packageVal.permissions.add(reqPermission[i]); //dodajemy permisje do ArrayList permisji
                            }
                        }

                    }
                    PackageValList.add(packageVal); //dodajemy nasz pomocniczy obiekt do list pomocniczych obiektow
                }

            }
        }


    }


    protected void killProcesses(ArrayList<Integer> toKill) //metoda do 'zabijania' procesow
    {

        int mypid = myPid(); //ustawiamy pid procesu naszej aplikacji by jej nie zamknac
        for (int iCnt = 0; iCnt < AppName.size(); iCnt++) //przechodzimy po calej liscie
        {

            //android.os.Process.sendSignal(toKill.get(iCnt), Process.SIGNAL_KILL); //wysylamy sygnal zamkniecia apliacji o zadanym pidzie
           // android.os.Process.killProcess(toKill.get(iCnt)); //wylaczamy
            ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(Activity.ACTIVITY_SERVICE);
            manager.killBackgroundProcesses(AppName.get(iCnt)); //zamykamy proces z danego package

        }

    }

    protected ArrayList<Integer> comparator() //funkcja sluzaca do znajdywania tablicy pidow aplikacji, ktore sa uruchomione
    {
        ActivityManager manager =  (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
        activityes = manager.getRunningAppProcesses();
        System.out.println(activityes.size());
        System.out.println(PackageName.size());

        for( int i = 1; i < activityes.size() - 1 ; i++) //przeszukujemy dwie tablice
            for( int j = 0; j < PackageName.size() - 1; j++)
            {

                if( PackageName.get(j).packageName.compareTo((activityes.get(i).processName)) == 0) //jestli aplikacja z niebiezpiecznymi permisjami jest uruchomiona
                {
                    ToKill.add(activityes.get(i).pid); //pobieramy pid i dodajemi do listy ToKill
                    AppName.add(PackageName.get(j).packageName); //dodajemy aby wyswietlic w nastepnej aktynwosci
                }
            }
        return ToKill; //zwracamy
    }
}