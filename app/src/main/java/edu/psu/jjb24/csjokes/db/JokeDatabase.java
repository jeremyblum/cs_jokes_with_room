package edu.psu.jjb24.csjokes.db;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

// Note version should be changed whenever database changes to ensure
// that db is recreated.  You can add a migration to say how the database
// should be changed from version to version.
// Note: If you are changing the schema of your database while debugging,
// you will get an error.  Simply uninstall the app on your phone to
// ensure that the database will be deleted, and then recreated with the
// new schema.
@Database(entities = {Joke.class}, version = 1, exportSchema = false)
public abstract class JokeDatabase extends RoomDatabase {
    public interface JokeListener {
        void onJokeReturned(Joke joke);
    }

    public abstract JokeDAO jokeDAO();

    private static JokeDatabase INSTANCE;

    public static JokeDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (JokeDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            JokeDatabase.class, "joke_database")
                            .addCallback(createJokeDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Note this call back will be run
    private static RoomDatabase.Callback createJokeDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            createJokeTable();
        }
    };

    private static void createJokeTable() {
        for (int i = 0; i < DefaultContent.TITLE.length; i++) {
            insert(new Joke(0, DefaultContent.TITLE[i], DefaultContent.SETUP[i], DefaultContent.PUNCHLINE[i], false));
        }
    }

    public static void getJoke(int id, JokeListener listener) {
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                listener.onJokeReturned((Joke) msg.obj);
            }
        };

        (new Thread(() -> {
                Message msg = handler.obtainMessage();
                msg.obj = INSTANCE.jokeDAO().getById(id);
                handler.sendMessage(msg);
            })).start();
    }

    public static void insert(Joke joke) {
        (new Thread(()-> INSTANCE.jokeDAO().insert(joke))).start();
    }

    public static void delete(int jokeId) {
        (new Thread(() -> INSTANCE.jokeDAO().delete(jokeId))).start();
    }


    public static void update(Joke joke) {
        (new Thread(() -> INSTANCE.jokeDAO().update(joke))).start();
    }
}