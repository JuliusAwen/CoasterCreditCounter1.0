package de.juliusawen.coastercreditcounter.frontend.attractions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import de.juliusawen.coastercreditcounter.R;
import de.juliusawen.coastercreditcounter.backend.GroupHeader.AttractionCategoryHeader;
import de.juliusawen.coastercreditcounter.backend.application.App;
import de.juliusawen.coastercreditcounter.backend.attractions.Attraction;
import de.juliusawen.coastercreditcounter.backend.attractions.IOnSiteAttraction;
import de.juliusawen.coastercreditcounter.backend.elements.Element;
import de.juliusawen.coastercreditcounter.backend.elements.IElement;
import de.juliusawen.coastercreditcounter.backend.elements.Park;
import de.juliusawen.coastercreditcounter.backend.orphanElements.Status;
import de.juliusawen.coastercreditcounter.frontend.contentRecyclerViewAdapter.ContentRecyclerViewAdapter;
import de.juliusawen.coastercreditcounter.frontend.contentRecyclerViewAdapter.ContentRecyclerViewAdapterProvider;
import de.juliusawen.coastercreditcounter.frontend.contentRecyclerViewAdapter.RecyclerOnClickListener;
import de.juliusawen.coastercreditcounter.globals.Constants;
import de.juliusawen.coastercreditcounter.toolbox.ActivityTool;
import de.juliusawen.coastercreditcounter.toolbox.ResultTool;
import de.juliusawen.coastercreditcounter.toolbox.Toaster;

public  class ShowAttractionsFragment extends Fragment
{
    private ShowAttractionsFragmentViewModel viewModel;
    private RecyclerView recyclerView;
    private ShowAttractionsFragmentInteraction showAttractionsFragmentInteraction;

    public ShowAttractionsFragment() {}

    public static ShowAttractionsFragment newInstance(String uuidString)
    {
        Log.i(Constants.LOG_TAG, Constants.LOG_DIVIDER_ON_CREATE + "ShowAttractionsFragment.newInstance:: instantiating fragment...");

        ShowAttractionsFragment showAttractionsFragment =  new ShowAttractionsFragment();
        Bundle args = new Bundle();
        args.putString(Constants.FRAGMENT_ARG_PARK_UUID, uuidString);
        showAttractionsFragment.setArguments(args);

        return showAttractionsFragment;
    }

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        Log.v(Constants.LOG_TAG, "ShowAttractionsFragment.onCreate:: creating fragment...");
        super.onCreate(savedInstanceState);

        this.viewModel = ViewModelProviders.of(this).get(ShowAttractionsFragmentViewModel.class);

        if(this.viewModel.park == null)
        {
            if(getArguments() != null)
            {
                this.viewModel.park = (Park) App.content.getContentByUuid(UUID.fromString(getArguments().getString(Constants.FRAGMENT_ARG_PARK_UUID)));
            }
        }

        if(this.viewModel.contentRecyclerViewAdapter == null)
        {
            this.viewModel.contentRecyclerViewAdapter = this.createContentRecyclerViewAdapter();
            this.viewModel.contentRecyclerViewAdapter.setTypefaceForType(AttractionCategoryHeader.class, Typeface.BOLD);
        }
        this.viewModel.contentRecyclerViewAdapter.setOnClickListener(this.getContentRecyclerViewAdapterOnClickListener());

        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_show_attractions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        this.recyclerView = view.findViewById(R.id.recyclerViewFragmentShowAttractions);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        this.recyclerView.setAdapter(this.viewModel.contentRecyclerViewAdapter);
    }

    @Override
    public void onDestroyView()
    {
        this.recyclerView.setAdapter(null);
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.i(Constants.LOG_TAG, String.format("ShowAttractionsFragment.onActivityResult:: requestCode[%s], resultCode[%s]", requestCode, resultCode));

        if(resultCode == Activity.RESULT_OK)
        {
            IElement selectedElement = ResultTool.fetchResultElement(data);

            if(requestCode == Constants.REQUEST_CODE_SORT_ATTRACTIONS)
            {
                List<IElement> resultElements = ResultTool.fetchResultElements(data);

                IElement parent = resultElements.get(0).getParent();
                if(parent != null)
                {
                    this.viewModel.park.reorderChildren(resultElements);

                    this.viewModel.contentRecyclerViewAdapter.setItems(this.viewModel.park.getChildrenOfType(IOnSiteAttraction.class));

                    if(selectedElement != null)
                    {
                        Log.d(Constants.LOG_TAG, String.format("ShowAttractionsFragment.onActivityResult<SortAttractions>:: scrolling to selected element %s...", selectedElement));
                        this.viewModel.contentRecyclerViewAdapter.scrollToItem(((Attraction)selectedElement).getAttractionCategory());
                    }
                }
            }
            else if(requestCode == Constants.REQUEST_CODE_PICK_STATUS)
            {
                Attraction attraction = (Attraction)this.viewModel.longClickedElement;
                attraction.setStatus((Status)selectedElement);
                Log.d(Constants.LOG_TAG, String.format("ShowAttractionsFragment.onActivityResult<PickStatus>:: updated %s's status to %s...", attraction, selectedElement));

                this.showAttractionsFragmentInteraction.updateElement(attraction);
                this.viewModel.contentRecyclerViewAdapter.notifyDataSetChanged();
            }
            else if(requestCode == Constants.REQUEST_CODE_CREATE_CUSTOM_ATTRACTION)
            {
                this.viewModel.contentRecyclerViewAdapter.setItems(this.viewModel.park.getChildrenOfType(IOnSiteAttraction.class));
                this.viewModel.contentRecyclerViewAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if(context instanceof ShowAttractionsFragment.ShowAttractionsFragmentInteraction)
        {
            this.showAttractionsFragmentInteraction = (ShowAttractionsFragment.ShowAttractionsFragmentInteraction) context;
        }
        else
        {
            throw new RuntimeException(context.toString() + " must implement ShowAttractionsFragmentInteraction");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        this.viewModel = null;
        this.recyclerView = null;
        this.showAttractionsFragmentInteraction = null;
    }

    private ContentRecyclerViewAdapter createContentRecyclerViewAdapter()
    {
        HashSet<Class<? extends IElement>> childTypesToExpand = new HashSet<>();
        childTypesToExpand.add(Attraction.class);

        ContentRecyclerViewAdapter contentRecyclerViewAdapter = ContentRecyclerViewAdapterProvider.getExpandableContentRecyclerViewAdapter(
                this.viewModel.park.getChildrenOfType(IOnSiteAttraction.class),
                childTypesToExpand,
                Constants.TYPE_ATTRACTION_CATEGORY);
        contentRecyclerViewAdapter.displayManufacturers(true);
        contentRecyclerViewAdapter.displayStatus(true);

        return contentRecyclerViewAdapter;
    }

    private RecyclerOnClickListener.OnClickListener getContentRecyclerViewAdapterOnClickListener()
    {
        return new RecyclerOnClickListener.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Element element = (Element) view.getTag();

                if(element instanceof Attraction)
                {
                    Toaster.makeToast(getContext(), String.format("ShowAttraction not yet implemented %s", (Element) view.getTag()));
                }
                else if(element instanceof AttractionCategoryHeader)
                {
                    viewModel.contentRecyclerViewAdapter.toggleExpansion(element);
                }
            }

            @Override
            public boolean onLongClick(View view)
            {
                if(view.getTag() instanceof AttractionCategoryHeader)
                {
                    AttractionCategoryHeader.handleOnAttractionCategoryHeaderLongClick(getActivity(), view);
                }
                else
                {
                    viewModel.longClickedElement = (IElement)view.getTag();

                    PopupMenu popupMenu = new PopupMenu(getContext(), view);
                    popupMenu.getMenu().add(0, Constants.SELECTION_CHANGE_STATUS, Menu.NONE, R.string.selection_change_status);

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                    {
                        @Override
                        public boolean onMenuItemClick(MenuItem item)
                        {
                            Log.i(Constants.LOG_TAG, String.format("ShowAttractionsFragment.onMenuItemClick:: [%S] selected", item.getItemId()));

                            int id = item.getItemId();

                            if(id == Constants.SELECTION_CHANGE_STATUS)
                            {
                                ActivityTool.startActivityPickForResult(
                                        getContext(),
                                        Constants.REQUEST_CODE_PICK_STATUS,
                                        App.content.getContentOfType(Status.class));
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }

                return true;
            }
        };
    }

    public interface ShowAttractionsFragmentInteraction
    {
        void updateElement(IElement elementToUpdate);
    }
}