package edu.psu.jjb24.csjokes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import edu.psu.jjb24.csjokes.db.Joke;
import edu.psu.jjb24.csjokes.db.JokeDatabase;
import edu.psu.jjb24.csjokes.db.JokeViewModel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends AppCompatActivity  {

    private boolean filtered = false;  // Are results filtered by likess
    private JokeViewModel jokeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            filtered = savedInstanceState.getBoolean("filtered");
        }

        // Set the action bar
        setSupportActionBar(findViewById(R.id.toolbar));

        RecyclerView recyclerView = findViewById(R.id.lstJokes);
        JokeListAdapter adapter = new JokeListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        jokeViewModel = new ViewModelProvider(this).get(JokeViewModel.class);
        jokeViewModel.filterJokes(filtered);

        //jokeViewModel.getAllJokes().observe(this, new Observer<List<Joke>>() {
        //    public void onChanged(@Nullable final List<Joke> jokes) {
        //        // Update the cached copy of the words in the adapter.
        //        adapter.setJokes(jokes);
        //    }
        //});
        // OR As a lambda expression:
        //jokeViewModel.getAllJokes().observe(this, jokes -> {adapter.setJokes(jokes);});
        // As a function reference
        // ContainingClass::staticMethodName
        // containingObject::instanceMethodName
        jokeViewModel.getAllJokes().observe(this, adapter::setJokes);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);

        if (filtered) {
            menu.getItem(1).setIcon(R.drawable.ic_thumbs_up_down);
        } else {
            menu.getItem(1).setIcon(R.drawable.ic_thumb_up);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                startActivity(new Intent(this, AddActivity.class));
                return true;
            case R.id.menu_filter:
                filtered = !filtered;
                if (filtered) {
                    item.setIcon(R.drawable.ic_thumbs_up_down);
                } else {
                    item.setIcon(R.drawable.ic_thumb_up);
                }
                RecyclerView recyclerView = findViewById(R.id.lstJokes);
                JokeListAdapter adapter = new JokeListAdapter(this);
                recyclerView.setAdapter(adapter);
                jokeViewModel = new ViewModelProvider(this).get(JokeViewModel.class);
                jokeViewModel.filterJokes(filtered);

                jokeViewModel.getAllJokes().observe(this, adapter::setJokes);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("filtered", filtered);
    }

    public void displaySetup(int id) {
        JokeDatabase.getJoke(id, joke -> {
            Bundle args = new Bundle();
            args.putInt("joke_id", joke.id);
            args.putString("title", joke.title);
            args.putString("setup", joke.setup);
            args.putString("punchline", joke.punchline);

            DisplaySetupDialog setupDialog = new DisplaySetupDialog();
            setupDialog.setArguments(args);
            setupDialog.show(getSupportFragmentManager(), "setupDialog");
        });
    }

    public void displayPunchline(int id) {
        JokeDatabase.getJoke(id, joke -> {
            Bundle args = new Bundle();
            args.putInt("joke_id", joke.id);
            args.putString("title", joke.title);
            args.putString("setup", joke.setup);
            args.putString("punchline", joke.punchline);

            DisplayPunchlineDialog punchlineDialog = new DisplayPunchlineDialog();
            punchlineDialog.setArguments(args);
            punchlineDialog.show(getSupportFragmentManager(), "punchlineDialog");
        });
    }


    // Notes: This can be an outer class or a static nested class. We will make an inner class
    // since it is only used in the MainActivity _and_ we would like to simplify communication
    // with the activity
    public class JokeListAdapter extends RecyclerView.Adapter<JokeListAdapter.JokeViewHolder> {
        // If the JokeListAdapter were an outer class, the JokeViewHolder could be
        // a static class.  We want to be able to get access to the MainActivity instance,
        // so we want it to be an inner class
        class JokeViewHolder extends RecyclerView.ViewHolder {
            private final TextView titleView;
            private final ImageView likedView;
            private Joke joke;

            // Note that this view holder will be used for different items -
            // The callbacks though will use the currently stored item
            private JokeViewHolder(View itemView) {
                super(itemView);
                titleView = itemView.findViewById(R.id.txtTitle);
                likedView = itemView.findViewById(R.id.imgLiked);

                itemView.setOnLongClickListener(view -> {
                    // Note that we need a reference to the MainActivity instance
                    Intent intent = new Intent(MainActivity.this, AddActivity.class);
                    // Note getItemId will return the database identifier
                    intent.putExtra("joke_id", joke.id);
                    // Note that we are calling a method of the MainActivity object
                    startActivity(intent);
                    return true;
                });

                itemView.setOnClickListener(view -> displaySetup(joke.id));

                likedView.setOnClickListener(view -> {
                    joke.liked = !joke.liked;
                    if (joke.liked) {
                        likedView.setImageResource(R.drawable.ic_thumb_up);
                    }
                    else {
                        likedView.setImageResource(R.drawable.ic_thumb_down);
                    }
                    JokeDatabase.update(joke);
                });
            }
        }

        private final LayoutInflater layoutInflater;
        private List<Joke> jokes; // Cached copy of jokes

        JokeListAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public JokeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.list_item, parent, false);
            return new JokeViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(JokeViewHolder holder, int position) {
            if (jokes != null) {
                Joke current = jokes.get(position);
                holder.joke = current;
                holder.titleView.setText(current.title);
                if (current.liked) {
                    holder.likedView.setImageResource(R.drawable.ic_thumb_up);
                }
                else {
                    holder.likedView.setImageResource(R.drawable.ic_thumb_down);
                }
            } else {
                // Covers the case of data not being ready yet.
                holder.titleView.setText("...intializing...");
                holder.likedView.setImageResource(R.drawable.ic_thumb_down);
                holder.likedView.setTag("N");
            }
        }


        void setJokes(List<Joke> jokes){
            this.jokes = jokes;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            if (jokes != null)
                return jokes.size();
            else return 0;
        }


    }


    public static class DisplaySetupDialog extends DialogFragment {
        int joke_id;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            joke_id = getArguments().getInt("joke_id");
            final String title = getArguments().getString("title");
            final String setup = getArguments().getString("setup");
            builder.setTitle(title)
                    .setMessage(setup)
                    .setPositiveButton("Punchline",
                            (dialog, id) -> ((MainActivity) getActivity()).displayPunchline(joke_id))
                    .setNegativeButton("Cancel",
                            (dialog, id) -> {});
            return builder.create();
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString("JJB", "tester");
        }
    }

    public static class DisplayPunchlineDialog extends DialogFragment {
        int joke_id;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            joke_id = getArguments().getInt("joke_id");
            String title = getArguments().getString("title");
            String punchline = getArguments().getString("punchline");

            builder.setTitle(title)
                    .setMessage(punchline)
                    .setPositiveButton("Liked", (dialog, id) -> {
                        JokeDatabase.getJoke(joke_id, joke -> {
                            joke.liked = true;
                            JokeDatabase.update(joke);
                        });
                    })
                    .setNeutralButton("Setup", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ((MainActivity) getActivity()).displaySetup(joke_id);
                        }
                    })
                    .setNegativeButton("Disiked", (dialog, id) -> {
                        JokeDatabase.getJoke(joke_id, joke -> {
                            joke.liked = false;
                            JokeDatabase.update(joke);
                        });
                    });

            return builder.create();
        }
    }

}