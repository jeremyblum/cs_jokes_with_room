package edu.psu.jjb24.csjokes.db;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface JokeDAO {
    @Query("SELECT * FROM jokes WHERE liked = :onlyLiked " +
            "ORDER BY title COLLATE NOCASE, rowid")
    LiveData<List<Joke>> getLiked(boolean onlyLiked);

    @Query("SELECT * FROM jokes ORDER BY title COLLATE NOCASE, rowid")
    LiveData<List<Joke>> getAll();

    @Query("SELECT * FROM jokes WHERE rowid = :jokeId")
    Joke getById(int jokeId);

    @Insert
    void insert(Joke... jokes);

    @Update
    void update(Joke... joke);

    @Delete
    void delete(Joke... user);

    @Query("DELETE FROM jokes WHERE rowid = :jokeId")
    void delete(int jokeId);
}
