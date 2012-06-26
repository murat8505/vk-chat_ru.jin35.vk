package com.jin35.vk;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.jin35.vk.view.PointedMapView;
import com.jin35.vk.view.PointedMapView.OnPointChangedListener;

public class LocationSelectActivity extends MapActivity {

    /**
     * Double[]{latitude, longitude}
     */
    public static final String LOC_EXTRA = "location";
    public static final String NEED_SELECT_BTN_EXTRA = "need select";

    private View btn;
    private boolean needSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.location_select);
        final PointedMapView map = (PointedMapView) findViewById(R.id.map);
        map.setBuiltInZoomControls(true);
        map.setClickable(true);
        MapController mc = map.getController();

        double[] location = getIntent().getDoubleArrayExtra(LOC_EXTRA);
        if (location != null && location.length == 2) {
            map.setPoint(new GeoPoint((int) (location[0] * 1000000), (int) (location[1] * 1000000)));
        } else {
            mc.setCenter(new GeoPoint(57000000, 38000000));
            mc.setZoom(6);
        }

        btn = findViewById(R.id.select_location_btn);
        needSelection = getIntent().getBooleanExtra(NEED_SELECT_BTN_EXTRA, true);
        if (needSelection) {
            btn.setEnabled(map.getPoint() != null);
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (map.getPoint() != null) {
                        Intent result = new Intent();
                        System.out.println("out: [" + (double) map.getPoint().getLatitudeE6() / 1000000 + "," + (double) map.getPoint().getLongitudeE6()
                                / 1000000 + "]");
                        result.putExtra(LOC_EXTRA, new double[] { (double) map.getPoint().getLatitudeE6() / 1000000,
                                (double) map.getPoint().getLongitudeE6() / 1000000 });
                        setResult(RESULT_OK, result);
                    }
                    finish();
                }
            });
            map.setOnPointChanged(new OnPointChangedListener() {
                @Override
                public void onPointChange(GeoPoint point) {
                    btn.setEnabled(map.getPoint() != null);
                }
            });
        } else {
            btn.setVisibility(View.GONE);
        }
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
