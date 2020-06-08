package id.ac.umn.bisik;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;


public class AddStatusFragment extends Fragment {

    private StorageReference mStorageRef;
    private ImageButton ibImage;
    private ImageView ivPicture;
    private EditText etTitle, etDiscussion;
    private Button btnPost;
    private Uri filePath;


    //Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    // request code
    private final int PICK_IMAGE_REQUEST = 22;

    //popup dialog message
    Dialog statusDialog;
    TextView tvPop;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mAuth.getCurrentUser();
        user = mAuth.getCurrentUser();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_add_status, container, false);

        //initialize views
        mStorageRef = FirebaseStorage.getInstance().getReference();
        ibImage = view.findViewById(R.id.ibImage);
        ivPicture = view.findViewById(R.id.ivPicture);
        etTitle = view.findViewById(R.id.etTitle);
        etDiscussion = view.findViewById(R.id.etDiscussion);
        btnPost = view.findViewById(R.id.btnPost);

        statusDialog = new Dialog(getContext());

        //ambil database
        db = FirebaseFirestore.getInstance();

        ibImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Tag ", "pilih image");
                chooseImage();
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Tag ", "post image");
                uploadImage();
            }
        });

        return view;
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
                        .getBitmap( getActivity().getContentResolver(), filePath);
                ivPicture.setImageBitmap(bitmap);
            }

            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }

    // UploadImage method
    private void uploadImage()
    {
        final int check = 0;
        if (filePath != null) {
            final ProgressDialog mDialog = new ProgressDialog(getActivity());
            mDialog.setMessage("Uploading...");
            mDialog.show();

            final String image = UUID.randomUUID().toString();    //Random name image upload
            final StorageReference imageFolder = storageReference.child("images/"+image);
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
                    final DocumentReference docuser = db.collection("users").document(user.getUid());
                    docuser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    String title = etTitle.getText().toString();
                                    String discussion = etDiscussion.getText().toString();

                                    // Create a new user with a first, middle, and last name
                                    Map<String, Object> discussions = new HashMap<>();
                                    discussions.put("title", title);
                                    discussions.put("description", discussion);
                                    discussions.put("idUser", user.getUid());
                                    discussions.put("picture", image);
                                    // Add a new document with a generated ID
                                    db.collection("discussions").add(discussions).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d("TAG ", "DocumentSnapshot added with ID: " + documentReference.getId());
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("TAG ", "Error adding document", e);
                                        }
                                    });

                                    //balikin edit text jadi null
                                    etTitle.setText(null);
                                    etDiscussion.setText(null);
                                    ivPicture.setImageURI(null);
                                }
                            }

                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0* taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    mDialog.setMessage("Uploading : "+progress+"%");
                }
            });
        } else {
            // kalo ga taro foto kasih foto temp
        }
//        //refresh fragment page
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
//        ft.detach(AddStatusFragment.this).attach(AddStatusFragment.this).commit();
    }
}
