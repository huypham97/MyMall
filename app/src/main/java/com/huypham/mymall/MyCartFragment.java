package com.huypham.mymall;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.huypham.mymall.DBqueries.addressesModelList;
import static com.huypham.mymall.DBqueries.cartItemModelList;
import static com.huypham.mymall.DBqueries.cartList;
import static com.huypham.mymall.DBqueries.loadAddresses;
import static com.huypham.mymall.DBqueries.loadCartList;
import static com.huypham.mymall.DBqueries.loadWishList;
import static com.huypham.mymall.DBqueries.wishList;
import static com.huypham.mymall.DBqueries.wishlistModelList;

public class MyCartFragment extends Fragment {

    public static RecyclerView cartItemsRecyclerView;
    private Button continueBtn;
    private Dialog loadingDialog;
    public static CartAdapter cartAdapter;
    private TextView totalAmount;

    public MyCartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_cart, container, false);

        /* ********* LOADING DIALOG********* */
        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(true);
        loadingDialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.show();
        /* ********* LOADING DIALOG********* */

        cartItemsRecyclerView = view.findViewById(R.id.cart_items_recyclerview);
        continueBtn = view.findViewById(R.id.cart_continue_btn);
        totalAmount = view.findViewById(R.id.total_cart_amount);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        cartItemsRecyclerView.setLayoutManager(layoutManager);

        for (int x = 0; x < cartItemModelList.size() - 2; x++) {
        }
        cartAdapter = new CartAdapter(DBqueries.cartItemModelList, totalAmount, true);
        cartItemsRecyclerView.setAdapter(cartAdapter);
        cartAdapter.notifyDataSetChanged();

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeliveryActivity.cartItemModelList = new ArrayList<>();
                DeliveryActivity.fromCart = true;

                for (int x = 0; x < DBqueries.cartItemModelList.size(); x++) {
                    CartItemModel cartItemModel = DBqueries.cartItemModelList.get(x);
                    if (cartItemModel.isInStock()) {
                        DeliveryActivity.cartItemModelList.add(cartItemModel);
                    }
                }
                DeliveryActivity.cartItemModelList.add(new CartItemModel(CartItemModel.TOTAL_AMOUNT));

                loadingDialog.show();
                if (addressesModelList.size() == 0) {
                    loadAddresses(getContext(), getActivity(), loadingDialog, true);
                } else {
                    loadingDialog.dismiss();
                    Intent deliveryIntent = new Intent(getContext(), DeliveryActivity.class);
                    startActivity(deliveryIntent);
                    getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slideout_from_left);
                }
            }
        });
        return view;
    }

    public static void refreshCartItem() {
        cartItemsRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                cartAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        cartAdapter.notifyDataSetChanged();
        if (!DeliveryActivity.backFromDelivery) {
            if (DBqueries.rewardModelList.size() == 0) {
                loadingDialog.show();
                DBqueries.loadRewards(getContext(), loadingDialog, false);
            }
            if (DBqueries.cartItemModelList.size() == 0) {
                DBqueries.cartList.clear();
                DBqueries.loadCartList(getContext(), loadingDialog, true, new TextView(getContext()), totalAmount);
            } else {
                DBqueries.cartList.clear();
                DBqueries.cartItemModelList.clear();
                DBqueries.loadCartList(getContext(), loadingDialog, true, new TextView(getContext()), totalAmount);
            }
        } else {
            DeliveryActivity.backFromDelivery = false;
        }
        loadingDialog.dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for (CartItemModel cartItemModel : DBqueries.cartItemModelList) {
            if (!TextUtils.isEmpty(cartItemModel.getSelectedCouponId())) {
                for (RewardModel rewardModel : DBqueries.rewardModelList) {
                    if (rewardModel.getCouponId().equals(cartItemModel.getSelectedCouponId())) {
                        rewardModel.setAlreadyUsed(false);
                    }
                }
                cartItemModel.setSelectedCouponId(null);
                if (MyRewardsFragment.myRewardsAdapter != null) {
                    MyRewardsFragment.myRewardsAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}