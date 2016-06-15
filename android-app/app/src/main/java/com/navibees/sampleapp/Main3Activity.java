package com.navibees.sampleapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.TiledLayer;
import com.esri.android.map.TiledServiceLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.navibees.sampleapp.R;
import com.navibees.sampleapp.SampleApplicationApp;
import com.navibees.sdk.AppManager;
import com.navibees.sdk.ApplicationConstants;
import com.navibees.sdk.model.metadata.json.IndoorLocation;
import com.navibees.sdk.model.postioning.IndoorLocationListener;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;

public class Main3Activity extends Activity implements AppManager.OnInitializedListener, IndoorLocationListener{

    private GraphicsLayer currentLocationGraphicsLayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        final Button button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Main3Activity.this);
                builder.setTitle("Title");

                // Set up the input
                final EditText input = new EditText(Main3Activity.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((SampleApplicationApp)getApplication()).experimentName = input.getText().toString();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        findViewById(R.id.stopButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SampleApplicationApp app = (SampleApplicationApp) getApplication();
                app.experimentName = "";
            }
        });

        ((SampleApplicationApp)getApplication()).textView = (TextView)findViewById(R.id.textView1);
        AppManager.getInstance().initialize(this, this);
    }

    @Override
    public void onInitialized(boolean success) {
        AppManager.getInstance().getPositionManager(this, this).startTracking();
        final MapView mapView = (MapView) findViewById(R.id.map);

        TiledServiceLayer tiledLayer = new ArcGISTiledMapServiceLayer("https://tiles1.arcgis.com/tiles/W83WxNh9tBPYYG7b/arcgis/rest/services/MTVC_FF/MapServer");

        mapView.addLayer(tiledLayer);

        mapView.setOnSingleTapListener(new OnSingleTapListener() {
            @Override
            public void onSingleTap(float x, float y) {
                Point p = mapView.toMapPoint(x, y);
                SampleApplicationApp app = (SampleApplicationApp) getApplication();

                ParseObject testObject = new ParseObject("GroundTruth");

                testObject.put("timeStamp", Calendar.getInstance().getTime());
                testObject.put("installation_id", ParseInstallation.getCurrentInstallation().getInstallationId());
                testObject.put("X", p.getX());
                testObject.put("Y", p.getY());
                testObject.put("experiment_name", app.experimentName);

                for (String r : app.regions) {
                    testObject.put("r_" + r, 1);
                }
                testObject.saveEventually();
            }
        });
        currentLocationGraphicsLayer = new GraphicsLayer();
        mapView.addLayer(currentLocationGraphicsLayer);
    }

    @Override
    public void locationCallback(IndoorLocation currentLocationWithoutSmoothing, IndoorLocation currentLocationAfterSmoothing, int numOfValidBeacons) {
        ((SampleApplicationApp)getApplication()).location = currentLocationAfterSmoothing;
        if (currentLocationAfterSmoothing != null){
            currentLocationGraphicsLayer.removeAll();
            IndoorLocation mCurrentLocation = currentLocationAfterSmoothing;
            Point pointGeometry = new Point(mCurrentLocation.getX(), mCurrentLocation.getY());
            PictureMarkerSymbol currentLocationSymbol = new PictureMarkerSymbol(this.getApplicationContext(), ContextCompat.getDrawable(this, com.navibees.sdk.R.drawable.com_navibees_sdk_current_location_pin));
            Graphic currentLocationGraphic = new Graphic(pointGeometry, currentLocationSymbol);
            currentLocationGraphicsLayer.addGraphic(currentLocationGraphic);
        }
    }
}


