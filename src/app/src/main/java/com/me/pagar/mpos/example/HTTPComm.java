package com.me.pagar.mpos.example;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import me.pagar.mposandroid.Mpos;


interface HTTPEvent {
    public void onResponse(JSONObject response, Mpos mpos);
    public void onErrorResponse(int statusCode, String message);
}

public class HTTPComm {

    private HTTPEvent event;
    private Mpos mpos;
    public enum HTTPMethod {
        GET,
        POST,
        PUT,
        DELETE
    }


    public void request(Context context, String url, Map<String,Object> params, HTTPMethod method, final Mpos mpos) {
        this.mpos = mpos;
        RequestQueue queue = Volley.newRequestQueue(context);
        int stringRequestMethod = Request.Method.DEPRECATED_GET_OR_POST;
        JSONObject jsonObject = new JSONObject(params);
        switch (method) {
            case POST:
                stringRequestMethod = Request.Method.POST;
                break;
            case GET:
                stringRequestMethod = Request.Method.GET;
                break;
            case PUT:
                stringRequestMethod = Request.Method.PUT;
                break;
            case DELETE:
                stringRequestMethod = Request.Method.DELETE;
                break;
            default:
                break;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(stringRequestMethod, url,
            jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                event.onResponse(response, mpos);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String responseBody = null;
                try {
                    responseBody = new String(error.networkResponse.data, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                event.onErrorResponse(error.networkResponse.statusCode, responseBody);
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
            60000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(jsonObjectRequest);

    }

    public void addListener(HTTPEvent event) {
        this.event = event;
    }

}

