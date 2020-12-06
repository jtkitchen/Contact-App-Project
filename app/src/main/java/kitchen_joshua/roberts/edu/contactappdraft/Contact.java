package kitchen_joshua.roberts.edu.contactappdraft;

import com.google.firebase.firestore.Exclude;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Contact {

    private String documentId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String description;
    private String alteredDescription;
    private ArrayList<String> relationships;
    //used to populate initial array with a common search term

    public Contact(){
        //public no-arg constructor needed
    }
    public Contact(String firstName, String lastName, String phoneNumber, String email, String description, ArrayList<String> relationships){
        this.firstName = firstName.toLowerCase();
        this.lastName = lastName.toLowerCase();
        this.fullName = firstName + " " + lastName;//Used for searching
        this.phoneNumber = phoneNumber;
        this.email = email.toLowerCase();
        this.relationships = relationships;
        this.description = description;
        alteredDescription = description.toLowerCase();
    }

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() { return firstName.toLowerCase() + " " + lastName.toLowerCase(); }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() { return description; }

    public ArrayList<String> getDescriptionAsArrayList() {

        ArrayList<String> al = new ArrayList<>(Arrays.asList(alteredDescription.split("[\\s,;.]")));

        return al;
    }

    public void setDescription(String description) { this.description = description; }

    public ArrayList<String> getRelationships() {
        return relationships;
    }

    public void setRelationships(ArrayList<String> relationships) {
        this.relationships = relationships;
    }
}
