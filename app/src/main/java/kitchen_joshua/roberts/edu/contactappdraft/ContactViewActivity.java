package kitchen_joshua.roberts.edu.contactappdraft;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

public class ContactViewActivity extends AppCompatActivity {
    private String documentId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String description;
    private ArrayList<String> relationships;
    private ArrayList<String> allRelationshipCategories;
    private ArrayList<String> filteredCategories;

    private TextView full_name_text_view;
    private TextView phone_number_text_view;
    private TextView email_text_view;
    private TextView description_text_view;
    private TextView relationships_text_view;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference contactBookRef = db.collection("Contacts");
    private String path;


    protected void onCreate(Bundle savedInstanceState) {
        //TODO Make this look pretty already!!!
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_contact_information_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        firstName = intent.getStringExtra(ListViewActivity.FIRST_EXTRA_TEXT);
        lastName = intent.getStringExtra(ListViewActivity.LAST_EXTRA_TEXT);
        documentId = intent.getStringExtra(ListViewActivity.DOCUMENT_EXTRA_TEXT);
        phoneNumber = intent.getStringExtra(ListViewActivity.PHONE_EXTRA_TEXT);
        email = intent.getStringExtra(ListViewActivity.EMAIL_EXTRA_TEXT);
        description = intent.getStringExtra(ListViewActivity.DESCRIPTION_EXTRA_TEXT);
        relationships = intent.getStringArrayListExtra(ListViewActivity.RELATIONSHIP_EXTRA_TEXT);
        allRelationshipCategories = intent.getStringArrayListExtra(ListViewActivity.RELATIONSHIP_CATEGORY_EXTRA_TEXT);
        filteredCategories = intent.getStringArrayListExtra(ListViewActivity.CURRENTLY_SELECTED_RELATIONSHIPS);

        path = intent.getStringExtra(ListViewActivity.EXTRA_TEXT);
        fullName = firstName + " " + lastName;
        setContactInformation();

        FloatingActionButton buttonDeleteContact = findViewById(R.id.delete_note_button);
        buttonDeleteContact.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteContact( v );
            }
        });

        FloatingActionButton buttonEditContact = findViewById(R.id.edit_note_button);
        buttonEditContact.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editContact();
            }
        });
    }

//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.view_contact_menu, menu);
//        MenuItem item = menu.findItem(R.id.back_button);
//        ImageButton backButton = (ImageButton) item.getActionView();
//
//        backButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                listView( v );
//            }
//        });
//
//        return true;
//    }
    public void setContactInformation() {
        full_name_text_view = findViewById(R.id.contact_full_name_text_view);
        phone_number_text_view = findViewById(R.id.contact_phone_number_text_view);
        email_text_view = findViewById(R.id.contact_email_text_view);
        description_text_view = findViewById(R.id.contact_description_text_view);
        relationships_text_view = findViewById(R.id.contact_relationships_text_view);

        full_name_text_view.setText(firstName.substring(0,1).toUpperCase() + firstName.substring(1) + " " +
                lastName.substring(0,1).toUpperCase() + lastName.substring(1));
        if( phoneNumber != null && !phoneNumber.equals("") ) {
            phone_number_text_view.setText(phoneNumber);
        } else {
            phone_number_text_view.setText("(No Number)");
        }

        if( email != null && !email.equals("") ) {
            email_text_view.setText(email);
        } else {
            email_text_view.setText("(No Email)");
        }

        if( description != null && !description.equals("")) {
            description_text_view.setText(description);
        } else {
            description_text_view.setText("(No Description)");
        }


        if(relationships.size() > 0) {
            for (int i = 0; i < relationships.size(); i++) {
                if( relationships.get(i) != null) {
                    relationships_text_view.setText(relationships_text_view.getText() +
                            relationships.get(i) + "\n");
                }
            }
        }
    }

    public void deleteContact( View v ) {
        DocumentReference documentReference = db.document( path );
        documentReference.delete();
        db.enableNetwork();
        startActivity(new Intent(ContactViewActivity.this, ListViewActivity.class));
    }

    public void editContact() {
        Intent intent = new  Intent(ContactViewActivity.this, NewContactActivity.class);
        Bundle b = new Bundle(); b.putBoolean("editingKey", true);
        b.putStringArrayList(ListViewActivity.RELATIONSHIP_CATEGORY_EXTRA_TEXT, allRelationshipCategories);
        b.putString(ListViewActivity.FIRST_EXTRA_TEXT, firstName);
        b.putString(ListViewActivity.LAST_EXTRA_TEXT, lastName);
        b.putString(ListViewActivity.EMAIL_EXTRA_TEXT, email);
        b.putString(ListViewActivity.DESCRIPTION_EXTRA_TEXT, description);
        b.putString(ListViewActivity.PHONE_EXTRA_TEXT, phoneNumber);
        b.putString(ListViewActivity.EXTRA_TEXT, path);
        b.putStringArrayList(ListViewActivity.RELATIONSHIP_EXTRA_TEXT, relationships);

        intent.putExtras(b);
        startActivity( intent );
    }

    public void listView( View v ) {
        Intent intent = new Intent(this, ListViewActivity.class);
        intent.putStringArrayListExtra(ListViewActivity.CURRENTLY_SELECTED_RELATIONSHIPS, filteredCategories);
        startActivity(intent);
    }
}
