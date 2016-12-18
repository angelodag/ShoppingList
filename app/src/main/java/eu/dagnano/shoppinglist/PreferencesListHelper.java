package eu.dagnano.shoppinglist;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

/**
 * Helper class encapsulating operations both on the arrayAdapter and on the Preferences backing the ListView
 * Created by angelo on 19/11/16.
 */

class PreferencesListHelper {

    private final ArrayAdapter<String> arrayAdapter;
    private final ListView list;
    private final SharedPreferences listPrefs;
    private final SharedPreferences.Editor editor;

    PreferencesListHelper(Context context, ListView list, String name) {
        this.list = list;
        arrayAdapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_multichoice);
        list.setAdapter(arrayAdapter);
        listPrefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        editor = listPrefs.edit();
        Map<String, ?> items = listPrefs.getAll();
        int i = 0;
        for (String item : items.keySet()) {
            boolean aBoolean = listPrefs.getBoolean(item, false);
            arrayAdapter.add(item);
            list.setItemChecked(i, aBoolean);
            i++;
        }
        sortItems();
    }

    SharedPreferences.Editor getPreferencesEditor() {
        return editor;
    }

    String getItem(int position) {
        return arrayAdapter.getItem(position);
    }

    void addNew(String object) {
        if (arrayAdapter.getPosition(object) == -1) {
            arrayAdapter.add(object);
            editor.putBoolean(object, false);
            editor.commit();
        } else {
            Toast.makeText(list.getContext(),
                    object + " " + list.getResources().getString(R.string.alreadyPresent),
                    Toast.LENGTH_LONG).show();
        }
    }

    void updateItem(String item, boolean b) {
        editor.putBoolean(item, b);
        editor.commit();
    }

    void remove(String object) {
        arrayAdapter.remove(object);
        editor.remove(object);
        editor.commit();
    }

    ArrayList<String> getCheckedItems() {
        SparseBooleanArray positions = list.getCheckedItemPositions();
        ArrayList<String> retVal = new ArrayList<>();
        int count = arrayAdapter.getCount();
        for (int i = 0; i < count; i++) {
            if (positions.get(i)) {
                retVal.add(arrayAdapter.getItem(i));
            }
        }
        return retVal;
    }

    void sortItems() {
        arrayAdapter.setNotifyOnChange(false);

        // Save the selected items
        final ArrayList<String> checkedItems = getCheckedItems();

        arrayAdapter.sort(new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });

        // Restore selected items
        for (int i = 0; i < arrayAdapter.getCount(); i++) {
            String item = arrayAdapter.getItem(i);
            list.setItemChecked(i, checkedItems.contains(item));
        }

        arrayAdapter.setNotifyOnChange(true);
        arrayAdapter.notifyDataSetChanged();
    }

    void delesectAll() {
        SparseBooleanArray checkedItemPositions = list.getCheckedItemPositions();
        for (int i = 0; i < checkedItemPositions.size(); i++) {
            if (checkedItemPositions.get(i)) {
                list.setItemChecked(checkedItemPositions.keyAt(i), false);
                editor.putBoolean(arrayAdapter.getItem(i), false);
            }
        }
        editor.commit();
    }
}
