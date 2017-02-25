package com.example.android.quakereport;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hiros on 2017/02/13.
 */

public class Earthquake {

    private static final DecimalFormat MAG_FORMAT = new DecimalFormat("0.0");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("LLL dd, yyyy");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("h:mm a");
    private static final Pattern LOCATION_OF_PATTERN = Pattern.compile("^(.*) of (.*)$");

    private int magColor;
    private String mag;
    private String offset;
    private String place;
    private String date;
    private String time;
    private String url;

    public Earthquake(double mag, String place, long time, String url) {
        this.magColor = getMagnitudeColor(mag);
        this.mag = MAG_FORMAT.format(mag);
        Matcher ofMatcher = LOCATION_OF_PATTERN.matcher(place);
        if (ofMatcher.matches()) {
            this.offset = ofMatcher.group(1) + " of";
            this.place = ofMatcher.group(2);
        } else {
            this.offset = "Near the";
            this.place = place;
        }
        Date datetime = new Date(time);
        this.date = DATE_FORMAT.format(datetime);
        this.time = TIME_FORMAT.format(datetime);
        this.url = url;
    }

    private int getMagnitudeColor(double mag) {
        switch ((int)Math.floor(mag)) {
            case 0:
            case 1: return R.color.magnitude_ge_0;
            case 2: return R.color.magnitude_ge_2;
            case 3: return R.color.magnitude_ge_3;
            case 4: return R.color.magnitude_ge_4;
            case 5: return R.color.magnitude_ge_5;
            case 6: return R.color.magnitude_ge_6;
            case 7: return R.color.magnitude_ge_7;
            case 8: return R.color.magnitude_ge_8;
            case 9: return R.color.magnitude_ge_9;
            default: return R.color.magnitude_ge_10;
        }
    }

    public static class Adapter extends ArrayAdapter<Earthquake> {

        public Adapter(Activity context, ArrayList<Earthquake> lst) {
            super(context, 0, lst);
        }

        private TextView getTextView(View view, int id) {
            return (TextView) view.findViewById(id);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(
                        R.layout.list_item, parent, false);
            }

            Earthquake cur = getItem(position);

            TextView magView = getTextView(view, R.id.list_item_magnitude);
            int magColor = ContextCompat.getColor(getContext(), cur.magColor);
            ((GradientDrawable)magView.getBackground()).setColor(magColor);
            magView.setText(cur.mag);
            getTextView(view, R.id.list_item_offset).setText(cur.offset);
            getTextView(view, R.id.list_item_place).setText(cur.place);
            getTextView(view, R.id.list_item_date).setText(cur.date);
            getTextView(view, R.id.list_item_time).setText(cur.time);
            view.setOnClickListener(new OnClickListener(cur.url));
            return view;
        }

        // Use OnItemClickListener instead of OnClickListener!!!
        private class OnClickListener implements View.OnClickListener {
            private String url;
            private OnClickListener(String url) { this.url = url; }
            public void onClick(View view) {
                Log.v("Eq.OnClickListener", this.url);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(this.url));
                Adapter.this.getContext().startActivity(i);
            }
        }
    }
}
