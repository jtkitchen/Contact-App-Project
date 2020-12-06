package kitchen_joshua.roberts.edu.contactappdraft;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ContactAdapter extends FirestoreRecyclerAdapter<Contact, ContactAdapter.ContactHolder> {
    private OnContactClickListener listener;
    public CardView.LayoutParams params;
    public CardView rootView;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference contactBookRef = db.collection("Contacts");
    public ArrayList<String> passedInRelCategories;
    public ArrayList<String> contactsRelCategories = new ArrayList<>();


    public ContactAdapter(@NonNull FirestoreRecyclerOptions<Contact> options, ArrayList<String> rCategories ) {
        super(options);
        passedInRelCategories = rCategories;
    }

    @Override
    protected void onBindViewHolder(@NonNull ContactHolder contactHolder, int i, @NonNull Contact contact) {
        contactsRelCategories = contact.getRelationships();

        if( containsRel( passedInRelCategories, contactsRelCategories ) || passedInRelCategories.size() == 0) {
            String tempFirstName = contact.getFirstName();
            tempFirstName = tempFirstName.substring(0, 1).toUpperCase() + tempFirstName.substring(1);
            String tempLastName = contact.getLastName();
            tempLastName = tempLastName.substring(0, 1).toUpperCase() + tempLastName.substring(1);
            contactHolder.full_name_textView.setText(tempFirstName + " " + tempLastName);
            contactHolder.phone_number_textView.setText(contact.getPhoneNumber());
        } else {
            //Makes non-matching contacts not be shown
            rootView.setLayoutParams( params );
        }
    }
                                                  //PassedIn                    //InContacts
    private boolean containsRel( ArrayList<String> searchedFor, ArrayList<String> searchedIn ) {
        boolean found = false;
        int count = 0;
        for( int i = 0; i < searchedIn.size(); i++ ) {
            if(searchedFor.contains( searchedIn.get(i) ) ) {
                count++;
            }
        }
        if( count == searchedFor.size() ) { found = true; }
        return found;
    }

    @NonNull
    @Override
    public ContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_item_recycler_view,
                parent, false);

        return new ContactHolder(v);
    }

    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }


    class ContactHolder extends RecyclerView.ViewHolder {
        TextView full_name_textView;
        TextView phone_number_textView;
        TextView relationships_textView;
        RelativeLayout itemLayout;
        CardView cardView;

        public ContactHolder(@NonNull View itemView) {
            super(itemView);
            //lets me "filter" by selected relationships without having to query Firestore

            params = new CardView.LayoutParams(0,0);
            rootView = itemView.findViewById(R.id.card_view);

            itemLayout = itemView.findViewById(R.id.contact_recycler_view_relative_layout);
            cardView = itemView.findViewById(R.id.card_view);

            full_name_textView = itemView.findViewById(R.id.text_view_full_name);
            phone_number_textView = itemView.findViewById(R.id.text_view_phone_number);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if ( position != RecyclerView.NO_POSITION && listener != null){
                        listener.onContactClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public interface OnContactClickListener {
        void onContactClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnContactClickListener(OnContactClickListener listener){
        this.listener = listener;
    }
}
