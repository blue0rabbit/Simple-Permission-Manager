package pm.ad.permissionmanager;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;


/**
 * Created by blue rabbit on 2018-01-27.
 */

public class PermissionAdapter extends ArrayAdapter<PackageVal> //Adapter sluzy do stworzenia naszej listy
{
    private Activity context ;
    private ArrayList<PackageVal> appinstall;

    public PermissionAdapter(Activity context, ArrayList<PackageVal> appinstall) //Konstruktor
    {
        super(context, R.layout.row, appinstall);
        this.context = context;
        //this.permissions = permissions;
        this.appinstall = appinstall;

    }
    public View getView(int position, View convertView, ViewGroup parent) //Funkcja ustawiajaca pojedynczy element
    {
        LayoutInflater layoutInflater = context.getLayoutInflater();
        View rowView = layoutInflater.inflate(R.layout.row, null, true);
        TextView title = (TextView) rowView.findViewById(R.id.Title);
        TextView row = (TextView) rowView.findViewById(R.id.Row);
        title.setText(appinstall.get(position).name); //Nazwa aplikacji
        row.setText((appinstall.get(position).permissions.get(1) + "...")); //Przykladowa permisja
        return rowView;
    }


}

