package id.ac.umn.bisik;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditProfileActivity extends AppCompatActivity {
    private String email,name,nim, picture;
    private EditText etName;
    private ImageButton btnSave;
    private TextView tvNim, tvEmail;
    private ImageButton ibEdit;
    private ImageView ivPicture;
    private FirebaseUser fAuth;
    private Uri filePath;

    private RecyclerView rvDiscussionList;
    private DiscussionDeleteAdapter adapter;
    private ArrayList<DiscussionsDelete> discussionArrayList;
    private ArrayList<String> listDelete;
    String username;

    private String sName = null;

    //Firebase
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    // request code
    private final int PICK_IMAGE_REQUEST = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etName = findViewById(R.id.etName);
        tvNim = findViewById(R.id.tvNim);
        tvEmail = findViewById(R.id.tvEmail);
        ivPicture = findViewById(R.id.ivPicture);
        ibEdit = findViewById(R.id.ibEdit);
        btnSave = findViewById(R.id.btnSave);

        fAuth = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        //        toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle("Edit Profile");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
//        end of toolbar code

        final DocumentReference docRef = db.collection("users").document(fAuth.getUid());
        // retrieve picture from firestore
        storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference profilRef = storageReference.child("status/"+fAuth.getUid()+"/profil.jpg");
        profilRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(ivPicture);
            }
        });

        if(fAuth != null){
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            email = document.getString("email");
                            name = document.getString("name");
                            nim = document.getString("nim");
                            picture = document.getString("picture");
                            etName.setHint(name);
                            tvNim.setText(nim);
                            tvEmail.setText(email);
                            if(picture == null){
                                ivPicture.setImageResource(R.drawable.ic_person_black_24dp);
                            }
                        }
//                        TINGGAL DI BENERIN LOGHNYA
                        else {
                            Log.d("signIn", "No such document");
                        }
                    } else {
                        Log.d("signIn", "get failed with ", task.getException());
                    }
                }
            });
        }
        ibEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Tag ", "pilih image");
                chooseImage();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
//                Fragment fragment = new ProfileFragment();
//                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//                transaction.replace(R.id.profileMenu, fragment);
//                transaction.commit();
            }
        });

        //my discussion code

        //create arrayList to save firestore data
        discussionArrayList = new ArrayList<>();
        listDelete = new ArrayList<>();

        //retrieve status collection from firestore
        db.collection("discussions").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        final String tempUser = document.getString("idUser");
                        if(document.getString("idUser").equals(fAuth.getUid())){
                            final String tempTitle = document.getString("title");
                            final String tempPic = document.getString("picture");
                            final String tempIdDiscussion = document.getId();
//                        Log.d("onComplete: ID DISC",tempIdDiscussion);
                            db.collection("comments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            final String user1 = document.getString("idUser");
                                            if(document.getString("idDiscussion").equals(tempIdDiscussion)){
                                                String idDisCom = document.getString("idDiscussion");
                                                String com = document.getString("comment");
                                                Log.d("onComplete: ", idDisCom+ " === "+ tempIdDiscussion);
                                            }
                                        }
//                                    Log.d("username post: ", tempUser);
                                        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot data : task.getResult()) {
                                                        String idUser = data.getId();
                                                        if(idUser.equals(tempUser)){
                                                            username = data.getString("name");
                                                        }
                                                    }
                                                    discussionArrayList.add(new DiscussionsDelete(tempTitle, username, tempIdDiscussion, tempPic));

                                                    rvDiscussionList = findViewById(R.id.rvDiscussions);

                                                    adapter = new DiscussionDeleteAdapter(getApplicationContext(), discussionArrayList);

                                                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(EditProfileActivity.this);

                                                    rvDiscussionList.setLayoutManager(layoutManager);

                                                    rvDiscussionList.setAdapter(adapter);

                                                    adapter.setOnItemClickListener(new DiscussionDeleteAdapter.OnItemClickListener() {
                                                        @Override
                                                        public void onDeleteClick(int position) {
                                                            String idDiscussion = discussionArrayList.get(position).getIdDiscussion();
                                                            Log.d("onClick: ", idDiscussion);
                                                            listDelete.add(idDiscussion);
                                                            removeItem(position);
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }
    // end onCreate method

    public void removeItem(int position) {
        discussionArrayList.remove(position);
        adapter.notifyItemRemoved(position);
    }

    private void chooseImage() {
        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Image from here..."),PICK_IMAGE_REQUEST);
    }

    // Override onActivityResult method
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            try {

                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap( this.getContentResolver(), filePath);
                ivPicture.setImageBitmap(bitmap);
            }

            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }

    // UploadImage method
    private void save() {
        for(int i=0; i<listDelete.size();i++){
            db.collection("discussions").document(listDelete.get(i)).delete();
        }
        if (filePath != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            final StorageReference imageFolder = storageReference.child("users/"+fAuth.getUid()+"/profil.jpg");
            imageFolder.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    mDialog.dismiss();

                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //Upload this url to avtar property of user
                            //First you need to add avtar property on user model
                            Map<String,Object> imageUpdate = new HashMap<>();
                            imageUpdate.put("imageUrl",uri.toString());
                        }
                    });
                    if(sName!= null){
                        sName = etName.getText().toString();
                        db.collection("users").document(fAuth.getUid()).update("name",sName);
                    }
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    double progress = (100.0* taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    mDialog.setMessage("Uploading : "+progress+"%");
                }
            });
        } else {
            //kalo ga ganti foto
            sName = etName.getText().toString();
            if(!TextUtils.isEmpty(sName)){
                //ganti nama
                Log.d("save: ", sName);
                db.collection("users").document(fAuth.getUid()).update("name",sName);
            }
        }
        Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
