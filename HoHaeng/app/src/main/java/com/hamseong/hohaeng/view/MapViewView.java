package com.hamseong.hohaeng.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.room.Room;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.hamseong.hohaeng.APIKey;
import com.hamseong.hohaeng.R;


import com.hamseong.hohaeng.YeosuUrbanInfo;
import com.hamseong.hohaeng.databinding.ActivityMapViewBinding;
import com.hamseong.hohaeng.model.AllCourseInfo;
import com.hamseong.hohaeng.model.MapData;
import com.hamseong.hohaeng.model.MapDataQuery;

import com.hamseong.hohaeng.viewmodel.MapViewModel;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;
import java.util.zip.Inflater;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static net.daum.mf.map.api.MapPoint.mapPointWithGeoCoord;


public class MapViewView extends AppCompatActivity implements MapView.POIItemEventListener {
    private MapViewModel mMapviewModel = new MapViewModel();//?????????
    private String API_Key = APIKey.KaKaoApi;//api key
    public int id = View.generateViewId();
    public static LayoutInflater inflater;//??? ??????????????? ????????????
    MapView mapView;

    //AllCourseInfo savedb = new AllCourseInfo();
    ArrayList<String> savedbX = new ArrayList<>();
    ArrayList<String> savedbY = new ArrayList<>();
    ArrayList<String> savedbName = new ArrayList<>();
    ActivityMapViewBinding binding;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONES_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};//?????????



    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
        findViewById(R.id.layout_tag).setVisibility(View.GONE);
        findViewById(R.id.layout_info).setVisibility(View.VISIBLE);
        findViewById(R.id.imageB_now).setVisibility(View.GONE);
        findViewById(R.id.imageB_drawline).setVisibility(View.GONE);
        int num = mapPOIItem.getTag();
        binding.infoName.setText(mMapviewModel.markerLocalData.get(num).getPlace_name());
        binding.infoAddress.setText(mMapviewModel.markerLocalData.get(num).getRoad_address());
    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_map_view);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,//????????????
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding.setThisacti(this);

        inflater = getLayoutInflater();

        mapView = new MapView(this);//?????? ????????????
        RelativeLayout mapViewContainer = (RelativeLayout) findViewById(R.id.linearLayoutTmap);
        mapViewContainer.addView(mapView);//?????? ??????
        mapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());
        mapView.setPOIItemEventListener(this);
        mapView.setCustomCurrentLocationMarkerTrackingImage(R.drawable.cr_hohaen,new MapPOIItem.ImageOffset(18,18));//????????? ???????????????

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mMapviewModel.onBalloonClick(query,mapView);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        Intent infomation = getIntent();
        if(infomation.getExtras().getString("urban").equals("??????")){

            mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(YeosuUrbanInfo.placeY, YeosuUrbanInfo.placeX),7,true);
        }
        //savedb.setLocation(infomation.getExtras().getString("urban"));//????????????
        String str = infomation.getExtras().getString("start");
        String[] array = str.split(".");
       // savedb.setStartYear(Integer.parseInt(array[0]));
        // savedb.setStartYear(Integer.parseInt(array[1]));
       // savedb.setStartYear(Integer.parseInt(array[2]));

        String str2 = infomation.getExtras().getString("end");
        String[] array2 = str.split(".");
        //savedb.setEndYear(Integer.parseInt(array[0]));
        //savedb.setEndMonth(Integer.parseInt(array[1]));
        //savedb.setEndDay(Integer.parseInt(array[2]));

        //savedb.setPeople(infomation.getExtras().getInt("people"));


        ImageView imageBDrawline = findViewById(R.id.imageB_drawline);//?????? ?????? ?????? ????????????
        imageBDrawline.setBackground(new ShapeDrawable(new OvalShape()));
        imageBDrawline.setClipToOutline(true);
        ImageView imageView = findViewById(R.id.imageB_now);
        imageView.setBackground(new ShapeDrawable(new OvalShape()));
        imageView.setClipToOutline(true);



        mMapviewModel.isLine.observe(this, new Observer<Boolean>() {//?????? ?????????????????? ??????
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    findViewById(R.id.layout_tag).setVisibility(View.VISIBLE);//?????? ????????? ?????????
                } else {
                    findViewById(R.id.layout_tag).setVisibility(View.GONE);//?????? ????????? ????????????
                    mMapviewModel.subTagOn.setValue(0);
                    mMapviewModel.TagOn.setValue(0);
                }
            }
        });

        mMapviewModel.mTMapmarker.observe(this, new Observer<ArrayList<MapPOIItem >>() {//?????? ???????????? ????????? ??????
            int Count;
            @Override

            public void onChanged(ArrayList<MapPOIItem > tMapMarkerItems) {
                mapView.removeAllPOIItems();//?????? ?????? ??????
                for(MapPOIItem mapPOIItem : tMapMarkerItems){
                    mapView.selectPOIItem(mapPOIItem, true);
                    mapView.addPOIItem(mapPOIItem);//?????? ????????? ??????
                }
                mapView.refreshDrawableState();
            }
        });

        mMapviewModel.isDraw.observe(this, new Observer<Boolean>() {//?????? ????????? ????????? ??????
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    findViewById(id).setVisibility(View.VISIBLE);//????????? ?????? ??? ?????????(?????? ?????? ??????)
                }else{
                    findViewById(id).setVisibility(View.INVISIBLE);//????????? ?????? ??? ????????????
                }
            }
        }); //??????????????? ?????? ?????????

        mMapviewModel.TagOn.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                switch (integer){
                    case 0:
                        binding.viewActivity.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewCafe.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewHis.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewMus.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewRestaurant.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        break;
                    case 1:
                        binding.viewActivity.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewCafe.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewHis.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewMus.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewRestaurant.setBackground(getResources().getDrawable(R.drawable.tag_on,null));
                        mMapviewModel.onTagbuttonClick("??????","FD6");
                        break;
                    case 2:
                        binding.viewActivity.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewCafe.setBackground(getResources().getDrawable(R.drawable.tag_on,null));
                        binding.viewHis.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewMus.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewRestaurant.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        mMapviewModel.onTagbuttonClick("??????","CE7");
                        break;
                    case 3:
                        binding.viewActivity.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewCafe.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewHis.setBackground(getResources().getDrawable(R.drawable.tag_on,null));
                        binding.viewMus.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewRestaurant.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        mMapviewModel.onTagbuttonClick("??????","CT1");
                        break;
                    case 4:
                        binding.viewActivity.setBackground(getResources().getDrawable(R.drawable.tag_on,null));
                        binding.viewCafe.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewHis.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewMus.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewRestaurant.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        mMapviewModel.onTagbuttonClick("????????????","AT4");
                        break;
                    case 5:
                        binding.viewActivity.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewCafe.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewHis.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewMus.setBackground(getResources().getDrawable(R.drawable.tag_on,null));
                        binding.viewRestaurant.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        mMapviewModel.onTagbuttonClick("?????????","CT1");
                        break;
                }
            }
        });

        mMapviewModel.subTagOn.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                switch (integer){
                    case 0:
                        binding.viewChild.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewLocal.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewPhoto.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        break;
                    case 1:
                        binding.viewChild.setBackground(getResources().getDrawable(R.drawable.tag_on,null));
                        binding.viewLocal.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewPhoto.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        mMapviewModel.onSubTagButtonClick();
                        break;
                    case 2:
                        binding.viewChild.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewLocal.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewPhoto.setBackground(getResources().getDrawable(R.drawable.tag_on,null));
                        mMapviewModel.onSubTagButtonClick();
                        break;
                    case 3:
                        binding.viewChild.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        binding.viewLocal.setBackground(getResources().getDrawable(R.drawable.tag_on,null));
                        binding.viewPhoto.setBackground(getResources().getDrawable(R.drawable.tag_off,null));
                        mMapviewModel.onSubTagButtonClick();
                        break;

                }
            }
        });



        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);//???????????? ??????
        View view_touch = new View(this);
        view_touch.setId(id);//??? ??????
        view_touch.setLayoutParams(layoutParams);//???????????? ??????
        ConstraintLayout constraintMapview = findViewById(R.id.constraint_mapview);
        constraintMapview.addView(view_touch);//??? ??????
        findViewById(id).setVisibility(View.INVISIBLE);



        imageBDrawline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("??????",Integer.toString(mapView.getZoomLevel()));
                mMapviewModel.onDrawButtonClick(mapView, view_touch);//???????????? ???????????? ??????
            }
        });


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MapViewView.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(MapViewView.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Log.i("?????? ", "??????");
                    if(mapView.isShowingCurrentLocationMarker()){
                        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);

                    }else{
                        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving);
                    }
                } else {
                    ActivityCompat.requestPermissions(MapViewView.this, REQUIRED_PERMISSIONS, PERMISSIONES_REQUEST_CODE);
                }
            }
        });

        //??????????????? ??? ?????? ?????????
        {

            View db_1 = (View) findViewById(R.id.bb_1) ;
            db_1.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(
                            getApplicationContext(), // ?????? ????????? ????????????
                            HomeActivity.class); // ?????? ????????? ????????? ??????
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent); // ?????? ???????????? ????????????
                }
            });
            View db_2 = (View) findViewById(R.id.bb_2) ;
            db_2.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(
                            getApplicationContext(), // ?????? ????????? ????????????
                            LikeView.class); // ?????? ????????? ????????? ??????
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent); // ?????? ???????????? ????????????
                }
            });
            View db_3 = (View) findViewById(R.id.bb_3) ;
            db_3.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(
                            getApplicationContext(), // ?????? ????????? ????????????
                            TMapViewView.class); // ?????? ????????? ????????? ??????
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent); // ?????? ???????????? ????????????


                }
            });
            View db_4 = (View) findViewById(R.id.bb_4) ;
            db_4.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(
                            getApplicationContext(), // ?????? ????????? ????????????
                            MyView.class); // ?????? ????????? ????????? ??????
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent); // ?????? ???????????? ????????????

                }
            });

        }

    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.layout_info).getVisibility()==View.VISIBLE) {
            findViewById(R.id.layout_info).setVisibility(View.GONE);
            findViewById(R.id.layout_tag).setVisibility(View.VISIBLE);
            findViewById(R.id.imageB_now).setVisibility(View.VISIBLE);
            findViewById(R.id.imageB_drawline).setVisibility(View.VISIBLE);
        } else if (mMapviewModel.isLine.getValue()) {
            mMapviewModel.isLine.setValue(false);
            mapView.removeAllPolylines();
            mapView.removeAllPOIItems();
            mMapviewModel.subTagOn.setValue(0);
            mMapviewModel.TagOn.setValue(0);

        } else {
            super.onBackPressed();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        mapView.removeAllPOIItems();//?????? ?????? ??????
        mapView.removeAllPolylines();
    }

    public void onTagClick(View view){
        int i = Integer.parseInt((String)view.getTag());
        mMapviewModel.TagOn.setValue(i);
    }

    public void onSubTagClick(View view){
        int i = Integer.parseInt((String)view.getTag());
        mMapviewModel.subTagOn.setValue(i);
    }
}


class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
    private final View mCalloutBalloon;

    public CustomCalloutBalloonAdapter() {
        mCalloutBalloon = MapViewView.inflater.inflate(R.layout.custom_balloon, null);

    }

    @Override
    public View getCalloutBalloon(MapPOIItem poiItem) {
        ((TextView) mCalloutBalloon.findViewById(R.id.textview_Pay)).setText(poiItem.getItemName());
        return mCalloutBalloon;
    }

    @Override
    public View getPressedCalloutBalloon(MapPOIItem poiItem) {
        return null;
    }
}
