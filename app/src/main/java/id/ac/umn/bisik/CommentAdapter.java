package id.ac.umn.bisik;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.StatusViewHolder> {
    private List<Comments> commentList;
    private Context mContext;

    public CommentAdapter(Context context, ArrayList<Comments> commentList) {
        this.mContext = context;
        this.commentList = commentList;
    }

    @Override
    public CommentAdapter.StatusViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.comments_row, parent, false);
        return new CommentAdapter.StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CommentAdapter.StatusViewHolder holder, int position) {
        holder.tvUser.setText(commentList.get(position).getUser());
        holder.tvComment.setText(commentList.get(position).getComment());

//        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
//        StorageReference discRef = storageReference.child("users/"+ pic);
//        discRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                Log.d( "onSuccess: ", String.valueOf(uri));
//                Picasso.get()
//                        .load(uri)
//                        .resize(100, 50)
//                        .centerCrop()
//                        .into(holder.ivPicture);
//            }
//        });

    }

    @Override
    public int getItemCount() {
        if(commentList != null){
            return commentList.size();
        } else {
            return 0;
        }
    }

    public class StatusViewHolder extends RecyclerView.ViewHolder{
        private TextView tvUser, tvComment;
        private CardView cvMain;

        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = (TextView) itemView.findViewById(R.id.tvUser);
            tvComment = (TextView) itemView.findViewById(R.id.tvComment);
            cvMain = (CardView) itemView.findViewById(R.id.cvMain);

        }
    }
}