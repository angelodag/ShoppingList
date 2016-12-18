package eu.dagnano.shoppinglist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class ListBuildActivity extends AppCompatActivity {

    public static final String SHOPS = "Shops";
    public static final String CURRENT_SHOP = "currentShop";
    public static final String AVAILABLE_SHOPS = "AvailableShops";
    public static final String ITEMS = "items";
    private PreferencesListHelper listHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_build);

        addToolbar();

        String currentListShop = getCurrentListShop();

        loadView(currentListShop);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentListShop = getCurrentListShop();
        loadView(currentListShop);
    }

    private void loadView(String currentListShop) {
        TextView listShopText = (TextView) findViewById(R.id.currentShopLabel);
        if (listShopText != null) {
            String label = getResources().getString(R.string.current_shop) + currentListShop;
            listShopText.setText(label);

            final ListView list = (ListView) findViewById(R.id.listView);
            if (list != null) {
                list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                listHelper = new PreferencesListHelper(this, list, currentListShop);

                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        SparseBooleanArray positions = list.getCheckedItemPositions();
                        listHelper.updateItem(listHelper.getItem(position), positions.get(position));
                    }
                });


                list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        String item = listHelper.getItem(position);
                        listHelper.remove(item);
                        return true;
                    }
                });
            }
        }
    }

    @NonNull
    private String getCurrentListShop() {
        SharedPreferences listShopPref = getSharedPreferences(SHOPS, Context.MODE_PRIVATE);
        String listShop = listShopPref.getString(CURRENT_SHOP, "--");
        if ("--".equals(listShop)) {
            SharedPreferences.Editor edit = listShopPref.edit();
            listShop = getResources().getString(R.string.basicShop);
            edit.putString(CURRENT_SHOP, listShop);
            Set<String> basicSet = new HashSet<>();
            basicSet.add(listShop);
            edit.putStringSet(AVAILABLE_SHOPS, basicSet);
            edit.commit();
        }
        return listShop;
    }


    private void addToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.newItem);
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
                listHelper.addNew(input.getText().toString());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_build, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.shop_mode) {
            Intent intent = new Intent(this, ShopManagerActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_mode) {
            Intent intent = new Intent(this, ActionActivity.class);
            // the putExtra puts additional data in the intent, this data can then be retrieved by the second activity
            intent.putExtra(ITEMS, listHelper.getCheckedItems());
            startActivity(intent);
        }
        if (id == R.id.action_sort) {
            listHelper.sortItems();
        }
        if (id == R.id.action_deselect) {
            listHelper.delesectAll();
        }
/*
        if (id == R.id.action_settings) {
            Toast.makeText(getApplicationContext(), "To Do, options management", Toast.LENGTH_LONG).show();
            return true;
        }
*/
        return super.onOptionsItemSelected(item);
    }
}
