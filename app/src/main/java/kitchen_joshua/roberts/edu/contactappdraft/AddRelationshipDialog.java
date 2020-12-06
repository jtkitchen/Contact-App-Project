package kitchen_joshua.roberts.edu.contactappdraft;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentManager;

public class AddRelationshipDialog extends AppCompatDialogFragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private EditText newRelationshipEditText;
    private AddRelationshipDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (AddRelationshipDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AddRelationshipDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_relationship_dialog_view, null);

        builder.setView(view)
                .setTitle("Add A Category")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newRel = newRelationshipEditText.getText().toString().toLowerCase();
                String temp = newRel.substring(0,1).toUpperCase() + newRel.substring(1);
                newRel = temp;
                listener.applyNewRelationships( newRel );
            }
        });
        newRelationshipEditText = view.findViewById(R.id.new_relationship_edit_text);

        return builder.create();
    }

    public interface AddRelationshipDialogListener {
        void applyNewRelationships( String newRelationship );
    }



}
