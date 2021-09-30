package com.huypham.mymall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.interfaces.HttpResponseCallback;
import com.braintreepayments.api.internal.HttpClient;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.huypham.mymall.DBqueries.addressesModelList;
import static com.huypham.mymall.DBqueries.cartItemModelList;
import static com.huypham.mymall.DBqueries.cartList;
import static com.huypham.mymall.DBqueries.loadAddresses;
import static com.huypham.mymall.DBqueries.selectedAddress;

public class DeliveryActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView deliveryRecyclerView;
    private Button changeOrAddNewAddressBtn;
    private TextView totalAmount;
    private TextView fullname;
    private TextView fullAddress;
    private TextView pincode;
    private Button continueBtn;
    private ConstraintLayout orderConfirmationLayout;
    private ImageButton continueShoppingBtn;
    private TextView orderId;
    public static Dialog loadingDialog;
    private Dialog paymentMethodDialog;
    private ImageButton paypal;
    private ImageButton cod;
    private TextView codTitle;

    private boolean successResponse = false;
    public static boolean fromCart;
    public static boolean codOrderConfirmed = false;
    public static boolean backFromDelivery = false;
    private boolean paypalMethodClicked = false;
    //    private boolean allProductsAvailable = true;
    public static boolean getQtyIDs = true;

    public static List<CartItemModel> cartItemModelList;
    HashMap<String, String> paramsHash;
    private String order_id;
    private String name, mobileNo, paymentMethod;
    String token, amount;
    final String API_GET_TOKEN = "http://192.168.0.5/braintree/main.php";
    final String API_GET_CHECKOUT = "http://192.168.0.5/braintree/checkout.php";
    private static final int REQUEST_CODE = 1234;
    public static final int SELECT_ADDRESS = 0;

    private FirebaseFirestore firebaseFirestore;
    public static CartAdapter cartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // mapping
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        deliveryRecyclerView = (RecyclerView) findViewById(R.id.delivery_recyclerview);
        changeOrAddNewAddressBtn = (Button) findViewById(R.id.change_or_add_address_btn);
        totalAmount = (TextView) findViewById(R.id.total_cart_amount);
        fullname = (TextView) findViewById(R.id.fullname);
        fullAddress = (TextView) findViewById(R.id.address);
        pincode = (TextView) findViewById(R.id.pincode);
        continueBtn = (Button) findViewById(R.id.cart_continue_btn);
        orderConfirmationLayout = (ConstraintLayout) findViewById(R.id.order_confirmation_layout);
        continueShoppingBtn = (ImageButton) findViewById(R.id.continue_shopping_btn);
        orderId = (TextView) findViewById(R.id.order_id);

        /* ********* LOADING DIALOG********* */
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        /* ********* LOADING DIALOG********* */

        /* ********* PAYMENT METHOD DIALOG********* */
        paymentMethodDialog = new Dialog(this);
        paymentMethodDialog.setContentView(R.layout.payment_method);
        paypal = (ImageButton) paymentMethodDialog.findViewById(R.id.paypal);
        cod = (ImageButton) paymentMethodDialog.findViewById(R.id.cod_btn);
        codTitle = (TextView) paymentMethodDialog.findViewById(R.id.cod_title);

        paymentMethodDialog.setCancelable(true);
        paymentMethodDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        paymentMethodDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        /* ********* PAYMENT METHOD DIALOG********* */

        firebaseFirestore = FirebaseFirestore.getInstance();
        getQtyIDs = true;

        order_id = UUID.randomUUID().toString().substring(0, 28);

        // set toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Delivery");
        getSupportActionBar().setHomeButtonEnabled(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        deliveryRecyclerView.setLayoutManager(layoutManager);

        cartAdapter = new CartAdapter(cartItemModelList, totalAmount, false);
        deliveryRecyclerView.setAdapter(cartAdapter);
        cartAdapter.notifyDataSetChanged();

        changeOrAddNewAddressBtn.setVisibility(View.VISIBLE);
        changeOrAddNewAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getQtyIDs = false;

                Intent myAddressesIntent = new Intent(DeliveryActivity.this, MyAddressesActivity.class);
                myAddressesIntent.putExtra("MODE", SELECT_ADDRESS);
                startActivity(myAddressesIntent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slideout_from_left);
            }
        });

        /////// Payment Method
        new getToken().execute();

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean allProductsAvailable = true;
                for (CartItemModel cartItemModel : cartItemModelList) {
                    if (cartItemModel.isQtyError()) {
                        allProductsAvailable = false;
                    }

                    if (cartItemModel.getType() == CartItemModel.CART_ITEM) {
                        if (!cartItemModel.isCOD()) {
                            cod.setEnabled(false);
                            cod.setAlpha(0.5f);
                            codTitle.setAlpha(0.5f);
                            break;
                        } else {
                            cod.setEnabled(true);
                            cod.setAlpha(1f);
                            codTitle.setAlpha(1f);
                        }
                    }
                }

                if (allProductsAvailable) {
                    paymentMethodDialog.show();
                }
            }
        });

        cod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentMethod = "COD";
                placeOrderDetails();
            }
        });

        paypal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentMethod = "PAYPAL";
                placeOrderDetails();
            }
        });
        /////// Payment Method
    }

    @Override
    protected void onStart() {
        super.onStart();

        /* ********* accessing quantity ********* */
        if (getQtyIDs) {
            if (!paypalMethodClicked) {
                int length = cartItemModelList.size();
                if (length > 1) {
                    length = length - 1;
                }
                for (int x = 0; x < length; x++) {
                    for (int y = 0; y < cartItemModelList.get(x).getProductQuantity(); y++) {
                        String quantityDocumentName = UUID.randomUUID().toString().substring(0, 20);

                        Map<String, Object> timestamp = new HashMap<>();
                        timestamp.put("time", FieldValue.serverTimestamp());
                        int finalX = x;
                        int finalY = y;
                        firebaseFirestore.collection("PRODUCTS")
                                .document(cartItemModelList.get(x).getProductID())
                                .collection("QUANTITY")
                                .document(quantityDocumentName)
                                .set(timestamp)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            cartItemModelList.get(finalX).getQtyIDs().add(quantityDocumentName);

                                            // last item in loop
                                            if (finalY + 1 == cartItemModelList.get(finalX).getProductQuantity()) {
                                                firebaseFirestore.collection("PRODUCTS")
                                                        .document(cartItemModelList.get(finalX).getProductID())
                                                        .collection("QUANTITY")
                                                        .orderBy("time", Query.Direction.ASCENDING)
                                                        .limit(cartItemModelList.get(finalX).getStockQuantity())
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    List<String> serverQuantity = new ArrayList<>();

                                                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                                                        serverQuantity.add(queryDocumentSnapshot.getId());
                                                                    }

                                                                    long availableQty = 0;
                                                                    boolean noLongerAvailable = true;
                                                                    cartItemModelList.get(finalX).setInStock(true);
                                                                    for (String qtyId : cartItemModelList.get(finalX).getQtyIDs()) {
                                                                        cartItemModelList.get(finalX).setQtyError(false);
                                                                        if (!serverQuantity.contains(qtyId)) {
                                                                            if (noLongerAvailable) {
                                                                                cartItemModelList.get(finalX).setInStock(false);
                                                                            } else {
                                                                                cartItemModelList.get(finalX).setQtyError(true);
                                                                                cartItemModelList.get(finalX).setMaxQuantity(availableQty);
                                                                                Toast.makeText(DeliveryActivity.this, "Sorry ! All products may not be available in required quantity...", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        } else {
                                                                            availableQty++;
                                                                            noLongerAvailable = false;
                                                                        }
                                                                    }

                                                                    if (cartItemModelList.get(finalX).isInStock()) {
//                                                                        allProductsAvailable = true;
                                                                        if (cartItemModelList.size() == 1) {
                                                                            cartItemModelList.add(new CartItemModel(CartItemModel.TOTAL_AMOUNT));
                                                                        }
                                                                    }
                                                                    cartAdapter.notifyDataSetChanged();
                                                                } else {
                                                                    // error
                                                                    String error = task.getException().getMessage();
                                                                    Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            }
                                        } else {
                                            loadingDialog.dismiss();
                                            String error = task.getException().getMessage();
                                            Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }
            }
        } else {
            getQtyIDs = true;
        }
        /* ********* accessing quantity ********* */

        name = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getName();
        mobileNo = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getMobileNo();
        if (addressesModelList.get(selectedAddress).getAlternateMobileNo().equals("")) {
            fullname.setText(name + " - " + mobileNo);
        } else {
            fullname.setText(name + " - " + mobileNo + " or " + DBqueries.addressesModelList.get(DBqueries.selectedAddress).getAlternateMobileNo());
        }

        String flatNo = addressesModelList.get(selectedAddress).getFlatNo();
        String city = addressesModelList.get(selectedAddress).getCity();
        String locality = addressesModelList.get(selectedAddress).getLocality();
        String landmark = addressesModelList.get(selectedAddress).getLandmark();
        String state = addressesModelList.get(selectedAddress).getState();

        if (landmark.equals("")) {
            fullAddress.setText(flatNo + ", " + locality + ", " + city + ", " + state);
        } else {
            fullAddress.setText(flatNo + ", " + locality + ", " + landmark + ", " + city + ", " + state);
        }

        pincode.setText(DBqueries.addressesModelList.get(DBqueries.selectedAddress).getPincode());

        if (codOrderConfirmed) {
            showConfirmationLayout(order_id);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        loadingDialog.dismiss();
        if (getQtyIDs) {
            if (!paypalMethodClicked) {
                int length = cartItemModelList.size();
                if (length > 1) {
                    length = length - 1;
                }
                for (int x = 0; x < length; x++) {
                    if (!successResponse) {
                        for (String qtyID : cartItemModelList.get(x).getQtyIDs()) {
                            int finalX = x;
                            firebaseFirestore.collection("PRODUCTS")
                                    .document(cartItemModelList.get(x).getProductID())
                                    .collection("QUANTITY")
                                    .document(qtyID)
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            if (qtyID.equals(cartItemModelList.get(finalX).getQtyIDs().get(cartItemModelList.get(finalX).getQtyIDs().size() - 1))) {
                                                cartItemModelList.get(finalX).getQtyIDs().clear();
                                            }
                                        }
                                    });
                        }
                    } else {
                        cartItemModelList.get(x).getQtyIDs().clear();
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        paypalMethodClicked = false;
        backFromDelivery = true;
        if (successResponse) {
            finish();
            return;
        }
        overridePendingTransition(R.anim.slideout_from_right, R.anim.slide_from_left);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        paymentMethodDialog.dismiss();
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce nonce = result.getPaymentMethodNonce();
                String strNounce = nonce.getNonce();
                if (!totalAmount.getText().toString().isEmpty()) {
                    amount = totalAmount.getText().toString().substring(1);
                    paramsHash = new HashMap<>();
                    paramsHash.put("amount", amount);
                    paramsHash.put("nonce", strNounce);

                    sendPayments();
                } else {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "User canceled", Toast.LENGTH_SHORT).show();
            } else {
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                Log.d("Err1", error.toString());
                Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendPayments() {
        RequestQueue queue = Volley.newRequestQueue(DeliveryActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, API_GET_CHECKOUT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.toString().contains("Successful")) {
                            Toast.makeText(DeliveryActivity.this, "Payment Success", Toast.LENGTH_SHORT).show();

                            Map<String, Object> updateStatus = new HashMap<>();
                            updateStatus.put("Payment Status", "Paid");
                            updateStatus.put("Order Status", "Ordered");

                            firebaseFirestore.collection("ORDERS")
                                    .document(order_id)
                                    .update(updateStatus)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Map<String, Object> userOrder = new HashMap<>();
                                                userOrder.put("order_id", order_id);
                                                userOrder.put("time", FieldValue.serverTimestamp());

                                                firebaseFirestore.collection("USERS")
                                                        .document(FirebaseAuth.getInstance().getUid())
                                                        .collection("USER_ORDERS")
                                                        .document(order_id)
                                                        .set(userOrder)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    showConfirmationLayout(order_id);
                                                                } else {
                                                                    Toast.makeText(DeliveryActivity.this, "Failed to update user orders list !", Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });
                                            } else {
                                                Toast.makeText(DeliveryActivity.this, "Order Cancelled !", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(DeliveryActivity.this, "Payment Failed", Toast.LENGTH_SHORT).show();
                        }
                        Log.d("Response", response);
                        loadingDialog.dismiss();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Err2", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if (paramsHash == null)
                    return null;
                Map<String, String> params = new HashMap<>();
                for (String key : paramsHash.keySet()) {
                    params.put(key, paramsHash.get(key));
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        RetryPolicy mRetryPolicy = new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(mRetryPolicy);
        queue.add(stringRequest);
    }

    private void submitPayment() {
        String payValue = totalAmount.getText().toString().substring(1);
        if (!payValue.isEmpty()) {
            DropInRequest dropInRequest = new DropInRequest().clientToken(token);
            startActivityForResult(dropInRequest.getIntent(this), REQUEST_CODE);
        } else
            Toast.makeText(this, "Enter a valid amount for payment", Toast.LENGTH_SHORT).show();

    }

    private class getToken extends AsyncTask {
        ProgressDialog mDailog;

        @Override
        protected Object doInBackground(Object[] objects) {
            HttpClient client = new HttpClient();
            client.get(API_GET_TOKEN, new HttpResponseCallback() {
                @Override
                public void success(final String responseBody) {
                    mDailog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            paymentMethodDialog.dismiss();

                            token = responseBody;
                        }
                    });
                }

                @Override
                public void failure(Exception exception) {
                    mDailog.dismiss();
                    Log.d("Err3", exception.toString());
                }
            });
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDailog = new ProgressDialog(DeliveryActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog);
            mDailog.setCancelable(false);
            mDailog.setMessage("Loading Wallet, Please Wait");
            mDailog.show();
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            paypalMethodClicked = false;
            backFromDelivery = true;
            finish();
            overridePendingTransition(R.anim.slideout_from_right, R.anim.slide_from_left);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showConfirmationLayout(String orderID) {
        successResponse = true;
        codOrderConfirmed = false;
        getQtyIDs = false;

        loadingDialog.show();

        for (int x = 0; x < cartItemModelList.size() - 1; x++) {
            for (String qtyID : cartItemModelList.get(x).getQtyIDs()) {
                firebaseFirestore.collection("PRODUCTS")
                        .document(cartItemModelList.get(x).getProductID())
                        .collection("QUANTITY")
                        .document(qtyID)
                        .update("user_ID", FirebaseAuth.getInstance().getUid());
            }
        }

        if (MainActivity.mainActivity != null) {
            MainActivity.mainActivity.finish();
            MainActivity.mainActivity = null;
            MainActivity.showCart = false;
        } else {
            MainActivity.resetMainActivity = true;
        }

        if (ProductDetailsActivity.productDetailsActivity != null) {
            ProductDetailsActivity.productDetailsActivity.finish();
            ProductDetailsActivity.productDetailsActivity = null;
        }

        if (fromCart) {
            loadingDialog.show();

            Map<String, Object> updateCartList = new HashMap<>();
            long cartListSize = 0;
            List<Integer> indexList = new ArrayList<>();
            List<String> tempCartList = new ArrayList<>();
            List<CartItemModel> tempCartItemModelList = new ArrayList<>();

            for (int x = 0; x < DBqueries.cartList.size(); x++) {
                if (!DBqueries.cartItemModelList.get(x).isInStock()) {
                    updateCartList.put("product_ID_" + cartListSize, DBqueries.cartItemModelList.get(x).getProductID());
                    cartListSize++;
                } else {
                    indexList.add(x);
                    tempCartItemModelList.add(cartItemModelList.get(x));
                }
            }
            updateCartList.put("list_size", cartListSize++);

            FirebaseFirestore.getInstance().collection("USERS")
                    .document(FirebaseAuth.getInstance().getUid())
                    .collection("USER_DATA")
                    .document("MY_CART")
                    .set(updateCartList)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                for (int x = 0; x < indexList.size(); x++) {
//                                                        cartList.remove(indexList.get(x).intValue());
                                    DBqueries.cartList.remove(tempCartItemModelList.get(x).getProductID());
//                                                        cartItemModelList.remove(indexList.get(x));
                                    cartItemModelList.remove(tempCartItemModelList.get(x));
//                                                        cartItemModelList.remove(DBqueries.cartItemModelList.size() - 1);
                                }
                                cartItemModelList.remove(DBqueries.cartItemModelList.get(DBqueries.cartItemModelList.size() - 1));
                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        loadingDialog.dismiss();

        orderId.setText("Order ID " + orderID);
        orderConfirmationLayout.setVisibility(View.VISIBLE);
        continueShoppingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void placeOrderDetails() {
        String userId = FirebaseAuth.getInstance().getUid();
        loadingDialog.show();

        for (CartItemModel cartItemModel : cartItemModelList) {
            if (cartItemModel.getType() == CartItemModel.CART_ITEM) {
                Map<String, Object> orderDetails = new HashMap<>();
                orderDetails.put("ORDER ID", order_id);
                orderDetails.put("Product Id", cartItemModel.getProductID());
                orderDetails.put("User Id", userId);
                orderDetails.put("Product Quantity", cartItemModel.getProductQuantity());
                if (cartItemModel.getCuttedPrice() != null) {
                    orderDetails.put("Cutted Price", cartItemModel.getCuttedPrice());
                } else {
                    orderDetails.put("Cutted Price", "");
                }
                orderDetails.put("Product Price", cartItemModel.getProductPrice());
                orderDetails.put("Product Image", cartItemModel.getProductImage());
                orderDetails.put("Product Title", cartItemModel.getProductTitle());
                if (cartItemModel.getSelectedCouponId() != null) {
                    orderDetails.put("Coupon Id", cartItemModel.getSelectedCouponId());
                } else {
                    orderDetails.put("Coupon Id", "");
                }
                if (cartItemModel.getDiscountedPrice() != null) {
                    orderDetails.put("Discounted Price", cartItemModel.getDiscountedPrice());
                } else {
                    orderDetails.put("Discounted Price", "");
                }
                orderDetails.put("Ordered Date", FieldValue.serverTimestamp());
                orderDetails.put("Shipped Date", FieldValue.serverTimestamp());
                orderDetails.put("Packed Date", FieldValue.serverTimestamp());
                orderDetails.put("Delivered Date", FieldValue.serverTimestamp());
                orderDetails.put("Cancelled Date", FieldValue.serverTimestamp());
                orderDetails.put("Payment Method", paymentMethod);
                orderDetails.put("Order Status", "Ordered");
                orderDetails.put("Address", fullAddress.getText().toString());
                orderDetails.put("Fullname", fullname.getText().toString());
                orderDetails.put("Pincode", pincode.getText().toString());
                orderDetails.put("Free Coupons", cartItemModel.getFreeCoupons());
                orderDetails.put("Delivery Price", cartItemModelList.get(cartItemModelList.size() - 1).getDeliveryPrice());
                orderDetails.put("Cancellation requested", false);

                firebaseFirestore.collection("ORDERS")
                        .document(order_id)
                        .collection("OrderItems")
                        .document(cartItemModel.getProductID())
                        .set(orderDetails)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                Map<String, Object> orderDetails = new HashMap<>();
                orderDetails.put("Total Items", cartItemModel.getTotalItems());
                orderDetails.put("Total Items Price", cartItemModel.getTotalItemsPrice());
                orderDetails.put("Delivery Price", cartItemModel.getDeliveryPrice());
                orderDetails.put("Total Amount", cartItemModel.getTotalAmount());
                orderDetails.put("Saved Amount", cartItemModel.getSavedAmount());
                orderDetails.put("Payment Status", "not paid");
                orderDetails.put("Order Status", "Cancelled");

                firebaseFirestore.collection("ORDERS")
                        .document(order_id)
                        .set(orderDetails)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    if (paymentMethod.equals("PAYPAL")) {
                                        paypal();
                                    } else {
                                        cod();
                                    }
                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }

    private void paypal() {
        paypalMethodClicked = true;
        submitPayment();
    }

    private void cod() {
        getQtyIDs = false;

        paymentMethodDialog.dismiss();
        codOrderConfirmed = true;

        Map<String, Object> updateStatus = new HashMap<>();
        updateStatus.put("Payment Status", "Paid");
        updateStatus.put("Order Status", "Ordered");

        firebaseFirestore.collection("ORDERS")
                .document(order_id)
                .update(updateStatus)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> userOrder = new HashMap<>();
                            userOrder.put("order_id", order_id);
                            userOrder.put("time", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("USERS")
                                    .document(FirebaseAuth.getInstance().getUid())
                                    .collection("USER_ORDERS")
                                    .document(order_id)
                                    .set(userOrder)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                showConfirmationLayout(order_id);
                                            } else {
                                                Toast.makeText(DeliveryActivity.this, "Failed to update user orders list !", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(DeliveryActivity.this, "Order Cancelled !", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}