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
import com.sknutti.capstoneproject.dialogs.EditMemberDialogFragment;


public class MemberListAdapter extends RecyclerView.Adapter<MemberListAdapter.MemberListAdapterViewHolder> {
    private Cursor mCursor;
    final private Context mContext;
    final private View mEmptyView;
    private int position;

    public class MemberListAdapterViewHolder extends RecyclerView.ViewHolder {
        public final TextView mInterviewerInitials;
        public final TextView mMemberName;


        public MemberListAdapterViewHolder(View view) {
            super(view);
            mInterviewerInitials = (TextView) view.findViewById(R.id.list_item_interviewer_initials);
            mMemberName = (TextView) view.findViewById(R.id.list_item_member_name);

            RxView.clicks(view).subscribe(aVoid -> {
                EditMemberDialogFragment dialogFragment = new EditMemberDialogFragment();
                mCursor.moveToPosition(getAdapterPosition());
                dialogFragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), dialogFragment.getClass().getName());
            });
            RxView.longClicks(view).subscribe(aVoid -> {
                setPosition(getAdapterPosition());
                view.showContextMenu();
            });
        }
    }


    public MemberListAdapter(Context context, View emptyView) {
        mContext = context;
        mEmptyView = emptyView;
    }

    @Override
    public MemberListAdapter.MemberListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if ( parent instanceof RecyclerView ) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_member, parent, false);
            view.setFocusable(true);
            return new MemberListAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(MemberListAdapter.MemberListAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String fullName = mCursor.getString(MemberListActivity.COL_NAME);

        if (mCursor.getInt(MemberListActivity.COL_IS_INTERVIEWER) == 1) {
            holder.mInterviewerInitials.setText(Utility.getInitialsFromFullName(fullName));
            holder.mInterviewerInitials.setBackground(mContext.getResources().getDrawable(R.drawable.ic_dark_blank_circle));
        } else {
            holder.mInterviewerInitials.setText(null);
            holder.mInterviewerInitials.setBackground(null);
        }
        holder.mMemberName.setText(fullName);
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
        if ( viewHolder instanceof MemberListAdapterViewHolder ) {
            MemberListAdapterViewHolder vfh = (MemberListAdapterViewHolder)viewHolder;
//            vfh.onClick(vfh.itemView);
        }
    }
}
