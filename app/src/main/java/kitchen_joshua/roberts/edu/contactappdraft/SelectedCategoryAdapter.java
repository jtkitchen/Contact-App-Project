package kitchen_joshua.roberts.edu.contactappdraft;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SelectedCategoryAdapter extends RecyclerView.Adapter<SelectedCategoryAdapter.SelectedCategoryHolder> {
    private ArrayList<String> categories;
    private ArrayList<String> selectedCategories;
    private OnItemClickListener listener;
    public static boolean[] selected = new boolean[100];

    public interface OnItemClickListener {
        void onItemClick( int position );
    }

    public void setOnItemClickListener( OnItemClickListener listen ) {
        listener = listen;
    }

    //TODO Add an ability to clear all selected relatinoships from the filter
    public static class SelectedCategoryHolder extends RecyclerView.ViewHolder{
        public TextView categoryTextView;
        public RelativeLayout selectedRelationshipView;
        public SelectedCategoryHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.text_view_select_relationship);
            selectedRelationshipView = itemView.findViewById(R.id.single_relationship_card_view_selector);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if( listener != null ) {
                        int position = getAdapterPosition();
                        if( position != RecyclerView.NO_POSITION ) {
                            if( !selected[position] ) {
                                selectedRelationshipView.setBackgroundResource(R.color.selectedRelationship);
                                selected[position] = true;
                            } else {
                                selectedRelationshipView.setBackgroundResource(R.color.defaultBackgroundSelectedRelationship);
                                selected[position] = false;
                            }
                            listener.onItemClick(position);

                        }
                    }
                }
            });
        }
    }

    public SelectedCategoryAdapter(ArrayList<String> categories, ArrayList<String> selectedCategories ) {
        this.selectedCategories = selectedCategories;
        this.categories = categories;
    }

    @NonNull
    @Override
    public SelectedCategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_relationship_categories_recycler_view, parent, false);
        SelectedCategoryHolder sch = new SelectedCategoryHolder(v, listener);
        return sch;
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedCategoryHolder holder, int position) {
        String currentCategory = categories.get( position );
        holder.categoryTextView.setText( currentCategory );

        if(selectedCategories.contains( currentCategory )) {
            selected[position] = true;
            holder.selectedRelationshipView.setBackgroundResource(R.color.selectedRelationship);
        } else {
            selected[position] = false;
            holder.selectedRelationshipView.setBackgroundResource(R.color.defaultBackgroundSelectedRelationship);
        }
        holder.categoryTextView.setText( currentCategory );
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

}
