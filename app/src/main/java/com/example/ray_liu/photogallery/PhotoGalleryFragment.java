package com.example.ray_liu.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ray_liu on 16/3/21.
 */
public class PhotoGalleryFragment extends VisibleFragment{
    private static final String TAG = "PhotoGalleryFragment";
    private RecyclerView mPhotoRecyclerView;
    private List<GalleyItem> mItems = new ArrayList<>();
    protected ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance(){
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        updateItems();

        Log.i(TAG, "In onCreate!");
        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>(){
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder photoHolder,
                                                      Bitmap bitmap){
                        Drawable drawable = new BitmapDrawable(getResources(),bitmap);
                        photoHolder.bindDrawble(drawable);
                    }
                }
        );
        mThumbnailDownloader.start();
        Log.i(TAG, "In onCreate and after mThumb start!");
        mThumbnailDownloader.getLooper();
        Log.i(TAG,"In onCreate, After getLooper and Background thread started");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.fragment_photo_gallery, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.d(TAG, "QueryTextSubmit: " + s);
                QueryPreferences.setStoredQuery(getActivity(), s);
                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(TAG, "QueryTextChange: " + s);
                return false;
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if(PollService.isServiceAlarmOn(getActivity())){
            toggleItem.setTitle(R.string.stop_polling);
        }else{
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shoudStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(),shoudStartAlarm);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems(){
        String query = QueryPreferences.getStoredQuery(getActivity());
        Log.i(TAG, "In updateItems and query: " + query );
        new FetchItemsTask(query).execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_photo_gallery,container,false);
        Log.i(TAG, "In onCreateView!");
        mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        setupAdapter();
        return v;
    }

    private void setupAdapter(){
        if(isAdded()){
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder{
        private ImageView mItemImageView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mItemImageView = (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
        }

        public void bindDrawble(Drawable drawable){
            mItemImageView.setImageDrawable(drawable);
        }
    }
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{
        private List<GalleyItem> mGalleyItems;

        public PhotoAdapter(List<GalleyItem> items){
            mGalleyItems = items;
        }
        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.gallery_item, parent,false);

            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleyItem galleyItem = mGalleyItems.get(position);
            Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
            holder.bindDrawble(placeholder);
            Log.i(TAG, "In Adapter ------- ready to queueThumbnail!");
            mThumbnailDownloader.queueThumbnail(holder, galleyItem.getUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleyItems.size();
        }
    }

    //AsyncTask is designed for work that is short lived and not repeated too often
    //From Android 3.2, AsyncTask doesnot create a thread for each instansce of AsyncTask,
    // instead it uses something called executor, to run background work on a single background thread.
    // Thus, many AsyncTask will hog that thread.
    private class FetchItemsTask extends AsyncTask<Void,Void,List<GalleyItem>>{
        private String mQuery;

        public FetchItemsTask(String query){
            mQuery = query;
        }

        @Override
        protected List<GalleyItem> doInBackground(Void... params){

            if(mQuery == null){
                return new FlickrFetchr().fetchRecentPhotos();
            }else {
                return new FlickrFetchr().searchPhotos(mQuery);
            }
        }
        //AsyncTask's 3rd param = result type of doInBackground
        // and input type of onPostExecute

        @Override
        protected void onPostExecute(List<GalleyItem> items){
            mItems = items;
            setupAdapter();
        }
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG,"Background thread destroyed");
    }

}
