package kitchen_joshua.roberts.edu.contactappdraft;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RelationshipAdapter extends RecyclerView.Adapter<RelationshipAdapter.RelationshipViewHolder> {
    private ArrayList<String> relationshipCategoryList;
    private ArrayList<String> passedInRelationships;
    private OnItemClickListener mListener;
    public static boolean[] selected = new boolean[100];

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class RelationshipViewHolder extends  RecyclerView.ViewHolder {
        public TextView relationshipCategory;
        public ImageView deleteRelationshipImage;
        //100 is arbitrary, serves as a feasible upper limit for the number of categories.


        public RelationshipViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            relationshipCategory = itemView.findViewById(R.id.single_relationship_category_view);
            deleteRelationshipImage = itemView.findViewById(R.id.delete_relationship_button);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ( listener != null ) {
                        int position = getAdapterPosition();
                        if ( position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                            if( !selected[position] ) {
                                relationshipCategory.setBackgroundResource(R.color.defaultBackgroundSelectedRelationship);
                                selected[position] = true;
                            } else {
                                relationshipCategory.setBackgroundResource(R.color.white);
                                selected[position] = false;
                            }
                        }
                    }
                }
            });
            deleteRelationshipImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ( listener != null ) {
                        int position = getAdapterPosition();
                        if ( position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });
        }
    }

    public RelationshipAdapter(ArrayList<String> mRelationshipCategoryList, ArrayList<String> passedIn ) {
        relationshipCategoryList = mRelationshipCategoryList;
        passedInRelationships = passedIn;
        Arrays.fill(selected, false);
    }

    @NonNull
    @Override
    public RelationshipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.relationship_recycler_view_item, parent, false);
        RelationshipViewHolder rvh = new RelationshipViewHolder(view, mListener);
        return rvh;
    }

    @Override
    public void onBindViewHolder(@NonNull RelationshipViewHolder holder, int position) {
        String currentItem = relationshipCategoryList.get(position);

        if(passedInRelationships.contains( currentItem )) {
            selected[position] = true;
            holder.relationshipCategory.setBackgroundResource(R.color.defaultBackgroundSelectedRelationship);
        } else {
            selected[position] = false;
            holder.relationshipCategory.setBackgroundResource(R.color.white);
        }
        holder.relationshipCategory.setText( currentItem );
    }

    @Override
    public int getItemCount() {
        return relationshipCategoryList.size();
    }
}
