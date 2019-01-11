package io.korok.mycontacts;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import java.util.List;

import io.korok.mycontacts.model.Contact;
import io.korok.mycontacts.repos.ContactsRepo;
import io.korok.mycontacts.view.CLLayoutManager;
import io.korok.mycontacts.view.CLSyncMediator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get fake data
        List<Contact> contacts = ContactsRepo.getFakeData(this);


        // setup avatar RecyclerView
        RecyclerView recyclerView = findViewById(R.id.list_avatar);
        CLLayoutManager layoutManager = new CLLayoutManager();
        recyclerView.setLayoutManager(layoutManager);
        layoutManager.lockCenter(recyclerView);
        recyclerView.setAdapter(new ListAvatarAdapter(this, contacts));

        // setup detail RecyclerView
        RecyclerView rvDetail = findViewById(R.id.list_detail);
        rvDetail.setLayoutManager(new LinearLayoutManager(this));
        SnapHelper helper = new PagerSnapHelper();
        helper.attachToRecyclerView(rvDetail);
        rvDetail.setAdapter(new ListDetailAdapter(this, contacts));

        // sync each other
        CLSyncMediator.sync(recyclerView, rvDetail);
    }

    /**
     * Adapter for avatar list.
     */
    static class ListAvatarAdapter extends RecyclerView.Adapter<AvatarViewHolder> {
        private Context context;
        private List<Contact> data;

        ListAvatarAdapter(Context context, List<Contact> data) {
            this.context = context;
            this.data = data;
        }

        @NonNull
        @Override
        public AvatarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.listitem_avatar, null);
            return new AvatarViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AvatarViewHolder holder, int position) {
            holder.render(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data == null? 0: data.size();
        }
    }

    static class AvatarViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        Context context;

        public AvatarViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            ivAvatar = itemView.findViewById(R.id.list_avatar);
        }

        public void render(Contact c) {
            Glide.with(context)
                    .load(Uri.parse("file:///android_asset/avatars/"+c.avatar))
                    .into(ivAvatar);
        }
    }

    /**
     * Adapter for detail list.
     */
    static class ListDetailAdapter extends RecyclerView.Adapter<DetailViewHolder> {
        private List<Contact> data;
        private Context context;

        public ListDetailAdapter(Context context, List<Contact> data) {
            this.context = context;
            this.data = data;
        }

        public void setData(List<Contact> data) {
            this.data = data;
        }


        @NonNull
        @Override
        public DetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            ViewGroup view = (ViewGroup) inflater.inflate(R.layout.listitem_detail, parent, false);
            return new DetailViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DetailViewHolder holder, int position) {
            holder.render(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data == null? 0 : data.size();
        }
    }

    static class DetailViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvContent;
        private TextView tvTitle;
        private TextView tvIntro;


        public DetailViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.list_detail_firstname);
            tvContent = itemView.findViewById(R.id.list_detail_lastname);
            tvTitle = itemView.findViewById(R.id.list_detail_title);
            tvIntro = itemView.findViewById(R.id.list_detail_intro);
        }

        public void render(Contact data) {
            tvName.setText(data.firstName);
            tvContent.setText(data.lastName);
            tvTitle.setText(data.title);
            tvIntro.setText(data.introduction);
        }
    }
}
