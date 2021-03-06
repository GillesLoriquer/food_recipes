package com.example.foodrecipes;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.foodrecipes.model.Recipe;
import com.example.foodrecipes.viewmodel.RecipeViewModel;
import com.example.foodrecipes.viewmodel.RecipeViewModelFactory;

public class RecipeActivity extends BaseActivity {

    private static final String TAG = "RecipeActivity";

    public static final String RECIPE = "RECIPE";

    private RecipeViewModel mRecipeViewModel;

    // UI components
    private AppCompatImageView mRecipeImage;
    private TextView mRecipeTitle, mRecipeRank;
    private LinearLayout mRecipeIngredientsContainer;
    private ScrollView mScrollView;         // Needed to set visibility to VISIBLE at some point

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        mRecipeImage = findViewById(R.id.recipe_image);
        mRecipeTitle = findViewById(R.id.recipe_title);
        mRecipeRank = findViewById(R.id.recipe_social_score);
        mRecipeIngredientsContainer = findViewById(R.id.ingredients_container);
        mScrollView = findViewById(R.id.parent_scrollview);

        mRecipeViewModel = new ViewModelProvider(
                this,
                new RecipeViewModelFactory(this.getApplication())).get(RecipeViewModel.class);

        getIncomingIntent();
    }

    private void getIncomingIntent() {
        if (getIntent().hasExtra(RECIPE)) {
            Recipe recipe = getIntent().getParcelableExtra(RECIPE);
            Log.d(TAG, "getIncomingIntent: " + recipe.getTitle());
            subscribeObservers(recipe.getRecipeId());
        }
    }

    private void subscribeObservers(final String recipeId) {
        mRecipeViewModel.searchRecipeApi(recipeId).observe(this, recipeResource -> {
            if (recipeResource != null) {
                if (recipeResource.data != null) {
                    switch (recipeResource.status) {
                        case LOADING: {
                            showProgressBar(true);
                            break;
                        }
                        case ERROR: {
                            Log.d(TAG, "subscribeObservers: status : ERROR, recipe: " + recipeResource.data.getTitle());
                            Log.d(TAG, "subscribeObservers: ERROR message: " + recipeResource.data.getTitle());
                            setRecipeProperties(recipeResource.data);
                            showParent();
                            showProgressBar(false);
                            break;
                        }
                        case SUCCESS: {
                            Log.d(TAG, "subscribeObservers: cache has been refreshed.");
                            Log.d(TAG, "subscribeObservers: SUCCESS, recipe: " + recipeResource.data.getTitle());
                            setRecipeProperties(recipeResource.data);
                            showParent();
                            showProgressBar(false);
                            break;
                        }
                    }
                }
            }
        });
    }

    private void showParent() {
        mScrollView.setVisibility(View.VISIBLE);
    }

    private void setRecipeProperties(Recipe recipe) {
        if (recipe != null) {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.white_background)
                    .error(R.drawable.white_background);

            Glide.with(this)
                    .setDefaultRequestOptions(options)
                    .load(recipe.getImageUrl())
                    .into(mRecipeImage);

            mRecipeTitle.setText(recipe.getTitle());
            mRecipeRank.setText(String.valueOf(Math.round(recipe.getSocialRank())));

            setIngredients(recipe);
        }
    }

    private void setIngredients(Recipe recipe) {
        mRecipeIngredientsContainer.removeAllViews();

        if (recipe.getIngredients() != null) {
            for (String ingredient : recipe.getIngredients()) {
                TextView textView = new TextView(this);
                textView.setText(ingredient);
                textView.setTextSize(15);
                textView.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                mRecipeIngredientsContainer.addView(textView);
            }
        } else {
            TextView textView = new TextView(this);
            textView.setText("Error retrieving ingredients.\nCheck network connection.");
            textView.setTextSize(15);
            textView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            mRecipeIngredientsContainer.addView(textView);
        }
    }
}
