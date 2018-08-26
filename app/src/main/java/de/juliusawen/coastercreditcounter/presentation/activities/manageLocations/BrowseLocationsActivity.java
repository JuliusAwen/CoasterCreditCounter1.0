package de.juliusawen.coastercreditcounter.presentation.activities.manageLocations;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.juliusawen.coastercreditcounter.R;
import de.juliusawen.coastercreditcounter.Toolbox.Constants;
import de.juliusawen.coastercreditcounter.Toolbox.DrawableTool;
import de.juliusawen.coastercreditcounter.Toolbox.StringTool;
import de.juliusawen.coastercreditcounter.Toolbox.Toaster;
import de.juliusawen.coastercreditcounter.Toolbox.ViewTool;
import de.juliusawen.coastercreditcounter.content.Content;
import de.juliusawen.coastercreditcounter.content.Element;
import de.juliusawen.coastercreditcounter.content.Location;
import de.juliusawen.coastercreditcounter.presentation.adapters.recyclerViews.baseRecyclerView.BaseRecyclerViewAdapter;
import de.juliusawen.coastercreditcounter.presentation.adapters.recyclerViews.baseRecyclerView.BaseRecyclerViewTouchListener;
import de.juliusawen.coastercreditcounter.presentation.fragments.HelpOverlayFragment;

public class BrowseLocationsActivity extends AppCompatActivity implements HelpOverlayFragment.HelpOverlayFragmentInteractionListener
{
    private Location currentLocation = Content.getInstance().getLocationRoot();
    private List<Location> recentLocations = new ArrayList<>();

    private Location longClickedLocation;

    private RecyclerView recyclerView;
    private BaseRecyclerViewAdapter baseRecyclerViewAdapter;
    private HelpOverlayFragment helpOverlayFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_locations_activity);

        this.initializeContent();

        this.initializeViews();
        this.refreshViews();
    }

    private void initializeContent()
    {
        Intent intent = getIntent();
        this.currentLocation = (Location) Content.getInstance().getElementByUuid(UUID.fromString(intent.getStringExtra(Constants.EXTRA_ELEMENT_UUID)));
    }

    private void initializeViews()
    {
        FrameLayout frameLayoutActivity = findViewById(R.id.frameLayoutBrowseLocations);
        View browseLocationsView = getLayoutInflater().inflate(R.layout.browse_locations_layout, frameLayoutActivity, false);
        frameLayoutActivity.addView(browseLocationsView);

        this.createToolbar(browseLocationsView);
        this.createNavigationBar(browseLocationsView);
        this.createContentRecyclerView(browseLocationsView);
        this.createFloatingActionButton();
        this.createHelpOverlayFragment(frameLayoutActivity.getId());
    }

    private void refreshViews()
    {
        this.createNavigationBar(this.findViewById(android.R.id.content).getRootView());
        this.baseRecyclerViewAdapter.updateList(new ArrayList<Element>(this.currentLocation.getChildren()));
    }

    private void createToolbar(View view)
    {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_locations));

        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        menu.clear();
        if(this.currentLocation.getParent() == null)
        {
            menu.add(0, Constants.SELECTION_EDIT, Menu.NONE, R.string.selection_rename_root);
        }
        if(this.currentLocation.getChildren().size() > 1)
        {
            menu.add(0, Constants.SELECTION_SORT_MANUALLY, Menu.NONE, R.string.selection_sort_entries);
        }
        menu.add(0, Constants.SELECTION_HELP, Menu.NONE, R.string.selection_help);

        return super.onPrepareOptionsMenu(menu);
    }

    private void createNavigationBar(View view)
    {
        if(!this.recentLocations.contains(this.currentLocation))
        {
            this.recentLocations.add(this.currentLocation);
        }

        LinearLayout linearLayoutNavigationBar = view.findViewById(R.id.linearLayoutBrowseLocationsNavigationBar);
        linearLayoutNavigationBar.invalidate();
        linearLayoutNavigationBar.removeAllViews();

        for (Location location : this.recentLocations)
        {
            View buttonView = getLayoutInflater().inflate(R.layout.button_transparent, linearLayoutNavigationBar, false);

            Button button = buttonView.findViewById(R.id.button_noBorder);

            if(this.recentLocations.indexOf(location) != 0)
            {
                Drawable drawable = DrawableTool.setTintToWhite(this, getDrawable(R.drawable.ic_baseline_chevron_left));
                button.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            }

            if(this.recentLocations.indexOf(location) != this.recentLocations.size() -1)
            {
                button.setText(location.getName());
            }
            else
            {
                button.setText(StringTool.getSpannableString(location.getName(), Typeface.BOLD_ITALIC));
            }

            button.setId(Constants.BUTTON_BACK);
            button.setTag(location);
            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Location location = (Location) view.getTag();

                    int length = recentLocations.size() - 1;
                    for (int i = length; i >= 0; i--)
                    {
                        if (recentLocations.get(i).equals(location))
                        {
                            recentLocations.remove(i);
                            break;
                        }
                        else
                        {
                            recentLocations.remove(i);
                        }
                    }

                    currentLocation = location;
                    refreshViews();
                }
            });

            linearLayoutNavigationBar.addView(buttonView);

            final HorizontalScrollView horizontalScrollView = view.findViewById(R.id.horizontalScrollViewBrowseLocationsNavigationBar);
            horizontalScrollView.post(new Runnable()
            {
                @Override
                public void run()
                {
                    horizontalScrollView.fullScroll(View.FOCUS_RIGHT);
                }
            });
        }
    }

    private void createContentRecyclerView(View view)
    {
        this.recyclerView = view.findViewById(R.id.recyclerViewBrowseLocations);
        this.baseRecyclerViewAdapter = new BaseRecyclerViewAdapter(new ArrayList<Element>(this.currentLocation.getChildren()));

        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.addOnItemTouchListener(new BaseRecyclerViewTouchListener(getApplicationContext(), recyclerView, new BaseRecyclerViewTouchListener.ClickListener()
        {
            @Override
            public void onClick(View view, int position)
            {
                currentLocation = (Location) view.getTag();
                refreshViews();
            }

            @Override
            public void onLongClick(final View view, int position)
            {
                longClickedLocation = (Location) view.getTag();

                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);

                popupMenu.getMenu().add(0, Constants.SELECTION_EDIT + Constants.CONTENT_TYPE_LOCATION, Menu.NONE, R.string.selection_edit_location);
                popupMenu.getMenu().add(0, Constants.SELECTION_DELETE + Constants.CONTENT_TYPE_LOCATION, Menu.NONE, R.string.selection_delete_location);

                if(!(longClickedLocation).getChildren().isEmpty())
                {
                    popupMenu.getMenu().add(0, Constants.SELECTION_REMOVE + Constants.CONTENT_TYPE_LOCATION, Menu.NONE, R.string.selection_remove_location_level);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    @Override
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        if(item.getItemId() == Constants.SELECTION_EDIT + Constants.CONTENT_TYPE_LOCATION)
                        {
                            startEditLocationActivity(longClickedLocation);
                            return true;
                        }
                        else if(item.getItemId() == Constants.SELECTION_DELETE + Constants.CONTENT_TYPE_LOCATION)
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(BrowseLocationsActivity.this);

                            builder.setTitle(R.string.alert_dialog_delete_location_title);
                            builder.setMessage(getString(R.string.alert_dialog_delete_location_message, longClickedLocation.getName()));

                            builder.setPositiveButton(R.string.button_text_accept, new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    dialog.dismiss();

                                    if(longClickedLocation.deleteNodeAndChildren())
                                    {
                                        Content.getInstance().deleteLocationAndChildren(longClickedLocation);
                                        baseRecyclerViewAdapter.notifyDataSetChanged();
                                    }
                                    else
                                    {
                                        Toaster.makeToast(getApplicationContext(), getString(R.string.error_text_delete_failed));
                                    }


                                    refreshViews();

                                    Snackbar snackbar = Snackbar.make(view, R.string.action_undo_delete_location_text, Snackbar.LENGTH_LONG);
                                    snackbar.setAction(R.string.action_undo_title, new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            if(longClickedLocation.undoDeleteNodeAndChildrenPossible && longClickedLocation.undoDeleteNodeAndChildren())
                                            {
                                                Content.getInstance().addLocationAndChildren(longClickedLocation);
                                                baseRecyclerViewAdapter.notifyDataSetChanged();
                                                refreshViews();
                                            }
                                            else
                                            {
                                                Toaster.makeToast(getApplicationContext(), getString(R.string.error_text_undo_not_possible));
                                            }
                                        }
                                    });
                                    snackbar.show();
                                }
                            });

                            builder.setNegativeButton(R.string.button_text_cancel, new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    dialog.dismiss();
                                }
                            });

                            AlertDialog alertDialog = builder.create();
                            alertDialog.setIcon(R.drawable.ic_baseline_warning);

                            alertDialog.show();

                            return true;
                        }
                        else if(item.getItemId() == Constants.SELECTION_REMOVE + Constants.CONTENT_TYPE_LOCATION)
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(BrowseLocationsActivity.this);

                            builder.setTitle(R.string.alert_dialog_remove_location_title);
                            builder.setMessage(getString(R.string.alert_dialog_remove_location_level_message, longClickedLocation.getName(),
                                    longClickedLocation.getParent().getName()));

                            builder.setPositiveButton(R.string.button_text_accept, new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    dialog.dismiss();

                                    if(longClickedLocation.removeNode())
                                    {
                                        Content.getInstance().deleteElement(longClickedLocation);
                                        currentLocation = longClickedLocation.getParent();
                                        refreshViews();
                                    }
                                    else
                                    {
                                        Toaster.makeToast(getApplicationContext(), getString(R.string.error_text_remove_failed));
                                    }

                                    Snackbar snackbar = Snackbar.make(view, R.string.action_undo_remove_location_text, Snackbar.LENGTH_LONG);
                                    snackbar.setAction(R.string.action_undo_title, new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            if(longClickedLocation.undoRemoveNodePossible && longClickedLocation.undoRemoveNode())
                                            {
                                                longClickedLocation.undoRemoveNode();
                                                Content.getInstance().addElement(longClickedLocation);
                                                baseRecyclerViewAdapter.notifyDataSetChanged();
                                                refreshViews();
                                            }
                                            else
                                            {
                                                Toaster.makeToast(getApplicationContext(), getString(R.string.error_text_undo_not_possible));
                                            }
                                        }
                                    });
                                    snackbar.show();
                                }
                            });

                            builder.setNegativeButton(R.string.button_text_cancel, new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    dialog.dismiss();
                                }
                            });

                            AlertDialog alertDialog = builder.create();
                            alertDialog.setIcon(R.drawable.ic_baseline_warning);

                            alertDialog.show();
                            return true;
                        }
                        else
                        {
                            return false;
                        }
                    }
                });

                popupMenu.show();
            }
        }));

        recyclerView.setAdapter(baseRecyclerViewAdapter);
    }

    private void createFloatingActionButton()
    {
        final FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButtonBrowseLocations);

        Drawable drawable = DrawableTool.setTintToWhite(this, getDrawable(R.drawable.ic_baseline_add));
        floatingActionButton.setImageDrawable(drawable);

        floatingActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), floatingActionButton);

                popupMenu.getMenu().add(0, Constants.SELECTION_ADD + Constants.CONTENT_TYPE_LOCATION, Menu.NONE, R.string.selection_add_location);
                popupMenu.getMenu().add(0, Constants.SELECTION_ADD + Constants.CONTENT_TYPE_PARK, Menu.NONE, R.string.selection_add_park);

                if(!currentLocation.getChildren().isEmpty())
                {
                    popupMenu.getMenu().add(0, Constants.SELECTION_INSERT + Constants.CONTENT_TYPE_LOCATION, Menu.NONE, R.string.selection_insert_location_level);
                }


                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    @Override
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        Intent intent;

                        if(item.getItemId() == Constants.SELECTION_ADD + Constants.CONTENT_TYPE_LOCATION)
                        {
                            intent = new Intent(getApplicationContext(), AddOrInsertLocationActivity.class);
                            intent.putExtra(Constants.EXTRA_ELEMENT_UUID, currentLocation.getUuid().toString());
                            intent.putExtra(Constants.EXTRA_SELECTION, Constants.SELECTION_ADD);
                            startActivityForResult(intent, Constants.REQUEST_ADD_OR_INSERT_LOCATION);
                            return true;
                        }
                        else if(item.getItemId() == Constants.SELECTION_ADD + Constants.CONTENT_TYPE_PARK)
                        {
                            //Todo: implement add park activity
                            Toaster.makeToast(getApplicationContext(), "not yet implemented");
                            return true;
                        }
                        else if (item.getItemId() == Constants.SELECTION_INSERT + Constants.CONTENT_TYPE_LOCATION)
                        {
                            intent = new Intent(getApplicationContext(), AddOrInsertLocationActivity.class);
                            intent.putExtra(Constants.EXTRA_ELEMENT_UUID, currentLocation.getUuid().toString());
                            intent.putExtra(Constants.EXTRA_SELECTION, Constants.SELECTION_INSERT);
                            startActivity(intent);
                            return true;
                        }
                        else
                        {
                            return false;
                        }
                    }
                });

                popupMenu.show();
            }
        });
    }

    private void setFloatingActionButtonVisibility(boolean isVisible)
    {
        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButtonBrowseLocations);
        floatingActionButton.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    private void createHelpOverlayFragment(int frameLayoutId)
    {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        this.helpOverlayFragment = HelpOverlayFragment.newInstance(getText(R.string.help_text_browse_locations), false);
        fragmentTransaction.add(frameLayoutId, this.helpOverlayFragment, Constants.FRAGMENT_TAG_HELP_OVERLAY);
        fragmentTransaction.commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putStringArrayList(Constants.KEY_ELEMENTS, Content.getInstance().getUuidStringsFromElements(this.recentLocations));
        outState.putString(Constants.KEY_CURRENT_ELEMENT, this.currentLocation.getUuid().toString());
        outState.putBoolean(Constants.KEY_HELP_VISIBLE, this.helpOverlayFragment.isVisible());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        this.recentLocations = Content.getInstance().getLocationsFromUuidStrings(savedInstanceState.getStringArrayList(Constants.KEY_ELEMENTS));
        this.currentLocation = (Location) Content.getInstance().getElementByUuid(UUID.fromString(savedInstanceState.getString(Constants.KEY_CURRENT_ELEMENT)));
        this.helpOverlayFragment.setVisibility(savedInstanceState.getBoolean(Constants.KEY_HELP_VISIBLE));
        this.setFloatingActionButtonVisibility(!savedInstanceState.getBoolean(Constants.KEY_HELP_VISIBLE));

        this.refreshViews();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        this.refreshViews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == Constants.SELECTION_EDIT)
        {
            this.startEditLocationActivity(this.currentLocation);
            return true;
        }
        else if(item.getItemId() == Constants.SELECTION_SORT_MANUALLY)
        {
            Intent intent = new Intent(this, SortElementsActivity.class);
            intent.putExtra(Constants.EXTRA_ELEMENT_UUID, this.currentLocation.getUuid().toString());
            startActivityForResult(intent, Constants.REQUEST_SORT_ELEMENTS);
            return true;
        }
        else if(item.getItemId() == Constants.SELECTION_HELP)
        {
            this.helpOverlayFragment.setVisibility(true);
            this.setFloatingActionButtonVisibility(false);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onHelpOverlayFragmentInteraction(View view)
    {
        if(view.getId() == Constants.BUTTON_CLOSE)
        {
            this.helpOverlayFragment.setVisibility(false);
            this.setFloatingActionButtonVisibility(true);
        }
    }

    private void startEditLocationActivity(Element element)
    {
        Intent intent = new Intent(getApplicationContext(), EditLocationActivity.class);
        intent.putExtra(Constants.EXTRA_ELEMENT_UUID, element.getUuid().toString());
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == Constants.REQUEST_ADD_OR_INSERT_LOCATION || requestCode == Constants.REQUEST_SORT_ELEMENTS)
        {
            if(resultCode == RESULT_OK)
            {
                String uuidString = data.getStringExtra(Constants.EXTRA_ELEMENT_UUID);
                Location receivedLocation = Content.getInstance().getLocationFromUuidString(uuidString);

                int position = this.currentLocation.getChildren().indexOf(receivedLocation);

                int scrollMargin = ViewTool.getScrollMarginForRecyclerView(this.recyclerView);
                if(this.currentLocation.getChildren().size() > position + scrollMargin)
                {
                    recyclerView.smoothScrollToPosition(position + scrollMargin);
                }
                else
                {
                    recyclerView.smoothScrollToPosition(position);
                }
            }
        }
    }
}