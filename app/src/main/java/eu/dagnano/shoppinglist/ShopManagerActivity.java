package eu.dagnano.shoppinglist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.HashSet;
import java.util.Set;

public class ShopManagerActivity extends AppCompatActivity {

    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_manager);
        Resources resources = getResources();
        getSupportActionBar().setTitle(resources.getString(R.string.app_name) + ": " +
                resources.getString(R.string.manage_shops));

        final ListView list = (ListView) findViewById(R.id.shopListView);
        if (list != null) {
            list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);
            list.setAdapter(arrayAdapter);
            final SharedPreferences shopPrefs = getSharedPreferences(ListBuildActivity.SHOPS, Context.MODE_PRIVATE);
            Set<String> availableShops = shopPrefs.getStringSet(ListBuildActivity.AVAILABLE_SHOPS, new HashSet<String>());
            arrayAdapter.addAll(availableShops);
            String currentShop = shopPrefs.getString(ListBuildActivity.CURRENT_SHOP, getResources().getString(R.string.basicShop));
            list.setSelection(arrayAdapter.getPosition(currentShop));
            list.setItemChecked(arrayAdapter.getPosition(currentShop), true);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String item = arrayAdapter.getItem(position);
                    SharedPreferences.Editor edit = shopPrefs.edit();
                    edit.putString(ListBuildActivity.CURRENT_SHOP, item);
                    edit.commit();
                }
            });

            list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    String item = arrayAdapter.getItem(position);
                    Set<String> availableShops = shopPrefs.getStringSet(ListBuildActivity.AVAILABLE_SHOPS, new HashSet<String>());
                    availableShops.remove(item);
                    arrayAdapter.remove(item);
                    SharedPreferences.Editor edit = shopPrefs.edit();
                    edit.putStringSet(ListBuildActivity.AVAILABLE_SHOPS, availableShops);
                    edit.commit();
                    // Cleanup of the preferences file containing items for the deleted shop
                    // ToDo: delete the file altogether
                    SharedPreferences sharedPreferences = getSharedPreferences(item, Context.MODE_PRIVATE);
                    SharedPreferences.Editor orphanPrefsEditor = sharedPreferences.edit();
                    orphanPrefsEditor.clear();
                    orphanPrefsEditor.commit();
                    return true;
                }
            });


        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.newShop);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    askForNewItem(view);
                }
            });
        }

    }

    private void askForNewItem(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle(getResources().getString(R.string.new_item));

        // Set up the input
        final EditText input = new EditText(view.getContext());
        // Specify the type of input expected
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final SharedPreferences shopPrefs = getSharedPreferences(ListBuildActivity.SHOPS, Context.MODE_PRIVATE);
                Set<String> availableShops = shopPrefs.getStringSet(ListBuildActivity.AVAILABLE_SHOPS, new HashSet<String>());
                String string = input.getText().toString();
                availableShops.add(string);
                arrayAdapter.add(string);
                SharedPreferences.Editor edit = shopPrefs.edit();
                edit.putStringSet(ListBuildActivity.AVAILABLE_SHOPS, availableShops);
                edit.commit();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.create().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        builder.show();
        input.requestFocus();
    }

}
