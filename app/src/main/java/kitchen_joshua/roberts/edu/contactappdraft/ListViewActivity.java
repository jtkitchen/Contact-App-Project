package kitchen_joshua.roberts.edu.contactappdraft;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.SearchView;

import android.view.inputmethod.EditorInfo;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
//import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.grpc.okhttp.internal.framed.FrameReader;

//change sort by relationship/name/...
//https://medium.com/firebase-developers/update-queries-without-
//changing-recyclerview-adapter-using-firebaseui-android-32098b3082b2

public class ListViewActivity extends AppCompatActivity implements Serializable {
    // variables to store the references to the data being passed through intents
    // TODO: change this to send a contact object, as most of the information is stored within it...
    public static final String DOCUMENT_EXTRA_TEXT = "DOCUMENT_EXTRA_TEXT";
    public static final String FIRST_EXTRA_TEXT = "FIRST_EXTRA_TEXT";
    public static final String LAST_EXTRA_TEXT = "LAST_EXTRA_TEXT";
    public static final String PHONE_EXTRA_TEXT = "PHONE_EXTRA_TEXT";
    public static final String EMAIL_EXTRA_TEXT = "EMAIL_EXTRA_TEXT";
    public static final String DESCRIPTION_EXTRA_TEXT = "DESCRIPTION_EXTRA_TEXT";
    public static final String RELATIONSHIP_EXTRA_TEXT = "RELATIONSHIP_EXTRA_TEXT";
    public static final String RELATIONSHIP_CATEGORY_EXTRA_TEXT = "RELATIONSHIP_CATEGORY_EXTRA_TEXT";
    public static final String CURRENTLY_SELECTED_RELATIONSHIPS = "CURRENTLY_SELECTED_RELATIONSHIPS";
    public static final String EXTRA_TEXT = "EXTRA_TEXT";

    // Firebase database set-up and usefull references
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference contactBookRef = db.collection("Contacts");
    private CollectionReference relationshipListRef = db.collection("RelationshipList");

    // Variables relating to the two recycler views created
    private ContactAdapter adapter;
    private SelectedCategoryAdapter categoryAdapter;
    private RecyclerView.LayoutManager categoryLayoutManager;
    private RecyclerView categoryRecyclerView;
    private RecyclerView contactRecyclerView;

    // Lists to contain the information used in updating and using the category-search funcition
    private ArrayList<String> relationshipCategories;
    private ArrayList<String> currentlySelectedRel;
    private String currentQuery = "firstName";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Cache should be sufficient for all queries
        //https://firebase.google.com/docs/firestore/manage-data/enable-offline#:~:text=is%20back%20online.-,Configure%20offline%20persistence,persistence%20is%20disabled%20by%20default.
        db.disableNetwork();

        relationshipCategories = new ArrayList<>();
        setContentView(R.layout.main_list_recycler_view);
        Intent selfIntent = getIntent();
        setCategories();

        //Updates relationship categories if any were added in the NewContact activity
        if( selfIntent.hasExtra( RELATIONSHIP_CATEGORY_EXTRA_TEXT )) {
            relationshipCategories = selfIntent.getStringArrayListExtra(RELATIONSHIP_CATEGORY_EXTRA_TEXT);
        }
        //Keeps track of the "filter" when viewing a contact and going back to the page
        if( selfIntent.hasExtra( CURRENTLY_SELECTED_RELATIONSHIPS )) {
            currentlySelectedRel = selfIntent.getStringArrayListExtra(CURRENTLY_SELECTED_RELATIONSHIPS);
        }
        if( currentlySelectedRel == null ) { currentlySelectedRel = new ArrayList<>(); }

        //Creates the button to create a new contact
        FloatingActionButton buttonAddContact = findViewById(R.id.button_add_contact);
        buttonAddContact.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new  Intent(ListViewActivity.this, NewContactActivity.class);
                Bundle b = new Bundle(); b.putBoolean("editingKey", false);
                b.putStringArrayList(RELATIONSHIP_CATEGORY_EXTRA_TEXT, relationshipCategories);
                intent.putExtras(b);
                //intent.putExtra(RELATIONSHIP_CATEGORY_EXTRA_TEXT, relationshipCategories);
                startActivity( intent );
            }
        });

        //Button used for clearing a current search of relationship categories
        FloatingActionButton clearCategoriesButton = findViewById(R.id.clear_search_categories_button);
        clearCategoriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentlySelectedRel.clear();
                setUpRelRecyclerView();
                updateMainRecyclerView();
            }
        });
        setUpMainRecyclerView();
    }

    //Returns which item was selected for search order
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch ( item.getItemId()) {
            case R.id.action_search:
                Toast.makeText(this, "search selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.first_name_category:
                Toast.makeText(this, "First name selected", Toast.LENGTH_SHORT).show();
                currentQuery = "firstName";
                changeQuery(currentQuery, "");
                return true;
            case R.id.last_name_category:
                Toast.makeText(this, "last name selected", Toast.LENGTH_SHORT).show();
                currentQuery = "lastName";
                changeQuery(currentQuery, "");
                return true;
            case R.id.email_category:
                currentQuery = "email";
                changeQuery(currentQuery, "");
                return true;
            case R.id.description_catgory:
                Toast.makeText(this, "description selected", Toast.LENGTH_SHORT).show();
                currentQuery = "description";
                //changeQuery(currentQuery, "");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Search bar action setup
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if( currentQuery == "relationships" ){

                }  else if( currentQuery == "description"){
                    descriptionQuery( s );
                } else {
                    changeQuery(currentQuery, s.toLowerCase());
                }
                //searchData(s);
                return false;
            }
        });
        return true;
    }

    private Query setQuery( String query ) {
        Query mQuery = contactBookRef.orderBy(query);
        return mQuery;
    }

    // Searches for any descriptions that contain up to 10 words from the search bar.
    private void descriptionQuery( String s ) {
        String[] splitS = s.split(" ");
        String[] searchArray = new String[10];
        for( int i = 0; i < 10; i++ ) {
            if(( splitS.length - 1 ) >= i ) {
                searchArray[i] = splitS[i];
            } else {
                searchArray[i] = " "; //Will never show as a result
            }
        }
        Query newQuery = contactBookRef.whereArrayContainsAny("descriptionAsArrayList",
                Arrays.asList(searchArray[0], searchArray[1], searchArray[2], searchArray[3],
                        searchArray[4], searchArray[5], searchArray[6],
                        searchArray[7], searchArray[8], searchArray[9]));

        FirestoreRecyclerOptions<Contact> newOptions = new FirestoreRecyclerOptions
                .Builder<Contact>()
                .setQuery(newQuery, MetadataChanges.EXCLUDE, Contact.class)
                .build();
        adapter.updateOptions(newOptions);
    }

    // Basic change in search cateogry, determined by the menu
    private void changeQuery( String queryField, String s )  {
        //https://stackoverflow.com/questions/46568142/google-firestore-query-on-substring-of-a-property-value-text-search
        Query newQuery = contactBookRef.orderBy(queryField).startAt(s).endAt(s+'~');
        FirestoreRecyclerOptions<Contact> newOptions = new FirestoreRecyclerOptions
                .Builder<Contact>()
                .setQuery(newQuery, MetadataChanges.EXCLUDE, Contact.class)
                .build();
        adapter.updateOptions(newOptions);
    }

    // refreshes the main recycler view
    private void updateMainRecyclerView() {
        contactRecyclerView.setAdapter( adapter );
    }

    private void setUpMainRecyclerView() {
        Query query = setQuery("firstName");
        FirestoreRecyclerOptions<Contact> options = new FirestoreRecyclerOptions
                .Builder<Contact>()
                .setQuery(query, MetadataChanges.EXCLUDE, Contact.class)
                .build();

        //fillList();
        adapter = new ContactAdapter(options, currentlySelectedRel);

        contactRecyclerView = findViewById(R.id.contacts_recycler_view);
        contactRecyclerView.setHasFixedSize(true);
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactRecyclerView.setAdapter(adapter);


        adapter.setOnContactClickListener(new ContactAdapter.OnContactClickListener() {
            @Override
            public void onContactClick(DocumentSnapshot documentSnapshot, int position) {
                Contact contact = documentSnapshot.toObject(Contact.class);
                openContactViewActivity( contact, documentSnapshot );
            }
        });
    }

    private void setUpRelRecyclerView() {
        categoryRecyclerView = findViewById(R.id.categories_recycler_view);
        //categoryRecyclerView.setHasFixedSize(true);
        categoryLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        categoryAdapter = new SelectedCategoryAdapter( relationshipCategories, currentlySelectedRel );

        categoryRecyclerView.setLayoutManager(categoryLayoutManager);
        categoryRecyclerView.setAdapter(categoryAdapter);

        categoryAdapter.setOnItemClickListener(new SelectedCategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String item = relationshipCategories.get(position);
                if( currentlySelectedRel.contains(relationshipCategories.get(position))) {
                    currentlySelectedRel.remove( currentlySelectedRel.indexOf( item ));
                } else {
                    currentlySelectedRel.add( item );
                }
                updateMainRecyclerView();
            }
        });
    }

    // Recycler view adapter basic methods
    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    // Sends through the necessary information to be able to view the information on a contact
    public void openContactViewActivity(Contact contact, DocumentSnapshot documentSnapshot) {
        Intent intent = new Intent(this, ContactViewActivity.class);
        String path = documentSnapshot.getReference().getPath();

        intent.putExtra(DOCUMENT_EXTRA_TEXT, contact.getDocumentId());
        intent.putExtra(FIRST_EXTRA_TEXT,contact.getFirstName());
        intent.putExtra(LAST_EXTRA_TEXT, contact.getLastName());
        intent.putExtra(PHONE_EXTRA_TEXT, contact.getPhoneNumber());
        intent.putExtra(EMAIL_EXTRA_TEXT, contact.getEmail());
        intent.putExtra(DESCRIPTION_EXTRA_TEXT, contact.getDescription());
        intent.putStringArrayListExtra(RELATIONSHIP_EXTRA_TEXT, contact.getRelationships());
        intent.putExtra(EXTRA_TEXT, path);
        intent.putStringArrayListExtra(RELATIONSHIP_CATEGORY_EXTRA_TEXT, relationshipCategories);
        intent.putStringArrayListExtra(CURRENTLY_SELECTED_RELATIONSHIPS, currentlySelectedRel);

        startActivity(intent);
    }

    // initiates the list of current relationship categories
        public void setCategories() {
            relationshipListRef.document("relationshipList").get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if( documentSnapshot.exists() ) {
                                Map<String, Object> list = documentSnapshot.getData();
                                relationshipCategories = (ArrayList<String>) list.get("list");
                                Collections.sort(relationshipCategories);
                                setUpRelRecyclerView();
                                //Toast.makeText(ListViewActivity.this, "Should work....", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ListViewActivity.this, "WROOOOOOONG", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ListViewActivity.this, "FAIIIILLLUURRRREEEE!!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
}