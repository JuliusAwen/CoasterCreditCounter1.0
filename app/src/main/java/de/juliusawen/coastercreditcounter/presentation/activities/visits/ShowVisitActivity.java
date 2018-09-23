package de.juliusawen.coastercreditcounter.presentation.activities.visits;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import de.juliusawen.coastercreditcounter.R;
import de.juliusawen.coastercreditcounter.data.elements.Attraction;
import de.juliusawen.coastercreditcounter.data.elements.Element;
import de.juliusawen.coastercreditcounter.data.elements.Park;
import de.juliusawen.coastercreditcounter.data.elements.Visit;
import de.juliusawen.coastercreditcounter.globals.App;
import de.juliusawen.coastercreditcounter.globals.Constants;
import de.juliusawen.coastercreditcounter.presentation.activities.BaseActivity;
import de.juliusawen.coastercreditcounter.presentation.fragments.ShowAttractionsFragment;
import de.juliusawen.coastercreditcounter.toolbox.ActivityTool;

public class ShowVisitActivity extends BaseActivity
{
    private ShowVisitViewModel viewModel;
    private ShowAttractionsFragment showAttractionsFragment;
    private DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(Constants.LOG_TAG, Constants.LOG_DIVIDER + "ShowVisitActivity.onCreate:: creating activity...");

        setContentView(R.layout.activity_show_visit);
        super.onCreate(savedInstanceState);

        this.viewModel = ViewModelProviders.of(this).get(ShowVisitViewModel.class);

        if(this.viewModel.parentPark == null)
        {
            Element passedElement = App.content.getElementByUuid(UUID.fromString(getIntent().getStringExtra(Constants.EXTRA_ELEMENT_UUID)));
            if(passedElement.isInstance(Visit.class))
            {
                this.viewModel.visit = (Visit) passedElement;
                this.viewModel.parentPark = (Park) this.viewModel.visit.getParent();
            }
            else if(passedElement.isInstance(Park.class))
            {
                this.viewModel.parentPark = (Park) passedElement;
            }
        }

        super.addToolbar();
        super.addToolbarHomeButton();

        addShowAttractionFragment();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        this.decorateToolbar();
        if(this.viewModel.visit == null)
        {
            Log.d(Constants.LOG_TAG, "ShowVisitActivity.onResume:: creating visit...");
            this.createVisit();
        }
        else
        {
            if(!this.showAttractionsFragment.isInitialized)
            {
                Log.d(Constants.LOG_TAG, String.format("ShowVisitActivity.onResume:: initializing ShowAttractionsFragment with %s...", this.viewModel.visit));
                this.showAttractionsFragment.initializeForVisit(this.viewModel.visit);
            }
        }
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState)
//    {
//        super.onSaveInstanceState(outState);
//        outState.putString(Constants.KEY_ELEMENT, this.viewModel.visit.getUuid().toString());
//    }
//
//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState)
//    {
//        super.onRestoreInstanceState(savedInstanceState);
//
//        this.viewModel.visit = (Visit) App.content.getElementByUuid(UUID.fromString(savedInstanceState.getString(Constants.KEY_ELEMENT)));
//        this.viewModel.parentPark = (Park) this.viewModel.visit.getParent();
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.i(Constants.LOG_TAG, String.format("ShowVisitActivity.onActivityResult:: requestCode[%s], resultCode[%s]", requestCode, resultCode));

        if(resultCode == Activity.RESULT_OK)
        {
            if(requestCode == Constants.REQUEST_PICK_ATTRACTIONS)
            {
                List<Element> selectedElements = App.content.fetchElementsByUuidStrings(data.getStringArrayListExtra(Constants.EXTRA_ELEMENTS_UUIDS));
                this.viewModel.visit.addAttractions(selectedElements);
                this.showAttractionsFragment.initializeForVisit(this.viewModel.visit);
            }
        }
    }

    private void createVisit()
    {
        Log.i(Constants.LOG_TAG, String.format("ShowVisitActivity.createVisit:: creating visit for %s", this.viewModel.parentPark));

        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(ShowVisitActivity.this, new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day)
            {
                calendar.set(year, month, day);
                onDateSetCreateVisit(calendar);
                showPickAttractionsDialog();

            }
        }, year, month, day);

        datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.text_cancel), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int position)
            {
                if (position == DialogInterface.BUTTON_NEGATIVE)
                {
                    finish();
                }
            }
        });

        datePickerDialog.getDatePicker().setFirstDayOfWeek(App.settings.getFirstDayOfTheWeek());
        datePickerDialog.setCancelable(false);
        datePickerDialog.setCanceledOnTouchOutside(false);
        datePickerDialog.show();
    }

    private void onDateSetCreateVisit(Calendar calendar)
    {
        this.datePickerDialog.dismiss();

        this.viewModel.visit = Visit.create(calendar);
        this.viewModel.parentPark.addChild(this.viewModel.visit);
        App.content.addElement(this.viewModel.visit);

        if(Visit.isSameDay(this.viewModel.visit.getCalendar(), Calendar.getInstance()))
        {
            Log.i(Constants.LOG_TAG, "ShowVisitActivity.onViewCreated:: created visit is today - set as open visit");
            Visit.setOpenVisit(this.viewModel.visit);
        }

        this.decorateToolbar();
    }

    private void showPickAttractionsDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog;

        builder.setTitle(R.string.alert_dialog_add_attractions_to_visit_title);
        builder.setMessage(getString(R.string.alert_dialog_add_attractions_to_visit_message));
        builder.setPositiveButton(R.string.text_accept, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                onClickAlertDialogPositivePickAttractions(dialog);
            }
        });

        builder.setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.dismiss();
                showAttractionsFragment.initializeForVisit(viewModel.visit);
            }
        });

        alertDialog = builder.create();
        alertDialog.setIcon(R.drawable.ic_baseline_notification_important);

        alertDialog.show();
    }

    private void onClickAlertDialogPositivePickAttractions(DialogInterface dialog)
    {
        dialog.dismiss();
        ActivityTool.startActivityPickForResult(this, Constants.REQUEST_PICK_ATTRACTIONS, this.viewModel.parentPark.getChildrenOfType(Attraction.class));
    }

    private void decorateToolbar()
    {
        super.setToolbarTitleAndSubtitle(
                this.viewModel.visit != null ? this.viewModel.visit.getName() : getString(R.string.title_visit_create),
                this.viewModel.visit != null ? this.viewModel.parentPark.getName() : null);
    }

    protected void addShowAttractionFragment()
    {
        Log.d(Constants.LOG_TAG, "ShowVisitActivity.addShowAttractionFragment:: adding fragment...");

        if(this.showAttractionsFragment == null || super.savedInstanceState == null)
        {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            this.showAttractionsFragment = ShowAttractionsFragment.newInstance();
            fragmentTransaction.add(R.id.linearLayoutShowVisit, this.showAttractionsFragment, Constants.FRAGMENT_TAG_SHOW_VISIT_ATTRACTIONS);
            fragmentTransaction.commit();
        }
        else
        {
            this.showAttractionsFragment = (ShowAttractionsFragment) getSupportFragmentManager().findFragmentByTag(Constants.FRAGMENT_TAG_SHOW_VISIT_ATTRACTIONS);
        }
    }
}
