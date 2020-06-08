package id.ac.umn.bisik;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class ProfileFragment extends Fragment {
    String email,name,nim, picture;
    TextView tvName,tvNim,tvEmail;
    ImageButton ibEdit;
    ImageView ivPicture;
    FirebaseUser fAuth;
    FirebaseFirestore db;
    StorageReference storageReference;

    private RecyclerView rvDiscussionList;
    private DiscussionAdapter adapter;
    private ArrayList<Discussions> discussionArrayList;
    private ArrayList<String> totalComment;
    private FirebaseAuth mAuth;
    String username;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvName = view.findViewById(R.id.tvName);
        tvNim = view.findViewById(R.id.tvNim);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivPicture = view.findViewById(R.id.ivPicture);
        ibEdit = view.findViewById(R.id.ibEdit);

        fAuth = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        final DocumentReference docRef = db.collection("users").document(fAuth.getUid());
        // retrieve picture from firestore
        storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference profilRef = storageReference.child("users/"+fAuth.getUid()+"/profil.jpg");
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
                            tvName.setText(name);
                            tvNim.setText(nim);
                            tvEmail.setText(email);
                            if(picture == null){
                                ivPicture.setImageResource(R.drawable.ic_person_black_24dp);
                            }
                        }
                        else {
                            Log.d("signIn", "No such document");
                        }
                    } else {
                        Log.d("signIn", "get failed with ", task.getException());
                    }
                }
            });
        }
//      load discussion picture
        storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference picRef = storageReference.child("users/"+fAuth.getUid()+"/profil.jpg");
        picRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d( "onSuccess: ", String.valueOf(uri));
                Picasso.get().load(uri).into(ivPicture);
            }
        });

        ibEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        //my discussion code

        //create arrayList to save firestore data
        discussionArrayList = new ArrayList<>();
        totalComment = new ArrayList<>();

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

                            final DocumentReference docRef = db.collection("users").document(tempUser);
                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    DocumentSnapshot document = task.getResult();
                                    if(document.exists()){
                                        username = document.getString("name");
                                    }
                                    //retrieve status collection from firestore
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
                                                        totalComment.add(com);
                                                    }
                                                }
                                                Log.d("onComplete: Size", String.valueOf(totalComment.size()));

                                                discussionArrayList.add(new Discussions(tempTitle, username, tempIdDiscussion, tempPic, totalComment.size()));
                                                totalComment.clear();

                                                rvDiscussionList = view.findViewById(R.id.rvDiscussions);

                                                adapter = new DiscussionAdapter( getContext() ,discussionArrayList);

                                                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());

                                                rvDiscussionList.setLayoutManager(layoutManager);

                                                rvDiscussionList.setAdapter(adapter);
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                }
            }
        });
//        //retrieve status collection from firestore
//        db.collection("discussions").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull final Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        final String tempTitle = document.getString("title");
//                        final String tempUser = document.getString("idUser");
//                        final String tempPic = document.getString("picture");
//                        final String tempIdDiscussion = document.getId();
////                        Log.d("onComplete: ID DISC",tempIdDiscussion);
//                        db.collection("comments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                            @Override
//                            public void onComplete(@NonNull final Task<QuerySnapshot> task) {
//                                if (task.isSuccessful()) {
//                                    for (QueryDocumentSnapshot document : task.getResult()) {
//                                        final String user1 = document.getString("idUser");
//                                        if(document.getString("idDiscussion").equals(tempIdDiscussion)){
//                                            String idDisCom = document.getString("idDiscussion");
//                                            String com = document.getString("comment");
//                                            Log.d("onComplete: ", idDisCom+ " === "+ tempIdDiscussion);
////                                            totalComment.add(com);
//                                        }
//                                    }
//                                    Log.d("onComplete: Size", String.valueOf(totalComment.size()));
////                                    Log.d("username post: ", tempUser);
//                                    db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                            if (task.isSuccessful()) {
//                                                for (QueryDocumentSnapshot data : task.getResult()) {
//                                                    String idUser = data.getId();
//                                                    if(idUser.equals(tempUser)){
//                                                        username = data.getString("name");
//                                                    }
//                                                }
//                                                discussionArrayList.add(new Discussions(tempTitle, username, tempIdDiscussion, tempPic, totalComment.size()));
//                                                totalComment.clear();
//
//                                                rvDiscussionList = view.findViewById(R.id.rvDiscussions);
//
//                                                adapter = new DiscussionAdapter( getContext() ,discussionArrayList);
//
//                                                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
//
//                                                rvDiscussionList.setLayoutManager(layoutManager);
//
//                                                rvDiscussionList.setAdapter(adapter);
//                                            }
//                                        }
//                                    });
//                                }
//                            }
//                        });
//                    }
//                }
//            }
//        });

        // Inflate the layout for this fragment
        return view;
    }
}
