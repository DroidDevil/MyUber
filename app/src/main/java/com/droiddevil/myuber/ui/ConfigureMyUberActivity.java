/*
 * Copyright (C) 2015 Tanner Perrien
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.droiddevil.myuber.ui;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.droiddevil.myuber.R;
import com.droiddevil.myuber.WidgetUpdateService;
import com.droiddevil.myuber.annotations.ForActivity;
import com.droiddevil.myuber.data.api.UberService;
import com.droiddevil.myuber.db.models.WidgetRecord;
import com.droiddevil.myuber.rx.EndlessObserver;
import com.droiddevil.myuber.uber.UberProduct;
import com.droiddevil.myuber.uber.UberProductResponse;
import com.droiddevil.myuber.utils.LocationUtils;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.SerialSubscription;
import timber.log.Timber;

public class ConfigureMyUberActivity extends BaseActivity {

    @Inject
    @ForActivity
    Context mContext;

    @Inject
    Picasso mPicasso;

    @Inject
    UberService mUberService;

    @Inject
    Tracker mAnalyticsTracker;

    @InjectView(R.id.title)
    EditText mTitle;

    @InjectView(R.id.address)
    EditText mAddress;

    @InjectView(R.id.address_status)
    ImageView mAddressStatus;

    @InjectView(R.id.user_location_address)
    TextView mUserLocationAddress;

    @InjectView(R.id.uber_ride_list)
    GridView mUberRideList;

    @InjectView(R.id.list_loading_indicator)
    View mListLoadingIndicator;

    private int mAppWidgetId;

    private UberRideAdapter mUberRideListAdapter;

    private List<UberProduct> mUberProducts;

    private UberProduct mSelectedUberProduct;

    private Address mDestinationAddress;

    private SerialSubscription mUberProductSubscription = new SerialSubscription();

    private SerialSubscription mUserLocationSubscription = new SerialSubscription();

    private SerialSubscription mUserLocationAddressSubscription = new SerialSubscription();

    private SerialSubscription mDestinationAddressSubscription = new SerialSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_configure_myuber);

        // View injection
        ButterKnife.inject(this);

        // Get app widget ID
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // Set default result
        setResult(RESULT_CANCELED);

        mUberProducts = new ArrayList<UberProduct>();
        mUberRideListAdapter = new UberRideAdapter(mContext, mPicasso, mUberProducts);
        mUberRideList.setAdapter(mUberRideListAdapter);

        mUberRideList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mUberRideList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedUberProduct = mUberProducts.get(position);
            }
        });

        // Get address field updates
        mAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mDestinationAddressSubscription.set(LocationUtils.getAddressFromLocation(mContext, s.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new EndlessObserver<Address>() {

                        @Override
                        public void onNext(Address address) {
                            mDestinationAddress = address;
                            mAddressStatus.setImageDrawable(new ColorDrawable(mDestinationAddress == null ? Color.RED : Color.GREEN));
                        }
                    }));
            }
        });

        // Get user location
        mUserLocationSubscription.set(LocationUtils.getUserLocation(mContext)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new EndlessObserver<Location>() {

                @Override
                public void onError(Throwable e) {
                    Timber.e(e, "An error occurred while fetching the user's location");
                }

                @Override
                public void onNext(Location location) {
                    onLocationLocked(location);
                }
            }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mUberProductSubscription.unsubscribe();
        mUserLocationSubscription.unsubscribe();
        mUserLocationAddressSubscription.unsubscribe();
        mDestinationAddressSubscription.unsubscribe();
    }

    private void onLocationLocked(Location location) {
        // Fetch Uber products for this location
        fetchUberProductsForLocation(location.getLatitude(), location.getLongitude());

        // Get address from GPS location
        mUserLocationAddressSubscription.set(LocationUtils.getAddressFromCoords(mContext, location.getLatitude(), location.getLongitude())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new EndlessObserver<Address>() {
                @Override
                public void onNext(Address address) {
                    if (address != null && address.getMaxAddressLineIndex() > 0) { // TODO: strengthen address formatting
                        mUserLocationAddress.setText(String.format("%s, %s", address.getAddressLine(0), address.getAddressLine(1)));
                    } else {
                        mUserLocationAddress.setText(R.string.activity_configure_section_ride_location_not_found);
                    }
                }
            }));
    }

    private void fetchUberProductsForLocation(double lat, double lng) {
        mUberProductSubscription.set(mUberService.getProducts(lat, lng)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<UberProductResponse>() {
                @Override
                public void onCompleted() {
                    mListLoadingIndicator.setVisibility(View.GONE);
                }

                @Override
                public void onError(Throwable e) {
                    Timber.e(e, "Could not fetch uber products");
                    mListLoadingIndicator.setVisibility(View.GONE);
                }

                @Override
                public void onNext(UberProductResponse response) {
                    mUberProducts.clear();
                    mUberProducts.addAll(response.getProducts());
                    mUberRideListAdapter.notifyDataSetChanged();
                }
            }));
    }

    @OnClick(R.id.save)
    public void onSaveClicked() {
        // Check form
        if (!isFormValid()) {
            Toast.makeText(mContext, R.string.activity_configure_invalid_form, Toast.LENGTH_LONG).show();
            return;
        }

        // Track event
        mAnalyticsTracker.send(
            new HitBuilders.EventBuilder()
                .setCategory("Widget")
                .setAction("Add")
                .setLabel(mSelectedUberProduct.getDisplayName())
                .setValue(1)
                .build());

        // Save data record
        WidgetRecord record = new WidgetRecord();
        record.setAppWidgetId(mAppWidgetId);
        record.setTitle(mTitle.getText().toString());
        record.setUberProductId(mSelectedUberProduct.getId());
        record.setUberProductDisplayName(mSelectedUberProduct.getDisplayName());
        record.setDestinationAddress(mAddress.getText().toString());
        record.setDestinationLatitude(mDestinationAddress.getLatitude());
        record.setDestinationLongitude(mDestinationAddress.getLongitude());
        record.save();

        // Get widget manager
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);

        // Update widget layout
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_small);
        views.setTextViewText(R.id.title, mTitle.getText());
        views.setTextViewText(R.id.ride, mSelectedUberProduct.getDisplayName());
        views.setTextViewText(R.id.eta, "...");
        views.setTextViewText(R.id.cost, "...");
        appWidgetManager.updateAppWidget(mAppWidgetId, views);

        // Start update service
        mContext.startService(WidgetUpdateService.createStartIntent(mContext, new int[]{mAppWidgetId}));

        // Build result intent
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, intent);
        finish();
    }

    @OnClick(R.id.cancel)
    public void onCancelClicked() {
        finish();
    }

    private boolean isFormValid() {
        if (TextUtils.isEmpty(mTitle.getText())) {
            return false;
        }
        if (mDestinationAddress == null) {
            return false;
        }
        if (mSelectedUberProduct == null) {
            return false;
        }
        return true;
    }

    private static class UberRideAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        private Picasso mPicasso;

        private List<UberProduct> mProducts;

        public UberRideAdapter(Context context, Picasso picasso, List<UberProduct> products) {
            mInflater = LayoutInflater.from(context);
            mPicasso = picasso;
            mProducts = products;
        }

        @Override
        public int getCount() {
            return mProducts.size();
        }

        @Override
        public Object getItem(int position) {
            return mProducts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.listitem_uber_product, parent, false);
            }

            // Get product
            UberProduct product = mProducts.get(position);

            // Set image
            ImageView image = ButterKnife.findById(convertView, R.id.image);
            mPicasso.load(product.getImage()).centerInside().fit().into(image);

            // Set title
            TextView title = ButterKnife.findById(convertView, R.id.title);
            title.setText(product.getDisplayName());

            return convertView;
        }
    }

}
