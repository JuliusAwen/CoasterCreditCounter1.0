package de.juliusawen.coastercreditcounter.content;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.juliusawen.coastercreditcounter.Toolbox.Constants;
import de.juliusawen.coastercreditcounter.content.database.DatabaseMock;

public class Content
{
    private Location locationRoot;
    private Map<UUID, Element> elements;

    private static final Content instance = new Content();

    public static Content getInstance()
    {
        return instance;
    }

    private Content()
    {
        Log.v(Constants.LOG_TAG,  String.format("Content:: Constructor called."));

        this.elements = new HashMap<>();

        new DatabaseMock().fetchContent(this);
        this.flattenContentTree(this.getLocationRoot());
    }

    public Location getLocationRoot()
    {
        return this.locationRoot;
    }

    public void setLocationRoot(Location locationRoot)
    {
        if(locationRoot.getParent() == null)
        {
            Log.v(Constants.LOG_TAG,  String.format("Content.setLocationRoot:: root[%s] set.", locationRoot.getName()));
            this.locationRoot = locationRoot;
        }
        else
        {
            throw new IllegalStateException("Location with parent can not be set as location root - parent has to be null.");
        }
    }

    public Element getElementByUuid(UUID uuid)
    {
        if(this.elements.containsKey(uuid))
        {
            return this.elements.get(uuid);
        }
        else
        {
            throw new IllegalStateException(String.format("No element found for uuid[%s].", uuid));
        }
    }

    private void flattenContentTree(Location location)
    {

        this.elements.put(location.getUuid(), location);

        for (Location child : location.getChildren())
        {
            this.flattenContentTree(child);
        }

        if(location.getClass().equals(Park.class))
        {
            for (Attraction attraction : ((Park) location).getAttractions())
            {
                this.elements.put(attraction.getUuid(), attraction);
            }
        }
    }

    public ArrayList<String> getUuidStringsFromElements(List<? extends Element> elements)
    {
        ArrayList<String> strings = new ArrayList<>();

        for(Element element : elements)
        {
            strings.add(element.getUuid().toString());
        }

        return strings;
    }

    public List<Element> getElementsFromUuidStrings(List<String> uuidStrings)
    {
        List<Element> elements = new ArrayList<>();

        for(String uuidString : uuidStrings)
        {
            elements.add(this.getElementByUuid(UUID.fromString(uuidString)));
        }

        return elements;
    }

    public Location getLocationFromUuidString(String uuidString)
    {
        return (Location) this.getElementByUuid(UUID.fromString(uuidString));
    }

    public List<Location> getLocationsFromUuidStrings(List<String> uuidStrings)
    {
        List<Location> locations = new ArrayList<>();

        for(String uuidString : uuidStrings)
        {
            locations.add((Location) this.getElementByUuid(UUID.fromString(uuidString)));
        }

        return locations;
    }

    public List<Location> convertElementsToLocations(List<Element> elements)
    {
        List<Location> locations = new ArrayList<>();

        for(Element element : elements)
        {
            locations.add(((Location) element));
        }

        return locations;
    }

    public List<Attraction> convertElementsToAttractions(List<Element> elements)
    {
        List<Attraction> attractions = new ArrayList<>();

        for(Element element : elements)
        {
            attractions.add(((Attraction) element));
        }

        return attractions;
    }

    public void addElement(Element element)
    {
        Log.v(Constants.LOG_TAG,  String.format("Content.addElement:: element[%s] added.", element.getName()));
        this.elements.put(element.getUuid(), element);
    }

    public void removeLocationAndChildren(Element element)
    {
        Location location = (Location) element;

        if(!location.getChildren().isEmpty())
        {
            for(Location child : location.getChildren())
            {
                this.removeLocationAndChildren(child);
            }
        }

        this.removeLocation(element);
    }

    public void removeLocation(Element element)
    {
        Log.v(Constants.LOG_TAG,  String.format("Content.removeElement:: element[%s] removed.", element.getName()));
        this.elements.remove(element.getUuid());
    }
}
