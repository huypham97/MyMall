package com.huypham.mymall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.huypham.mymall.MyAddressesActivity.refreshItem;

public class AddAddressActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Dialog loadingDialog;
    private EditText city;
    private EditText locality;
    private EditText flatNo;
    private EditText pinCode;
    private EditText landmark;
    private EditText name;
    private EditText mobileNo;
    private EditText alternateMobileNo;
    private Spinner stateSpinner;
    private Button saveBtn;

    private AddressesModel addressesModel;
    private String stateList[], selectedState;
    private boolean updateAddress = false;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        toolbar = findViewById(R.id.toolbar);
        saveBtn = findViewById(R.id.save_btn);
        city = findViewById(R.id.city);
        locality = findViewById(R.id.locality);
        flatNo = findViewById(R.id.flat_no);
        pinCode = findViewById(R.id.pincode);
        landmark = findViewById(R.id.landmark);
        name = findViewById(R.id.name);
        mobileNo = findViewById(R.id.mobile_no);
        stateSpinner = findViewById(R.id.state_spinner);
        alternateMobileNo = findViewById(R.id.alternate_mobile_no);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Add a new address");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* ********* LOADING DIALOG********* */
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(true);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        /* ********* LOADING DIALOG********* */

        stateList = getResources().getStringArray(R.array.vietnam_states);

        ArrayAdapter spinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, stateList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        stateSpinner.setAdapter(spinnerAdapter);

        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedState = stateList[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (getIntent().getStringExtra("INTENT").equals("update_address")) {
            updateAddress = true;
            position = getIntent().getIntExtra("Position", -1);
            addressesModel = DBqueries.addressesModelList.get(position);

            flatNo.setText(addressesModel.getFlatNo());
            locality.setText(addressesModel.getLocality());
            landmark.setText(addressesModel.getLandmark());
            city.setText(addressesModel.getCity());
            pinCode.setText(addressesModel.getPincode());
            name.setText(addressesModel.getName());
            mobileNo.setText(addressesModel.getMobileNo());
            alternateMobileNo.setText(addressesModel.getAlternateMobileNo());

            for (int i = 0; i < stateList.length; i++) {
                if (stateList[i].equals(addressesModel.getState())) {
                    stateSpinner.setSelection(i);
                }
            }
            saveBtn.setText("Update");
        } else {
            position = DBqueries.addressesModelList.size();
        }

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(city.getText())) {
                    if (!TextUtils.isEmpty(locality.getText())) {
                        if (!TextUtils.isEmpty(flatNo.getText())) {
                            if (!TextUtils.isEmpty(pinCode.getText()) && pinCode.getText().length() == 6) {
                                if (!TextUtils.isEmpty(name.getText())) {
                                    if (!TextUtils.isEmpty(mobileNo.getText()) && mobileNo.getText().length() == 10) {

                                        loadingDialog.show();

                                        Map<String, Object> addAddress = new HashMap();
                                        addAddress.put("city_" + (position + 1), city.getText().toString());
                                        addAddress.put("locality_" + (position + 1), locality.getText().toString());
                                        addAddress.put("flat_no_" + (position + 1), flatNo.getText().toString());
                                        addAddress.put("pincode_" + (position + 1), pinCode.getText().toString());
                                        addAddress.put("landmark_" + (position + 1), landmark.getText().toString());
                                        addAddress.put("name_" + (position + 1), name.getText().toString());
                                        addAddress.put("mobile_no_" + (position + 1), mobileNo.getText().toString());
                                        addAddress.put("alternate_mobile_no_" + (position + 1), alternateMobileNo.getText().toString());
                                        addAddress.put("state_" + (position + 1), selectedState);

                                        if (!updateAddress) {
                                            addAddress.put("list_size", (long) DBqueries.addressesModelList.size() + 1);
                                            if (getIntent().getStringExtra("INTENT").equals("manage")) {
                                                addAddress.put("selected_" + (position + 1), true);
                                            } else {
                                                addAddress.put("selected_" + (position + 1), true);
                                            }

                                            if (DBqueries.addressesModelList.size() > 0) {
                                                addAddress.put("selected_" + (DBqueries.selectedAddress + 1), false);
                                            }
                                        }

                                        FirebaseFirestore.getInstance()
                                                .collection("USERS")
                                                .document(FirebaseAuth.getInstance().getUid())
                                                .collection("USER_DATA")
                                                .document("MY_ADDRESSES")
                                                .update(addAddress)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            if (!updateAddress) {
                                                                if (DBqueries.addressesModelList.size() > 0) {
                                                                    DBqueries.addressesModelList.get(DBqueries.selectedAddress).setSelected(false);
                                                                }

                                                                DBqueries.addressesModelList.add(new AddressesModel(city.getText().toString()
                                                                        , locality.getText().toString()
                                                                        , flatNo.getText().toString()
                                                                        , pinCode.getText().toString()
                                                                        , landmark.getText().toString()
                                                                        , name.getText().toString()
                                                                        , mobileNo.getText().toString()
                                                                        , alternateMobileNo.getText().toString()
                                                                        , selectedState
                                                                        , true));

                                                                DBqueries.selectedAddress = DBqueries.addressesModelList.size() - 1;
                                                            } else {
                                                                DBqueries.addressesModelList.set(position, new AddressesModel(
                                                                        city.getText().toString()
                                                                        , locality.getText().toString()
                                                                        , flatNo.getText().toString()
                                                                        , pinCode.getText().toString()
                                                                        , landmark.getText().toString()
                                                                        , name.getText().toString()
                                                                        , mobileNo.getText().toString()
                                                                        , alternateMobileNo.getText().toString()
                                                                        , selectedState
                                                                        , true));
                                                            }

                                                            if (getIntent().getStringExtra("INTENT").equals("deliveryIntent")) {
                                                                Intent intent = new Intent(AddAddressActivity.this, DeliveryActivity.class);
                                                                startActivity(intent);
                                                                overridePendingTransition(R.anim.slide_from_right, R.anim.slideout_from_left);
                                                            } else {
                                                                DBqueries.addressesModelList.get(DBqueries.addressesModelList.size() - 1).setSelected(true);
                                                                refreshItem(DBqueries.selectedAddress, DBqueries.addressesModelList.size() - 1);
                                                                overridePendingTransition(R.anim.slideout_from_right, R.anim.slide_from_left);
                                                            }
                                                            DBqueries.selectedAddress = DBqueries.addressesModelList.size() - 1;
                                                            finish();
                                                        } else {
                                                            String error = task.getException().getMessage();
                                                            Toast.makeText(AddAddressActivity.this, error, Toast.LENGTH_SHORT).show();
                                                        }
                                                        loadingDialog.dismiss();
                                                    }
                                                });
                                    } else {
                                        mobileNo.requestFocus();
                                        Toast.makeText(AddAddressActivity.this, "Please provide valid No.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    name.requestFocus();
                                }
                            } else {
                                pinCode.requestFocus();
                                Toast.makeText(AddAddressActivity.this, "Please provide valid pincode", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            flatNo.requestFocus();
                        }
                    } else {
                        locality.requestFocus();
                    }
                } else {
                    city.requestFocus();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.slideout_from_right, R.anim.slide_from_left);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slideout_from_right, R.anim.slide_from_left);
    }
}