package id.ac.umn.bisik;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {
    private EditText etAddComment;
    private Button ibPostComment;
    private ImageView ivUser, ivDisPic;

    private RecyclerView rvCommentList;
    private CommentAdapter adapter;
    private ArrayList<Comments> commentArrayList;

    StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    //Firebase Firestore
    private FirebaseFirestore db;

    String idDiscussion, username, comment , title, idUser, description, picture;
    TextView tvTitle, tvUser, tvDescription;

    ArrayList<String> listComment;
    ArrayList<String> listIdUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        //retrieve data to intent
        Intent intent = getIntent();
        idDiscussion = intent.getStringExtra("idDiscussion");

        // database
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        commentArrayList = new ArrayList<>();
        listComment = new ArrayList<>();
        listIdUser = new ArrayList<>();

        //Initialize views
        tvTitle = findViewById(R.id.tvTitle);
        tvUser = findViewById(R.id.tvUser);
        tvDescription = findViewById(R.id.tvDescription);
        ibPostComment = findViewById(R.id.ibPostComment);
        ivUser = findViewById(R.id.ivUser);
        ivDisPic = findViewById(R.id.ivDisPic);
        etAddComment = findViewById(R.id.etAddComment);

        //        toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle("Details");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CommentActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
//        end of toolbar code

        final DocumentReference docRef = db.collection("discussions").document(idDiscussion);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        title = document.getString("title");
                        idUser = document.getString("idUser");
                        picture = document.getString("picture");
                        description = document.getString("description");
                        tvTitle.setText(title);
                        tvDescription.setText(description);
                    }
                    final DocumentReference docRef1 = db.collection("users").document(idUser);
                    docRef1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                username = document.getString("name");
                                tvUser.setText(username);
                            }
                        }
                    });

//                    load discussion picture
                    storageReference = FirebaseStorage.getInstance().getReference();
                    StorageReference picRef = storageReference.child("images/"+picture);
                    picRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d( "onSuccess: ", String.valueOf(uri));
                            Picasso.get().load(uri).into(ivDisPic);
                        }
                    });
                }
            }
        });

        //retrieve status collection from firestore
        db.collection("comments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if(document.getString("idDiscussion").equals(idDiscussion)){
                            final String tempComment = document.getString("comment");
                            final String tempUser = document.getString("idUser");
                            Log.d("onComplete User", tempComment);
                            listComment.add(tempComment);

                            final DocumentReference docRef = db.collection("users").document(tempUser);
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        username = document.getString("name");
                                        listIdUser.add(username);
                                        Log.d("onComplete User", username);

                                        commentArrayList.add(new Comments(username, tempComment));

                                        rvCommentList = findViewById(R.id.rvComments);

                                        adapter = new CommentAdapter(getApplicationContext(), commentArrayList);

                                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

                                        rvCommentList.setLayoutManager(layoutManager);

                                        rvCommentList.setAdapter(adapter);
                                    }
                                }
                            });
                        } else {
                            //code kalo gaada yang comment
                        }
                    }
                }
            }
        });

        ibPostComment.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                //user comment
                comment = etAddComment.getText().toString();

                Log.d( "onClick: ", "post comment");
                // Create a new comments with a comment, idDiscussion, and idUser
                Map<String, Object> comments = new HashMap<>();
                comments.put("comment", comment);
                comments.put("idDiscussion", idDiscussion);
                comments.put("idUser", user.getUid());

                // Add a new document with a generated ID
                db.collection("comments")
                        .add(comments)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("TAG", "Error adding document", e);
                            }
                        });
                Toast.makeText(CommentActivity.this,"Comment Posted !" , Toast.LENGTH_SHORT).show();
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        });
    }
}
