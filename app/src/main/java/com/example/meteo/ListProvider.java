package com.example.meteo;

import android.content.Context;
import android.widget.ArrayAdapter;

import org.json.JSONException;

import java.util.ArrayList;

interface ListProvider<E>{
    String getURL();

    ArrayList<E> toList(final String str)  throws JSONException;

    ArrayAdapter<E> getAdapter(final Context context);
}