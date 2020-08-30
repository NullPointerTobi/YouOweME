package com.example.youoweme;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;

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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.youoweme.classes.Person;
import com.example.youoweme.classes.Schuld;

import java.text.DecimalFormat;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, PopupMenu.OnMenuItemClickListener {
////
    ImageButton addButton;
    LinearLayout personList;

    //die Person, die zuletzt lange angecklicked wurde, wird in onMenuItemClick() gebraucht
    private Person longClickedPerson;

    //Eine Einzigartige ID für die Zeilen mit den Personennamen. Sie wird in der onClick-Methode gebraucht
    int lineId;

    //Tools
    private DecimalFormat df = new DecimalFormat("0.00");
    OweDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialisiert Variablen
        addButton = findViewById(R.id.addButton);
        personList = findViewById(R.id.personList);

        lineId = ViewCompat.generateViewId();

        //fügt EventListener hinzu
        addButton.setOnClickListener(this);

        //initialisiert das UI

        db = new OweDatabase(MainActivity.this);
        List<Person> pers = db.getAllPerson();
        db.close();

        addPeopletolist(pers);
    }

    @Override
    public void onResume() {
        super.onResume();

        //update the ui
        personList.removeAllViews();
        addPeopletolist(db.getAllPerson());
        db.close();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == addButton.getId()) {
            newPersonDialog();
        } else if (view.getId() == lineId) {
            //öffnet die Detailansicht
            Intent intent = new Intent(this, DetailesViewActivity.class);

            //das erste Child jeder Zeile mit der lineId ist ein textView, das den Namen der Person enthält
            String name = ((TextView) ((LinearLayout) view).getChildAt(0)).getText().toString();

            intent.putExtra("name", name);
            startActivity(intent);
        }
    }

    @Override
    public boolean onLongClick(View view) {

        String clickedPersonName = ((TextView) ((LinearLayout) view).getChildAt(0)).getText().toString();
        longClickedPerson = db.getPerson(clickedPersonName);
        db.close();

        /*
            Erstellt ein PopUp-Menü mit den Optionen 'Bearbeiten' und 'Löschen' der Person (aus einer xml-Vorlage).
            Nur die Personen haben einen OnLongClick-Listener, deshalb ist es nicht nötog die IDs zu vergleichen (wie in OnClick()).
         */

        PopupMenu popup = new PopupMenu(this, view);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.person_popup, popup.getMenu());

        popup.setOnMenuItemClickListener(this);
        popup.show();

        //sagt onClick, dass es nicht mehr aufgerufen werden soll
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.edit) {
            editPersonDialog();
        } else if (menuItem.getItemId() == R.id.delete) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setMessage("'" + longClickedPerson.getName() + "' mit allen zugehörigen Schulden löschen?");

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
                    //löscht die Person as der Datenbank
                    db.deletePerson(longClickedPerson);

                    //updated das UI
                    personList.removeAllViews();
                    addPeopletolist(db.getAllPerson());

                    db.close();
                }
            });

            dialog.create();
            dialog.show();
        }

        return false;
    }

    private void addPeopletolist(List<Person> person) {
        //erstellt für jeden Namen in 'names' eine neue, anklickbare Zeile mit dem jeweiligen Namen und dem Schuldenwert
        for (int i = 0; i < person.size(); i++) {
            LinearLayout newLine = new LinearLayout(this);

            //fügt den Namen hinzu
            TextView nameText = new TextView(this);
            nameText.setText(person.get(i).getName());
            nameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            nameText.setPadding(20, 20, 20, 20);

            //mit LayoutParams kann das Layout näher festgelegt werden. Der letzte Paremeter gibt an wie viel Platz dieses View im vergleich zu den Anderen Verbraucht
            LinearLayout.LayoutParams nameTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.5f);
            nameText.setLayoutParams(nameTextParams);

            newLine.addView(nameText);


            //fügt den Schuldenwert hinzu
            TextView deptText = new TextView(this);
            deptText.setText("" + df.format(getSchuldenGesamt(person.get(i))) + "€");

            deptText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            deptText.setPadding(20, 20, 10, 20);


            if (person.get(i).getBetrag() < 0)
                deptText.setTextColor(Color.parseColor("#aa0000"));

            //mit LayoutParams kann das Layout näher festgelegt werden. Der letzte Paremeter gibt an wie viel Platz dieses View im vergleich zu den Anderen Verbraucht
            LinearLayout.LayoutParams deptTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);
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
            personList.addView(newLine);
        }
    }

    private void newPersonDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        //custom Layout hinzufügen
        LayoutInflater inflater = this.getLayoutInflater();
        dialog.setView(inflater.inflate(R.layout.neue_person_dialog, null));

        dialog.setMessage("Neue Person hinzufügen");


        //Abbrechen-Button
        dialog.setNeutralButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        //Akzeptieren-Button
        dialog.setPositiveButton("Akzeptieren", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText wertbox = ((AlertDialog) dialogInterface).findViewById(R.id.namebox);
                Person abfrage = new Person("");
                String name = wertbox.getText().toString();
                try {
                    abfrage = db.getPerson(name);
                } catch (Exception ex) {

                }
                if (abfrage.getName().equals("")) {
                    Person person = new Person(name, 0.0);
                    Toast.makeText(MainActivity.this, "Hinzugefügt", Toast.LENGTH_SHORT).show();
                    db.insertPerson(person);

                    //update the ui
                    personList.removeAllViews();
                    List<Person> pers = db.getAllPerson();
                    addPeopletolist(pers);

                    db.close();
                }

                else {
                    Toast.makeText(MainActivity.this, "Bereits vorhanden", Toast.LENGTH_LONG).show();
                }



            }
        });

        dialog.create();
        dialog.show();
    }

    private void editPersonDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        //custom Layout hinzufügen
        LayoutInflater inflater = this.getLayoutInflater();
        dialog.setView(inflater.inflate(R.layout.edit_person_dialog, null));

        dialog.setMessage("Person bearbeiten");

        //Abbrechen-Button
        dialog.setNeutralButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        //Akzeptieren-Button
        dialog.setPositiveButton("Akzeptieren", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText nameTextbox = ((AlertDialog) dialogInterface).findViewById(R.id.textbox);
                String name = nameTextbox.getText().toString();
                Person abfrage = new Person("");
                try {
                    abfrage = db.getPerson(name);
                }catch (Exception ex){

                }
                if (abfrage.getName().equals("")){
                    longClickedPerson.setName(name);
                    db.updatePerson(longClickedPerson);

                    //update the ui
                    personList.removeAllViews();
                    List<Person> pers = db.getAllPerson();
                    addPeopletolist(pers);

                    db.close();
                }
                else {
                    Toast.makeText(MainActivity.this, "Bereits vorhanden", Toast.LENGTH_LONG).show();

                }




            }
        });

        dialog.create();
        dialog.show();

    }

    private double getSchuldenGesamt(Person person) {
        List<Schuld> test = db.getallSchulden(person);
        db.close();

        double erg = 0;
        for (int i = 0; i < test.size(); i++) {
            erg += test.get(i).getSchuldenbetrag();

        }
        person.setBetrag(erg);

        return erg;
    }


}


