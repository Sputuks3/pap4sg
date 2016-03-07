package sg.org.pap.pickle.ui.fragments;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import it.moondroid.coverflow.BuildConfig;
import java.io.IOException;
import java.util.List;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.api.RestClient;
import sg.org.pap.pickle.models.Town;
import sg.org.pap.pickle.models.User;
import sg.org.pap.pickle.ui.activities.WebplayerActivity;
import sg.org.pap.pickle.ui.activities.WebviewActivity;
import sg.org.pap.pickle.ui.base.BaseFragment;
import sg.org.pap.pickle.ui.base.Connection;
import sg.org.pap.pickle.ui.pickle.PickleApp;

public class TownFragment extends BaseFragment {
    @Bind({2131624198})
    TextView mAddress;
    Town mContent;
    @Bind({2131624192})
    RelativeLayout mContentLayout;
    LatLng mCoordinates;
    @Bind({2131624100})
    TextView mEmail;
    @Bind({2131624186})
    ImageView mHeader;
    @Bind({2131624195})
    TextView mHotline;
    @Bind({2131624071})
    ProgressBar mLoading;
    @Bind({2131624199})
    MapView mMap;
    User mUser;
    @Bind({2131624104})
    TextView mWebsite;
    GoogleMap map;
    boolean showMap;

    private class TownAsyncTask extends AsyncTask<String, Void, Town> {
        private TownAsyncTask() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            TownFragment.this.mLoading.setVisibility(0);
            TownFragment.this.mContentLayout.setVisibility(8);
        }

        protected Town doInBackground(String... strings) {
            try {
                Town response = RestClient.getInstance().getTown(strings[0]);
                TownFragment.this.mCoordinates = TownFragment.this.getLocationFromAddress(response.getAddress() + " Singapore " + TownFragment.this.mUser.getPostalCode());
                response.setLatitude(TownFragment.this.mCoordinates.latitude);
                response.setLongitude(TownFragment.this.mCoordinates.longitude);
                return response;
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(Town town) {
            super.onPostExecute(town);
            TownFragment.this.mLoading.setVisibility(8);
            TownFragment.this.mContent = town;
            PickleApp.getInstance().setmTown(TownFragment.this.mContent);
            if (town != null) {
                TownFragment.this.bindView(TownFragment.this.mContent);
                return;
            }
            Snackbar snackbar = Snackbar.make(TownFragment.this.getView(), "Your Town is not available", 0);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(-12303292);
            ((TextView) snackbarView.findViewById(R.id.snackbar_text)).setTextColor(-1);
            snackbar.show();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContent = PickleApp.getInstance().getmTown();
        this.mUser = PickleApp.getInstance().getUser();
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (status == 0) {
            this.showMap = true;
            return;
        }
        this.showMap = false;
        GooglePlayServicesUtil.getErrorDialog(status, getActivity(), 10).show();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sendScreenAnalytics("/townCouncil");
        View v = inflater.inflate(R.layout.fragment_town, container, false);
        ButterKnife.bind(this, v);
        if (this.showMap) {
            this.mMap.onCreate(savedInstanceState);
        }
        return v;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!Connection.connected(getActivity())) {
            Connection.showSnackBar(getActivity(), getView());
        } else if (!TextUtils.isEmpty(this.mUser.getPostalCode())) {
            String mId = this.mUser.getTownId();
            new TownAsyncTask().execute(new String[]{mId});
        }
    }

    public void bindView(Town mTown) {
        this.mLoading.setVisibility(8);
        this.mHotline.setText(mTown.getHotline());
        final String hotline = mTown.getHotline().trim();
        this.mHotline.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                String uri = "tel:" + hotline.replaceAll("\\s+", BuildConfig.FLAVOR);
                Intent intent = new Intent("android.intent.action.CALL");
                intent.setData(Uri.parse(uri));
                TownFragment.this.startActivity(intent);
            }
        });
        this.mEmail.setText(mTown.getEmail());
        final String mEmailT = mTown.getEmail();
        this.mEmail.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent("android.intent.action.SEND", Uri.fromParts("mailto:", mEmailT, null));
                intent.setType("message/rfc822");
                intent.putExtra("android.intent.extra.EMAIL", new String[]{mEmailT});
                intent.putExtra("android.intent.extra.SUBJECT", "Mail from #PAP4SG App");
                intent.putExtra("android.intent.extra.TEXT", BuildConfig.FLAVOR);
                TownFragment.this.startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });
        this.mWebsite.setText(mTown.getWebsite());
        String mWeb = mTown.getWebsite();
        this.mWebsite.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                String url = ((TextView) view).getText().toString();
                Intent intent = new Intent(TownFragment.this.getActivity(), WebplayerActivity.class);
                intent.putExtra(WebviewActivity.CONTENT, url);
                TownFragment.this.startAnimatedActivity(intent);
            }
        });
        this.mAddress.setText(mTown.getAddress());
        Glide.with(getActivity()).load(mTown.getPhoto1()).crossFade().into(this.mHeader);
        if (this.showMap) {
            this.mCoordinates = new LatLng(this.mContent.getLatitude(), this.mContent.getLongitude());
            if (this.mCoordinates != null) {
                new Thread(new Runnable() {
                    public void run() {
                        while (TownFragment.this.map == null) {
                            try {
                                TownFragment.this.map = TownFragment.this.mMap.getMap();
                            } catch (Exception ignored) {
                                ignored.printStackTrace();
                                return;
                            }
                        }
                        TownFragment.this.getActivity().runOnUiThread(new Thread(new Runnable() {
                            public void run() {
                                TownFragment.this.map.getUiSettings().setMyLocationButtonEnabled(false);
                                TownFragment.this.map.setMyLocationEnabled(true);
                                MapsInitializer.initialize(TownFragment.this.getActivity());
                                TownFragment.this.map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(TownFragment.this.mContent.getLatitude(), TownFragment.this.mContent.getLongitude()), 14.0f));
                                TownFragment.this.map.addMarker(new MarkerOptions().position(TownFragment.this.mCoordinates).snippet(TownFragment.this.mContent.getAddress()).title(TownFragment.this.mContent.getName()));
                            }
                        }));
                    }
                }).start();
            }
            this.mContentLayout.setVisibility(0);
        }
    }

    public void onLowMemory() {
        super.onLowMemory();
        if (this.showMap) {
            this.mMap.onLowMemory();
        }
    }

    public void onResume() {
        super.onResume();
        if (this.showMap) {
            this.mMap.onResume();
        }
    }

    public void onPause() {
        super.onPause();
        if (this.showMap) {
            this.mMap.onPause();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.showMap) {
            this.mMap.onDestroy();
        }
    }

    public LatLng getLocationFromAddress(String strAddress) {
        try {
            List<Address> address = new Geocoder(getActivity()).getFromLocationName(strAddress, 1);
            if (address == null) {
                return null;
            }
            Address location = (Address) address.get(0);
            location.getLatitude();
            location.getLongitude();
            LatLng p1 = new LatLng(location.getLatitude(), location.getLongitude());
            return p1;
        } catch (IOException e) {
            return null;
        }
    }
}
