package com.huypham.mymall;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyRewardsAdapter extends RecyclerView.Adapter<MyRewardsAdapter.Viewholder> {

    private List<RewardModel> rewardModelList;
    private Boolean useMiniLayout = false;
    private RecyclerView couponsRecyclerView;
    private static LinearLayout selectedCoupon;
    private Long productOriginalPrice;
    private TextView selectedCouponTitle;
    private TextView selectedCouponExpiryDate;
    private TextView selectedCouponBody;
    private TextView discountedPrice;
    private int cartItemPosition = -1;
    private LinearLayout applyOrRemoveBtnContainer;
    private List<CartItemModel> cartItemModelList;

    public MyRewardsAdapter(List<RewardModel> rewardModelList, Boolean useMiniLayout) {
        this.rewardModelList = rewardModelList;
        this.useMiniLayout = useMiniLayout;
    }

    public MyRewardsAdapter(List<RewardModel> rewardModelList, Boolean useMiniLayout, RecyclerView couponsRecyclerView, LinearLayout selectedCoupon,
                            Long productOriginalPrice, TextView couponTitle, TextView couponExpiryDate, TextView couponBody, TextView discountedPrice,
                            LinearLayout applyOrRemoveBtnContainer) {
        this.rewardModelList = rewardModelList;
        this.useMiniLayout = useMiniLayout;
        this.couponsRecyclerView = couponsRecyclerView;
        this.selectedCoupon = selectedCoupon;
        this.productOriginalPrice = productOriginalPrice;
        this.selectedCouponTitle = couponTitle;
        this.selectedCouponExpiryDate = couponExpiryDate;
        this.selectedCouponBody = couponBody;
        this.discountedPrice = discountedPrice;
        this.applyOrRemoveBtnContainer = applyOrRemoveBtnContainer;
    }

    public MyRewardsAdapter(int cartItemPosition, List<RewardModel> rewardModelList, Boolean useMiniLayout, RecyclerView couponsRecyclerView,
                            LinearLayout selectedCoupon, Long productOriginalPrice, TextView couponTitle, TextView couponExpiryDate, TextView couponBody,
                            TextView discountedPrice, LinearLayout applyOrRemoveBtnContainer, List<CartItemModel> cartItemModelList) {
        this.cartItemPosition = cartItemPosition;
        this.rewardModelList = rewardModelList;
        this.useMiniLayout = useMiniLayout;
        this.couponsRecyclerView = couponsRecyclerView;
        this.selectedCoupon = selectedCoupon;
        this.productOriginalPrice = productOriginalPrice;
        this.selectedCouponTitle = couponTitle;
        this.selectedCouponExpiryDate = couponExpiryDate;
        this.selectedCouponBody = couponBody;
        this.discountedPrice = discountedPrice;
        this.applyOrRemoveBtnContainer = applyOrRemoveBtnContainer;
        this.cartItemModelList = cartItemModelList;
    }

    @NonNull
    @NotNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view;
        if (useMiniLayout) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mini_rewards_item_layout, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rewards_item_layout, parent, false);
        }
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyRewardsAdapter.Viewholder holder, int position) {
        String couponId = rewardModelList.get(position).getCouponId();
        String type = rewardModelList.get(position).getType();
        Timestamp validity = rewardModelList.get(position).getTimestamp();
        String body = rewardModelList.get(position).getCouponBody();
        String lowerLimit = rewardModelList.get(position).getLowerLimit();
        String upperLimit = rewardModelList.get(position).getUpperLimit();
        String discountOrAmount = rewardModelList.get(position).getDiscountOrAmount();
        boolean alreadyUsed = rewardModelList.get(position).isAlreadyUsed();

        holder.setData(couponId, type, validity, body, lowerLimit, upperLimit, discountOrAmount, alreadyUsed);
    }

    @Override
    public int getItemCount() {
        return rewardModelList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {

        private TextView couponTitle;
        private TextView couponExpiryDate;
        private TextView couponBody;

        public Viewholder(@NonNull @NotNull View itemView) {
            super(itemView);

            couponTitle = itemView.findViewById(R.id.reward_coupon_title);
            couponExpiryDate = itemView.findViewById(R.id.reward_coupon_validity);
            couponBody = itemView.findViewById(R.id.reward_coupon_body);
        }

        private void setData(String couponId, String type, Timestamp validity, String body, String lowerLimit, String upperLimit, String discountOrAmount, boolean alreadyUsed) {
            if (type.equals("Discount")) {
                couponTitle.setText(type);
            } else {
                couponTitle.setText("FLAT $" + discountOrAmount + " OFF");
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

            if (alreadyUsed) {
                couponExpiryDate.setTextColor(itemView.getContext().getResources().getColor(R.color.colorPrimary));
                couponExpiryDate.setText("Already Used");
                couponBody.setTextColor(Color.parseColor("#50ffffff"));
                couponTitle.setTextColor(Color.parseColor("#50ffffff"));
            } else {
                couponBody.setTextColor(itemView.getContext().getResources().getColorStateList(android.R.color.white));
                couponTitle.setTextColor(itemView.getContext().getResources().getColorStateList(android.R.color.white));
                couponExpiryDate.setTextColor(itemView.getContext().getResources().getColor(R.color.couponPurple));
                couponExpiryDate.setText("till " + simpleDateFormat.format(validity.toDate()));
            }
            couponBody.setText(body);

            if (useMiniLayout) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!alreadyUsed) {
                            if (type.equals("Discount")) {
                                selectedCouponTitle.setText(type);
                            } else {
                                selectedCouponTitle.setText("FLAT $" + discountOrAmount + " OFF");
                            }
                            selectedCouponExpiryDate.setText("till " + simpleDateFormat.format(validity.toDate()));
                            selectedCouponBody.setText(body);

                            if (productOriginalPrice > Long.valueOf(lowerLimit) && productOriginalPrice < Long.valueOf(upperLimit)) {
                                if (type.equals("Discount")) {
                                    Long discountAmount = productOriginalPrice * Long.valueOf(discountOrAmount) / 100;
                                    discountedPrice.setText("$" + String.valueOf(productOriginalPrice - discountAmount));
                                } else {
                                    discountedPrice.setText("$" + String.valueOf(productOriginalPrice - Long.valueOf(discountOrAmount)));
                                }

                                if (cartItemPosition != -1) {
                                    cartItemModelList.get(cartItemPosition).setSelectedCouponId(couponId);
                                }
                            } else {
                                if (cartItemPosition != -1) {
                                    cartItemModelList.get(cartItemPosition).setSelectedCouponId(null);
                                }

                                discountedPrice.setText("Invalid");
                                Toast.makeText(itemView.getContext(), "Product doesn't matches Coupon terms.", Toast.LENGTH_SHORT).show();
                            }

                            if (couponsRecyclerView.getVisibility() == View.GONE) {
                                couponsRecyclerView.setVisibility(View.VISIBLE);
                                selectedCoupon.setVisibility(View.GONE);
                                applyOrRemoveBtnContainer.setVisibility(View.GONE);
                            } else {
                                couponsRecyclerView.setVisibility(View.GONE);
                                selectedCoupon.setVisibility(View.VISIBLE);
                                applyOrRemoveBtnContainer.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            }
        }
    }
}
