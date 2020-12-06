package kitchen_joshua.roberts.edu.contactappdraft;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RelationshipDialogue extends AppCompatDialogFragment  {
    private RecyclerView categoryRecyclerView;
    private RelationshipAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private RelationshipDialogueListener listener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (RelationshipDialogueListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "Must implement RelationshipDialogueListener");
        }
    }

    private ArrayList<String> relationshipCategories;
    private ArrayList<String> passedInRelationships;
    private String[][] selectedCategoryList;
    private ArrayList<String> finalizedSelectedCategories;

    private TextView relationshipCategoryView;
    public RelationshipDialogue( ArrayList<String> categories, ArrayList<String> passedIn ) {
        relationshipCategories = categories;
        passedInRelationships = passedIn;
        initializeSelectedCategoryList();
        finalizedSelectedCategories = new ArrayList<>();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialogue_layout, null);

        builder.setView(view)
                .setTitle("Relationship Picker")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getSelectedCategoriesAsList( selectedCategoryList );
                        listener.applyChoices( finalizedSelectedCategories, relationshipCategories );
                    }
                })
                .setNeutralButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openDialog();
                    }
                });

        //relationshipCategoryView = view.findViewById(R.id.testArrayView);
        categoryRecyclerView = view.findViewById(R.id.categories_recycler_view);
        layoutManager = new LinearLayoutManager( getActivity() );
        adapter = new RelationshipAdapter( relationshipCategories, passedInRelationships );

        categoryRecyclerView.setLayoutManager(layoutManager);
        categoryRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new RelationshipAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                updateSelectedCategoryList(position);
            }

            @Override
            public void onDeleteClick(int position) {
                relationshipCategories.remove(position);
                adapter.notifyItemRemoved(position);
                DocumentReference dr = db.document("RelationshipList/relationshipList");
                dr.update("list", relationshipCategories);
            }
        });
        return builder.create();
    }

    public void initializeSelectedCategoryList() {
        selectedCategoryList = new String[relationshipCategories.size()][2];
        int count = 0;
        for(int i = 0; i < relationshipCategories.size(); i++) {
            if( passedInRelationships.isEmpty() || passedInRelationships.size() <= count) {
                selectedCategoryList[i][0] = "0";
                selectedCategoryList[i][1] = "";

            } else {
                if( passedInRelationships.get(count) == relationshipCategories.get(i) ) {
                    selectedCategoryList[i][0] = "1";
                    selectedCategoryList[i][1] = passedInRelationships.get(count++);
                } else {
                    selectedCategoryList[i][0] = "0";
                    selectedCategoryList[i][1] = "";
                }
            }
        }
    }

    public void updateSelectedCategoryList( int position ) {
        if(selectedCategoryList[position][0].equals("0")) {
            selectedCategoryList[position][0] = "1";
            selectedCategoryList[position][1] = relationshipCategories.get(position);

            Toast.makeText(getActivity(), "Selected " + relationshipCategories.get(position), Toast.LENGTH_SHORT).show();
        } else if (selectedCategoryList[position][0].equals("1") ){
            selectedCategoryList[position][0] = "0";
            selectedCategoryList[position][1] = "";

            Toast.makeText(getActivity(), "De-selected " + relationshipCategories.get(position), Toast.LENGTH_SHORT).show();
        }
    }

    public void getSelectedCategoriesAsList( String[][] list ) {
        for(int i = 0; i < list.length; i++) {
            if ( list[i][1] != "" ) {
                finalizedSelectedCategories.add(list[i][1]);
            }
        }
    }

    public void openDialog() {
        AddRelationshipDialog dialog = new AddRelationshipDialog();
        dialog.show(getFragmentManager(), "Add Category");
    }

    public interface RelationshipDialogueListener {
        void applyChoices( ArrayList<String> selectedCategories, ArrayList<String> allCategories );
    }
}


