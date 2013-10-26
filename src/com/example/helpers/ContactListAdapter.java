package com.example.helpers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.bouncecloud.R;


public class ContactListAdapter extends BaseAdapter {
	
	LayoutInflater layoutInflater; 
	String TAG = "ContactListAdapter"; 
	
	public ContactListAdapter(Context ctx)
	{
			this.layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
	}
	
	@Override
	public int getCount() {
		Log.d(TAG, "getCount called and returned" + DataHolder.getDataHolder().getContactsSize());
		return DataHolder.getDataHolder().getContactsSize(); 
	}

	@Override
	public Object getItem(int position) {
		Log.d(TAG, "getItem called");
		return null;
	}

	@Override
	public long getItemId(int position) {
		Log.d(TAG, "getItemId called");
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		 Log.d(TAG, "getView called");
		 ViewHolder viewHolder;
	     if (convertView == null) {
	            convertView = layoutInflater.inflate(R.layout.contact_view, null);
	            viewHolder = new ViewHolder();
	            viewHolder.name = (TextView) convertView.findViewById(R.id.contact_name);
	            viewHolder.id = (TextView) convertView.findViewById(R.id.contact_id);
	            convertView.setTag(viewHolder);
	     } else {
	            viewHolder = (ViewHolder) convertView.getTag();
	     }
	     applyName(viewHolder.name, position);
	     applyId(viewHolder.id, position);
	     
	     return convertView;
	}
	
	private void applyName(TextView name, int position) {
		Log.d(TAG, "setting name to " + DataHolder.getDataHolder().getContactAtIndex(position).getLogin().toString());
        name.setText(DataHolder.getDataHolder().getContactAtIndex(position).getLogin().toString()); 
    }

    private void applyId(TextView id, int position) {
		Log.d(TAG, "setting id to " + DataHolder.getDataHolder().getContactAtIndex(position).getId().toString());
        id.setText(DataHolder.getDataHolder().getContactAtIndex(position).getId().toString());
    }
	
    static class ViewHolder {
        TextView name;
        TextView id;
    }


}
