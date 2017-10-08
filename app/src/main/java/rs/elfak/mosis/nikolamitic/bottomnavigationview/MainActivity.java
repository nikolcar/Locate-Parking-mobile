package rs.elfak.mosis.nikolamitic.bottomnavigationview;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.BitmapManipulation;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.Parking;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.User;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends.FriendsFragment;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Home.HomeFragment;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Login.LoginActivity;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Settings.SettingsFragment;

public class MainActivity extends Activity
{
    private static final String TAG = "Locate Parking";

    private int clicked = 1, newClicked =0;
    private static HomeFragment homeFragment;
    private FriendsFragment friendsFragment;
    private SettingsFragment settingsFragment;
    private Fragment newFragment;

    private FirebaseAuth.AuthStateListener authListener;
    static private FirebaseAuth mAuth;
    static private FirebaseUser loggedUser;
    static private DatabaseReference parkings, users;

    private MyLocationService myLocationService;
    public Intent backgroundService;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener()
    {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.navigation_home:
                    Log.d(TAG, "navigation_home");
                    newClicked = 1;
                    break;
                case R.id.navigation_friends:
                    Log.d(TAG, "navigation_friends");
                    newClicked = 2;
                    break;
                case R.id.navigation_settings:
                    Log.d(TAG, "navigation_settings");
                    newClicked = 3;
                    break;
            }

            if( 0 < newClicked && newClicked < 4 && clicked != newClicked)
            {
                changeFragment(clicked, false);
                changeFragment(newClicked, true);
                clicked = newClicked;
                return true;
            }

            return false;
        }

    };

    void addFragments()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        homeFragment = new HomeFragment();
        ft.add(R.id.fragmentContainer, homeFragment);
        ft.hide(homeFragment);

        friendsFragment = new FriendsFragment();
        ft.add(R.id.fragmentContainer, friendsFragment);
        ft.hide(friendsFragment);


//        Bundle fragment = new Bundle();
//        Bundle extras = getIntent().getExtras();
//        String display_name = loggedUser.getDisplayName();
//        if (extras != null)
//            display_name = getIntent().getStringExtra("DISPLAY_NAME");
//
//        fragment.putString("display_name", display_name);
        settingsFragment = new SettingsFragment();
//        settingsFragment.setArguments(fragment);

        ft.add(R.id.fragmentContainer, settingsFragment);
        ft.hide(settingsFragment);

        ft.commit();
    }

    void changeFragment(int position, boolean show)
    {
        switch (position)
        {
            case 1:
                newFragment = homeFragment;
                break;
            case 2:
                newFragment = friendsFragment;
                break;
            case 3:
                newFragment = settingsFragment;
                break;
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();

        if(show)
            ft.show(newFragment);
        else
            ft.hide(newFragment);

        ft.commit();
    }

    public void disableDoubleSelect(int i)
    {
        Menu menu = null;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);

        menu.getItem(1).setCheckable(true);
        menu.getItem(2).setCheckable(true);
        menu.getItem(3).setCheckable(true);

        menu.getItem(i).setCheckable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mAuth = FirebaseAuth.getInstance();
//      SignOut();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                loggedUser = firebaseAuth.getCurrentUser();
                if (loggedUser == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        loggedUser = mAuth.getCurrentUser();

        if(loggedUser!=null)
        {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            parkings = database.getReference("parkings");
            users = database.getReference("users");

            //remove title bar
            requestWindowFeature(Window.FEATURE_NO_TITLE);

            setContentView(R.layout.activity_main);

            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
            navigation.setSelectedItemId(R.id.navigation_home);

            addFragments();
            changeFragment(1, true);
            backgroundService = new Intent(this,MyLocationService.class);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        }
        else
        {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Log.d(TAG, "onStart");
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        Log.d(TAG, "onStop");
        if (authListener != null)
        {
            mAuth.removeAuthStateListener(authListener);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d(TAG, "onPause");
        if(!settingsFragment.workback_status && isMyServiceRunning(MyLocationService.class))
        {
            Toast.makeText(this,"Stopping background service",Toast.LENGTH_SHORT).show();
            stopService(backgroundService);
        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        Log.d(TAG, "onDestroy");
//        if (backgroundService != null)
//            stopService(backgroundService);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(TAG, "onResume");
        setParametars();
    }

    public void setParametars()
    {
        if(loggedUser!=null)
        {
            if(homeFragment.googleMap!=null)
            {
                homeFragment.googleMap.clear();
            }

            friendsFragment.getFriendsFromServer();
            friendsFragment.pauseWaitingForFriendsList=true;

            Runnable r2 = new Runnable()
            {
                @Override
                public void run()
                {
                    //TODO
                    homeFragment.mapUserIdMarker.clear();
                    homeFragment.mapFriendIdMarker.clear();

                    while(friendsFragment.pauseWaitingForFriendsList)
                    {
                        synchronized (this)
                        {
                            try
                            {
                                wait(100);
                                //Log.d(TAG,"Waiting 100ms");
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }

                    while(homeFragment.googleMap==null)
                    {
                        synchronized (this)
                        {
                            try
                            {
                                wait(100);
                                //Log.d(TAG,"Waiting 100ms");
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }

                    settingsFragment.getSettingsFromServer();   //loadUsersFromServer() must be inside this function
                    loadParkingsFromServer();
                }
            };
            Thread loadEverythingFromServer = new Thread(r2);
            loadEverythingFromServer.start();
        }
    }

    private void loadParkingsFromServer()
    {
        //https://firebase.google.com/docs/database/android/lists-of-data
        ChildEventListener childEventListener = new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName)
            {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                final Parking parking = dataSnapshot.getValue(Parking.class);
                Log.d(TAG, "onChildAdded:" + parking.getName());
                Marker marker = addMarkers(parking.getLatitude(), parking.getLongitude(), parking.getName(), parking.getDescription(), null, "", false, false, dataSnapshot.getKey(), parking.isSecret());

                //Add to searchable HashMap
                homeFragment.mapMarkersParkings.put(parking, marker);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName)
            {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
                //We don't have a ability to change a landmark
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
                //We don't have a ability to change a landmark
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName)
            {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
                //We don't have a ability to move a landmark in DB.
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
            }
        };
        parkings.addChildEventListener(childEventListener);
    }

    private Marker addMarkers(double lat, double lng, String title, String snippet, Bitmap icon, String uId, boolean friends_status, boolean player_status, String parkingId, boolean secret)
    {
        Log.d(TAG,"addMarkers uid:" + uId);
        Marker marker = null;
        Float factor = 0.7f;

        MarkerOptions mo = new MarkerOptions();
        mo.position(new LatLng(lat, lng));
        mo.title(title);
        if(snippet!=null && !snippet.equals(""))
        {
            mo.snippet(snippet);
        }
        if(uId==null || uId.equals(""))
        {
            if(secret)
            {
                mo.icon(BitmapDescriptorFactory.fromBitmap(bitmapSizeByScall(BitmapManipulation.getMarkerBitmapFromView(R.mipmap.free, MainActivity.this),factor)));
            }
            else
            {
                mo.icon(BitmapDescriptorFactory.fromBitmap(bitmapSizeByScall(BitmapManipulation.getMarkerBitmapFromView(R.mipmap.occupied, MainActivity.this),factor)));
            }

            marker = homeFragment.googleMap.addMarker(mo);
        }
        else
        {
            if(uId.equals(loggedUser.getUid()))
            {
                mo.icon(BitmapDescriptorFactory.fromBitmap(bitmapSizeByScall(BitmapManipulation.getMarkerBitmapFromView(R.mipmap.me, MainActivity.this),factor)));
                CameraPosition mCameraPosition = new CameraPosition.Builder().target(new LatLng(lat,lng)).zoom(15).build();
                homeFragment.googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));

                marker = homeFragment.googleMap.addMarker(mo);
                homeFragment.mapUserIdMarker.put(uId, marker);
            }
            else
            {
                if(friendsFragment.friendsList.contains(uId))
                {
                    mo.icon(BitmapDescriptorFactory.fromBitmap(bitmapSizeByScall(BitmapManipulation.getMarkerBitmapFromView(R.mipmap.friend, MainActivity.this),0.6f)));
                    mo.visible(friends_status);

                    marker = homeFragment.googleMap.addMarker(mo);
                    homeFragment.mapFriendIdMarker.put(uId, marker);
                }
                else
                {
                    mo.icon(BitmapDescriptorFactory.fromBitmap(bitmapSizeByScall(BitmapManipulation.getMarkerBitmapFromView(R.mipmap.user, MainActivity.this),0.5f)));
                    mo.visible(player_status);

                    marker = homeFragment.googleMap.addMarker(mo);
                    homeFragment.mapUserIdMarker.put(uId, marker);

                }
            }
        }

        return marker;
    }

    public Bitmap bitmapSizeByScall(Bitmap bitmapIn, float scall_zero_to_one_f)
    {
        Bitmap bitmapOut = Bitmap.createScaledBitmap(bitmapIn,
                Math.round(bitmapIn.getWidth() * scall_zero_to_one_f),
                Math.round(bitmapIn.getHeight() * scall_zero_to_one_f), false);

        return bitmapOut;
    }

    public void changeVisibility(boolean playersOptionChanged, boolean friendsOptionChanged)
    {
        if (playersOptionChanged)
        {
            for (Marker mMarker : homeFragment.mapUserIdMarker.values())
            {
                mMarker.setVisible(!mMarker.isVisible());
            }

            homeFragment.mapUserIdMarker.get(loggedUser.getUid()).setVisible(true);
        }

        if(friendsOptionChanged)
        {
            for (Marker mMarker : homeFragment.mapFriendIdMarker.values())
            {
                mMarker.setVisible(!mMarker.isVisible());
            }
        }
    }

    public void loadPlayersFromServer(final Boolean players_status, final Boolean friends_status)
    {
        ChildEventListener childEventListener = new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName)
            {
                    final User user = dataSnapshot.getValue(User.class);
                    String uid = dataSnapshot.getKey();
                    Marker marker = addMarkers(user.getLatitude(), user.getLongitude(), user.getNickname(), "", null, uid, friends_status, players_status, "", false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName)
            {
                User user = dataSnapshot.getValue(User.class);
                String uid = dataSnapshot.getKey();

                Marker mMarker;
                mMarker = homeFragment.mapUserIdMarker.get(uid);

                if(mMarker==null)
                {
                    mMarker = homeFragment.mapFriendIdMarker.get(uid);
                }

                mMarker.setPosition(new LatLng(user.getLatitude(), user.getLongitude()));
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName)
            {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
            }
        };
        users.addChildEventListener(childEventListener);
    }

    static public HomeFragment getHomeFragment()
    {
        return homeFragment;
    }

    public static FirebaseUser getLoggedUser()
    {
        return loggedUser;
    }

    public static DatabaseReference getParkings()
    {
        return parkings;
    }

    public static DatabaseReference getUsers()
    {
        return users;
    }

    public void SignOut()
    {
        mAuth.signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }
}
