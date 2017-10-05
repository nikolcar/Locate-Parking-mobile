package rs.elfak.mosis.nikolamitic.bottomnavigationview.Home;

import android.Manifest;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.Parking;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.User;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.R;

import static rs.elfak.mosis.nikolamitic.bottomnavigationview.MyLocationService.latitude;
import static rs.elfak.mosis.nikolamitic.bottomnavigationview.MyLocationService.longitude;

public class HomeFragment extends Fragment
{
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    FloatingActionButton btnAddParking;

    public GoogleMap googleMap;
    MapView mMapView;
    public static HashMap<Parking, Marker> mapMarkersParkings = new HashMap<Parking, Marker>();
    public static HashMap<String, Marker> mapUserIdMarker = new HashMap<String, Marker>();
    public static HashMap<String, Marker> mapFriendIdMarker = new HashMap<String, Marker>();

    private Circle distanceCircle;

    public Dialog dialog;
    public EditText etName, etDescription, etLongitude, etLatitude;
    public Spinner sType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        final Rect displayRectangle = new Rect();
        Window window = getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        final LayoutInflater layoutInflater = (LayoutInflater)getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);

        btnAddParking = (FloatingActionButton) view.findViewById(R.id.btn_add_new_parking);

        btnAddParking.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog = new Dialog(getActivity(),R.style.dialog_no_tytle);

                View layout = layoutInflater.inflate(R.layout.dialog_add_parking, null);
                layout.setMinimumWidth((int)(displayRectangle.width() * 0.8f));

                dialog.setContentView(layout);

                TextView tvTitle = (TextView) dialog.findViewById(R.id.add_parking_title);
                tvTitle.setText(R.string.Add_new_parking);

                etName = (EditText) dialog.findViewById(R.id.add_parking_name);
                etDescription = (EditText) dialog.findViewById(R.id.add_parking_desc);
                etLatitude = (EditText) dialog.findViewById(R.id.add_parking_lati);
                etLongitude = (EditText) dialog.findViewById(R.id.add_parking_long);
                sType = (Spinner) dialog.findViewById(R.id.add_parking_type);

                getGpsCoordinates();
                dialog.show();
            }
        });

        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try
        {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback()
        {
            @Override
            public void onMapReady(GoogleMap mMap)
            {
                googleMap = mMap;

                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);

                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.


                    //checkLocationPermission();
                    return;
                }
                googleMap.setMyLocationEnabled(false);

                //LatLng currentLocation = new LatLng(43.318731, 21.891143);

                // For dropping a marker at a point on the Map
                //MarkerOptions markerOptions = new MarkerOptions();
                //markerOptions.position(currentLocation);

                //Bitmap markImage = BitmapFactory.decodeResource(getResources(), R.mipmap.me);
                //markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmapSizeByScall(markImage,0.1f)));
                //markerOptions.title("You");

                //googleMap.addMarker(markerOptions);

                // For zooming automatically to the location of the marker
                //CameraPosition mCameraPosition = new CameraPosition.Builder().target(currentLocation).zoom(15).build();
                //googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            }
        });

        //TODO
        //friendsMarker = new HashMap<>();
        //parkingsMarker = new HashMap<>();

        return view;
    }

    public void getGpsCoordinates()
    {
        if(latitude!=null && longitude!=null)
        {
            etLatitude.setText(String.valueOf(latitude));
            etLongitude.setText(String.valueOf(longitude));
        }
        else
        {
            Toast.makeText(getActivity(),"Please turn on GPS",Toast.LENGTH_SHORT).show();
            etLatitude.setText("unknown");
            etLongitude.setText("unknown");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        /*
        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission. ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            //Request location updates:
            //locationManager.requestLocationUpdates(provider, 400, 1, this);

            }
        }
        */
        mMapView.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public boolean checkLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission. ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission. ACCESS_FINE_LOCATION))
            {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        }).create().show();
            }
            else
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission. ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case MY_PERMISSIONS_REQUEST_LOCATION:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission. ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED)
                    {
                        //Request location updates:
                        //locationManager.requestLocationUpdates(provider, 400, 1, this);
                    }
                }
                else
                {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }
}
