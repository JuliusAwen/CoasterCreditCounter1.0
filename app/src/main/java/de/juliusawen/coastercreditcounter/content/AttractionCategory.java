package de.juliusawen.coastercreditcounter.content;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.juliusawen.coastercreditcounter.globals.Constants;

public class AttractionCategory extends Element
{
    private AttractionCategory(String name, UUID uuid)
    {
        super(name, uuid);
    }

    public static AttractionCategory create(String name)
    {
        AttractionCategory attractionCategory = null;
        if(!name.trim().isEmpty())
        {
            name = name.trim();

            attractionCategory = new AttractionCategory(name, UUID.randomUUID());
            Log.e(Constants.LOG_TAG,  String.format("AttractionCategory.create:: %s created.", attractionCategory));
        }
        else
        {
            Log.e(Constants.LOG_TAG,  String.format("AttractionCategory.create:: invalid name[%s] - attractionCategory not created.", name));
        }
        return attractionCategory;
    }

    public static List<AttractionCategory> convertToAttractionCategories(List<? extends Element> elements)
    {
        List<AttractionCategory> attractionCategories = new ArrayList<>();
        for(Element element : elements)
        {
            if(element.isInstance(AttractionCategory.class))
            {
                attractionCategories.add(0, (AttractionCategory) element);
            }
            else
            {
                String errorMessage = String.format("Attraction.convertToAttractionCategories:: type mismatch - %s is not of type <AttractionCategory>", element);
                Log.e(Constants.LOG_TAG, errorMessage);
                throw new IllegalStateException(errorMessage);
            }
        }
        return attractionCategories;
    }

    public static void removeAllChildren(List<AttractionCategory> attractionCategories)
    {
        for(AttractionCategory attractionCategory : attractionCategories)
        {
            attractionCategory.getChildren().clear();
        }
        Log.v(Constants.LOG_TAG,  String.format("AttractionCategory.removeAllChildren::children removed from #[%d] categories.", attractionCategories.size()));
    }
}