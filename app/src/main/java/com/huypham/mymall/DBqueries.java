package com.huypham.mymall;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.huypham.mymall.HomeFragment.swipeRefreshLayout;
import static com.huypham.mymall.ProductDetailsActivity.initialRating;
import static com.huypham.mymall.ProductDetailsActivity.productID;
import static com.huypham.mymall.ProductDetailsActivity.rateNowContainer;
import static com.huypham.mymall.ProductDetailsActivity.running_rating_query;
import static com.huypham.mymall.ProductDetailsActivity.running_wishlist_query;
import static com.huypham.mymall.ProductDetailsActivity.setRating;

public class DBqueries {

    public static String email, fullname, profile;

    public static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    public static FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    public static List<CategoryModel> categoryModelList = new ArrayList<CategoryModel>();

    public static List<List<HomePageModel>> lists = new ArrayList<>();
    public static List<String> loadedCategoriesNames = new ArrayList<>();

    public static List<String> wishList = new ArrayList<>();
    public static List<WishlistModel> wishlistModelList = new ArrayList<>();

    public static List<String> myRateIds = new ArrayList<>();
    public static List<Long> myRating = new ArrayList<>();

    public static List<String> cartList = new ArrayList<>();
    public static List<CartItemModel> cartItemModelList = new ArrayList<>();

    public static int selectedAddress = -1;
    public static List<AddressesModel> addressesModelList = new ArrayList<>();

    public static List<RewardModel> rewardModelList = new ArrayList<>();

    public static List<MyOrderItemModel> myOrderItemModelList = new ArrayList<>();

    public static List<NotificationModel> notificationModelList = new ArrayList<>();

    private static ListenerRegistration registration;

    public static void loadCategories(RecyclerView categoryRecyclerView, Context context) {
        categoryModelList.clear();
        firebaseFirestore.collection("CATEGORIES").orderBy("index").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                categoryModelList.add(new CategoryModel(documentSnapshot.get("icon").toString(), documentSnapshot.get("categoryName").toString()));
                            }
                            CategoryAdapter categoryAdapter = new CategoryAdapter(categoryModelList);
                            categoryRecyclerView.setAdapter(categoryAdapter);
                            categoryAdapter.notifyDataSetChanged();
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static void loadFragmentData(RecyclerView homePageRecyclerView, Context context, Activity activity, int index, String categoryName) {
        firebaseFirestore.collection("CATEGORIES")
                .document(categoryName.toUpperCase())
                .collection("TOP_DEALS")
                .orderBy("index")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                if ((long) documentSnapshot.get("view_type") == 0) {
                                    List<SliderModel> sliderModelList = new ArrayList<SliderModel>();
                                    long no_of_banners = (long) documentSnapshot.get("no_of_banner");

                                    for (long x = 1; x <= no_of_banners; x++) {
                                        sliderModelList.add(new SliderModel(documentSnapshot.get("banner_" + x).toString(),
                                                documentSnapshot.get("banner_" + x + "_background").toString()));
                                    }
                                    lists.get(index).add(new HomePageModel(0, sliderModelList));
                                } else if ((long) documentSnapshot.get("view_type") == 1) {
                                    lists.get(index).add(new HomePageModel(1, documentSnapshot.get("strip_ad_banner").toString(),
                                            documentSnapshot.get("background").toString()));
                                } else if ((long) documentSnapshot.get("view_type") == 2) {
                                    List<WishlistModel> viewAllProductList = new ArrayList<WishlistModel>();
                                    List<HorizontalProductScrollModel> horizontalProductScrollModelList = new ArrayList<HorizontalProductScrollModel>();

                                    ArrayList<String> productIds = (ArrayList<String>) documentSnapshot.get("products");

                                    for (String productId : productIds) {
                                        horizontalProductScrollModelList.add(new HorizontalProductScrollModel(productId
                                                , ""
                                                , ""
                                                , ""
                                                , ""));

                                        viewAllProductList.add(new WishlistModel(productId
                                                , ""
                                                , ""
                                                , 0
                                                , ""
                                                , 0
                                                , ""
                                                , ""
                                                , false
                                                , false));
                                    }
                                    lists.get(index).add(new HomePageModel(2, documentSnapshot.get("layout_title").toString(), documentSnapshot.get("layout_background").toString(), horizontalProductScrollModelList, viewAllProductList));
                                } else if ((long) documentSnapshot.get("view_type") == 3) {
                                    List<HorizontalProductScrollModel> gridLayoutModelList = new ArrayList<HorizontalProductScrollModel>();
                                    ArrayList<String> productIds = (ArrayList<String>) documentSnapshot.get("products");

                                    for (String productId : productIds) {
                                        gridLayoutModelList.add(new HorizontalProductScrollModel(productId
                                                , ""
                                                , ""
                                                , ""
                                                , ""));
                                    }
                                    lists.get(index).add(new HomePageModel(3, documentSnapshot.get("layout_title").toString(), documentSnapshot.get("layout_background").toString(), gridLayoutModelList));
                                }
                            }
                            HomePageAdapter homePageAdapter = new HomePageAdapter(lists.get(index), activity);
                            homePageRecyclerView.setAdapter(homePageAdapter);
                            homePageAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static void loadWishList(Context context, Dialog dialog, boolean loadProductData) {
        wishList.clear();
        firebaseFirestore.collection("USERS")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("USER_DATA")
                .document("MY_WISHLIST")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            for (int x = 0; x < (long) task.getResult().get("list_size"); x++) {
                                String pid = task.getResult().get("product_ID_" + x).toString();
                                if (!wishList.contains(pid)) {
                                    wishList.add(pid);

                                    if (wishList.contains(productID)) {
                                        ProductDetailsActivity.ALREADY_ADDED_TO_WISHLIST = true;
                                        if (ProductDetailsActivity.addToWishListBtn != null) {
                                            ProductDetailsActivity.addToWishListBtn.setSupportImageTintList(context.getResources().getColorStateList(R.color.colorPrimary));
                                        }
                                    } else {
                                        ProductDetailsActivity.ALREADY_ADDED_TO_WISHLIST = false;
                                        if (ProductDetailsActivity.addToWishListBtn != null) {
                                            ProductDetailsActivity.addToWishListBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#9E9E9E")));
                                        }
                                    }

                                    if (loadProductData) {
                                        wishlistModelList.clear();
                                        String productId = task.getResult().get("product_ID_" + x).toString();

                                        firebaseFirestore.collection("PRODUCTS")
                                                .document(productId)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot documentSnapshot = task.getResult();
                                                            firebaseFirestore.collection("PRODUCTS")
                                                                    .document(productId)
                                                                    .collection("QUANTITY")
                                                                    .orderBy("time", Query.Direction.ASCENDING)
                                                                    .get()
                                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                                                                            if (task.isSuccessful()) {
                                                                                if (task.getResult().getDocuments().size() < (long) documentSnapshot.get("stock_quantity")) {
                                                                                    wishlistModelList.add(new WishlistModel(productId
                                                                                            , documentSnapshot.get("product_image_1").toString()
                                                                                            , documentSnapshot.get("product_title").toString()
                                                                                            , (long) documentSnapshot.get("free_coupons")
                                                                                            , documentSnapshot.get("average_rating").toString()
                                                                                            , (long) documentSnapshot.get("total_ratings")
                                                                                            , documentSnapshot.get("product_price").toString()
                                                                                            , documentSnapshot.get("cutted_price").toString()
                                                                                            , (boolean) documentSnapshot.get("COD")
                                                                                            , true));
                                                                                } else {
                                                                                    wishlistModelList.add(new WishlistModel(productId
                                                                                            , documentSnapshot.get("product_image_1").toString()
                                                                                            , documentSnapshot.get("product_title").toString()
                                                                                            , (long) documentSnapshot.get("free_coupons")
                                                                                            , documentSnapshot.get("average_rating").toString()
                                                                                            , (long) documentSnapshot.get("total_ratings")
                                                                                            , documentSnapshot.get("product_price").toString()
                                                                                            , documentSnapshot.get("cutted_price").toString()
                                                                                            , (boolean) documentSnapshot.get("COD")
                                                                                            , false));
                                                                                }
                                                                                MyWishListFragment.wishlistAdapter.notifyDataSetChanged();
                                                                            } else {
                                                                                // error
                                                                                String error = task.getException().getMessage();
                                                                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                        } else {
                                                            String error = task.getException().getMessage();
                                                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });
    }

    public static void removeFromWishList(int index, Context context) {
        String removedProductId = wishList.get(index);
        wishList.remove(index);
        Map<String, Object> updateWishList = new HashMap<>();

        for (int x = 0; x < wishList.size(); x++) {
            updateWishList.put("product_ID_" + x, wishList.get(x));
        }
        updateWishList.put("list_size", (long) wishList.size());

        firebaseFirestore.collection("USERS")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("USER_DATA")
                .document("MY_WISHLIST")
                .set(updateWishList)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (wishlistModelList.size() != 0) {
                                wishlistModelList.remove(index);
                                MyWishListFragment.wishlistAdapter.notifyDataSetChanged();
                            }
                            ProductDetailsActivity.ALREADY_ADDED_TO_WISHLIST = false;
                            Toast.makeText(context, "Remove successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            wishList.add(index, removedProductId);
                            ProductDetailsActivity.addToWishListBtn.setSupportImageTintList(context.getResources().getColorStateList(R.color.colorPrimary));
                            String error = task.getException().getMessage();
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                        }
                        running_wishlist_query = false;
                    }
                });
    }

    public static void loadRatingList(Context context) {
        if (!ProductDetailsActivity.running_rating_query) {
            ProductDetailsActivity.running_rating_query = true;
            myRateIds.clear();
            myRating.clear();

            firebaseFirestore.collection("USERS")
                    .document(FirebaseAuth.getInstance().getUid())
                    .collection("USER_DATA")
                    .document("MY_RATINGS")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                List<String> orderProductIds = new ArrayList<>();
                                for (int x = 0; x < myOrderItemModelList.size(); x++) {
                                    orderProductIds.add(myOrderItemModelList.get(x).getProductId());
                                }

                                for (long x = 0; x < (long) task.getResult().get("list_size"); x++) {
                                    if (!myRateIds.contains(ProductDetailsActivity.productID)) {
                                        myRateIds.add(task.getResult().get("product_ID_" + x).toString());
                                    }
                                    myRating.add((long) task.getResult().get("rating_" + x));

                                    if (task.getResult().get("product_ID_" + x).toString().equals(ProductDetailsActivity.productID)) {
                                        ProductDetailsActivity.initialRating = Integer.parseInt(String.valueOf((long) task.getResult().get("rating_" + x))) - 1;
                                        if (ProductDetailsActivity.rateNowContainer != null) {
                                            ProductDetailsActivity.setRating(ProductDetailsActivity.initialRating);
                                        }
                                    }

                                    if (orderProductIds.contains(task.getResult().get("product_ID_" + x).toString())) {
                                        myOrderItemModelList.get(orderProductIds.indexOf(task.getResult().get("product_ID_" + x).toString()))
                                                .setRating(Integer.parseInt(String.valueOf((long) task.getResult().get("rating_" + x))) - 1);
                                    }
                                }

                                if (MyOrdersFragment.myOrderAdapter != null) {
                                    MyOrdersFragment.myOrderAdapter.notifyDataSetChanged();
                                }
                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                            }
                            ProductDetailsActivity.running_rating_query = false;
                        }
                    });
        }
    }

    public static void loadCartList(Context context, Dialog dialog, boolean loadProductData, TextView badgeCount, TextView totalAmount) {
        cartList.clear();
        firebaseFirestore.collection("USERS")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("USER_DATA")
                .document("MY_CART")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            long length = (long) task.getResult().get("list_size");
                            for (int x = 0; x < (long) task.getResult().get("list_size"); x++) {
                                String pid = task.getResult().get("product_ID_" + x).toString();
                                if (!cartList.contains(pid)) {
                                    cartList.add(pid);

                                    if (cartList.contains(productID)) {
                                        ProductDetailsActivity.ALREADY_ADDED_TO_CART = true;
                                    } else {
                                        ProductDetailsActivity.ALREADY_ADDED_TO_CART = false;
                                    }

                                    if (loadProductData) {
                                        cartItemModelList.clear();
                                        String productId = task.getResult().get("product_ID_" + x).toString();

                                        firebaseFirestore.collection("PRODUCTS")
                                                .document(productId)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot documentSnapshot = task.getResult();
                                                            firebaseFirestore.collection("PRODUCTS")
                                                                    .document(productId)
                                                                    .collection("QUANTITY")
                                                                    .orderBy("time", Query.Direction.ASCENDING)
                                                                    .get()
                                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                                                                            if (task.isSuccessful()) {
                                                                                if (task.getResult().getDocuments().size() < (long) documentSnapshot.get("stock_quantity")) {
                                                                                    cartItemModelList.add(new CartItemModel(CartItemModel.CART_ITEM
                                                                                            , productId
                                                                                            , documentSnapshot.get("product_image_1").toString()
                                                                                            , documentSnapshot.get("product_title").toString()
                                                                                            , (long) documentSnapshot.get("free_coupons")
                                                                                            , documentSnapshot.get("product_price").toString()
                                                                                            , documentSnapshot.get("cutted_price").toString()
                                                                                            , (long) 1
                                                                                            , (long) documentSnapshot.get("max_quantity")
                                                                                            , (long) documentSnapshot.get("stock_quantity")
                                                                                            , (long) documentSnapshot.get("offers_applied")
                                                                                            , (long) 0
                                                                                            , true
                                                                                            , (boolean) documentSnapshot.get("COD")));
                                                                                } else {
                                                                                    cartItemModelList.add(new CartItemModel(CartItemModel.CART_ITEM
                                                                                            , productId
                                                                                            , documentSnapshot.get("product_image_1").toString()
                                                                                            , documentSnapshot.get("product_title").toString()
                                                                                            , (long) documentSnapshot.get("free_coupons")
                                                                                            , documentSnapshot.get("product_price").toString()
                                                                                            , documentSnapshot.get("cutted_price").toString()
                                                                                            , (long) 1
                                                                                            , (long) documentSnapshot.get("max_quantity")
                                                                                            , (long) documentSnapshot.get("stock_quantity")
                                                                                            , (long) documentSnapshot.get("offers_applied")
                                                                                            , (long) 0
                                                                                            , false
                                                                                            , (boolean) documentSnapshot.get("COD")));
                                                                                }

                                                                                if (cartItemModelList.size() == length) {
                                                                                    CartItemModel temp = new CartItemModel();
                                                                                    for (int i = 0; i < cartItemModelList.size() - 1; i++) {
                                                                                        if (!cartItemModelList.get(i).getProductID().equals(cartList.get(i))) {
                                                                                            for (int j = i + 1; j < cartItemModelList.size(); j++) {
                                                                                                if (cartItemModelList.get(j).getProductID().equals(cartList.get(i))) {
                                                                                                    temp = cartItemModelList.get(i);
                                                                                                    cartItemModelList.set(i, cartItemModelList.get(j));
                                                                                                    cartItemModelList.set(j, temp);
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }

                                                                                    cartItemModelList.add(new CartItemModel(CartItemModel.TOTAL_AMOUNT));
                                                                                    LinearLayout parent = (LinearLayout) totalAmount.getParent().getParent();
                                                                                    parent.setVisibility(View.VISIBLE);
                                                                                }

                                                                                if (cartList.size() == 0) {
                                                                                    cartItemModelList.clear();
                                                                                }

                                                                                MyCartFragment.cartAdapter.notifyDataSetChanged();
                                                                            } else {
                                                                                // error
                                                                                String error = task.getException().getMessage();
                                                                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                        } else {
                                                            String error = task.getException().getMessage();
                                                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }

                            if (cartList.size() != 0) {
                                badgeCount.setVisibility(View.VISIBLE);
                            } else {
                                badgeCount.setVisibility(View.INVISIBLE);
                            }

                            if (cartList.size() < 99) {
                                badgeCount.setText(String.valueOf(cartList.size()));
                            } else {
                                badgeCount.setText("99+");
                            }
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });
    }

    public static void removeFromCart(int index, Context context, TextView cartTotalAmount) {
        String removedProductId = cartList.get(index);
        cartList.remove(index);
        Map<String, Object> updateCartList = new HashMap<>();

        for (int x = 0; x < cartList.size(); x++) {
            updateCartList.put("product_ID_" + x, cartList.get(x));
        }
        updateCartList.put("list_size", (long) cartList.size());

        firebaseFirestore.collection("USERS")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("USER_DATA")
                .document("MY_CART")
                .set(updateCartList)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (cartItemModelList.size() != 0) {
                                cartItemModelList.remove(index);
                                MyCartFragment.cartAdapter.notifyDataSetChanged();
                            }

                            if (cartList.size() == 0) {
                                LinearLayout parent = (LinearLayout) cartTotalAmount.getParent().getParent();
                                parent.setVisibility(View.GONE);
                                cartItemModelList.clear();
                            }

                            ProductDetailsActivity.ALREADY_ADDED_TO_CART = false;
                            Toast.makeText(context, "Remove successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            cartList.add(index, removedProductId);
                            String error = task.getException().getMessage();
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                        }
                        ProductDetailsActivity.running_cart_query = false;
                    }
                });
    }

    public static void loadAddresses(Context context, Activity activity, Dialog loadingDialog, boolean gotoDeliveryAcitvity) {
        addressesModelList.clear();
        firebaseFirestore.collection("USERS")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("USER_DATA")
                .document("MY_ADDRESSES")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Intent deliveryIntent = null;
                            if ((long) task.getResult().get("list_size") == 0) {
                                deliveryIntent = new Intent(context, AddAddressActivity.class);
                                deliveryIntent.putExtra("INTENT", "deliveryIntent");
                            } else {
                                for (long x = 1; x <= (long) task.getResult().get("list_size"); x++) {
                                    addressesModelList.add(new AddressesModel(
                                            task.getResult().get("city_" + x).toString(),
                                            task.getResult().get("locality_" + x).toString(),
                                            task.getResult().get("flat_no_" + x).toString(),
                                            task.getResult().get("pincode_" + x).toString(),
                                            task.getResult().get("landmark_" + x).toString(),
                                            task.getResult().get("name_" + x).toString(),
                                            task.getResult().get("mobile_no_" + x).toString(),
                                            task.getResult().get("alternate_mobile_no_" + x).toString(),
                                            task.getResult().get("state_" + x).toString(),
                                            (boolean) task.getResult().get("selected_" + x)));

                                    if ((boolean) task.getResult().get("selected_" + x)) {
                                        selectedAddress = Integer.parseInt(String.valueOf(x - 1));
                                    }
                                }
                                if (gotoDeliveryAcitvity) {
                                    deliveryIntent = new Intent(context, DeliveryActivity.class);
                                }
                            }
                            if (gotoDeliveryAcitvity) {
                                context.startActivity(deliveryIntent);
                                activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slideout_from_left);
                            }
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                        }
                        loadingDialog.dismiss();
                    }
                });
    }

    public static void loadRewards(Context context, Dialog loadingDialog, boolean onRewardFragment) {
        rewardModelList.clear();

        firebaseFirestore.collection("USERS")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            final Date lastSeenDate = task.getResult().getDate("Last seen");

                            firebaseFirestore.collection("USERS")
                                    .document(FirebaseAuth.getInstance().getUid())
                                    .collection("USER_REWARDS")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {

                                                    if (documentSnapshot.get("type").equals("Discount") && lastSeenDate.before(((Timestamp) documentSnapshot.get("validity")).toDate())) {
                                                        rewardModelList.add(new RewardModel(documentSnapshot.getId()
                                                                , documentSnapshot.get("type").toString()
                                                                , documentSnapshot.get("lower_limit").toString()
                                                                , documentSnapshot.get("upper_limit").toString()
                                                                , documentSnapshot.get("percentage").toString()
                                                                , documentSnapshot.get("body").toString()
                                                                , (Timestamp) documentSnapshot.get("validity")
                                                                , (boolean) documentSnapshot.get("already_used")));
                                                    } else {
                                                        if (lastSeenDate.before(((Timestamp) documentSnapshot.get("validity")).toDate())) {
                                                            rewardModelList.add(new RewardModel(documentSnapshot.getId()
                                                                    , documentSnapshot.get("type").toString()
                                                                    , documentSnapshot.get("lower_limit").toString()
                                                                    , documentSnapshot.get("upper_limit").toString()
                                                                    , documentSnapshot.get("amount").toString()
                                                                    , documentSnapshot.get("body").toString()
                                                                    , (Timestamp) documentSnapshot.get("validity")
                                                                    , (boolean) documentSnapshot.get("already_used")));
                                                        }
                                                    }
                                                }
                                                if (onRewardFragment) {
                                                    MyRewardsFragment.myRewardsAdapter.notifyDataSetChanged();
                                                }
                                            } else {
                                                String error = task.getException().getMessage();
                                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                                            }
                                            loadingDialog.dismiss();
                                        }
                                    });
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    public static void loadOrders(Context context, @Nullable MyOrderAdapter myOrderAdapter, Dialog loadingDialog) {
        myOrderItemModelList.clear();
        firebaseFirestore.collection("USERS")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("USER_ORDERS")
                .orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                                firebaseFirestore.collection("ORDERS")
                                        .document(documentSnapshot.get("order_id").toString())
                                        .collection("OrderItems")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (DocumentSnapshot orderItems : task.getResult().getDocuments()) {
                                                        MyOrderItemModel myOrderItemModel = new MyOrderItemModel(orderItems.getString("Product Id")
                                                                , orderItems.getString("Product Title")
                                                                , orderItems.getString("Product Image")
                                                                , orderItems.getString("Order Status")
                                                                , orderItems.getString("Address")
                                                                , orderItems.getString("Coupon Id")
                                                                , orderItems.getString("Product Price")
                                                                , orderItems.getString("Cutted Price")
                                                                , orderItems.getString("Discounted Price")
                                                                , ((Timestamp) orderItems.get("Ordered Date")).toDate()
                                                                , ((Timestamp) orderItems.get("Packed Date")).toDate()
                                                                , ((Timestamp) orderItems.get("Shipped Date")).toDate()
                                                                , ((Timestamp) orderItems.get("Delivered Date")).toDate()
                                                                , ((Timestamp) orderItems.get("Cancelled Date")).toDate()
                                                                , orderItems.getLong("Free Coupons")
                                                                , orderItems.getLong("Product Quantity")
                                                                , orderItems.getString("Fullname")
                                                                , orderItems.getString("ORDER ID")
                                                                , orderItems.getString("Payment Method")
                                                                , orderItems.getString("Pincode")
                                                                , orderItems.getString("User Id")
                                                                , orderItems.getString("Delivery Price")
                                                                , (boolean) orderItems.get("Cancellation requested"));

                                                        myOrderItemModelList.add(myOrderItemModel);
                                                    }

                                                    loadRatingList(context);

                                                    if (myOrderAdapter != null) {
                                                        myOrderAdapter.notifyDataSetChanged();
                                                    }
                                                } else {
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                                                }
                                                loadingDialog.dismiss();
                                            }
                                        });
                            }
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                    }
                });
    }

    public static void checkNotifications(boolean remove, @Nullable TextView notifyCount) {
        if (remove) {
            registration.remove();
        } else {
            registration = firebaseFirestore.collection("USERS")
                    .document(FirebaseAuth.getInstance().getUid())
                    .collection("USER_DATA")
                    .document("MY_NOTIFICATIONS")
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable @org.jetbrains.annotations.Nullable DocumentSnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {
                            if (value != null && value.exists()) {
                                notificationModelList.clear();
                                int unread = 0;

                                for (long x = 0; x < (long) value.get("list_size"); x++) {
                                    notificationModelList.add(0, new NotificationModel(value.getString("image_" + x)
                                            , value.getString("body_" + x)
                                            , value.getBoolean("read_" + x)));

                                    if (!value.getBoolean("read_" + x)) {
                                        unread++;

                                        if (notifyCount != null) {
                                            if (unread > 0) {
                                                notifyCount.setVisibility(View.VISIBLE);
                                                if (unread < 99) {
                                                    notifyCount.setText(String.valueOf(unread));
                                                } else {
                                                    notifyCount.setText("99");
                                                }
                                            } else {
                                                notifyCount.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    }

                                    if (NotificationActivity.adapter != null) {
                                        NotificationActivity.adapter.notifyDataSetChanged();
                                    }
                                }

                            }
                        }
                    });
        }
    }

    public static void clearData() {
        categoryModelList.clear();
        lists.clear();
        loadedCategoriesNames.clear();
        wishList.clear();
        wishlistModelList.clear();
        cartList.clear();
        cartItemModelList.clear();
        myRateIds.clear();
        myRating.clear();
        addressesModelList.clear();
        rewardModelList.clear();
        myOrderItemModelList.clear();
    }
}
