package com.huypham.mymall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private TextView textView;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchView = findViewById(R.id.search_view);
        textView = findViewById(R.id.notification_textview);
        recyclerView = findViewById(R.id.recycler_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView.setVisibility(View.VISIBLE);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        List<WishlistModel> list = new ArrayList<>();
        List<String> ids = new ArrayList<>();

        adapter = new Adapter(list, false);
        adapter.setFromSearch(true);
        recyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                list.clear();
                ids.clear();

                String[] tags = query.toLowerCase().split(" ");
                for (int i = 0; i < tags.length; i++) {
                }

                for (String tag : tags) {
                    tag.trim();
                    FirebaseFirestore.getInstance()
                            .collection("PRODUCTS")
                            .whereArrayContains("tags", tag)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                                            WishlistModel model = new WishlistModel(documentSnapshot.getId()
                                                    , documentSnapshot.get("product_image_1").toString()
                                                    , documentSnapshot.get("product_title").toString()
                                                    , (long) documentSnapshot.get("free_coupons")
                                                    , documentSnapshot.get("average_rating").toString()
                                                    , (long) documentSnapshot.get("total_ratings")
                                                    , documentSnapshot.get("product_price").toString()
                                                    , documentSnapshot.get("cutted_price").toString()
                                                    , (boolean) documentSnapshot.get("COD")
                                                    , true);

                                            model.setTags((ArrayList<String>) documentSnapshot.get("tags"));

                                            if (!ids.contains(model.getProductId())) {
                                                list.add(model);
                                                ids.add(model.getProductId());
                                            }
                                        }

                                        if (tag.equals(tags[tags.length - 1])) {
                                            if (list.size() == 0) {
                                                textView.setVisibility(View.VISIBLE);
                                                recyclerView.setVisibility(View.GONE);
                                            } else {
                                                textView.setVisibility(View.GONE);
                                                recyclerView.setVisibility(View.VISIBLE);
                                                adapter.getFilter().filter(query);
                                            }
                                        }
                                    } else {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(SearchActivity.this, error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    class Adapter extends WishlistAdapter implements Filterable {

        private List<WishlistModel> originalList;

        public Adapter(List<WishlistModel> wishlistModelList, Boolean wishlist) {
            super(wishlistModelList, wishlist);
            originalList = wishlistModelList;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    List<WishlistModel> filteredList = new ArrayList<>();
                    String[] tags = constraint.toString().toLowerCase().split(" ");

                    for (WishlistModel model : originalList) {
                        ArrayList<String> presentTags = new ArrayList<>();
                        for (String tag : tags) {
                            if (model.getTags().contains(tag)) {
                                presentTags.add(tag);
                            }
                        }
                        model.setTags(presentTags);
                    }

                    for (int i = tags.length; i > 0; i--) {
                        for (WishlistModel model : originalList) {
                            if (model.getTags().size() == i) {
                                filteredList.add(model);
                            }
                        }
                    }

                    filterResults.values = filteredList;
                    filterResults.count = filteredList.size();

                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results.count > 0) {
                        setWishlistModelList((List<WishlistModel>) results.values);
                    }
                    notifyDataSetChanged();
                }
            };
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}