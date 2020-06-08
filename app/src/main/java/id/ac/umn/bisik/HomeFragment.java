package id.ac.umn.bisik;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;


public class HomeFragment extends Fragment {
    private RecyclerView rvDiscussionList;
    private Button btnSName, btnSCom;
    private DiscussionAdapter adapter;
    private ArrayList<Discussions> discussionArrayList;
    private ArrayList<String> totalComment;
    StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    //Firebase Firestore
    private FirebaseFirestore db;

    private boolean ascending = true;
    private boolean cAscending = true;
    boolean name = false;
    boolean comment = false;

    String username;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        btnSName = view.findViewById(R.id.btnSName);
        btnSCom = view.findViewById(R.id.btnSCom);

        // database
        db = FirebaseFirestore.getInstance();

        //create arrayList to save firestore data
        discussionArrayList = new ArrayList<>();
        totalComment = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        btnSName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortName(ascending);
                ascending = !ascending;
                name = true;
                Log.d("onClick: ", String.valueOf(ascending));
                if(!ascending){
                    btnSName.setText("Sort Name DESC");
                } else {
                    btnSName.setText("Sort Name ASC");
                }
            }
        });

        btnSCom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortComment(cAscending);
                cAscending = !cAscending;
                comment = true;
                btnSName.setText("Sort Comment DESC");
            }
        });

        //retrieve status collection from firestore
        db.collection("discussions").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        final String tempTitle = document.getString("title");
                        final String tempUser = document.getString("idUser");
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
        });

        // Inflate the layout for this fragment
        return view;
    }

    private void sortName(boolean asc)
    {
        //SORT ARRAY ASCENDING AND DESCENDING

        if (asc){
            Collections.sort(discussionArrayList);
        } else {
         Collections.reverse(discussionArrayList);
        }

        //ADAPTER
        adapter = new DiscussionAdapter(getContext(), discussionArrayList);
        rvDiscussionList.setAdapter(adapter);
    }

    private void sortComment(boolean asc)
    {
        //SORT ARRAY ASCENDING AND DESCENDING

        if (comment && asc){
            Collections.sort(discussionArrayList, new DiscussionChainedComparator(new DiscussionCommentComparator()));
        } else {
//            Collections.reverse(discussionArrayList, new DiscussionChainedComparator(new DiscussionNameComparator()));
        }

        //ADAPTER
        adapter = new DiscussionAdapter(getContext(), discussionArrayList);
        rvDiscussionList.setAdapter(adapter);
    }
}
