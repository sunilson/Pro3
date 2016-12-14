package com.pro3.planner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.pro3.planner.Interfaces.BundleInterface;
import com.pro3.planner.Interfaces.ConfirmDialogResult;
import com.pro3.planner.ItemTouchHelper.SimpleItemTouchHelperCallbackMain;
import com.pro3.planner.R;
import com.pro3.planner.adapters.ElementRecyclerAdapter;
import com.pro3.planner.baseClasses.Checklist;
import com.pro3.planner.baseClasses.Element;
import com.pro3.planner.dialogs.ListAlertDialog;
import com.pro3.planner.dialogs.PasswordDialog;

public class BundleActivity extends BaseElementActivity implements BundleInterface, ConfirmDialogResult {

    private RecyclerView bundleList;
    private DatabaseReference mChecklistElementsReference, mNoteElementsReference, mBinReference;
    private ChildEventListener mChecklistElementsListener, mNoteElementsListener;
    private ElementRecyclerAdapter elementRecyclerAdapter;
    private View.OnClickListener recycleOnClickListener;
    private CoordinatorLayout coordinatorLayout;
    private View.OnLongClickListener recycleOnLongClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.activity_bundle);

        if (mElementReference != null) {
            //Recyclerview Initialization
            bundleList = (RecyclerView) findViewById(R.id.bundleList);
            bundleList.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            bundleList.setLayoutManager(linearLayoutManager);
            initializeRecyclerOnClickListener();
            initializeRecyclerOnLongClickListener();

            elementRecyclerAdapter = new ElementRecyclerAdapter(this, recycleOnClickListener, recycleOnLongClickListener);

            bundleList.setAdapter(elementRecyclerAdapter);
            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallbackMain(elementRecyclerAdapter);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
            itemTouchHelper.attachToRecyclerView(bundleList);

            //Checklist Element Listeners
            initializeNoteElementsListener();
            initializeChecklistElementsListener();
            mNoteElementsReference = mElementReference.child("notes");
            mChecklistElementsReference = mElementReference.child("checklists");
            mNoteElementsReference.addChildEventListener(mNoteElementsListener);
            mChecklistElementsReference.addChildEventListener(mChecklistElementsListener);

            mBinReference = mElementReference.child("bin");

        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialog = ListAlertDialog.newInstance(getResources().getString(R.string.add_Element_Title), "addElementBundle", 0);
                dialog.show(getSupportFragmentManager(), "dialog");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChecklistElementsListener != null) {
            mChecklistElementsReference.removeEventListener(mChecklistElementsListener);
        }

        if (mNoteElementsListener != null) {
            mNoteElementsReference.removeEventListener(mNoteElementsListener);
        }
    }

    private void initializeNoteElementsListener() {
        mNoteElementsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Element element = dataSnapshot.getValue(Element.class);
                if (element != null) {
                    elementRecyclerAdapter.add(element);
                    elementRecyclerAdapter.hideElements();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Element element = dataSnapshot.getValue(Element.class);
                elementRecyclerAdapter.update(element, element.getNoteID());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                final DataSnapshot dataSnapshotValue = dataSnapshot;
                final Element element = dataSnapshot.getValue(Element.class);
                elementRecyclerAdapter.remove(element.getNoteID());
                DatabaseReference dRef = mBinReference.child("notes").push();
                element.setNoteID(dRef.getKey());
                dRef.setValue(element);

                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, R.string.moved_to_bin, 6000)
                        .setAction(R.string.undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Snackbar snackbar1 = Snackbar.make(coordinatorLayout, R.string.element_restored, Snackbar.LENGTH_SHORT);
                                snackbar1.show();
                                mBinReference.child("notes").child(element.getNoteID()).removeValue();
                                Element element = dataSnapshotValue.getValue(Element.class);
                                DatabaseReference dRef = mNoteElementsReference.push();
                                element.setNoteID(dRef.getKey());
                                dRef.setValue(element);
                            }
                        });
                snackbar.show();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void initializeChecklistElementsListener() {
        mChecklistElementsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Element element = dataSnapshot.getValue(Element.class);
                if (element != null) {
                    elementRecyclerAdapter.add(element);
                    elementRecyclerAdapter.hideElements();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Element element = dataSnapshot.getValue(Element.class);
                elementRecyclerAdapter.update(element, element.getNoteID());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                final DataSnapshot dataSnapshotValue = dataSnapshot;
                 Element element = dataSnapshot.getValue(Element.class);
                elementRecyclerAdapter.remove(element.getNoteID());
                final DatabaseReference dRef = mBinReference.child("checklists").push();
                Checklist checklist = dataSnapshot.getValue(Checklist.class);
                checklist.setNoteID(dRef.getKey());
                dRef.setValue(checklist);

                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, R.string.moved_to_bin, 6000)
                        .setAction(R.string.undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Snackbar snackbar1 = Snackbar.make(coordinatorLayout, R.string.element_restored, Snackbar.LENGTH_SHORT);
                                snackbar1.show();
                                mBinReference.child("checklists").child(dRef.getKey()).removeValue();
                                DatabaseReference dRef = mChecklistElementsReference.push();
                                Checklist checklist = dataSnapshotValue.getValue(Checklist.class);
                                checklist.setNoteID(dRef.getKey());
                                dRef.setValue(checklist);
                            }
                        });
                snackbar.show();
            }


            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void initializeRecyclerOnClickListener() {
        recycleOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = bundleList.getChildLayoutPosition(v);
                Element element = elementRecyclerAdapter.getItem(itemPosition);
                if (element.getLocked()) {
                    DialogFragment dialogFragment = PasswordDialog.newInstance("passwordOpenElement", element.getNoteType(), element.getNoteID(), element.getTitle(), element.getColor());
                    dialogFragment.show(getSupportFragmentManager(), "dialog");
                } else {
                    Intent i = null;
                    if (element.getNoteType().equals("checklist")) {
                        i = new Intent(BundleActivity.this, ChecklistActivity.class);
                    } else if (element.getNoteType().equals("note")) {
                        i = new Intent(BundleActivity.this, NoteActivity.class);
                    } else if (element.getNoteType().equals("bundle")) {
                        i = new Intent(BundleActivity.this, BundleActivity.class);
                    }

                    i.putExtra("elementID", element.getNoteID());
                    i.putExtra("elementTitle", element.getTitle());
                    i.putExtra("elementColor", element.getColor());
                    i.putExtra("elementType", element.getNoteType());
                    i.putExtra("parent", elementID);
                    startActivity(i);
                }
            }
        };
    }

    private void initializeRecyclerOnLongClickListener() {
        recycleOnLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int itemPosition = bundleList.getChildLayoutPosition(v);
                Element element = elementRecyclerAdapter.getItem(itemPosition);

                if (element.getLocked()) {
                    DialogFragment dialogFragment = PasswordDialog.newInstance("passwordEditElement", "", "", "", itemPosition);
                    dialogFragment.show(getSupportFragmentManager(), "dialog");
                } else {
                    DialogFragment dialog = ListAlertDialog.newInstance(getResources().getString(R.string.edit_element_title), "editElement", itemPosition);
                    dialog.show(getSupportFragmentManager(), "dialog");
                }
                return true;
            }
        };
    }


    @Override
    public DatabaseReference getChecklistElementsReference() {
        return mChecklistElementsReference;
    }

    @Override
    public DatabaseReference getNoteElementsReference() {
        return mNoteElementsReference;
    }

    @Override
    public ElementRecyclerAdapter getElementAdapter() {
        return elementRecyclerAdapter;
    }

    @Override
    public TextView getSortTextView() {
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_bundle, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
            return true;
        } else if (id == R.id.bundle_menu_bin) {
            Intent i = new Intent(BundleActivity.this, BinActivity.class);
            i.putExtra("elementID", elementID);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void confirmDialogResult(boolean bool, String type, Bundle args) {
        if (type.equals("passwordOpenElement")) {
            if (bool) {
                Intent i = null;
                if (args.getString("elementType").equals("checklist")) {
                    i = new Intent(BundleActivity.this, ChecklistActivity.class);
                } else if (args.getString("elementType").equals("note")) {
                    i = new Intent(BundleActivity.this, NoteActivity.class);
                }

                i.putExtra("elementID", args.getString("elementID"));
                i.putExtra("elementTitle", args.getString("elementTitle"));
                i.putExtra("elementColor", args.getInt("elementColor"));
                i.putExtra("elementType", args.getString("elementType"));
                i.putExtra("parent", elementID);
                startActivity(i);
            } else {
                Toast.makeText(this, R.string.wrong_password, Toast.LENGTH_SHORT).show();
            }
        } else if (type.equals("passwordEditElement")) {
            if (bool) {
                DialogFragment dialog = ListAlertDialog.newInstance(getResources().getString(R.string.edit_element_title), "editElement", args.getInt("elementColor"));
                dialog.show(getSupportFragmentManager(), "dialog");
            } else {
                Toast.makeText(this, R.string.wrong_password, Toast.LENGTH_SHORT).show();
            }
        }
    }
}