package com.example.userpc.roadtracker;

import java.util.ArrayList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.SimpleLocationOverlay;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.ViewGroup;


public class Show_Map extends Activity {

	private SimpleLocationOverlay mMyLocationOverlay;
	private ScaleBarOverlay mScaleBarOverlay;  
	ItemizedIconOverlay<OverlayItem> currentLocationOverlay;
	DefaultResourceProxyImpl resourceProxy;
	private MyLocationOverlay myLocationoverlay;
	private MapView map;

	@SuppressWarnings("deprecation")
	ArrayList<OverlayItem> upload()
	{
		ArrayList<OverlayItem> overlay=new ArrayList<OverlayItem>();
		ArrayList<String> longitude=new ArrayList<String>();
		ArrayList<String> latitude=new ArrayList<String>();
		DatabaseHelper helper=DatabaseHelper.getInstance(this);
		SQLiteDatabase db=helper.getUsableDataBase();
		String sql="select * from `location`;";
		Cursor cur=db.rawQuery(sql, null);
		if(cur==null){
			ReceiveThread.cancelEngagement();
			return null;
		}			
		if(cur.moveToFirst())
		{
			do
			{
				String temp_long=cur.getString(cur.getColumnIndex("longitude"));
				String temp_lat=cur.getString(cur.getColumnIndex("latitude"));
				longitude.add(temp_long);
				latitude.add(temp_lat);
			}
			while(cur.moveToNext());
			cur.close();
		}
		for(int i=0;i<longitude.size();i++)
		{
			GeoPoint geo=new GeoPoint(Double.parseDouble(latitude.get(i)),Double.parseDouble(longitude.get(i)));
			OverlayItem over=new OverlayItem("Here", "Location", geo);
			Drawable draw=getResources().getDrawable(R.drawable.ic_launcher);
			over.setMarker(draw);
			overlay.add(over);
		}
		return overlay;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapfragmet);

		map = (MapView) findViewById(R.id.map);
		map.setTileSource(new XYTileSource("MapQuest",ResourceProxy.string.mapquest_osm, 0, 17, 256, ".jpg", new String[] {
				"http://otile1.mqcdn.com/tiles/1.0.0/map/",
				"http://otile2.mqcdn.com/tiles/1.0.0/map/",
				"http://otile3.mqcdn.com/tiles/1.0.0/map/",
				"http://otile4.mqcdn.com/tiles/1.0.0/map/"}));
		map.setBuiltInZoomControls(true);
		map.setMultiTouchControls(true);
		map.setUseDataConnection(false); //optional, but a good way to prevent loading from the network and test your zip loading. 
		IMapController mapController = map.getController();
		mapController.setZoom(14);
		LocationManager locationManger=(LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		Location lastLocation=locationManger.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		GeoPoint startPoint;
		if(lastLocation==null)
		{
			startPoint = new GeoPoint(22.5567,88.3022);
		}
		else
		{
			startPoint = new GeoPoint(lastLocation.getLatitude(),lastLocation.getLongitude());
		}
		
		mapController.setCenter(startPoint);
		

		
		//add markers part

		this.mMyLocationOverlay = new SimpleLocationOverlay(this); 
		map.getOverlays().add(mMyLocationOverlay);
		this.mScaleBarOverlay = new ScaleBarOverlay(this); 
		map.getOverlays().add(mScaleBarOverlay);
		resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
		GeoPoint  currentLocation = new GeoPoint(22.5567,88.3022); 
		GeoPoint  currentLocation2 = new GeoPoint(22.513365,88.403003); 
		OverlayItem myLocationOverlayItem = new OverlayItem("Here", "Current Position", currentLocation);
		Drawable myCurrentLocationMarker = this.getResources().getDrawable(R.drawable.ic_launcher);
		myLocationOverlayItem.setMarker(myCurrentLocationMarker);

		ArrayList<OverlayItem> items = upload();
		if(items==null)
			items=new ArrayList<OverlayItem>();
		
		//test loading location 1 (BESU) 
		items.add(myLocationOverlayItem);


		//test loading location 2(RUBY Hospital)
		myLocationOverlayItem = new OverlayItem("Here", "Current Position", currentLocation2);
		myCurrentLocationMarker = this.getResources().getDrawable(R.drawable.ic_launcher);
		myLocationOverlayItem.setMarker(myCurrentLocationMarker);
		items.add(myLocationOverlayItem);
		
		
		currentLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items,
				new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
			public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
				return true;
			}
			public boolean onItemLongPress(final int index, final OverlayItem item) {
				return true;
			}
		}, resourceProxy);
		map.getOverlays().add(this.currentLocationOverlay);

		//my locations part
		myLocationoverlay = new MyLocationOverlay(getApplicationContext(), map);
		myLocationoverlay.enableMyLocation(); // not on by default
		myLocationoverlay.enableCompass();
		myLocationoverlay.setDrawAccuracyEnabled(true);
		map.getOverlays().add(myLocationoverlay);

	}

	@Override
	public void finish() {
		ViewGroup viewGroup= (ViewGroup) getWindow().getDecorView();
		viewGroup.removeAllViews();
		super.finish();
	}

	@Override
	protected void onDestroy() {
		myLocationoverlay.disableMyLocation();
		myLocationoverlay.disableCompass();
		map.getTileProvider().clearTileCache();
		super.onDestroy();
	}
}



