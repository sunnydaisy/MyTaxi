package com.example.s1.mytaxi.Fragments;

import com.example.s1.mytaxi.Notifications.MyResponse;
import com.example.s1.mytaxi.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAKoh4iSA:APA91bFV-xYKfzUl0LMphUNCu1jANlTqa2oSFV_fLC-xxAkTJUU3-fQuVz27HuMkyDkL3QoHOi3DqHPRz2k1bRdsfIUsg60uLg-lbSAkqwDt_xZJbBhTv3ux8cOY0uNUAlJjIpOnHBQk"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
