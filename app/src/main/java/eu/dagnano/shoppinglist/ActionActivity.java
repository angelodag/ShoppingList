package eu.dagnano.shoppinglist;

import android.content.Intent;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class ActionActivity extends AppCompatActivity {

    public static final String REMOVED_ITEMS = "removedItems";
    private final Stack<String> removedItems = new Stack<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);
        final Resources resources = getResources();
        getSupportActionBar().setTitle(resources.getString(R.string.app_name) + ": " +
                resources.getString(R.string.action_mode));

        final ListView listView = (ListView) findViewById(R.id.actionItems);
        if (listView != null) {
            // Defined Array values to show in ListView
            Intent intent = getIntent();
            ArrayList<String> items = intent.getStringArrayListExtra(ListBuildActivity.ITEMS);

            adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, items);

            listView.setAdapter(adapter);

            final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.undo);
            if (fab != null) {
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!removedItems.isEmpty()) {
                            adapter.add(removedItems.pop());
                            fab.setVisibility(removedItems.isEmpty() ? View.GONE : View.VISIBLE);
                        }
                    }
                });

                fab.setVisibility(removedItems.isEmpty() ? View.GONE : View.VISIBLE);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String item = adapter.getItem(position);
                        adapter.remove(item);
                        removedItems.push(item);
                        fab.setVisibility(removedItems.isEmpty() ? View.GONE : View.VISIBLE);
                        if (adapter.getCount() == 0) {
                            notifyCompletion(view);
                        }
                    }
                });
            }
        }
    }

    private void notifyCompletion(View view) {
        Toast.makeText(view.getContext(),
                getResources().getString(R.string.congratulations),
                Toast.LENGTH_LONG).show();
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArray(REMOVED_ITEMS, removedItems.toArray(new String[removedItems.size()]));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        String[] removedItemsArray = savedInstanceState.getStringArray(REMOVED_ITEMS);
        if (removedItemsArray != null) {
            removedItems.addAll(Arrays.asList(removedItemsArray));
            final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.undo);
            if (fab != null) {
                fab.setVisibility(removedItems.isEmpty() ? View.GONE : View.VISIBLE);
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
}
