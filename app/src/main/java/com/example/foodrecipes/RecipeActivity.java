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

        mRecipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        showProgressBar(true);

        subscribeObservers();

        getIncomingIntent();
    }

    private void subscribeObservers() {
        mRecipeViewModel.getRecipe().observe(this, recipe -> {
            if (recipe != null
                    && recipe.getRecipeId().equals(mRecipeViewModel.getRecipeId())) {
                setRecipeProperties(recipe);
                mRecipeViewModel.setDidRetrieveRecipe(true);
            }
        });
        mRecipeViewModel.isRecipeRequestTimeout().observe(this, isRecipeRequestTimeout -> {
            if (isRecipeRequestTimeout && !mRecipeViewModel.getDidRetrieveRecipe()) {
                // TODO : show user timeout
                Log.d(TAG, "subscribeObservers: recipe request timeout");
            }
        });
    }

    private void getIncomingIntent() {
        if (getIntent().hasExtra(RECIPE)) {
            Recipe recipe = getIntent().getParcelableExtra(RECIPE);
            mRecipeViewModel.searchRecipeApi(recipe.getRecipeId());
        }
    }

    private void setRecipeProperties(Recipe recipe) {
        if (recipe != null) {
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_launcher_background);
            Glide.with(this)
                    .setDefaultRequestOptions(requestOptions)
                    .load(recipe.getImageUrl())
                    .into(mRecipeImage);

            mRecipeTitle.setText(recipe.getTitle());

            mRecipeRank.setText(String.valueOf(Math.round(recipe.getSocialRank())));

            mRecipeIngredientsContainer.removeAllViews();
            for (String ingredient : recipe.getIngredients()) {
                TextView textView = new TextView(this);
                textView.setText(ingredient);
                textView.setTextSize(15);
                textView.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                mRecipeIngredientsContainer.addView(textView);
            }
            showProgressBar(false);
            showParent();
        }
    }

    private void showParent() {
        mScrollView.setVisibility(View.VISIBLE);
    }
}
