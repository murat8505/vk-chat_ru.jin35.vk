package com.jin35.vk.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.jin35.vk.R;

public class PointedMapView extends MapView {

    private static final String apiKey = "0MfCvNWQa8w6SWl8VdDl7Ove53y__WUARYyb4NA";

    private GeoPoint point;
    private Bitmap pin;
    private OnPointChangedListener listener;
    private boolean constantPoint = false;

    public PointedMapView(Context context) {
        super(context, apiKey);
        onCreate();
    }

    public PointedMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        onCreate();
    }

    public PointedMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        onCreate();
    }

    private void onCreate() {
        pin = BitmapFactory.decodeResource(getResources(), R.drawable.ic_map_pin);
        getOverlays().add(new PointOverlay());
    }

    public void setPoint(GeoPoint point) {
        this.point = point;
        if (listener != null) {
            listener.onPointChange(point);
        }
    }

    public GeoPoint getPoint() {
        return point;
    }

    public void setOnPointChanged(OnPointChangedListener listener) {
        this.listener = listener;
    }

    class PointOverlay extends Overlay {
        @Override
        public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
            super.draw(canvas, mapView, shadow);

            if (point != null) {
                Point screenPts = new Point();
                mapView.getProjection().toPixels(point, screenPts);
                canvas.drawBitmap(pin, screenPts.x - (pin.getWidth() / 2), screenPts.y - pin.getHeight(), null);
            }
            return true;
        }

        @Override
        public boolean onTap(GeoPoint p, MapView mapView) {
            if (!constantPoint) {
                setPoint(p);
                return true;
            }
            return false;
        }
    }

    public void setConstantPoint(boolean constantPoint) {
        this.constantPoint = constantPoint;
    }

    public interface OnPointChangedListener {
        void onPointChange(GeoPoint point);
    }

    public void setZoom(int zoomLevel) {
        getController().setZoom(zoomLevel);
    }
}
