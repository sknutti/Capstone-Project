package com.sknutti.capstoneproject;

import android.view.ContextMenu;

public class RecyclerViewContextMenuInfo implements ContextMenu.ContextMenuInfo {

    public RecyclerViewContextMenuInfo(int position, long id) {
        this.position = position;
        this.id = id;
    }

    final public int position;
    final public long id;
}