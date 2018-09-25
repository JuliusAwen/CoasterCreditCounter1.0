package de.juliusawen.coastercreditcounter.data.orphanElements;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import de.juliusawen.coastercreditcounter.data.elements.Element;
import de.juliusawen.coastercreditcounter.data.elements.Visit;
import de.juliusawen.coastercreditcounter.globals.App;
import de.juliusawen.coastercreditcounter.globals.Constants;

public class YearHeader extends OrphanElement
{
    private YearHeader(String name, UUID uuid)
    {
        super(name, uuid);
    }

    public static YearHeader create(String name)
    {
        YearHeader yearHeader = null;
        if(!name.trim().isEmpty())
        {
            name = name.trim();

            yearHeader = new YearHeader(name, UUID.randomUUID());
            Log.v(Constants.LOG_TAG,  String.format("YearHeader.create:: %s created", yearHeader));
        }
        else
        {
            Log.e(Constants.LOG_TAG,  String.format("YearHeader.create:: invalid name[%s] - yearHeader not created", name));
        }
        return yearHeader;
    }

    public static YearHeader getLatestYearHeader(List<? extends Element> yearHeaders)
    {
        YearHeader latestYearHeader = null;

        for(Element yearHeader : yearHeaders)
        {
            if(latestYearHeader == null)
            {
                latestYearHeader = (YearHeader) yearHeader;
            }
            else if((Integer.valueOf(yearHeader.getName()) > (Integer.valueOf(latestYearHeader.getName()))))
            {
                latestYearHeader = (YearHeader) yearHeader;
            }
        }

        Log.v(Constants.LOG_TAG,  String.format("YearHeader.getLatestYearHeader:: [%s] to be found latest YearHeader in a list of [%d]", latestYearHeader, yearHeaders.size()));
        return latestYearHeader;
    }

//    public static List<YearHeader> convertToYearHeader(List<? extends Element> elements)
//    {
//        List<YearHeader> yearHeaders = new ArrayList<>();
//        for(Element element : elements)
//        {
//            if(element.isInstance(YearHeader.class))
//            {
//                yearHeaders.add((YearHeader) element);
//            }
//            else
//            {
//                String errorMessage = String.format("type mismatch: %s is not of type <YearHeader>", element);
//                Log.e(Constants.LOG_TAG, "YearHeader.convertToYearHeader:: " + errorMessage);
//                throw new IllegalStateException(errorMessage);
//            }
//        }
//        return yearHeaders;
//    }

    public static List<Element> addYearHeaders(List<Visit> visits)
    {
        if(visits.isEmpty())
        {
            Log.v(Constants.LOG_TAG, "YearHeader.addYearHeaders:: no elements found");
            return new ArrayList<Element>(visits);
        }

        Log.v(Constants.LOG_TAG, String.format("YearHeader.addYearHeaders:: adding YearHeaders to [%d] elements...", visits.size()));

        //Todo: remove children from all YearHeaders
        OrphanElement.removeAllChildren(App.content.getOrphanElementsOfType(YearHeader.class));

//        List<Visit> visits = Visit.convertToVisits(elements);
        List<Element> preparedElements = new ArrayList<>();

        DateFormat simpleDateFormat = new SimpleDateFormat(Constants.SIMPLE_DATE_FORMAT_YEAR_PATTERN, Locale.getDefault());

        for(Visit visit : visits)
        {
            String year = String.valueOf(simpleDateFormat.format(visit.getCalendar().getTime()));

            Element existingYearHeader = null;
            for(Element yearHeader : preparedElements)
            {
                if(yearHeader.getName().equals(year))
                {
                    existingYearHeader = yearHeader;
                }
            }

            if(existingYearHeader != null)
            {
                existingYearHeader.addChildToOrphanElement(visit);
            }
            else
            {
                YearHeader yearHeader;
                yearHeader = (YearHeader) App.content.getOrphanElement(year);
                if(yearHeader == null)
                {
                    yearHeader = YearHeader.create(year);
                    App.content.addOrphanElement(yearHeader);
                    Log.d(Constants.LOG_TAG, String.format("YearHeader.addYearHeaders:: created new %s", yearHeader));
                }

                yearHeader.addChildToOrphanElement(visit);
                preparedElements.add(yearHeader);
            }
        }

        Log.d(Constants.LOG_TAG, String.format("YearHeader.addYearHeaders:: [%d] YearHeaders added", preparedElements.size()));
        return preparedElements;
    }
}