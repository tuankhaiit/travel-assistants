package com.tuankhai.travelassistants.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tuankhai.travelassistants.R;
import com.tuankhai.travelassistants.activity.ListPlaceScheduleActivity;
import com.tuankhai.travelassistants.library.ratingbar.MaterialRatingBar;
import com.tuankhai.travelassistants.utils.MyCache;
import com.tuankhai.travelassistants.webservice.DTO.AddScheduleDayDTO;
import com.tuankhai.travelassistants.webservice.DTO.PlaceNearDTO;
import com.tuankhai.travelassistants.webservice.main.RequestService;

import java.util.ArrayList;
import java.util.List;

import static com.tuankhai.travelassistants.utils.MyCache.bg_place_global_4_3;

/**
 * Created by tuank on 15/11/2017.
 */

public class PlaceScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private ListPlaceScheduleActivity context;
    private List<PlaceNearDTO.Result> arrPlace;
    private List<PlaceNearDTO.Result> arrSearch;
    private ArrayList<AddScheduleDayDTO.ScheduleDay> arrHasPlace;
    private RecyclerView mRecyclerView;
    private Location location;

    private PlaceNearListAdapter.LayoutListPlaceNearItemListener itemListener;
    private PlaceNearListAdapter.OnLoadMoreListener mOnLoadMoreListener;
    private OnCheckedChangeListener mCheckedChangeListener;

    private boolean isLoading;
    private int visibleThreshold = 1;
    private int lastVisibleItem, totalItemCount;

    public void setOnLoadMoreListener(PlaceNearListAdapter.OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    public void setOnItemClickListener(PlaceNearListAdapter.LayoutListPlaceNearItemListener onItemClickListener) {
        this.itemListener = onItemClickListener;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.mCheckedChangeListener = listener;
    }

    public PlaceScheduleAdapter(
            ListPlaceScheduleActivity context,
            RecyclerView recyclerView,
            List<PlaceNearDTO.Result> arrPlace,
            ArrayList<AddScheduleDayDTO.ScheduleDay> arrHasPlace) {
        this.context = context;
        this.arrPlace = arrPlace;
        this.arrHasPlace = arrHasPlace;
        this.mRecyclerView = recyclerView;
        this.location = context.location;

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (mOnLoadMoreListener != null) {
                        mOnLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
//            final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
//            return new PlaceNearViewHolder(
//                    MaterialRippleLayout.on(inflater.inflate(R.layout.item_place_near_liner, viewGroup, false))
//                            .rippleOverlay(true)
//                            .rippleAlpha(0.2f)
//                            .rippleColor(R.integer.rippleColor)
//                            .rippleHover(true)
//                            .create()
//            );
            View view = LayoutInflater.from(context).inflate(R.layout.item_place_near_schedule_liner, viewGroup, false);
            return new PlaceScheduleAdapter.PlaceNearViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_progressbar, viewGroup, false);
            return new PlaceScheduleAdapter.LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return arrPlace.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            PlaceScheduleAdapter.PlaceNearViewHolder holder = (PlaceScheduleAdapter.PlaceNearViewHolder) viewHolder;
            PlaceNearDTO.Result item = arrPlace.get(position);
            holder.txtName.setText(item.name);
            holder.ratingBar.invalidate();
            holder.ratingBar.setMax(5);
            holder.ratingBar.setNumStars(5);
            holder.ratingBar.setStepSize(0.1f);
            holder.ratingBar.setRating(item.getRaring() + 0.1f);
            if (item.distance == 0) {
                Location mLocation = new Location("Place");
                mLocation.setLongitude(Double.parseDouble(item.geometry.location.lng));
                mLocation.setLatitude(Double.parseDouble(item.geometry.location.lat));
                arrPlace.get(position).distance = mLocation.distanceTo(location);
                double distance = ((double) Math.round(mLocation.distanceTo(location) / 100)) / 10;
                if (distance == 0) {
                    holder.txtDistance.setText(Math.round(mLocation.distanceTo(location)) + " m");
                } else {
                    holder.txtDistance.setText(distance + " km");
                }
            } else {
                double distance = ((double) Math.round(item.distance / 100)) / 10;
                if (distance == 0) {
                    holder.txtDistance.setText(Math.round(item.distance) + " m");
                } else {
                    holder.txtDistance.setText(distance + " km");
                }
            }
            if (item.photos != null && item.photos.length > 0) {
                Glide.with(context)
                        .load(RequestService.getImageAdapter(item.photos[0].photo_reference))
                        .override(context.getResources().getInteger(R.integer.width_16_9),
                                context.getResources().getInteger(R.integer.height_16_9))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.imageView);
            } else {
                if (MyCache.getInstance().getBitmapFromMemCache(bg_place_global_4_3) == null) {
                    Bitmap image = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg_place_global_4_3);
                    MyCache.getInstance().addBitmapToMemoryCache(bg_place_global_4_3, image);
                    holder.imageView.setImageBitmap(image);
                } else {
                    holder.imageView.setImageBitmap(MyCache.getInstance().getBitmapFromMemCache(bg_place_global_4_3));
                }
            }
            if (containArray(item.place_id)) {
                holder.checkBox.setOnCheckedChangeListener(null);
                holder.checkBox.setChecked(true);
                holder.checkBox.setOnCheckedChangeListener(holder);
            } else {
                holder.checkBox.setOnCheckedChangeListener(null);
                holder.checkBox.setChecked(false);
                holder.checkBox.setOnCheckedChangeListener(holder);
            }
        } else {
            ((PlaceScheduleAdapter.LoadingViewHolder) viewHolder).progressBar.setIndeterminate(true);
        }
    }

    private boolean containArray(String id) {
        for (AddScheduleDayDTO.ScheduleDay item : arrHasPlace) {
            if (item.place_id.equals(id)) return true;
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return arrPlace == null ? 0 : arrPlace.size();
    }

    public void setLoaded() {
        isLoading = false;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<PlaceNearDTO.Result> results = new ArrayList<PlaceNearDTO.Result>();
                if (arrSearch == null)
                    arrSearch = arrPlace;
                if (constraint != null) {
                    if (arrSearch != null && arrSearch.size() > 0) {
                        for (final PlaceNearDTO.Result g : arrSearch) {
                            if (g.name.toLowerCase()
                                    .contains(constraint.toString()))
                                results.add(g);
                        }
                    }
                    oReturn.values = results;
                }
                return oReturn;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                arrPlace = (ArrayList<PlaceNearDTO.Result>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void clearTextFilter() {
        arrPlace = arrSearch;
        notifyDataSetChanged();
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    public class PlaceNearViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            CompoundButton.OnCheckedChangeListener {
        TextView txtName, txtDistance;
        ImageView imageView;
        MaterialRatingBar ratingBar;
        CheckBox checkBox;

        public PlaceNearViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_name);
            txtDistance = itemView.findViewById(R.id.txt_distance);
            imageView = itemView.findViewById(R.id.img_place);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            checkBox = itemView.findViewById(R.id.checkbox);
            checkBox.setChecked(false);
            checkBox.setOnCheckedChangeListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemListener.onItemPlaceNearClick(view, arrPlace.get(getAdapterPosition()));
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            mCheckedChangeListener.onCheckedChange(b, arrPlace.get(getAdapterPosition()));
        }
    }

    public interface LayoutListPlaceNearItemListener {
        void onItemPlaceNearClick(View view, PlaceNearDTO.Result item);
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public interface OnCheckedChangeListener {
        void onCheckedChange(boolean isChecked, PlaceNearDTO.Result item);
    }
}