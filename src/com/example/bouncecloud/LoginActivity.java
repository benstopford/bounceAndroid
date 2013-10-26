package com.example.bouncecloud;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.helpers.DataHolder;
import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;

public class LoginActivity extends Activity implements QBCallback {
	
	EditText usernameView;
	EditText passwordView;
	QBUser user;
	ProgressDialog progressDialog; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		
		usernameView = (EditText) findViewById(R.id.editUsername);
		passwordView = (EditText) findViewById(R.id.editPassword);
		
		progressDialog = new ProgressDialog(this);
	    progressDialog.setMessage("Loading");
	}
	 
	public void onLoginButtonClick(View v)
	{
	       String username = usernameView.getText().toString();
	       String password = passwordView.getText().toString();

	       user = new QBUser(username, password);

	       progressDialog.show(); 
	       QBUsers.signIn(user, LoginActivity.this);
	       
	}

	@Override
	public void onComplete(Result result) {
		// TODO Auto-generated method stub
		
		if (progressDialog != null) {
            progressDialog.dismiss();
        }
		
		if (result.isSuccess()) {	
            Intent intent = new Intent(this, BouncesActivity.class);
            intent.putExtra("myId", user.getId());
            intent.putExtra("myUsername", user.getLogin());
            intent.putExtra("myPassword", user.getPassword());
            
            DataHolder.getDataHolder().setSignInUser(user); 

            startActivity(intent);
            Toast.makeText(this, "You've been successfully logged in application",
                    Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Error(s) occurred. Look into DDMS log for details, " +
                    "please. Errors: " + result.getErrors()).create().show();
        }
		
	}

	@Override
	public void onComplete(Result arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}
}
