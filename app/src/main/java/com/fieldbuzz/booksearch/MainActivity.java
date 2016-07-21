package com.fieldbuzz.booksearch;

import android.app.Activity;
import android.app.VoiceInteractor;
import android.os.AsyncTask;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fieldbuzz.booksearch.model.Cat;
import com.fieldbuzz.booksearch.model.Dog;
import com.fieldbuzz.booksearch.model.Person;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends Activity {

    public static final String TAG = MainActivity.class.getName();
    private LinearLayout rootLayout = null;

    private Realm realm;
    private RealmConfiguration realmConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootLayout = (LinearLayout) findViewById(R.id.container);
        rootLayout.removeAllViews();
        // These operations are small enough that
        // we can generally safely run them on the UI thread.

        //create the RealmConfiguration
        realmConfiguration = new RealmConfiguration.Builder(this).build();

        // get the instance to start realm
        realm = Realm.getInstance(realmConfiguration);

        basicCRUD();
        basicQuery();
        basicLinkQuery();


        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPostExecute(String s) {
                showStatus(s);
            }

            @Override
            protected String doInBackground(Void... params) {
                String info;
                info = complexReadWrite();
                //  info+=complexQuery();
                return info;
            }
        }.execute();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private String complexReadWrite() {
        String status = "\nPerforming complex Read/Write operation...";

        Realm realm = Realm.getInstance(realmConfiguration);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Dog fido = realm.createObject(Dog.class);
                fido.name = "fido";
                for (int i = 0; i < 10; i++) {
                    Person person = realm.createObject(Person.class);
                    //Cat cat=realm.createObject(Cat.class);
                    person.setId(i);
                    person.setName("Person No. " + i);
                    person.setAge(i);
                    person.setDog(fido);
                    person.setTempReference(42);
                    for (int j = 0; j < i; j++) {
                        Cat cat = realm.createObject(Cat.class);
                        cat.name = "Cat_" + j;
                        person.getCats().add(cat);
                    }

                }

            }
        });

        status += "\nPersons" + realm.where(Person.class).count();

        //Iterate over all projects;

        for (Person person : realm.where(Person.class).findAll()) {
            String dogName;
            if (person.getDog() == null) {
                dogName = "None";
            } else {
                dogName = person.getDog().name;
            }
            status += "\n" + person.getName() + ":" + person.getAge() + ";" + dogName + " : " + person.getCats();
        }

        RealmResults<Person> sortedresults = realm.where(Person.class).findAllSorted("age", Sort.ASCENDING);
        status += "\nSorting " + sortedresults.last().getName() + " == " + realm.where(Person.class).findFirst()
                .getName();
        realm.close();
        return status;
    }

    private void basicQuery() {

        showStatus("\nPerforming basic Query operation...");
        showStatus("Number of Persons " + realm.where(Person.class).count());

        Person person = realm.where(Person.class).equalTo("age", 99).findFirst();

        showStatus("Size: " + person.getName());

    }

    private void basicLinkQuery() {
        showStatus("\nPerforming basic Query operation...");
        showStatus("Number of Persons " + realm.where(Person.class).count());

        RealmResults<Person> results = realm.where(Person.class).equalTo("cats.name", "Tiger").findAll();
        showStatus("Size: " + results.size());


    }

    private void basicCRUD() {
        showStatus("Performing basic Create/Update/Read/Delete Operation...");

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Person person = realm.createObject(Person.class);
                person.setAge(14);
                person.setId(1);
                person.setName("Yusuf");
            }
        });
        final Person person = realm.where(Person.class).findFirst();
        showStatus(person.getName() + ":" + person.getAge());

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                person.setName("Senior");
                person.setAge(99);
                showStatus(person.getName() + " gt older " + person.getAge());
            }
        });
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
//                realm.delete(Person.class);
//                showStatus("Deleted Succesfully");
            }
        });
    }

    private void showStatus(String txt) {

        Log.i(TAG, txt);
        TextView tv = new TextView(this);
        tv.setText(txt);
        rootLayout.addView(tv);
    }
}
