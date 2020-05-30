package de.juliusawen.coastercreditcounter.userInterface.contentRecyclerViewAdapter;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.juliusawen.coastercreditcounter.dataModel.elements.IElement;
import de.juliusawen.coastercreditcounter.tools.logger.Log;
import de.juliusawen.coastercreditcounter.tools.logger.LogLevel;

abstract class AdapterContentHandler extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    protected RecyclerView recyclerView;

    protected List<IElement> content = new ArrayList<>();
    protected ArrayList<IElement> ungroupedContent = new ArrayList<>();

    private  ContentRecyclerViewAdapterConfiguration configuration;

    private GroupType groupType = GroupType.NONE;
    private final GroupHeaderProvider groupHeaderProvider;

    @Deprecated
    AdapterContentHandler()
    {
        this.groupHeaderProvider = new GroupHeaderProvider();
        Log.frame(LogLevel.VERBOSE, "instantiated", '=', true);
    }

    AdapterContentHandler(ContentRecyclerViewAdapterConfiguration configuration)
    {
        this.configuration = configuration;
        this.groupHeaderProvider = new GroupHeaderProvider();
        Log.frame(LogLevel.VERBOSE, "instantiated", '=', true);
    }

    protected void setConfiguration(ContentRecyclerViewAdapterConfiguration configuration)
    {
        this.configuration = configuration;
        Log.v(String.format(Locale.getDefault(), "[%d] relevant child types", configuration.getRelevantChildTypes().size()));
    }

    protected ContentRecyclerViewDecoration getDecoration()
    {
        return this.configuration.getDecoration();
    }

    protected boolean isSelectable()
    {
        return this.configuration.isSelecetable();
    }

    protected boolean isMultipleSelection()
    {
        return this.configuration.isMultipleSelection();
    }

    protected boolean hasRelevantChildTypes()
    {
        return !this.getRelevantChildTypes().isEmpty();
    }

    protected LinkedHashSet<Class<? extends IElement>> getRelevantChildTypes()
    {
        return this.configuration.getRelevantChildTypes();
    }

    protected GroupType getGroupType()
    {
        return this.groupType;
    }

    protected boolean useBottomSpacer()
    {
        return this.configuration.useBottomSpacer();
    }

    protected boolean hasExternalOnClickListeners()
    {
        return !(this.configuration.getOnClickListenersByType().isEmpty() && this.configuration.getOnLongClickListenersByType().isEmpty());
    }

    protected Map<Class<? extends IElement>, View.OnClickListener> getExternalOnClickListenersByType()
    {
        return this.configuration.getOnClickListenersByType();
    }

    protected Map<Class<? extends IElement>, View.OnLongClickListener> getExternalOnLongClickListenersByType()
    {
        return this.configuration.getOnLongClickListenersByType();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        Log.d("attaching to RecyclerView...");
        this.recyclerView = recyclerView;
        super.onAttachedToRecyclerView(recyclerView);
    }

    protected RecyclerView.LayoutManager getLayoutManager()
    {
        return this.recyclerView.getLayoutManager();
    }

    @Override
    public int getItemCount()
    {
        return this.content.size();
    }

    protected void setContent(List<IElement> content)
    {
        Log.v(String.format(Locale.getDefault(), "setting [%d] items...", content.size()));

        this.content = content;
        this.ungroupedContent = new ArrayList<>(content);
        this.groupContent(this.groupType);
    }

    protected boolean exists(IElement element)
    {
        if(element == null)
        {
            Log.w("Element is null");
            return false;
        }

        return this.content.contains(element);
    }

    protected IElement getItem(int position)
    {
        if(position >= this.getItemCount())
        {
            throw new IllegalStateException(String.format(Locale.getDefault(), "Position[%d] does not exist: Content contains [%d] items", position, this.getItemCount()));
        }

        return this.content.get(position);
    }

    protected int getPosition(IElement element)
    {
        if(!this.exists(element))
        {
            throw new IllegalStateException(String.format("Item %s does not exist in Content", element));
        }

        return this.content.indexOf(element);
    }

    protected void insertItem(IElement element)
    {
        this.insertItem(this.getItemCount(), element);
    }

    protected void insertItem(int position, IElement element)
    {
        this.content.add(position, element);
        super.notifyItemInserted(this.getPosition(element));
    }

    protected void notifyItemChanged(IElement element)
    {
        if(!this.exists(element))
        {
            Log.w(String.format("cannot notify - %s does not exist in content", element));
            return;
        }

        super.notifyItemChanged(this.getPosition(element));
    }

    protected void removeItem(IElement element)
    {
        if(!this.exists(element))
        {
            Log.w(String.format("cannot remove - %s does not exist in content", element));
            return;
        }

        super.notifyItemRemoved(this.getPosition(element));
        this.content.remove(element);
    }

    protected void swapItems(IElement element1, IElement element2)
    {
        if(!this.exists(element1))
        {
            Log.w(String.format("cannot swap - %s does not exist in content", element1));
            return;
        }

        if(!this.exists(element2))
        {
            Log.w(String.format("cannot swap - %s does not exist in content", element2));
            return;
        }

        int fromPosition = this.getPosition(element1);
        int toPosition = this.getPosition(element2);

        Collections.swap(this.content, fromPosition, toPosition);
        super.notifyItemMoved(fromPosition, toPosition);
        this.scrollToItem(element1);
    }

    protected void scrollToItem(IElement element)
    {
        if(this.content.isEmpty())
        {
            Log.w("cannot scroll - Content is empty");
            return;
        }

        if(!this.exists(element))
        {
            Log.w(String.format("cannot scroll - %s does not exist in Content", element));
            return;
        }

        if(this.recyclerView == null)
        {
            Log.w("cannot scroll - ContentRecyclerViewAdapter is not attached to RecyclerView yet");
            return;
        }

        Log.d(String.format("scrolling to %s", element));
        this.recyclerView.scrollToPosition(this.getPosition(element));
    }

    protected void groupContent(GroupType groupType)
    {
        if(groupType == null)
        {
            Log.w("GroupType is null - falling back to default GroupType.NONE");
            groupType = GroupType.NONE;
        }

        this.groupType = groupType;
        Log.d(String.format("GroupType[%s]...", this.groupType));


        //        if(App.preferences.expandLatestYearHeaderByDefault())
        //        {
        //            SpecialGroupHeader latestSpecialGroupHeader = this.OLDGroupHeaderProvider.getSpecialGroupHeaderForLatestYear(groupedItems);
        //            this.expandedItems.add(latestSpecialGroupHeader);
        //        }

        this.content = this.groupHeaderProvider.groupElements(this.ungroupedContent, groupType);
        super.notifyDataSetChanged();
        this.scrollToItem(this.getItem(0));
    }

    protected boolean hasRelevantChildren(IElement element)
    {
        return !this.fetchRelevantChildren(element).isEmpty();
    }

    protected ArrayList<IElement> fetchRelevantChildren(IElement item)
    {
        ArrayList<IElement> distinctRelevantChildren = new ArrayList<>();

        for(IElement child : item.getChildren())
        {
            for(Class<? extends IElement> childType : this.getRelevantChildTypes())
            {

                if(childType.isAssignableFrom(child.getClass()) && !distinctRelevantChildren.contains(child))
                {
                    distinctRelevantChildren.add(child);
                    break;
                }
            }
        }

        return distinctRelevantChildren;
    }
}
