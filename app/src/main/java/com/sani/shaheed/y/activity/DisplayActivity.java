package com.sani.shaheed.y.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.sani.shaheed.y.model.Entry;
import com.sani.shaheed.y.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class DisplayActivity extends AppCompatActivity {

    private com.sani.shaheed.y.model.ListAdapter listAdapter;
    private RecyclerView mEntryList;
    FirebaseFirestore db;

    ArrayList<Entry> mArrayList;
    List<Entry> mList;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Query mQueryCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mEntryList = findViewById(R.id.entry_list);

        mArrayList = new ArrayList<>();
        mList = new ArrayList<>();

        listAdapter = new com.sani.shaheed.y.model.ListAdapter(mList, this);

        mEntryList.setHasFixedSize(true);
        mEntryList.setLayoutManager(new LinearLayoutManager(this));
        mEntryList.setAdapter(listAdapter);


        String currentUserId = mAuth.getCurrentUser().getUid();

        db.collection("Journal Entry").document(currentUserId).collection("My Entry").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for (DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                    if (doc.getType() == DocumentChange.Type.ADDED || doc.getType() == DocumentChange.Type.REMOVED){

                        String the_title, contents, picture, uid, date;

                        the_title = doc.getDocument().getString("Title");
                        contents = doc.getDocument().getString("Content");
                        picture = doc.getDocument().getString("Picture");
                        uid = doc.getDocument().getString("UID");
                        date = doc.getDocument().getString("Date");

                        if (!the_title.isEmpty() && !contents.isEmpty() && !picture.isEmpty() && !uid.isEmpty() && date.isEmpty()){
                            Entry entry = new Entry(the_title, contents, picture, date, uid);
                            mList.add(entry);
                        }

                    }
                }
            }
        });

        mEntryList = (RecyclerView) findViewById(R.id.entry_list);
        mEntryList.setHasFixedSize(true);
        mEntryList.setLayoutManager(new LinearLayoutManager(this));


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                if(firebaseAuth.getCurrentUser() == null ){
                    startActivity(new Intent(DisplayActivity.this, MainActivity.class));
                }
            }
        };

        if (isNetworkConnected() || isWifiConnected()) {
            Toast.makeText(this, "Network Is Active", Toast.LENGTH_SHORT).show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("No Internet Connection")
                    .setCancelable(false)
                    .setMessage("It looks like your internet connection is off. Please turn it " +
                            "on or Some features might not work")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).setIcon(R.drawable.warning).show();
        }

    }

    private boolean isWifiConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE); // 1
        assert connMgr != null;
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo(); // 2
        return networkInfo != null && networkInfo.isConnected(); // 3
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connMgr != null;
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && (ConnectivityManager.TYPE_WIFI == networkInfo.getType()) && networkInfo.isConnected();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

    }

    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_add){

            startActivity(new Intent(DisplayActivity.this, EntryActivity.class));

        }
        if (item.getItemId() == R.id.action_logout){
            signout();

        }

        return super.onOptionsItemSelected(item);
    }

    private void signout() {
        mAuth.signOut();
    }
}
