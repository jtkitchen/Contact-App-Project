package kitchen_joshua.roberts.edu.contactappdraft;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewContactActivity extends AppCompatActivity implements RelationshipDialogue.RelationshipDialogueListener, AddRelationshipDialog.AddRelationshipDialogListener {
    private static final String TAG = "MainActivity";

    private EditText editTextFirstName;
    private EditText editTextLastName;
    private EditText editTextPhoneNumber;
    private EditText editTextEmail;
    private EditText editTextDescription;
    private TextView textViewSelectedRelationships;
    private Button pickRelationshipButton;
    ArrayList<String> relationships = new ArrayList<>();

    private String path;
    private boolean editing;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference contactBookRef;
    private ArrayList<String> relationshipCategories = new ArrayList<>();

    //TODO Change the "back" button to be in the top right in the menu-bar rather than as a FAB
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contact_view);


        editTextFirstName = findViewById(R.id.edit_text_firstName);
        editTextLastName = findViewById(R.id.edit_text_lastName);
        editTextPhoneNumber = findViewById(R.id.edit_text_phoneNumber);
        editTextPhoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        editTextEmail = findViewById(R.id.edit_text_email);
        editTextDescription = findViewById(R.id.edit_text_description);
        textViewSelectedRelationships = findViewById(R.id.selected_relationships);
        pickRelationshipButton = findViewById(R.id.pick_relationships_button);

        Bundle b = getIntent().getExtras();
        //Intent intent = getIntent();
        relationshipCategories = b.getStringArrayList(ListViewActivity.RELATIONSHIP_CATEGORY_EXTRA_TEXT);
        editing = b.getBoolean("editingKey");

        if(editing) { getValues( b ); }

        FloatingActionButton buttonSaveContact = findViewById(R.id.button_save_contact);
        buttonSaveContact.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(editing) {
                    saveContact( v );
                } else {
                    addContact( v );
                }
                listView( v );
            }
        });

        pickRelationshipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        FloatingActionButton buttonBackAction = findViewById(R.id.add_contact_button_back_action);
        buttonBackAction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                listView( v );
            }
        });
    }

    public ArrayList<String> getRelationshipCategories() {
        return relationshipCategories;
    }

    public void setToEdit() {
        editing = true;
    }

    public void setToAdd() {
        editing = false;
    }

    public void addContact(View v) {

        db.enableNetwork(); //enables updating when something is being changed
        contactBookRef = db.collection("Contacts");
        String firstName = editTextFirstName.getText().toString();
        String lastName = editTextLastName.getText().toString();
        String phoneNumber = editTextPhoneNumber.getText().toString();
        String email = editTextEmail.getText().toString();
        String description = editTextDescription.getText().toString();

        Contact contact = new Contact(firstName, lastName, phoneNumber, email, description, relationships);

        contactBookRef.add(contact).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(NewContactActivity.this, "Added Note", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewContactActivity.this, "Error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void saveContact(View v) {

        db.enableNetwork(); //enables updating when something is being changed
        DocumentReference contact = db.document(path);
        String firstName = editTextFirstName.getText().toString();
        String lastName = editTextLastName.getText().toString();
        String phoneNumber = editTextPhoneNumber.getText().toString();
        String email = editTextEmail.getText().toString();
        String description = editTextDescription.getText().toString();

        contact.update("firstName", firstName.toLowerCase());
        contact.update("lastName", lastName.toLowerCase() );
        contact.update("email", email);
        contact.update("phoneNumber", phoneNumber);
        contact.update("description", description);
        contact.update("relationships", relationships)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(NewContactActivity.this, "Saved Contact", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void listView( View v ) {
        Intent intent = new Intent(this, ListViewActivity.class);
        intent.putStringArrayListExtra(ListViewActivity.RELATIONSHIP_CATEGORY_EXTRA_TEXT, getRelationshipCategories());
        startActivity(intent);
    }

    public void openDialog() {
        RelationshipDialogue dialogue = new RelationshipDialogue( relationshipCategories, relationships );
        dialogue.show(getSupportFragmentManager(), "Relationship Picker");

    }

    @Override
    public void applyChoices( ArrayList<String> choices, ArrayList<String> allCategories ) {
        relationships = choices; //Picked from the dialogue
        relationshipCategories = allCategories;

        textViewSelectedRelationships.setText("");
        for( int i = 0; i < relationships.size(); i++ ) {
            textViewSelectedRelationships.setText( textViewSelectedRelationships.getText() +
                    relationships.get(i) + "\n");
        }
    }

    @Override
    public void applyNewRelationships(String newRelationship) {
        if( !relationshipCategories.contains(newRelationship) ) {
            relationshipCategories.add(newRelationship);
        }
        DocumentReference dr = db.document("RelationshipList/relationshipList");
        dr.update("list", relationshipCategories);
    }

    public void getValues( Bundle b ) {
        editTextFirstName.setText(b.getString(ListViewActivity.FIRST_EXTRA_TEXT));
        editTextLastName.setText(b.getString(ListViewActivity.LAST_EXTRA_TEXT));
        editTextPhoneNumber.setText(b.getString(ListViewActivity.PHONE_EXTRA_TEXT));
        editTextEmail.setText(b.getString(ListViewActivity.EMAIL_EXTRA_TEXT));
        editTextDescription.setText(b.getString(ListViewActivity.DESCRIPTION_EXTRA_TEXT));
        relationships = b.getStringArrayList(ListViewActivity.RELATIONSHIP_EXTRA_TEXT);
        path = b.getString(ListViewActivity.EXTRA_TEXT);
        textViewSelectedRelationships.setText("");
        for( String r : relationships) {
            textViewSelectedRelationships.setText( textViewSelectedRelationships.getText() + r + "\n" );
        }
    }
}
