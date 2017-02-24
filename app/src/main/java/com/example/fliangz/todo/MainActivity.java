package com.example.fliangz.todo;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.app.AlertDialog.Builder;
import android.app.AlertDialog;


import com.myscript.atk.scw.SingleCharWidget;
import com.myscript.atk.scw.SingleCharWidgetApi;
import com.myscript.atk.core.ui.IStroker;
import com.myscript.atk.scw.SingleCharWidgetApi;
import com.myscript.atk.text.CandidateInfo;
import com.example.fliangz.todo.MyCertificate;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;


public class MainActivity extends AppCompatActivity implements
        SingleCharWidgetApi.OnConfiguredListener,
        SingleCharWidgetApi.OnTextChangedListener {

    private static final String TAG = "SingleCharDemo";

    private SingleCharWidgetApi widget;

    private ArrayList<String> items;
    private ArrayAdapter<String> itemsAdapter;
    private ListView lvItems;

    private void readItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");
        try {
            items = new ArrayList<String>(FileUtils.readLines(todoFile));
        } catch (IOException e) {
            items = new ArrayList<String>();
        }
    }

    private void writeItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");
        try {
            FileUtils.writeLines(todoFile, items);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // ADD HERE
        lvItems = (ListView) findViewById(R.id.lvItems);
        readItems();
        itemsAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items);
        lvItems.setAdapter(itemsAdapter);
        setupListViewListener();

        widget = (SingleCharWidget) findViewById(R.id.singleChar_widget);
        if (!widget.registerCertificate(com.example.fliangz.todo.MyCertificate.getBytes()))
        {
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
            dlgAlert.setMessage("Please use a valid certificate.");
            dlgAlert.setTitle("Invalid certificate");
            dlgAlert.setCancelable(false);
            dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    //dismiss the dialog
                }
            });
            dlgAlert.create().show();
            return;
        }

        widget.setOnConfiguredListener(this);
        widget.setOnTextChangedListener(this);

        // references assets directly from the APK to avoid extraction in application
        // file system
        widget.addSearchDir("zip://" + getPackageCodePath() + "!/assets/conf");

        // The configuration is an asynchronous operation. Callbacks are provided to
        // monitor the beginning and end of the configuration process and update the UI
        // of the input method accordingly.
        //
        // "en_US" references the en_US bundle name in conf/en_US.conf file in your assets.
        // "si_text" references the configuration name in en_US.conf
        widget.configure("en_US", "si_text");

    }

    @Override
    protected void onDestroy()
    {
        widget.setOnTextChangedListener(null);
        widget.setOnConfiguredListener(null);

        super.onDestroy();
    }

    @Override
    public void onConfigured(SingleCharWidgetApi widget, boolean success)
    {
        if(!success)
        {
            Toast.makeText(getApplicationContext(), widget.getErrorString(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Unable to configure the Single Char Widget: " + widget.getErrorString());
            return;
        }
        Toast.makeText(getApplicationContext(), "Single Char Widget Configured", Toast.LENGTH_SHORT).show();
        if(BuildConfig.DEBUG)
            Log.d(TAG, "Single Char Widget configured!");
    }

    @Override
    public void onTextChanged(SingleCharWidgetApi widget, String s, boolean intermediate)
    {
        Toast.makeText(getApplicationContext(), "Recognition update", Toast.LENGTH_SHORT).show();
        if(BuildConfig.DEBUG)
        {
            Log.d(TAG, "Single Char Widget recognition: " + widget.getText());
        }
    }

    private void setupListViewListener() {
        lvItems.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapter,
                                                   View item, int pos, long id) {
                        items.remove(pos);
                        itemsAdapter.notifyDataSetChanged();
                        writeItems(); // <---- Add this line
                        return true;
                    }

                });

    }

    public void onAddItem(View v) {
        EditText etNewItem = (EditText) findViewById(R.id.etNewItem);
        String itemText = etNewItem.getText().toString();
        itemsAdapter.add(itemText);
        etNewItem.setText("");
        writeItems(); // <---- Add this line
    }

}
