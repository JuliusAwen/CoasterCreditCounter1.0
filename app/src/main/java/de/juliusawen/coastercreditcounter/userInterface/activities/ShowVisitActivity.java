package de.juliusawen.coastercreditcounter.userInterface.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.juliusawen.coastercreditcounter.R;
import de.juliusawen.coastercreditcounter.application.App;
import de.juliusawen.coastercreditcounter.dataModel.elements.Attraction;
import de.juliusawen.coastercreditcounter.dataModel.elements.Element;
import de.juliusawen.coastercreditcounter.dataModel.elements.IAttraction;
import de.juliusawen.coastercreditcounter.dataModel.elements.IElement;
import de.juliusawen.coastercreditcounter.dataModel.elements.IOnSiteAttraction;
import de.juliusawen.coastercreditcounter.dataModel.elements.Ride;
import de.juliusawen.coastercreditcounter.dataModel.elements.Visit;
import de.juliusawen.coastercreditcounter.dataModel.orphanElements.Status;
import de.juliusawen.coastercreditcounter.dataModel.temporaryElements.GroupHeader;
import de.juliusawen.coastercreditcounter.dataModel.temporaryElements.VisitedAttraction;
import de.juliusawen.coastercreditcounter.globals.Constants;
import de.juliusawen.coastercreditcounter.tools.DrawableProvider;
import de.juliusawen.coastercreditcounter.tools.ResultFetcher;
import de.juliusawen.coastercreditcounter.tools.Toaster;
import de.juliusawen.coastercreditcounter.tools.activityDistributor.ActivityDistributor;
import de.juliusawen.coastercreditcounter.tools.activityDistributor.RequestCode;
import de.juliusawen.coastercreditcounter.tools.menuAgents.OptionsMenuAgent;
import de.juliusawen.coastercreditcounter.tools.menuAgents.PopupMenuAgent;
import de.juliusawen.coastercreditcounter.userInterface.contentRecyclerViewAdapter.ContentRecyclerViewAdapter;
import de.juliusawen.coastercreditcounter.userInterface.contentRecyclerViewAdapter.ContentRecyclerViewAdapterProvider;
import de.juliusawen.coastercreditcounter.userInterface.contentRecyclerViewAdapter.GroupType;
import de.juliusawen.coastercreditcounter.userInterface.contentRecyclerViewAdapter.RecyclerOnClickListener;
import de.juliusawen.coastercreditcounter.userInterface.fragments.AlertDialogFragment;

import static de.juliusawen.coastercreditcounter.globals.Constants.LOG_TAG;

public class ShowVisitActivity extends BaseActivity implements AlertDialogFragment.AlertDialogListener
{
    private ShowVisitActivityViewModel viewModel;
    private boolean actionConfirmed;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(LOG_TAG, Constants.LOG_DIVIDER_ON_CREATE + "ShowVisitActivity.onCreate:: creating activity...");

        setContentView(R.layout.activity_show_visit);
        super.onCreate(savedInstanceState);

        if(App.isInitialized)
        {
            this.viewModel = ViewModelProviders.of(this).get(ShowVisitActivityViewModel.class);

            if(this.viewModel.visit == null)
            {
                this.viewModel.visit = (Visit) App.content.getContentByUuid(UUID.fromString(getIntent().getStringExtra(Constants.EXTRA_ELEMENT_UUID)));
            }

            if(this.viewModel.optionsMenuAgent == null)
            {
                this.viewModel.optionsMenuAgent = new OptionsMenuAgent();
            }

            super.addHelpOverlayFragment(getString(R.string.title_help, getString(R.string.title_visit_show)), getString(R.string.help_text_show_visit));
            super.addToolbar();
            super.addToolbarHomeButton();
            super.setToolbarTitleAndSubtitle(this.viewModel.visit.getName(), this.viewModel.visit.getParent().getName());
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if(App.isInitialized)
        {
            invalidateOptionsMenu();

            if(this.viewModel.contentRecyclerViewAdapter == null)
            {
                this.viewModel.contentRecyclerViewAdapter = this.createContentRecyclerView()
                        .setTypefaceForType(GroupHeader.class, Typeface.BOLD);
            }
            this.viewModel.contentRecyclerViewAdapter.setOnClickListener(this.getContentRecyclerViewAdapterOnClickListener())
                    .addRideOnClickListener(this.getAddRideOnClickListener())
                    .deleteRideOnClickListener(this.getRemoveRideOnClickListener());

            this.recyclerView = findViewById(R.id.recyclerViewShowVisit);
            this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
            this.recyclerView.setAdapter(this.viewModel.contentRecyclerViewAdapter);

            if(Visit.isCurrentVisit(this.viewModel.visit))
            {
                this.viewModel.visit.setEditingEnabled(true);
            }

            if(this.viewModel.visit.isEditingEnabled())
            {
                this.viewModel.contentRecyclerViewAdapter.setFormatAsPrettyPrint(false);
            }
            else
            {
                this.viewModel.contentRecyclerViewAdapter.setFormatAsPrettyPrint(true);
            }

            super.addFloatingActionButton();
            this.decorateFloatingActionButton();
            this.handleFloatingActionButtonVisibility();

            Log.d(LOG_TAG, String.format("ShowVisitActivity.onResume:: %s isEditingEnabled[%S]", this.viewModel.visit, this.viewModel.visit.isEditingEnabled()));

            this.decorateFloatingActionButton();
        }
    }

    @Override
    public void onDestroy()
    {
        this.recyclerView.setAdapter(null);
        super.onDestroy();
    }


    //region --- OPTIONS MENU

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if(App.isInitialized)
        {
            this.viewModel.optionsMenuAgent
                    .add(OptionsMenuAgent.DISABLE_EDITING)
                    .add(OptionsMenuAgent.ENABLE_EDITING)
                    .add(OptionsMenuAgent.EXPAND_ALL)
                    .add(OptionsMenuAgent.COLLAPSE_ALL)
                    .create(menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        if(App.isInitialized)
        {
            this.viewModel.optionsMenuAgent
                    .setVisible(OptionsMenuAgent.DISABLE_EDITING, this.viewModel.visit.isEditingEnabled())
                    .setVisible(OptionsMenuAgent.ENABLE_EDITING, !this.viewModel.visit.isEditingEnabled())
                    .setEnabled(OptionsMenuAgent.EXPAND_ALL, !this.viewModel.contentRecyclerViewAdapter.isAllExpanded())
                    .setEnabled(OptionsMenuAgent.COLLAPSE_ALL, !this.viewModel.contentRecyclerViewAdapter.isAllCollapsed())
                    .prepare(menu);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(this.viewModel.optionsMenuAgent.handleOptionsItemSelected(item, this))
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleExpandAllSelected()
    {
        this.viewModel.contentRecyclerViewAdapter.expandAll();
    }

    @Override
    public void handleCollapseAllSelected()
    {
        this.viewModel.contentRecyclerViewAdapter.collapseAll();
    }

    @Override
    public void handleEnableEditingSelected()
    {
        this.viewModel.visit.setEditingEnabled(true);
        this.viewModel.contentRecyclerViewAdapter.setFormatAsPrettyPrint(false);

        this.handleFloatingActionButtonVisibility();

        Log.d(LOG_TAG, String.format("ShowVisitActivity.onOptionsItemSelected<ENABLE_EDITING>:: enabled editing for %s", this.viewModel.visit));
    }

    @Override
    public void handleDisableEditingSelected()
    {
        this.viewModel.visit.setEditingEnabled(false);
        this.viewModel.contentRecyclerViewAdapter.setFormatAsPrettyPrint(true);

        this.handleFloatingActionButtonVisibility();

        Log.d(LOG_TAG, String.format("ShowVisitActivity.onOptionsItemSelected<DISABLE_EDITING>:: disabled editing %s", this.viewModel.visit));
    }

    //endregion --- OPTIONS MENU


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(LOG_TAG, String.format("ShowVisitActivity.onActivityResult:: requestCode[%s], resultCode[%s]", requestCode, resultCode));

        if(resultCode == Activity.RESULT_OK)
        {
            List<IElement> resultElements = ResultFetcher.fetchResultElements(data);

            switch(RequestCode.values()[requestCode])
            {
                case PICK_ATTRACTIONS:
                {
                    for(IElement element : resultElements)
                    {
                        VisitedAttraction visitedAttraction = VisitedAttraction.create((IOnSiteAttraction) element);
                        this.viewModel.visit.addChildAndSetParent(visitedAttraction);

                        super.markForCreation(visitedAttraction);
                    }

                    this.viewModel.contentRecyclerViewAdapter.setItems(this.viewModel.visit.getChildrenOfType(VisitedAttraction.class));

                    super.markForUpdate(this.viewModel.visit);
                    break;
                }

                case SORT_ATTRACTIONS:
                {
                    IElement parent = resultElements.get(0).getParent();
                    if(parent != null)
                    {
                        this.viewModel.visit.reorderChildren(resultElements);
                        Log.d(LOG_TAG,
                                String.format("ShowVisitActivity.onActivityResult<SortAttractions>:: replaced %s's <children> with <sorted children>", this.viewModel.visit));

                        updateContentRecyclerView(true);

                        super.markForUpdate(this.viewModel.visit);
                    }
                    break;
                }
            }
        }
    }

    private void decorateFloatingActionButton()
    {
        super.setFloatingActionButtonIcon(DrawableProvider.getColoredDrawable(R.drawable.ic_baseline_add, R.color.white));
        super.setFloatingActionButtonOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.i(LOG_TAG, "ShowVisitActivity.onClickFloatingActionButton:: FloatingActionButton pressed");

                ActivityDistributor.startActivityPickForResult(
                        ShowVisitActivity.this,
                        RequestCode.PICK_ATTRACTIONS,
                        new LinkedList<IElement>(getNotYetAddedAttractionsWithDefaultStatus()));

            }
        });
    }

    private void handleFloatingActionButtonVisibility()
    {
        if(this.allAttractionsAdded() || !this.viewModel.visit.isEditingEnabled())
        {
            super.setFloatingActionButtonVisibility(false);
        }
        else
        {
            super.setFloatingActionButtonVisibility(true);
            this.viewModel.contentRecyclerViewAdapter.addBottomSpacer();
        }
    }

    private ContentRecyclerViewAdapter createContentRecyclerView()
    {
        HashSet<Class<? extends IElement>> childTypesToExpand = new HashSet<>();
        childTypesToExpand.add(VisitedAttraction.class);

        return ContentRecyclerViewAdapterProvider.getCountableContentRecyclerViewAdapter(this.viewModel.visit.getChildrenOfType(VisitedAttraction.class), childTypesToExpand)
                .groupItemsByType(GroupType.ATTRACTION_CATEGORY);
    }

    private RecyclerOnClickListener.OnClickListener getContentRecyclerViewAdapterOnClickListener()
    {
        return new RecyclerOnClickListener.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Element element = (Element) view.getTag();

                if(element instanceof GroupHeader)
                {
                    viewModel.contentRecyclerViewAdapter.toggleExpansion(element);
                }
                else if(element instanceof Attraction)
                {
                    Toaster.makeToast(ShowVisitActivity.this, element + " clicked");
                }
            }

            @Override
            public boolean onLongClick(View view)
            {
                viewModel.longClickedElement = (Element) view.getTag();

                if(viewModel.longClickedElement instanceof GroupHeader)
                {
                    PopupMenuAgent.getAgent()
                            .add(PopupMenuAgent.SORT_ATTRACTIONS)
                            .setVisible(PopupMenuAgent.SORT_ATTRACTIONS,
                                    viewModel.longClickedElement.getChildCountOfType(Attraction.class) > 1 || viewModel.longClickedElement.getChildCountOfType(VisitedAttraction.class) > 1)
                            .show(ShowVisitActivity.this, view);
                }
                else if(viewModel.longClickedElement instanceof Attraction)
                {
                    PopupMenuAgent.getAgent()
                            .add(PopupMenuAgent.DELETE_ELEMENT)
                            .show(ShowVisitActivity.this, view);
                }

                return true;
            }
        };
    }

    @Override
    public void handleSortAttractionsClicked()
    {
        List<IElement> attractions = new ArrayList<>();

        if(this.viewModel.longClickedElement.hasChildrenOfType(Attraction.class))
        {
            attractions = this.viewModel.longClickedElement.getChildrenOfType(Attraction.class);
        }
        else if(this.viewModel.longClickedElement.hasChildrenOfType(VisitedAttraction.class))
        {
            attractions = this.viewModel.longClickedElement.getChildrenOfType(VisitedAttraction.class);
        }

        ActivityDistributor.startActivitySortForResult(Objects.requireNonNull(ShowVisitActivity.this), RequestCode.SORT_ATTRACTIONS, attractions);
    }

    @Override
    public void handleDeleteElementClicked()
    {
        //let user verify delete when any rides are counted
        if(viewModel.longClickedElement.getChildCount() > 0)
        {
            AlertDialogFragment alertDialogFragmentDelete =
                    AlertDialogFragment.newInstance(
                            R.drawable.ic_baseline_warning,
                            getString(R.string.alert_dialog_title_delete_element),
                            getString(R.string.alert_dialog_message_delete_visited_attraction, viewModel.longClickedElement.getName()),
                            getString(R.string.text_accept),
                            getString(R.string.text_cancel),
                            RequestCode.DELETE,
                            false);

            alertDialogFragmentDelete.setCancelable(false);
            alertDialogFragmentDelete.show(getSupportFragmentManager(), Constants.FRAGMENT_TAG_ALERT_DIALOG);
        }
        else
        {
            deleteVisitedAttraction();
        }
    }

    @Override
    public void onAlertDialogClick(RequestCode requestCode, DialogInterface dialog, int which)
    {
        dialog.dismiss();

        Snackbar snackbar;

        if(which == DialogInterface.BUTTON_POSITIVE)
        {
            if(requestCode.equals(RequestCode.DELETE))
            {
                snackbar = Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.action_confirm_delete_text, viewModel.longClickedElement.getName()),
                        Snackbar.LENGTH_LONG);

                snackbar.setAction(R.string.action_confirm_text, new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        actionConfirmed = true;
                        Log.i(LOG_TAG, "ShowVisitActivity.onSnackbarClick<DELETE>:: action <DELETE> confirmed");
                    }
                });

                snackbar.addCallback(new Snackbar.Callback()
                {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event)
                    {
                        if(actionConfirmed)
                        {
                            actionConfirmed = false;
                            deleteVisitedAttraction();
                        }
                        else
                        {
                            Log.d(LOG_TAG, "ShowVisitActivity.onDismissed<DELETE>:: action <DELETE> not confirmed - doing nothing");
                        }
                    }
                });

                snackbar.show();
            }
        }
    }

    private void deleteVisitedAttraction()
    {
        Log.i(LOG_TAG, String.format("ShowVisitActivity.deleteVisitedAttraction:: deleting %s...", viewModel.longClickedElement));

        super.markForDeletion(this.viewModel.longClickedElement, true);
        super.markForUpdate(this.viewModel.longClickedElement.getParent());
        this.viewModel.longClickedElement.deleteElementAndDescendants();
        updateContentRecyclerView(true);
        this.handleFloatingActionButtonVisibility();
    }

    private View.OnClickListener getAddRideOnClickListener()
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                VisitedAttraction visitedAttraction = (VisitedAttraction) view.getTag();

                Log.v(LOG_TAG, String.format("ShowVisitActivity.getAddRideOnClickListener.onClick:: adding ride to %s for %s", visitedAttraction, visitedAttraction.getParent()));

                Ride ride = visitedAttraction.addRide();

                ShowVisitActivity.super.markForCreation(ride);
                ShowVisitActivity.super.markForUpdate(ShowVisitActivity.this.viewModel.visit);

                updateContentRecyclerView(false);
            }
        };
    }

    private View.OnClickListener getRemoveRideOnClickListener()
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                VisitedAttraction visitedAttraction = (VisitedAttraction) view.getTag();

                Log.v(LOG_TAG, String.format("ShowVisitActivity.getRemoveRideOnClickListener.onClick:: deleting latest ride on %s for %s",
                        visitedAttraction.getOnSiteAttraction(), visitedAttraction.getParent()));

                Ride ride = visitedAttraction.deleteLatestRide();
                if(ride != null)
                {
                    ShowVisitActivity.super.markForDeletion(ride, false);
                    ShowVisitActivity.super.markForUpdate(ShowVisitActivity.this.viewModel.visit);
                    updateContentRecyclerView(false);
                }
            }
        };
    }

    private boolean allAttractionsAdded()
    {
        if(this.viewModel.visit != null)
        {
            List<IAttraction> notYetAddedAttractions = this.getNotYetAddedAttractionsWithDefaultStatus();
            if(notYetAddedAttractions.size() > 0)
            {
                Log.i(LOG_TAG, String.format("ShowVisitActivity.allAttractionsAdded:: [%d] attractions not added yet", notYetAddedAttractions.size()));
            }
            else
            {
                Log.i(LOG_TAG, "ShowVisitActivity.allAttractionsAdded:: all attractions added");
            }

            return notYetAddedAttractions.isEmpty();
        }
        else
        {
            return false;
        }
    }

    private List<IAttraction> getNotYetAddedAttractionsWithDefaultStatus()
    {
        List<IAttraction> visitedAttractions = new ArrayList<>();
        for(VisitedAttraction visitedAttraction : viewModel.visit.getChildrenAsType(VisitedAttraction.class))
        {
            visitedAttractions.add(visitedAttraction.getOnSiteAttraction());
        }

        List<IAttraction> allAttractions = new LinkedList<IAttraction>(this.viewModel.visit.getParent().getChildrenAsType(IOnSiteAttraction.class));
        allAttractions.removeAll(visitedAttractions);

        List<IAttraction> attractionsWithDefaultStatus = new LinkedList<>();
        for(IAttraction attraction : allAttractions)
        {
            if(attraction.getStatus().equals(Status.getDefault()))
            {
                attractionsWithDefaultStatus.add(attraction);
            }
        }
        
        return attractionsWithDefaultStatus;
    }

    private void updateContentRecyclerView(boolean resetContent)
    {
        if(resetContent)
        {
            Log.d(LOG_TAG, "ShowVisitActivity.updateContentRecyclerView:: resetting content...");
            this.viewModel.contentRecyclerViewAdapter.setItems(this.viewModel.visit.getChildrenOfType(VisitedAttraction.class))
                    .expandAll();

            if(!this.allAttractionsAdded())
            {
                this.viewModel.contentRecyclerViewAdapter.addBottomSpacer();
            }
        }
        else
        {
            Log.d(LOG_TAG, "ShowVisitActivity.updateContentRecyclerView:: notifying data set changed...");
            this.viewModel.contentRecyclerViewAdapter.notifyDataSetChanged();
        }
    }
}