package edu.psu.jjb24.csjokes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import edu.psu.jjb24.csjokes.db.Joke;
import edu.psu.jjb24.csjokes.db.JokeDatabase;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class AddActivity extends AppCompatActivity {
    private int joke_id;
    private boolean liked;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        setSupportActionBar(findViewById(R.id.my_toolbar));

        joke_id = getIntent().getIntExtra("joke_id", -1);

        // Note: that we do not want to lose the state if the activity is being
        // recreated
        if (savedInstanceState == null) {
            if (joke_id != -1) {
                JokeDatabase.getJoke(joke_id, joke -> {
                    ((EditText) findViewById(R.id.txtEditTitle)).setText(joke.title);
                    ((EditText) findViewById(R.id.txtEditSetup)).setText(joke.setup);
                    ((EditText) findViewById(R.id.txtEditPunchline)).setText(joke.punchline);
                    liked = joke.liked;
                });
            }
        }
        else {
            liked = savedInstanceState.getBoolean("liked");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_add, menu);
        if (joke_id == -1) {
            menu.getItem(1).setIcon(R.drawable.ic_cancel);
            menu.getItem(1).setTitle(R.string.menu_cancel);
            setTitle("Add joke");
        }
        else {
            setTitle("Edit joke");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                updateDatabase();
                return true;
            case R.id.menu_delete:
                if (joke_id != -1) {
                    ConfirmDeleteDialog confirmDialog = new ConfirmDeleteDialog();
                    confirmDialog.show(getSupportFragmentManager(), "deletionConfirmation");
                }
                else {
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateDatabase() {
        Joke joke = new Joke(joke_id == -1?0:joke_id,
                ((EditText) findViewById(R.id.txtEditTitle)).getText().toString(),
                ((EditText) findViewById(R.id.txtEditSetup)).getText().toString(),
                ((EditText) findViewById(R.id.txtEditPunchline)).getText().toString(),
                liked);
        if (joke_id == -1) {
            JokeDatabase.insert(joke);
        } else {
            JokeDatabase.update(joke);
        }
        finish(); // Quit activity
    }

    public void deleteRecord() {
        JokeDatabase.delete(joke_id);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("liked", liked);
    }

    public static class ConfirmDeleteDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle("Delete the joke?")
                    .setMessage("You will not be able to undo the deletion!")
                    .setPositiveButton("Delete",
                            (dialog,id) -> {
                                ((AddActivity) getActivity()).deleteRecord();
                                getActivity().finish();
                            })
                    .setNegativeButton("Return to joke list",
                            (dialog, id) -> getActivity().finish());
            return builder.create();
        }
    }

}
