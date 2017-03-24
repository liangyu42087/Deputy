package deputy.android.com.deputyliang.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import deputy.android.com.deputyliang.R;
import deputy.android.com.deputyliang.model.Shift;
import deputy.android.com.deputyliang.util.GenericUtil;

/**
 * Created by liangyu42087 on 2017/3/21.
 */

public class ShiftAdapter extends RecyclerView.Adapter<ShiftAdapter.ShiftAdapterViewHolder> {

    private Shift[] mShiftData;
    private final ShiftAdapterOnClickHandler mClickHandler;


    public interface ShiftAdapterOnClickHandler {
        void onClick(Shift shift);
    }

    public ShiftAdapter(ShiftAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public class ShiftAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView imageView;
        public final TextView tv_main_start_time;
        public final TextView tv_main_end_time;
        public final TextView tv_main_id;

        public ShiftAdapterViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.imageView);
            tv_main_end_time = (TextView) view.findViewById(R.id.tv_main_end_time);
            tv_main_id = (TextView) view.findViewById(R.id.tv_main_id);
            tv_main_start_time = (TextView) view.findViewById(R.id.tv_main_start_time);
            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Shift shift = mShiftData[adapterPosition];
            mClickHandler.onClick(shift);
        }
    }

    @Override
    public ShiftAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.shift_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new ShiftAdapterViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ShiftAdapterViewHolder shiftAdapterViewHolder, int position) {
        Shift shift = mShiftData[position];
        shiftAdapterViewHolder.tv_main_id.setText(String.valueOf(shift.get_id()));
        if(shift.getStart() > 0){
            String startTime = GenericUtil.getFormattedTime(shift.getStart());
            shiftAdapterViewHolder.tv_main_start_time.setText(startTime);
        }
        if(shift.getEnd() > 0){
            String endTime = GenericUtil.getFormattedTime(shift.getEnd());
            shiftAdapterViewHolder.tv_main_end_time.setText(endTime);
        }


        //shiftAdapterViewHolder.tv_shift.setText(String.valueOf(shift.getShift_id()));
    }


    @Override
    public int getItemCount() {
        if (mShiftData == null) return 0;
        return mShiftData.length;
    }


    public void setShiftData(Shift[] shiftData) {
        mShiftData = shiftData;
        notifyDataSetChanged();
    }

}
