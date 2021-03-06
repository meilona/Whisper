package id.ac.umn.bisik;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DiscussionDeleteAdapter extends RecyclerView.Adapter<DiscussionDeleteAdapter.DiscussionViewHolder> {

    private Context mContext;
    private List<DiscussionsDelete> dataList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public DiscussionDeleteAdapter(Context context, ArrayList<DiscussionsDelete> dataList) {
        this.mContext = context;
        this.dataList = dataList;
    }

    @Override
    public DiscussionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.discussions_row_delete, parent, false);
        DiscussionViewHolder views = new DiscussionViewHolder(view, mListener);
        return views;
    }

    @Override
    public void onBindViewHolder(final DiscussionViewHolder holder, final int position) {
        if(dataList != null) {
            final DiscussionsDelete dis = dataList.get(position);
            holder.tvUser.setText("Posted by " + dis.getUser());
            holder.tvTitle.setText(dis.getTitle());
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

    void setDaftarDiscussion(List<DiscussionsDelete> dis) {
        dataList = dis;
        notifyDataSetChanged();
    }

    public DiscussionDeleteAdapter(ArrayList<DiscussionsDelete> dis) {
        dataList = dataList;
    }

    public class DiscussionViewHolder extends RecyclerView.ViewHolder{
        public TextView tvUser, tvTitle;
        public CardView cvMain;
        public ImageView ivPicture;
        public ImageView mDeleteImage;
        public List<String> listDeleteId = new ArrayList<>();

        public DiscussionViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            tvUser = (TextView) itemView.findViewById(R.id.tvUser);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            ivPicture = (ImageView) itemView.findViewById(R.id.ivPicture);
            cvMain = (CardView) itemView.findViewById(R.id.cvMain);
            mDeleteImage = (ImageView) itemView.findViewById(R.id.ivDelete);

            itemView.setClickable(false);
            mDeleteImage.setClickable(true);


            final Context context = itemView.getContext();

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    int itemPosition = getLayoutPosition();
//                    String idDiscussion = dataList.get(itemPosition).getIdDiscussion();
//                    Log.d("onClick: ", idDiscussion);
//                    Toast.makeText(context, "Buka " + itemPosition, Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(context,CommentActivity.class);
//                    intent.putExtra("idDiscussion",idDiscussion);
//                    context.startActivity(intent);
//
//                }
//            });

            mDeleteImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });
        }
    }
}
