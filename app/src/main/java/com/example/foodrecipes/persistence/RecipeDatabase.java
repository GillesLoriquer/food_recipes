package com.example.foodrecipes.persistence;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.foodrecipes.AppExecutors;
import com.example.foodrecipes.BuildConfig;
import com.example.foodrecipes.model.Recipe;

@Database(entities = {Recipe.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class RecipeDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "recipes_db";

    private static RecipeDatabase instance;

    public static RecipeDatabase getInstance(final Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    RecipeDatabase.class,
                    DATABASE_NAME
            ).build();
        }
        // TODO : To remove one development over
        if (BuildConfig.DEBUG) {
            AppExecutors.getInstance().getDiskIO().execute(() -> instance.clearAllTables());
        }
        return instance;
    }

    public abstract RecipeDao getRecipeDao();
}