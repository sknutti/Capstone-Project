package com.sknutti.capstoneproject;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.sknutti.capstoneproject.dialogs.EditIntListDialogFragment;


public class InterviewListAdapter extends RecyclerView.Adapter<InterviewListAdapter.InterviewListAdapterViewHolder> {
    private Cursor mCursor;
    final private Context mContext;
    final private View mEmptyView;
    private int position;

    public class InterviewListAdapterViewHolder extends RecyclerView.ViewHolder {
        public final TextView mIsCurrent;
        public final TextView mInterviewListName;
        public final TextView mStartDate;
        public final TextView mEndDate;


        public InterviewListAdapterViewHolder(View view) {
            super(view);
            mIsCurrent = (TextView) view.findViewById(R.id.list_item_is_current_list);
            mInterviewListName = (TextView) view.findViewById(R.id.list_item_int_list_name);
            mStartDate = (TextView) view.findViewById(R.id.list_item_start_date);
            mEndDate = (TextView) view.findViewById(R.id.list_item_end_date);

            RxView.clicks(view).subscribe(aVoid -> {
                EditIntListDialogFragment dialogFragment = new EditIntListDialogFragment();
                mCursor.moveToPosition(getAdapterPosition());
                dialogFragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), dialogFragment.getClass().getName());
            });
            RxView.longClicks(view).subscribe(aVoid -> {
                setPosition(getAdapterPosition());
                view.showContextMenu();
            });
        }
    }


    public InterviewListAdapter(Context context, View emptyView) {
        mContext = context;
        mEmptyView = emptyView;
    }

    @Override
    public InterviewListAdapter.InterviewListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if ( parent instanceof RecyclerView ) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_int_list, parent, false);
            view.setFocusable(true);
            return new InterviewListAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(InterviewListAdapter.InterviewListAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        if (mCursor.getInt(InterviewListActivity.COL_IS_CURRENT_LIST) == 1) {
            holder.mIsCurrent.setBackground(mContext.getResources().getDrawable(R.drawable.ic_check_circle));
        } else {
            holder.mIsCurrent.setBackground(null);
        }
        holder.mInterviewListName.setText(mCursor.getString(InterviewListActivity.COL_NAME));
        holder.mStartDate.setText(mCursor.getString(InterviewListActivity.COL_START_DATE));
        holder.mEndDate.setText(mCursor.getString(InterviewListActivity.COL_END_DATE));
    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        if (mEmptyView != null) {
            mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }


    public Cursor getCursor() {
        return mCursor;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if ( viewHolder instanceof InterviewListAdapterViewHolder ) {
            InterviewListAdapterViewHolder vfh = (InterviewListAdapterViewHolder)viewHolder;
//            vfh.onClick(vfh.itemView);
        }
    }
}
