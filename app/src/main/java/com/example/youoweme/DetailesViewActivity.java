package com.example.youoweme;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;

import com.example.youoweme.classes.Person;
import com.example.youoweme.classes.Schuld;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DetailesViewActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, PopupMenu.OnMenuItemClickListener {

    //UI-Elemente
    TextView nameText;
    TextView gesamtschuldText;
    ImageButton neueSchuldButton;
    LinearLayout schuldenList;

    //tools
    private OweDatabase db;
    private DecimalFormat df = new DecimalFormat("0.00");

    //variablen
    private List<Schuld> schulden;
    private Person person;

    //Eine Einzigartige ID für die Zeilen mit den Schulden. Sie wird in der onClick-Methode gebraucht
    int lineId;

    //die Schuld, die zuletzt lange angecklicked wurde, wird in onMenuItemClick() gebraucht
    Schuld longClickedSchuld;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailes_view);

        //initialisiert Variablen
        nameText = findViewById(R.id.nameText);
        gesamtschuldText = findViewById(R.id.gesamtschuldText);
        neueSchuldButton =(ImageButton) findViewById(R.id.neueSchuldButton);
        schuldenList = findViewById(R.id.schuldenList);

        lineId = ViewCompat.generateViewId();
        schulden = new ArrayList<Schuld>();
        db = new OweDatabase(DetailesViewActivity.this);

        //fügt EventListener hinzu
        neueSchuldButton.setOnClickListener(this);

        //etup des UIs
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");

        this.person = db.getPerson(name);
        addSchuldentoList();

        db.close();

        nameText.setText(name);

        setGesamtschuldText();
    }

    @Override
    //hier muss das view final sein, damit auch von annonymen Klassen aus darauf zugegriffen werden kann
    public void onClick(final View view)
    {
        if (view.getId() == neueSchuldButton.getId())
        {
            //erstellt den neue-Schuld-hinzufügen-Dialog

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            //custom Layout hinzufügen
            LayoutInflater inflater = this.getLayoutInflater();
            //eigenes layout für hinzufügen von schulden
            dialog.setView(inflater.inflate(R.layout.neue_schuld_dialog, null));

            dialog.setMessage("Neue Schuld hinzufügen");

            //Abbrechen-Button
            dialog.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            //Akzeptieren-Button
            dialog.setPositiveButton("Hinzufügen", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    EditText wertBox = ((AlertDialog) dialogInterface).findViewById(R.id.wertBox);
                    EditText betreff = ((AlertDialog) dialogInterface).findViewById(R.id.betreff);
                    DatePicker date = ((AlertDialog) dialogInterface).findViewById(R.id.date);

                    String wert = wertBox.getText().toString();
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(date.getYear(),date.getMonth(),date.getDayOfMonth());
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

                    //überprüft ob ein Wert eingegeben wurde, und setzt die schuld augf 0, wenn das nicht der Fall ist
                    wert = wert.length() > 0 ? wert : "0";


                    String datum = sdf.format(calendar.getTime());

                    String bet = betreff.getText().toString();

                    Schuld s = new Schuld(Double.parseDouble(wert),bet,datum,person.getID());

                    db.addSchuld(s, person);
                    db.close();

                    //update the ui
                    schuldenList.removeAllViews();
                    addSchuldentoList();

                    setGesamtschuldText();
                }
            });

            dialog.create();
            dialog.show();
        }
        else if(view.getId() == lineId)
        {
            //öffnet die DetailAnsicht für die Schulden

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

            //custom Layout hinzufügen
            LayoutInflater inflater = this.getLayoutInflater();
            //eigenes layout für hinzufügen von schulden
            dialogBuilder.setView(inflater.inflate(R.layout.schuld_detail, null));


            final AlertDialog dialog = dialogBuilder.create();

            //fügt die richtigen Texte dynamisch ein
            dialog.setOnShowListener( new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface)
                {
                    OweDatabase db = new OweDatabase(DetailesViewActivity.this);

                    TextView top =  dialog.findViewById(R.id.upperText);
                    TextView date =  dialog.findViewById(R.id.dateText);
                    TextView value =  dialog.findViewById(R.id.valueText);
                    TextView reason =  dialog.findViewById(R.id.reasonText);

                    //der Index des LinearLayouts, in dem die Schuld dargestellt wird, ist auch der Index der Schuld in der Schulden-Liste
                    int index = schuldenList.indexOfChild(view);
                    Schuld detailSchuld = schulden.get(index);

                    if(detailSchuld.getSchuldenbetrag() >= 0)
                    {
                        top.setText("hast du " + person.getName());
                        value.setText("" + df.format(detailSchuld.getSchuldenbetrag()) + "€");
                        value.setTextColor(Color.parseColor("#00aa00"));
                    }
                    else
                    {
                        top.setText("hat dir " + person.getName());
                        value.setText("" + df.format(detailSchuld.getSchuldenbetrag() * -1) + "€");
                        value.setTextColor(Color.parseColor("#aa0000"));
                    }

                    date.setText(detailSchuld.getDatum());

                    reason.setText(detailSchuld.getBetreff());
                }
            });

            dialog.show();
        }
    }

    @Override
    public boolean onLongClick(View view)
    {
        PopupMenu meinPopup = new PopupMenu(this, view);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.schuld_popup, meinPopup.getMenu());

        meinPopup.setOnMenuItemClickListener(this);
        meinPopup.show();

        //der Index des LinearLayouts, in dem die Schuld dargestellt wird, ist auch der Index der Schuld in der Schulden-Liste
        int index = schuldenList.indexOfChild(view);

        longClickedSchuld = schulden.get(index);

        return false;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem)
    {
        //öffnet den Löschen-Dialog

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setMessage("Diese Schuld löschen?");

        //Abbrechen-Button
        dialog.setNeutralButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        //Akzeptieren/Löschen-Button
        dialog.setPositiveButton("Löschen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                db.deleteSchuld(longClickedSchuld);

                db.close();

                //update the ui
                schuldenList.removeAllViews();
                List<Person> pers = db.getAllPerson();
                addSchuldentoList();



                setGesamtschuldText();

                Toast.makeText(DetailesViewActivity.this, "Schuld gelöscht", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.create();
        dialog.show();

        return false;
    }

    private void addSchuldentoList()
    {
        //Updated / initialisiert die SchuldenListe,
        schulden = db.getallSchulden(person);
        db.close();

        //erstellt für jede Schuld in 'schulden' eine neue, anklickbare Zeile mit dem jeweiligen Datum und Schuldenwert
        for (int i = 0; i < schulden.size(); i++)
        {
            LinearLayout newLine = new LinearLayout(this);

            //fügt das Datum hinzu
            TextView nameText = new TextView(this);
            nameText.setText(schulden.get(i).getDatum());
            nameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            nameText.setPadding(20, 20, 20, 20);

            //mit LayoutParams kann das Layout näher festgelegt werden. Der letzte Paremeter gibt an wie viel Platz dieses View im vergleich zu den Anderen Verbraucht
            LinearLayout.LayoutParams nameTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);
            nameText.setLayoutParams(nameTextParams);

            newLine.addView(nameText);


            //fügt den Schuldenwert hinzu
            TextView deptText = new TextView(this);
            deptText.setText("" + df.format(schulden.get(i).getSchuldenbetrag()) + "€");
            deptText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            deptText.setPadding(20, 20, 20, 20);

            if (schulden.get(i).getSchuldenbetrag() < 0)
                deptText.setTextColor(Color.parseColor("#aa0000"));

            //mit LayoutParams kann das Layout näher festgelegt werden. Der letzte Paremeter gibt an wie viel Platz dieses View im vergleich zu den Anderen Verbraucht
            LinearLayout.LayoutParams deptTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 2);
            deptText.setLayoutParams(deptTextParams);

            newLine.addView(deptText);

            //damit die Zeile angeklickt werden kann und die onClick/onLongClick-Methode sie erkennt (/weiß was angeklickt wurde)
            newLine.setOnClickListener(this);
            newLine.setOnLongClickListener(this);
            newLine.setId(lineId);

            //lässt sich die Hintergrundfarben der Zeilen abwechseln, damit sie besser erkannt werden können
            if (i % 2 == 0)
                newLine.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.subtileBackgroundLight, null));
            else
                newLine.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.subtileBackgroundDark, null));

            //fügt die neue Zeile zu dem UI hinzu
            schuldenList.addView(newLine);

            //updated das UI
            setGesamtschuldText();
        }
    }

    private void setGesamtschuldText()
    {
        double gesamtschuld = getSchuldenGesamt(person);

        if (gesamtschuld > 0) {
            gesamtschuldText.setText(person.getName() + " schuldet dir: " + df.format(gesamtschuld) + "€");
            gesamtschuldText.setTextColor(Color.parseColor("#009900"));
        } else if (gesamtschuld < 0) {
            gesamtschuldText.setText("Du schuldest " + person.getName() + ": " + df.format(gesamtschuld * -1) + "€");
            gesamtschuldText.setTextColor(Color.parseColor("#aa0000"));
        } else {
            gesamtschuldText.setText("Ihr seid quitt.");
            gesamtschuldText.setTextColor(Color.parseColor("#000000"));
        }
    }

    private double getSchuldenGesamt(Person person)
    {
        OweDatabase db = new OweDatabase(DetailesViewActivity.this);
        List<Schuld> schuldList = db.getallSchulden(person);

        double erg=0;
        for (int i = 0; i <schuldList.size() ; i++) {
           erg+= schuldList.get(i).getSchuldenbetrag();
        }

        return erg;
    }
}