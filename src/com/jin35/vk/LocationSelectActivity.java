package com.jin35.vk;


//public class LocationSelectActivity extends MapActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.location_select);
//
//        findViewById(R.id.select_location_btn).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ((MapView)findViewById(R.id.map)).get
//            }
//        });
//    }
//
//    @Override
//    protected boolean isRouteDisplayed() {
//        return false;
//    }
//
//    class MapOverlay extends com.google.android.maps.Overlay {
//        @Override
//        public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
//            // ...
//        }
//
//        @Override
//        public boolean onTouchEvent(MotionEvent event, MapView mapView) {
//            // ---when user lifts his finger---
//            if (event.getAction() == 1) {
//                GeoPoint p = mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
//                Toast.makeText(getBaseContext(), p.getLatitudeE6() / 1E6 + "," + p.getLongitudeE6() / 1E6, Toast.LENGTH_SHORT).show();
//            }
//            return false;
//        }
//    }
// }
