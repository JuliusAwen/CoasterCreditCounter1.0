package de.juliusawen.coastercreditcounter.userInterface.contentRecyclerViewAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import de.juliusawen.coastercreditcounter.R;
import de.juliusawen.coastercreditcounter.dataModel.elements.IElement;
import de.juliusawen.coastercreditcounter.tools.logger.Log;
import de.juliusawen.coastercreditcounter.tools.logger.LogLevel;

class ContentRecyclerViewAdapter extends AdapterExpansionHandler implements IContentRecyclerViewAdapter
{
    ContentRecyclerViewAdapter(Configuration configuration, List<IElement> content)
    {
        this.initialize(configuration, content);

        Log.wrap(LogLevel.DEBUG,
                String.format("Details:\n\n%s\n\n%s\n\n%s",
                        String.format(Locale.getDefault(), "[%d] Elements", this.content.size()),
                        configuration,
                        configuration.getDecoration()),
                '=', false);
        Log.wrap(LogLevel.INFO, "instantiated", '#', true);
    }

    private void initialize(Configuration configuration, List<IElement> content)
    {
        super.configure(configuration);
        super.setContent(content);
    }

    @Override
    public int getItemViewType(int position)
    {
        IElement element = super.getItem(position);

        if(element.isVisitedAttraction())
        {
            return ItemViewType.VISITED_ATTRACTION.ordinal();
        }
        else if(element.isBottomSpacer())
        {
            return ItemViewType.BOTTOM_SPACER.ordinal();
        }
        else
        {
            return ItemViewType.ELEMENT.ordinal();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int typeOfView)
    {
        RecyclerView.ViewHolder viewHolder;
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());

        switch(ItemViewType.getValue(typeOfView))
        {
            case ELEMENT:
                view = layoutInflater.inflate(R.layout.layout_recycler_view_item_view_type_element, viewGroup, false);
                viewHolder = new ViewHolderElement(view);
                break;

            case VISITED_ATTRACTION:
                view = layoutInflater.inflate(R.layout.layout_recycler_view_item_view_type_visited_attraction, viewGroup, false);
                viewHolder = new ViewHolderVisitedAttraction(view);
                break;

            case BOTTOM_SPACER:
                view = layoutInflater.inflate(R.layout.layout_bottom_spacer, viewGroup, false);
                viewHolder = new ViewHolderBottomSpacer(view);
                break;

            default:
                throw new IllegalStateException(String.format("unknown ViewType [%s]", ItemViewType.getValue(typeOfView)));
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position)
    {
        ItemViewType itemViewType = ItemViewType.getValue(viewHolder.getItemViewType());
        Log.v(String.format(Locale.getDefault(), "binding ViewType[%s] for position[%d]", itemViewType, position));

        switch (itemViewType)
        {
            case ELEMENT:
                ViewHolderElement viewHolderElement = (ViewHolderElement) viewHolder;
                super.bindViewHolderElement(viewHolderElement, position);
                break;

//            case VISITED_ATTRACTION:
//                ViewHolderVisitedAttraction viewHolderVisitedAttraction = (ViewHolderVisitedAttraction) viewHolder;
//                this.bindViewHolderVisitedAttraction(viewHolderVisitedAttraction, position);
//                break;

            case BOTTOM_SPACER:
                break;

            default:
                throw new IllegalStateException(String.format("unknown ViewType[%s]", itemViewType));
        }
    }


    private enum ItemViewType
    {
        UNDETERMINED,
        ELEMENT,
        VISITED_ATTRACTION,
        BOTTOM_SPACER;

        static ItemViewType getValue(int ordinal)
        {
            if(ItemViewType.values().length >= ordinal)
            {
                return ItemViewType.values()[ordinal];
            }
            else
            {
                Log.e(String.format("ordinal [%s] out of bounds (Enum has [%s] values) - returning [%s]", ordinal, values().length, values()[0]));
                return values()[0];
            }
        }
    }
}

