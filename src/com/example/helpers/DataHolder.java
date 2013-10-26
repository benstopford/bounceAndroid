package com.example.helpers;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.example.interfaces.ContactListListener;
import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserPagedResult;

public class DataHolder {
	private String TAG = "DataHolder"; 
	private static DataHolder dataHolder;
	private QBUser signedInUser;
	private List<QBUser> contacts; 
	private static ArrayList<ContactListListener> contactListListeners;
	private QBCustomObject contactsObject; 
	
	public void registerContactListListener(ContactListListener listener)
	{
		if (contactListListeners == null) contactListListeners = new ArrayList<ContactListListener>(); 
		contactListListeners.add(listener);
	}
	
	public static synchronized DataHolder getDataHolder() {
        if (dataHolder == null) {
            dataHolder = new DataHolder();
        }
		if (contactListListeners == null) contactListListeners = new ArrayList<ContactListListener>(); 
        return dataHolder;
    }
	
	public void setSignInUser(QBUser signInUser) {
		this.signedInUser = signInUser; 
    }

    public QBUser getSignInUserId() {
        return signedInUser;
    }
    
    private void addUsers(ArrayList<String> ss)
    {
        	QBUsers.getUsersByIDs(ss, new QBCallback() {
				@Override
				public void onComplete(Result arg0, Object arg1) {
					// TODO Auto-generated method stub
				}
				@Override
				public void onComplete(Result result) {
					// TODO Auto-generated method stub
					if (result.isSuccess()) {
						QBUserPagedResult results = (QBUserPagedResult) result; 
						ArrayList<QBUser> users = results.getUsers();
						contacts = users;
						
				        notifyContactChanged();
				          
					}
				}
			});
        	
    }
    
    void notifyContactChanged()
    {
	  if (contactListListeners == null) contactListListeners = new ArrayList<ContactListListener>(); 
      for (ContactListListener listener: contactListListeners) {
      	  listener.onContactsChanged();  
      }
    }
    
    public int getContactsSize()
    {
    	if (contacts==null) {
    		contacts = new ArrayList<QBUser>();   
    	}
    	return contacts.size(); 
    }
    
    public QBUser getContactAtIndex(int position)
    {
    	return contacts.get(position); 
    }
    
    public void addContact(QBUser user)
    {
    	if (contacts.contains(user)) 
    	{	
    		Log.d("TAG", "trying to add a contact which already exists");
    		return;
    	}
    	
    	contacts.add(user);
    	notifyContactChanged();
    	
    	ArrayList<String> ss = (ArrayList<String>) contactsObject.getFields().get("list");
    	ss.add(user.getId().toString());
    	
    	QBCustomObjects.updateObject(contactsObject, new QBCallback() {
			
			@Override
			public void onComplete(Result arg0, Object arg1) {
				
			}
			
			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					Log.d(TAG, "Contacts object was updated");
				} else {
					Log.e(TAG, "Contacts object was NOT UPDATED!!! ERROR");
				}
			}
		}); 
    	
    }
    
    public void loadContacts() {
    	QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder(); 
		requestBuilder.eq("owner", signedInUser.getId());  
		
		QBCustomObjects.getObjects("Contacts", requestBuilder, new QBCallback() {
			
			@Override
			public void onComplete(Result result, Object arg1) {
				// TODO Auto-generated method stub 
			}
			
			@Override
			public void onComplete(Result result) {
				// TODO Auto-generated method stub
				if (result.isSuccess()) {
		             QBCustomObjectLimitedResult coresult = (QBCustomObjectLimitedResult) result;
		             ArrayList<QBCustomObject> co = coresult.getCustomObjects();
		             Log.d("Records: ", co.toString());
		 		             
		             for (int i=0; i < co.size(); i++) {
		            	 
		            	contactsObject =co.get(i);
		            	Object a = co.get(i).getFields().get("list"); 
		            	Log.d("Objects ", " is " + a.toString());
		            	
		            	ArrayList<String> ss = (ArrayList<String>) a; 
		            	Log.d("Strings ", " is " + ss.toString());
		            	addUsers(ss);
		             }   
		         } else {
		             Log.e("Errors",result.getErrors().toString());
		         }		
			}
		}); 
    }
    
}
