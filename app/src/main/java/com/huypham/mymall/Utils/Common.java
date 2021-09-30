package com.huypham.mymall.Utils;

import com.huypham.mymall.Retrofit.IDrinkShopAPI;
import com.huypham.mymall.Retrofit.RetrofitClient;

public class Common {
    private static final String BASE_URL = "http://10.0.3.2/drinkshop/";

    public static IDrinkShopAPI getAPI() {
        return RetrofitClient.getClient(BASE_URL).create(IDrinkShopAPI.class);
    }
}
