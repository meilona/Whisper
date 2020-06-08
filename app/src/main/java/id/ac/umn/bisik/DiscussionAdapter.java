package id.ac.umn.bisik;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

    public class DiscussionAdapter extends RecyclerView.Adapter<DiscussionAdapter.DiscussionViewHolder> {

    private Context mContext;
    private List<Discussions> dataList;

    public DiscussionAdapter(Context context, ArrayList<Discussions> dataList) {
        this.mContext = context;
        this.dataList = dataList;
    }

    @Override
    public DiscussionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.discussions_row, parent, false);
        return new DiscussionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DiscussionViewHolder holder, final int position) {
        if(dataList != null) {
            final Discussions dis = dataList.get(position);
            String com = String.valueOf(dis.getComment());
            holder.tvUser.setText("Posted by " + dis.getUser());
            holder.tvTitle.setText(dis.getTitle());
            holder.tvComments.setText(com);
            String pic = dis.getPicture();

            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference discRef = storageReference.child("images/"+ pic);
            discRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.d( "onSuccess: ", String.valueOf(uri));
                    Picasso.get()
                            .load(uri)
                            .resize(100, 50)
                            .centerCrop()
                            .into(holder.ivPicture);
                }
            });

        } else {
            holder.tvTitle.setText("Belum ada discussion");
        }
    }

    @Override
    public int getItemCount() {
        if(dataList != null){
            return dataList.size();
        } else {
            return 0;
        }
    }

    public Discussions getDiscussionAtPosition(int posisi){
        return dataList.get(posisi);
    }

    void setDaftarDiscussion(List<Discussions> dis) {
        dataList = dis;
        notifyDataSetChanged();
    }

    public class DiscussionViewHolder extends RecyclerView.ViewHolder{
        private TextView tvUser, tvTitle, tvComments;
        private CardView cvMain;
        private ImageView ivPicture;

        public DiscussionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = (TextView) itemView.findViewById(R.id.tvUser);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            ivPicture = (ImageView) itemView.findViewById(R.id.ivPicture);
            cvMain = (CardView) itemView.findViewById(R.id.cvMain);
            tvComments = (TextView) itemView.findViewById(R.id.tvComments);

            itemView.setClickable(true);

            final Context context = itemView.getContext();

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int itemPosition = getLayoutPosition();
                    String idDiscussion = dataList.get(itemPosition).getIdDiscussion();
                    Log.d("onClick: ", idDiscussion);
//                    Toast.makeText(context, "Buka " + itemPosition, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context,CommentActivity.class);
                    intent.putExtra("idDiscussion",idDiscussion);
                    context.startActivity(intent);

                }
            });
        }
    }
}
